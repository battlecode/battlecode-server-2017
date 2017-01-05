package battlecode.instrumenter;

import battlecode.instrumenter.bytecode.InstrumentingClassVisitor;
import battlecode.server.Config;
import battlecode.server.ErrorReporter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class IndividualClassLoader extends ClassLoader {
    /**
     * Packages players are not allowed to use.
     * Some elements of these packages *are* permitted to be used,
     * e.g. java.util.*, but those classes are prefixed with "instrumented"
     * during instrumentation.
     */
    protected final static String[] disallowedPlayerPackages = {"java.", "battlecode.", "sun."};

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
     * Classes this particular IndividualClassLoader has already loaded.
     */
    private final Map<String, Class<?>> loadedCache;

    /**
     * A shared cache of instrumented classes, and team error information.
     */
    private final Cache sharedCache;

    /**
     * Create an IndividualClassLoader.
     *
     * @param teamPackageName the name of the team this classloader is loading.
     * @param sharedCache the cache to use to store the byte forms of classes we
     *                    instrument
     * @throws InstrumentationException
     */
    public IndividualClassLoader(String teamPackageName, Cache sharedCache)
            throws InstrumentationException {

        // use our classloader as a parent, rather than the default
        // system classloader
        super(IndividualClassLoader.class.getClassLoader());

        this.sharedCache = sharedCache;

        // always instrument any classes we load
        this.clearAssertionStatus();
        this.setDefaultAssertionStatus(true);

        // check that the package we're trying to load isn't contained in a disallowed package
        String teamNameSlash = teamPackageName + "/";
        for (String sysName : disallowedPlayerPackages) {
            if (teamNameSlash.startsWith(sysName)) {
                throw new InstrumentationException(
                        "Invalid package name: \""
                                + teamPackageName
                                + "\"\nPlayer packages cannot be contained "
                                + "in system packages (e.g., java., battlecode.)"
                );
            }
        }

        this.teamPackageName = teamPackageName.intern();
        this.loadedCache = new HashMap<>();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Don't bother to recreate a class if we've done so before -
        // in *this particular* IndividualClassLoader.
        if (loadedCache.containsKey(name)) {
            return loadedCache.get(name);
        }

        // this is the class we'll return
        Class finishedClass;

        if (sharedCache.hasCached(name)) {
            byte[] classBytes = sharedCache.getCached(name);
            finishedClass = defineClass(null, classBytes, 0, classBytes.length);
        } else if (alwaysRedefine.contains(name)) {
            // We want each robot to have its own copy of this class
            // so that it isn't possible to send messages by calling
            // hashCode repeatedly.  But we don't want to instrument it.
            // So just add its raw bytes to the instrumented classes cache.

            ClassReader cr = reader(name);

            ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS);
            cr.accept(cw, 0);
            finishedClass = saveAndDefineClass(name, cw.toByteArray());
        } else if (name.startsWith(teamPackageName)) {

            // Check if the team we're loading already has errors.
            // Note that we only do this check when loading team
            // classes - we'll only get team loading failures when
            // loading team classes, which keeps the engine consistent
            // in where its failures happen.
            if (sharedCache.getError(teamPackageName)) {
                throw new InstrumentationException("Team is known to have errors: " +
                        teamPackageName);
            }

            final byte[] classBytes;
            try {
                classBytes = instrument(
                        name,
                        true,
                        Config.getGlobalConfig().getBoolean("bc.engine.debug-methods")
                );
            } catch (InstrumentationException e) {
                sharedCache.setError(teamPackageName);
                throw e;
            }

            finishedClass = saveAndDefineClass(name, classBytes);
        } else if (name.startsWith("instrumented.")) {
            // Each robot has its own version of java.util classes.
            // If permgen space becomes a problem, we could make it so
            // that only one copy of these classes is loaded, but
            // we would need to modify ObjectHashCode.
            byte[] classBytes;
            try {
                classBytes = instrument(name, false, false);
            } catch (InstrumentationException ie) {
                sharedCache.setError(teamPackageName);
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
            return sharedCache.getLoader().getResource(name);
        } else {
            return super.getResource(name);
        }
    }

    /**
     * Get a ClassReader for a class.
     *
     * @param className the name of the class, using .s or /s
     * @return a reader for the class
     * @throws InstrumentationException if the class can't be found
     */
    public ClassReader reader(String className) throws InstrumentationException {
        String uninstrumentedName;
        if (className.startsWith("instrumented.") ||
                className.startsWith("instrumented/")) {
            uninstrumentedName = className.substring(13);
        } else {
            uninstrumentedName = className;
        }

        String finalName = uninstrumentedName.replace('.', '/') + ".class";

        try {
            return new ClassReader(getResourceAsStream(finalName));
        } catch (IOException e) {
            ErrorReporter.report("Can't find the class \"" + className + "\"",
                    "Make sure the team name is spelled correctly.\n" +
                    "Make sure the .class files are in the right directory (src/teamname/*.class)");
            throw new InstrumentationException("Can't load class "+className, e);
        }
    }

    public Class<?> saveAndDefineClass(String name, byte[] classBytes) {
        if (classBytes == null) {
            throw new InstrumentationException("Can't save class with null bytes: " + name);
        }

        Class<?> theClass = defineClass(null, classBytes, 0, classBytes.length);
        sharedCache.setCached(name, classBytes);

        return theClass;

    }

    public byte[] instrument(String className,
                             boolean checkDisallowed,
                             boolean debugMethodsEnabled) throws InstrumentationException {

        ClassReader cr = reader(className);

        ClassWriter cw = new ClassWriter(COMPUTE_MAXS); // passing true sets maxLocals and maxStack, so we don't have to
        ClassVisitor cv = new InstrumentingClassVisitor(
                cw,
                this,
                teamPackageName,
                false,
                checkDisallowed,
                debugMethodsEnabled
        );
        cr.accept(cv, 0);        //passing false lets debug info be included in the transformation, so players get line numbers in stack traces
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

    /**
     * A class that caches data used by an IndividualClassLoader.
     * Should be shared between IndividualClassLoaders that use the same source
     * for their code, i.e. both from the same URL, or both from the local classpath.
     */
    public static final class Cache {

        /**
         * The loader used to load classes and resources.
         */
        private final ClassLoader loader;

        /**
         * Caches the binary format of classes that have been instrumented.
         * The values are byte arrays, not Classes, because each instance of
         * InstrumentingClassLoader should define its own class, even if another
         * InstrumentingClassLoader has already loaded a class from the same class file.
         */
        private final Map<String, byte[]> instrumentedClasses;

        /**
         * Caches the names of teams with errors, so that if a class is loaded for
         * that team, it immediately throws an exception.
         */
        private final Set<String> teamsWithErrors;

        /**
         * Create a cache for classes loaded from a URL, or the local classpath.
         * The URL can point to a jar file or a directory containing class
         * files, preferably locally - running arbitrary code from the internet
         * is a bad idea, even if we do try to instrument it.
         * Resources will be searched for at the URL and then locally.
         *
         * @param classURL the URL to load clases from
         */
        public Cache(final URL classURL) {
            this.loader = new URLClassLoader(
                    new URL[] { classURL }
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
            this.teamsWithErrors = new HashSet<>();
        }

        /**
         * Create a cache for classes loaded only from the local classpath.
         */
        public Cache() {
            // we make a new classloader so that when we get GC'd,
            // the classes we've loaded will also get GC'd.
            this.loader = new ClassLoader(getClass().getClassLoader()) {};
            this.instrumentedClasses = new HashMap<>();
            this.teamsWithErrors = new HashSet<>();
        }

        /**
         * @return the classloader associated with this cache
         */
        public ClassLoader getLoader() {
            return this.loader;
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
         * @param teamName the team to look up
         * @return whether the team is known to have errors
         */
        public boolean getError(String teamName) {
            return this.teamsWithErrors.contains(teamName);
        }

        /**
         * @param teamName the team that we want to remember has errors
         */
        public void setError(String teamName) {
            this.teamsWithErrors.add(teamName);
        }

    }
}
