package battlecode.engine.instrumenter;

import battlecode.engine.ErrorReporter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.util.*;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class IndividualClassLoader extends InstrumentingClassLoader {
    private boolean retryLoad;
    private final static String[] disallowedPlayerPackages = {"java/", "battlecode/", "sun/"};

    // caches the binary format of classes that have been instrumented
    // the values are byte arrays, not Classes, because each instance of InstrumentingClassLoader should define its own class,
    // even if another InstrumentingClassLoader has already loaded a class from the same class file
    private final static Map<String, byte[]> instrumentedClasses = new HashMap<String, byte[]>();

    // caches the names of teams with errors, so that if a class is loaded for that team, it immediately throws an exception
    private final static Set<String> teamsWithErrors = Collections.newSetFromMap(new IdentityHashMap<String, Boolean>());

    // the name of the team this InstrumentingClassLoader is loading
    private final String teamPackageName;

    public static void reset() {
        instrumentedClasses.clear();
        teamsWithErrors.clear();
        singletonLoader = new SingletonClassLoader();
    }

    static SingletonClassLoader singletonLoader = new SingletonClassLoader();

    public IndividualClassLoader(String teamPackageName, boolean debugMethodsEnabled, boolean silenced, boolean retry) throws InstrumentationException {
        super(silenced, debugMethodsEnabled, singletonLoader);
	retryLoad = retry;
        checkSettings();

        // check that the package we're trying to load isn't contained in a disallowed package
        String teamNameSlash = teamPackageName + "/";
        for (String sysName : disallowedPlayerPackages) {
            if (teamNameSlash.startsWith(sysName)) {
                ErrorReporter.report("Invalid package name: \"" + teamPackageName + "\"\nPlayer packages cannot be contained in system packages (e.g., java., battlecode.)", false);
                throw new InstrumentationException();
            }
        }

        this.teamPackageName = teamPackageName.intern();

    }

    private void dumpToFile(String name, byte[] bytes) {
        try {
            java.io.File file = new java.io.File("classes/" + name + ".class");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            stream.write(bytes);
            stream.close();
        } catch (Exception e) {
        }
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        synchronized (teamPackageName) {

            // check if the team we're loading already has errors
            if (teamsWithErrors.contains(teamPackageName))
                throw new InstrumentationException();

            name = name.replace('.', '/');

            // this is the class we'll return
            Class finishedClass = null;

            //System.out.println("loadClass "+name);

            if (instrumentedClasses.containsKey(name)) {
                byte[] classBytes = instrumentedClasses.get(name);
                finishedClass = defineClass(null, classBytes, 0, classBytes.length);
            } else if (name.equals("battlecode/engine/instrumenter/lang/ObjectHashCode") ||
                    name.equals("battlecode/engine/instrumenter/lang/InstrumentableFunctions")) {
                // We want each robot to have its own copy of this class
                // so that it isn't possible to send messages by calling
                // hashCode repeatedly.  But we don't want to instrument it.
                ClassReader cr = null;
                try {
                    cr = new ClassReader(name);
                } catch (IOException ioe) {
                    ErrorReporter.report("Can't find the class \"" + name + "\"", "Make sure the team name is spelled correctly.\nMake sure the .class files are in the right directory (teams/teamname/*.class)");
                    throw new InstrumentationException();
                }
                ClassWriter cw = new ClassWriter(cr, COMPUTE_MAXS);
                cr.accept(cw, 0);
                finishedClass = saveAndDefineClass(name, cw.toByteArray());
            } else if (name.startsWith(teamPackageName)) {
                byte[] classBytes = null;
		boolean retry = true;
		while(retry) {
		    if(!retryLoad)
			retry = false;
		    try {
			classBytes = instrument(name, true, teamPackageName);
			//dumpToFile(name,classBytes);
			retry = false;
		    } catch (InstrumentationException ie) {			
			if(!retryLoad) {
			    teamsWithErrors.add(teamPackageName);
			    throw ie;
			} else {
			    try {
				Thread.sleep(10000);
			    } catch(Exception e) {}
			}
		    }
                }

                finishedClass = saveAndDefineClass(name, classBytes);
            }
            // Each robot has its own version of java.util classes.
            // If permgen space becomes a problem, we could make it so
            // that only one copy of these classes is loaded, but
            // we would need to modify ObjectHashCode.
            else if (name.startsWith("instrumented")) {
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
                try {
                    return singletonLoader.loadClass(name, resolve);
                } catch (InstrumentationException ie) {
                    teamsWithErrors.add(teamPackageName);
                    throw ie;
                }
            }

            if (resolve)
                resolveClass(finishedClass);

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
