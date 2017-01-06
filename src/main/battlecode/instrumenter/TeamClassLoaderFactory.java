package battlecode.instrumenter;

import battlecode.instrumenter.bytecode.ClassReferenceUtil;
import battlecode.instrumenter.bytecode.InstrumentingClassVisitor;
import battlecode.server.Config;
import battlecode.server.ErrorReporter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static battlecode.instrumenter.InstrumentationException.Type.ILLEGAL;
import static battlecode.instrumenter.InstrumentationException.Type.MISSING;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * Creates ClassLoaders for RobotPlayers on one team.
 *
 * ClassLoaders for a team are, mostly, isolated from each other; and nearly
 * completely isolated from the enemy team.
 *
 * This is a bit of a god object. A demigod object, anyway.
 *
 * We all know what happens to demigods.
 */
public final class TeamClassLoaderFactory {

    /**
     * The classloader being used to load the normal parts of -server.
     *
     * Not necessarily the *bootstrap* classloader, just the *system* classloader.
     */
    private final static ClassLoader NORMAL_CLASS_LOADER =
            TeamClassLoaderFactory.class.getClassLoader();

    /**
     * Packages players are not allowed to use.
     * Some elements of these packages *are* permitted to be used,
     * e.g. java.util.*, but those classes are prefixed with "instrumented"
     * during instrumentation.
     */
    protected final static String[] disallowedPlayerPackages = {"java.", "battlecode.", "sun.", "com.sun."};

    /**
     * Classes that don't need to be instrumented but do need to be reloaded
     * for every individual player.
     */
    protected final static Set<String> alwaysRedefine = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "battlecode.instrumenter.inject.ObjectHashCode",
            "battlecode.instrumenter.inject.InstrumentableFunctions",
            "battlecode.instrumenter.inject.System",
            "battlecode.instrumenter.inject.RobotMonitor",
            "battlecode.common.Clock"
    )));

    /**
     * The name of the team this InstrumentingClassLoader is loading.
     */
    private final String teamPackageName;

    /**
     * We use this to find resources from the player URL.
     *
     * Note that this is never used to *define* classes; we just use it
     * for convenience in lookups. Loaders do all the actual class defining.
     */
    private final ClassLoader teamResourceLookup;

    /**
     * Caches the binary format of classes that have been instrumented.
     * The values are byte arrays, not Classes, because each instance of
     * InstrumentingClassLoader should define its own class, even if another
     * InstrumentingClassLoader has already loaded a class from the same class file.
     */
    private final Map<String, byte[]> instrumentedClasses;

    /**
     * If this team has an error, don't bother trying to cache again.
     */
    private boolean hasError;

    /**
     * The ClassReferenceUtil used by this cache.
     */
    private final ClassReferenceUtil refUtil;

    /**
     * Create a cache for classes loaded from a URL (or local file).
     * The URL can point to a jar file or a directory containing class
     * files, preferably locally - running arbitrary code from the internet
     * is a bad idea, even if we do try to instrument it.
     * Resources will be searched for at the URL and then locally.
     *
     * @param classURL the URL to load clases from
     */
    public TeamClassLoaderFactory(final String teamPackageName, final String classURL) {
        this.teamPackageName = teamPackageName.intern();

        URL url;
        try {
            url = getFilesystemURL(classURL);
        } catch (InstrumentationException e) {
            this.teamResourceLookup = null;
            this.hasError = true;
            this.instrumentedClasses = null;
            this.refUtil = null;
            return;
        }

        this.teamResourceLookup = new URLClassLoader(
                new URL[] { url }
        ) {
            @Override
            public URL getResource(String name) {
                // We override getResource because by default URLClassLoader
                // tries to load files from the system classpath before the
                // url classpath, but we only want to load files from
                // the player classpath.

                return findResource(name);
            }
        };
        this.instrumentedClasses = new HashMap<>();
        this.hasError = false;
        this.refUtil = new ClassReferenceUtil();
    }

    /**
     * Create a cache with no players.
     * For legacy tooling reasons; never during an actual match.
     */
    public TeamClassLoaderFactory() {
        this.teamResourceLookup = new ClassLoader() {
            @Override
            public URL getResource(String name) {
                return null;
            }

            @Override
            public URL findResource(String name) {
                return null;
            }
        };
        this.instrumentedClasses = new HashMap<>();
        this.hasError = false;
        this.refUtil = new ClassReferenceUtil();
        this.teamPackageName = "".intern();
    }

    /**
     * Create a loader for a new robot.
     * @return
     */
    public Loader createLoader() {
        return new Loader();
    }

    /**
     * @param className the class to look up
     * @return whether we've cached the bytes of the class
     */
    public boolean hasCached(String className) {
        return this.instrumentedClasses.containsKey(className);
    }

    /**
     * @param className the class to look up
     * @return the cached bytes of the class, or null if the class has never
     *         been seen before
     */
    public byte[] getCached(String className) {
        return this.instrumentedClasses.get(className);
    }

    /**
     * @param className the class to store
     * @param classBytes the bytes of the class
     */
    public void setCached(String className, byte[] classBytes) {
        this.instrumentedClasses.put(className, classBytes);
    }

    /**
     * @return whether the team is known to have errors
     */
    public boolean getError() {
        return this.hasError;
    }

    /**
     * Get the URL of a resource from the team's container.
     *
     * @param resource the resource to find
     * @return the URL of the resource, loaded from the team's container, or null if it cannot be found.
     */
    private URL getTeamURL(String resource) {
        return teamResourceLookup.getResource(resource);
    }

    /**
     * Load a resource using the normal system classloader.
     *
     * @param resource the resource to find
     * @return the URL of the resource, loaded from the normal classpath, or null if it cannot be found.
     */
    public static URL getNormalURL(String resource) {
        return NORMAL_CLASS_LOADER.getResource(resource);
    }

    /**
     * Attempt to read a class from the system classloader
     *
     * @param className the name of the class
     * @return a Reader for the class
     */
    public static ClassReader normalReader(String className) throws InstrumentationException {
        URL resURL = getNormalURL(toResourceName(className));
        if (resURL != null) {
            try {
                return new ClassReader(resURL.openStream());
            } catch (IOException e) {
                ErrorReporter.report("Can't find the class \"" + className + "\" on the system classpath",
                        "Make sure the team name is spelled correctly.\n" +
                                "Make sure the .class files are in the right directory (src/teamname/*.class)");
                throw new InstrumentationException(MISSING, "Can't load class "+className, e);
            }
        }
        ErrorReporter.report("Can't find the class \"" + className + "\" on the system classpath",
                "Make sure the team name is spelled correctly.\n" +
                        "Make sure the .class files are in the right directory (src/teamname/*.class)");
        throw new InstrumentationException(MISSING, "Can't load class "+className);
    }

    /**
     * Attempt to read a class from the team classloader
     *
     * @param className the name of the class
     * @return a Reader for the class
     * @throws InstrumentationException if the class cannot be read
     */
    public ClassReader teamReader(String className) throws InstrumentationException {
        URL resURL = getTeamURL(toResourceName(className));
        if (resURL != null) {
            try {
                return new ClassReader(resURL.openStream());
            } catch (IOException e) {
                ErrorReporter.report("Can't find the class \"" + className + "\" on the system classpath",
                        "Make sure the team name is spelled correctly.\n" +
                                "Make sure the .class files are in the right directory (src/teamname/*.class)");
                throw new InstrumentationException(MISSING, "Can't load class "+className, e);
            }
        }
        ErrorReporter.report("Can't find the class \"" + className + "\" on the system classpath",
                "Make sure the team name is spelled correctly.\n" +
                        "Make sure the .class files are in the right directory (src/teamname/*.class)");
        throw new InstrumentationException(MISSING, "Can't load class "+className);
    }

    /**
     * @param maybeFactory a factory to look in, or null to only look at the system.
     * @param className the class to read.
     * @return
     */
    public static ClassReader teamOrSystemReader(TeamClassLoaderFactory maybeFactory,
                                                String className) throws InstrumentationException {
        try {
            if (maybeFactory != null) {
                return maybeFactory.teamReader(className);
            }
        } catch (InstrumentationException e) {
            // Do nothing
        }

        return normalReader(className);
    }

    /**
     * @param className class name in the form java/lang/Double or java.lang.Double (no .class)
     *                  or instrumented/java/lang/Double
     * @return class name in the form java/lang/Double.class
     */
    private static String toResourceName(String className) throws InstrumentationException {
        if (className.endsWith(".class")) {
            throw new InstrumentationException(ILLEGAL, "Something has gone wrong: "+className);
        }

        String uninstrumentedName;
        if (className.startsWith("instrumented.") ||
                className.startsWith("instrumented/")) {
            uninstrumentedName = className.substring(13);
        } else {
            uninstrumentedName = className;
        }

        return uninstrumentedName.replace('.', '/') + ".class";
    }

    static private URL getFilesystemURL(String urlOrRelative) throws InstrumentationException {
        if (urlOrRelative == null) {
            throw new InstrumentationException(MISSING, "Can't load player with no URL!");
        }

        // Make sure that we're loading local files, if we're in a jar
        if (urlOrRelative.startsWith("jar:")) {
            String inside = urlOrRelative.substring(4);
            if (!inside.startsWith("file:")) {
                throw new InstrumentationException(MISSING, "You can only load from local jar files: "+urlOrRelative);
            }
        }

        try {
            URL url = new URL(urlOrRelative);

            if (!url.getProtocol().equals("jar") && !url.getProtocol().equals("file")) {
                throw new InstrumentationException(MISSING, "Can't load over protocol: "+url.getProtocol());
            }

            return url;
        } catch (MalformedURLException e) {
            // okay, it might be a local file
        }

        try {
            File result = new File(urlOrRelative);
            if (!result.exists()) {
                throw new InstrumentationException(MISSING, "Can't load from nonexistent file: "+result);
            }
            return result.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new InstrumentationException(MISSING, "Can't load player code from url "+urlOrRelative, e);
        }
    }

    public class Loader extends ClassLoader {

        /**
         * Classes this particular Loader has already loaded.
         */
        private final Map<String, Class<?>> loadedCache;

        /**
         * Create a loader.
         *
         * @throws InstrumentationException if we fail to create a loader for some reason.
         */
        private Loader() throws InstrumentationException {

            // use our classloader as a parent, rather than the default
            // system classloader
            super(Loader.class.getClassLoader());

            // always instrument any classes we load
            this.clearAssertionStatus();
            this.setDefaultAssertionStatus(true);

            // check that the package we're trying to load isn't contained in a disallowed package
            String teamNameSlash = teamPackageName + "/";
            for (String sysName : disallowedPlayerPackages) {
                if (teamNameSlash.startsWith(sysName)) {
                    throw new InstrumentationException(ILLEGAL,
                            "Invalid package name: \""
                                    + teamPackageName
                                    + "\"\nPlayer packages cannot be contained "
                                    + "in system packages (e.g., java., battlecode.)");
                }
            }

            this.loadedCache = new HashMap<>();
        }

        public TeamClassLoaderFactory getFactory() {
            return TeamClassLoaderFactory.this;
        }

        public ClassReferenceUtil getRefUtil() {
            return TeamClassLoaderFactory.this.refUtil;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (TeamClassLoaderFactory.this.getError()) {
                throw new InstrumentationException(ILLEGAL, "Team is known to have errors: " +
                        teamPackageName);
            }

            // Don't bother to recreate a class if we've done so before -
            // in *this particular* Loader.
            if (loadedCache.containsKey(name)) {
                return loadedCache.get(name);
            }

            // this is the class we'll return
            Class finishedClass;

            if (TeamClassLoaderFactory.this.hasCached(name)) {
                byte[] classBytes = TeamClassLoaderFactory.this.getCached(name);
                finishedClass = defineClass(null, classBytes, 0, classBytes.length);
            } else if (alwaysRedefine.contains(name)) {
                // We want each robot to have its own copy of this class
                // so that it isn't possible to send messages by calling
                // hashCode repeatedly.  But we don't want to instrument it.
                // So just add its raw bytes to the instrumented classes cache.
                System.out.println("alwaysRedefine reader "+name);
                ClassReader cr = normalReader(name);

                ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS);
                cr.accept(cw, 0);
                finishedClass = saveAndDefineClass(name, cw.toByteArray());
            } else if (name.startsWith(teamPackageName)) {

                // Check if the team we're loading already has errors.
                // Note that we only do this check when loading team
                // classes - we'll only get team loading failures when
                // loading team classes, which keeps the engine consistent
                // in where its failures happen.

                final byte[] classBytes;
                try {
                    classBytes = instrument(
                            TeamClassLoaderFactory.this.teamReader(name),
                            true,
                            Config.getGlobalConfig().getBoolean("bc.engine.debug-methods")
                    );
                } catch (InstrumentationException e) {
                    TeamClassLoaderFactory.this.hasError = true;
                    throw e;
                }

                finishedClass = saveAndDefineClass(name, classBytes);
            } else if (name.startsWith("instrumented.")) {
                // Each robot has its own version of java.util classes.
                // We don't check them for disallowed or debug methods.
                // If permgen space becomes a problem, we could make it so
                // that only one copy of these classes is loaded, but
                // we would need to modify ObjectHashCode.
                byte[] classBytes;
                try {
                    classBytes = instrument(
                            TeamClassLoaderFactory.normalReader(name),
                            false, false);
                } catch (InstrumentationException ie) {
                    TeamClassLoaderFactory.this.hasError = true;
                    throw ie;
                }

                finishedClass = saveAndDefineClass(name, classBytes);
            } else {
                // Load class normally; note that we use the dotted form of the name.
                finishedClass = super.loadClass(name, resolve);
            }

            if (resolve)
                resolveClass(finishedClass);

            loadedCache.put(name, finishedClass);

            return finishedClass;
        }

        @Override
        public URL getResource(String name) {
            // We override getResource to have it look in the correct places for things;
            // in the team package jar if it's a team resource, on the normal classpath
            // otherwise
            if (name.startsWith(teamPackageName)) {
                return TeamClassLoaderFactory.this.teamResourceLookup.getResource(name);
            } else {
                return super.getResource(name);
            }
        }


        public Class<?> saveAndDefineClass(String name, byte[] classBytes) {
            if (classBytes == null) {
                throw new InstrumentationException(ILLEGAL, "Can't save class with null bytes: " + name);
            }

            Class<?> theClass = defineClass(null, classBytes, 0, classBytes.length);
            TeamClassLoaderFactory.this.setCached(name, classBytes);

            return theClass;

        }

        public byte[] instrument(ClassReader reader,
                                 boolean checkDisallowed,
                                 boolean debugMethodsEnabled) throws InstrumentationException {

            ClassWriter cw = new ClassWriter(COMPUTE_MAXS); // passing true sets maxLocals and maxStack, so we don't have to
            ClassVisitor cv = new InstrumentingClassVisitor(
                    cw,
                    this,
                    teamPackageName,
                    false,
                    checkDisallowed,
                    debugMethodsEnabled
            );
            reader.accept(cv, 0);        //passing false lets debug info be included in the transformation, so players get line numbers in stack traces
            return cw.toByteArray();
        }

        @SuppressWarnings("unused")
        private void dumpToFile(String name, byte[] bytes) {
            try {
                Files.write(Paths.get("instrumented", name + ".class"), bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
