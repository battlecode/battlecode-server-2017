package battlecode.engine.instrumenter;

import battlecode.engine.ErrorReporter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.util.*;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class IndividualClassLoader extends InstrumentingClassLoader {
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
            "battlecode.engine.instrumenter.lang.ObjectHashCode",
            "battlecode.engine.instrumenter.lang.InstrumentableFunctions",
            "battlecode.engine.instrumenter.lang.System",
            "battlecode.engine.instrumenter.lang.System$1",
            "battlecode.engine.instrumenter.lang.RoboRandom",
            "battlecode.engine.instrumenter.RobotMonitor",
            "battlecode.common.Clock"
    )));

    /**
     * Caches the binary format of classes that have been instrumented.
     * The values are byte arrays, not Classes, because each instance of
     * InstrumentingClassLoader should define its own class, even if another
     * InstrumentingClassLoader has already loaded a class from the same class file.
     */
    private final static Map<String, byte[]> instrumentedClasses = new HashMap<>();

    /**
     * Caches the names of teams with errors, so that if a class is loaded for
     * that team, it immediately throws an exception.
     * <p>
     * Note that this is an identity-based Set because we synchronize on the interned
     * team name during loading.
     */
    private final static Set<String> teamsWithErrors = Collections.newSetFromMap(new IdentityHashMap<>());

    public static void reset() {
        instrumentedClasses.clear();
        teamsWithErrors.clear();
    }

    /**
     * The name of the team this InstrumentingClassLoader is loading.
     */
    private final String teamPackageName;

    /**
     * Classes this particular IndividualClassLoader has already loaded.
     */
    private final Map<String, Class<?>> loadedCache;

    public IndividualClassLoader(String teamPackageName, boolean silenced) throws InstrumentationException {
        super(IndividualClassLoader.class.getClassLoader(), silenced);

        checkSettings();

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

        synchronized (teamPackageName) {

            // check if the team we're loading already has errors
            if (teamsWithErrors.contains(teamPackageName))
                throw new InstrumentationException();

            // this is the class we'll return
            Class finishedClass;

            if (instrumentedClasses.containsKey(name)) {
                byte[] classBytes = instrumentedClasses.get(name);
                finishedClass = defineClass(null, classBytes, 0, classBytes.length);
            } else if (alwaysRedefine.contains(name)) {
                // We want each robot to have its own copy of this class
                // so that it isn't possible to send messages by calling
                // hashCode repeatedly.  But we don't want to instrument it.
                // So just add its raw bytes to the instrumented classes cache.

                ClassReader cr;

                try {
                    cr = getReaderForClass(name);
                } catch (IOException e) {
                    throw new InstrumentationException(
                            "Couldn't load required class: "+name,
                            e
                    );
                }

                ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS);
                cr.accept(cw, 0);
                finishedClass = saveAndDefineClass(name, cw.toByteArray());

            } else if (name.startsWith(teamPackageName)) {
                final byte[] classBytes;
                try {
                    classBytes = instrument(name, true, teamPackageName);
                } catch (InstrumentationException e) {
                        teamsWithErrors.add(teamPackageName);
                        throw new InstrumentationException("Can't find the class \"" + name + "\". "
                                    + "Make sure the team name is spelled correctly. "
                                    + "Make sure the .class files are in the right directory (teams/teamname/*.class)",
                                    e);
                }

                finishedClass = saveAndDefineClass(name, classBytes);
            } else if (name.startsWith("instrumented.")) {
                // Each robot has its own version of java.util classes.
                // If permgen space becomes a problem, we could make it so
                // that only one copy of these classes is loaded, but
                // we would need to modify ObjectHashCode.
                byte[] classBytes;
                try {
                    classBytes = instrument(name, false, teamPackageName);
                    //dumpToFile(name,classBytes);
                } catch (InstrumentationException ie) {
                    teamsWithErrors.add(teamPackageName);
                    throw ie;
                }
                finishedClass = saveAndDefineClass(name, classBytes);
            } else if (name.startsWith("forbidden.")) {
                ErrorReporter.report("Illegal class: " + name.substring(10) + "\nThis class cannot be referenced by player " + teamPackageName, false);
                throw new InstrumentationException();
            } else {
                // Load class normally; note that we use the dotted form of the name.
                finishedClass = super.loadClass(name, resolve);
            }

            if (resolve)
                resolveClass(finishedClass);

            loadedCache.put(name, finishedClass);

            return finishedClass;

        }
    }

    public Class<?> saveAndDefineClass(String name, byte[] classBytes) {
        if (classBytes == null) {
            ErrorReporter.report("Can't find instrumented class " + name + ", but no errors reported", true);
            throw new InstrumentationException();
        }

        Class<?> theClass = defineClass(null, classBytes, 0, classBytes.length);
        instrumentedClasses.put(name, classBytes);

        return theClass;

    }

    public byte[] instrument(String className, boolean checkDisallowed, String teamPackageName) throws InstrumentationException {
        ClassReader cr;
        try {
            if (className.startsWith("instrumented.")) {
                cr = getReaderForClass(className.substring(13));
            } else {
                cr = getReaderForClass(className);
            }
        } catch (IOException ioe) {
            ErrorReporter.report("Can't find the class \"" + className + "\"", "Make sure the team name is spelled correctly.\nMake sure the .class files are in the right directory (teams/teamname/*.class)");
            throw new InstrumentationException("Can't load class: "+className, ioe);
        }
        ClassWriter cw = new ClassWriter(COMPUTE_MAXS); // passing true sets maxLocals and maxStack, so we don't have to
        ClassVisitor cv = new RoboAdapter(cw, teamPackageName, silenced, checkDisallowed);
        cr.accept(cv, 0);        //passing false lets debug info be included in the transformation, so players get line numbers in stack traces
        return cw.toByteArray();
    }

    /**
     * Gets a ClassReader for a given class.
     *
     * @param className the binary name (i.e. "."s and "$"s for inner classes)
     * @return an asm class reader for the class
     * @throws IOException if the class can't be read
     */
    private ClassReader getReaderForClass(final String className) throws IOException {
        String fileName = className.replace(".", "/") + ".class";

        // read the raw bytes of the file, using this classloader to locate it
        return new ClassReader(getResourceAsStream(fileName));
    }

}
