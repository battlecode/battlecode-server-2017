package battlecode.engine.instrumenter;

import battlecode.engine.ErrorReporter;
import org.objectweb.asm.ClassReader;
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
    private final static String[] disallowedPlayerPackages = {"java/", "battlecode/", "sun/"};

    /**
     * Classes that don't need to be instrumented but do need to be reloaded
     * for every individual player.
     */
    private final static Set<String> alwaysRedefine = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "battlecode/engine/instrumenter/lang/ObjectHashCode",
            "battlecode/engine/instrumenter/lang/InstrumentableFunctions",
            "battlecode/engine/instrumenter/lang/System",
            "battlecode/engine/instrumenter/RobotMonitor",
            "battlecode/engine/instrumenter/RoboRandom",
            "battlecode/common/Clock"
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
    private static final Map<String, Class<?>> loadedCache = new HashMap<String, Class<?>>();

    public IndividualClassLoader(String teamPackageName,
                                 boolean silenced) throws InstrumentationException {

        super(silenced);

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

            name = name.replace('.', '/');

            // this is the class we'll return
            Class finishedClass;

            //System.out.println("loadClass "+name);

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
                    cr = new ClassReader(name);
                } catch (IOException e) {
                    throw new InstrumentationException(
                            "Couldn't load required class"
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
            } else if (name.startsWith("instrumented")) {
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
            } else if (name.startsWith("forbidden/")) {
                ErrorReporter.report("Illegal class: " + name.substring(10) + "\nThis class cannot be referenced by player " + teamPackageName, false);
                throw new InstrumentationException();
            } else {
                // Load class normally
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

}
