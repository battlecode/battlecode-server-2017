package battlecode.engine.instrumenter;

import battlecode.engine.ErrorReporter;

import java.util.HashMap;
import java.util.Map;

class SingletonClassLoader extends InstrumentingClassLoader {

    // caches the classes that have been loaded and designated as reusable, to speed up future attempts to load them
    private final Map<String, Class> loadedReusableClasses = new HashMap<String, Class>();

    protected SingletonClassLoader() {
        super(false, false);
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        name = name.replace('.', '/');

        // this is the class we'll return
        Class finishedClass = null;

        //System.out.println("loadClass "+name);

        if (loadedReusableClasses.containsKey(name))
            finishedClass = loadedReusableClasses.get(name);
        else if (name.startsWith("instrumented/")) {
            byte[] classBytes;
            classBytes = instrument(name, false, "");

            /*
               if(name.startsWith("hardplayer/"))
               try {
                   java.io.File file = new java.io.File("classes/"+name+".class");
                   java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
                   stream.write(classBytes);
                   stream.close();
               } catch(Exception e) { }
               */

            finishedClass = saveAndDefineClass(name, classBytes);
        } else {
            finishedClass = Class.forName(name.replace('/', '.'));    // TODO: should change this to explicitly use the parent classloader?
            // since it shouldn't be instrumented, it defaults to being reusable, and so we cache the defined class
            loadedReusableClasses.put(name, finishedClass);
        }

        if (resolve)
            resolveClass(finishedClass);

        return finishedClass;
    }

    public Class<?> saveAndDefineClass(String name, byte[] classBytes) {
        if (classBytes == null) {
            ErrorReporter.report("Can't find instrumented class " + name + ", but no errors reported", true);
            throw new InstrumentationException();
        }
        Class<?> theClass = defineClass(null, classBytes, 0, classBytes.length);
        loadedReusableClasses.put(name, theClass);
        return theClass;

    }

}
