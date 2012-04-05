package battlecode.engine.instrumenter;

import battlecode.engine.ErrorReporter;
import battlecode.server.Config;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public abstract class InstrumentingClassLoader extends ClassLoader {

    // silenced is not used any more
    protected final boolean silenced;
    protected final boolean debugMethodsEnabled;

    private static boolean lazy;
    private static boolean fastHash;
    private static boolean checkedSettings;

    /**
     * Returns the value of the property bc.engine.lazy-instrumenter.
     * If the instrumenter is not lazy, then robots are killed if their class files
     * contain any forbidden code.  If the instrumenter is lazy, then robots are killed
     * if they try to execute any forbidden code.
     * <p/>
     * Laziness might be necessary if we want to allow external libraries like Guava or
     * languages like Scala.  If you're only using your own code then it's better to
     * turn laziness off so you don't get any bad surprises.
     * <p/>
     * {@see ClassReferenceUtil#illegalClass}
     * {@see RoboMethodAdapter#forbidden}
     */
    public static boolean lazy() {
        return lazy;
    }

    /**
     * Returns the value of the property bc.engine.fast-hash.
     * If fast-hash is set, then the instrumenter will not use
     * reflection to check whether an object overrides hashCode().
     * The check is slow but without it, there is a one in 2^32 chance
     * of the instrumenter thinking that hashCode() is not overridden
     * when in fact it is overridden.
     * <p/>
     * {@see battlecode.engine.instrumenter.lang.ObjectHashCode}
     * {@see RoboMethodAdapter#visitMethodInsn}
     */
    public static boolean fastHash() {
        return fastHash;
    }

    protected static void checkSettings() {
        if (!checkedSettings) {
            checkedSettings = true;
            Config config = Config.getGlobalConfig();
            lazy = config.getBoolean("bc.engine.lazy-instrumenter");
            fastHash = config.getBoolean("bc.engine.fast-hash");
        }
    }

    public InstrumentingClassLoader(boolean silenced, boolean debugMethodsEnabled) {
        super();
        this.silenced = silenced;
        this.debugMethodsEnabled = debugMethodsEnabled;
    }

    public InstrumentingClassLoader(boolean silenced, boolean debugMethodsEnabled, ClassLoader cl) {
        super(cl);
        this.silenced = silenced;
        this.debugMethodsEnabled = debugMethodsEnabled;
    }

    public byte[] instrument(String className, boolean checkDisallowed, String teamPackageName) throws InstrumentationException {
        //System.out.println("instrumenting "+className+", checkDisallowed "+checkDisallowed);
        ClassReader cr;
        try {
            if (className.startsWith("instrumented/"))
                cr = new ClassReader(className.substring(13));
            else
                cr = new ClassReader(className);
        } catch (IOException ioe) {
            ErrorReporter.report("Can't find the class \"" + className + "\"", "Make sure the team name is spelled correctly.\nMake sure the .class files are in the right directory (teams/teamname/*.class)");
            throw new InstrumentationException();
        }
        ClassWriter cw = new ClassWriter(COMPUTE_MAXS); // passing true sets maxLocals and maxStack, so we don't have to
        ClassVisitor cv = new RoboAdapter(cw, teamPackageName, debugMethodsEnabled, silenced, checkDisallowed);
        cr.accept(cv, 0);        //passing false lets debug info be included in the transformation, so players get line numbers in stack traces
        byte[] bytes = cw.toByteArray();
        return bytes;
    }

    public abstract Class<?> saveAndDefineClass(String name, byte[] classBytes);

}
