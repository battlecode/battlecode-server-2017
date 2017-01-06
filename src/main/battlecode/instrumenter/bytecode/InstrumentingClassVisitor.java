package battlecode.instrumenter.bytecode;

import battlecode.instrumenter.TeamClassLoaderFactory;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Instruments a class. Overrides class references and runs an
 * InstrumentingMethodVisitor on every method.
 *
 * @author adamd
 */
public class InstrumentingClassVisitor extends ClassVisitor implements Opcodes {
    private String className;
    private final String teamPackageName;
    private final boolean silenced;
    private final boolean debugMethodsEnabled;

    // Used to find other class files, which is occasionally necessary.
    private TeamClassLoaderFactory.Loader loader;

    // We check contestants' code for disallowed packages.
    // But some builtin Java libraries use disallowed packages so
    // don't check those.
    private final boolean checkDisallowed;

    /**
     * Creates a InstrumentingClassVisitor to instrument a given class.
     *
     * @param cv                  the ClassVisitor that should be used to read the class
     * @param teamPackageName     the package name of the team for which this class is being instrumented
     * @param silenced            whether System.out should be silenced for this class
     * @param checkDisallowed     whether to check for disallowed classes and methods
     */
    public InstrumentingClassVisitor(final ClassVisitor cv,
                                     final TeamClassLoaderFactory.Loader loader,
                                     final String teamPackageName,
                                     boolean silenced,
                                     boolean checkDisallowed,
                                     boolean debugMethodsEnabled) {
        super(Opcodes.ASM5, cv);
        this.loader = loader;
        this.teamPackageName = teamPackageName;
        this.silenced = silenced;
        this.checkDisallowed = checkDisallowed;
        this.debugMethodsEnabled = debugMethodsEnabled;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        className = loader.getRefUtil().classReference(name, teamPackageName, checkDisallowed);
        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = loader.getRefUtil().classReference(interfaces[i], teamPackageName, checkDisallowed);
        }
        String newSuperName;
        newSuperName = loader.getRefUtil().classReference(superName, teamPackageName, checkDisallowed);
        super.visit(version, access, className, loader.getRefUtil().methodSignatureReference(signature, teamPackageName, checkDisallowed), newSuperName, interfaces);
    }

    /**
     * @inheritDoc
     */
    public MethodVisitor visitMethod(
            int access,
            final String name,
            final String desc,
            final String signature,
            final String[] exceptions) {

        // Nothing bad should happen if a function is synchronized, because
        // there isn't any way for two robots to get the same instance of
        // an instrumented class.  But we may as well strip the keyword
        // for performance reasons.
        access &= ~Opcodes.ACC_SYNCHRONIZED;

        if (exceptions != null) {
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = loader.getRefUtil().classReference(exceptions[i], teamPackageName, checkDisallowed);
            }
        }
        MethodVisitor mv = cv.visitMethod(access,
                name,
                loader.getRefUtil().methodDescReference(desc, teamPackageName, checkDisallowed),
                loader.getRefUtil().methodSignatureReference(signature, teamPackageName, checkDisallowed),
                exceptions);
        // create a new InstrumentingMethodVisitor, and let it loose on this method
        return mv == null ? null : new InstrumentingMethodVisitor(
                mv,
                loader,
                className,
                access,
                name,
                desc,
                signature,
                exceptions,
                teamPackageName,
                silenced,
                checkDisallowed,
                debugMethodsEnabled
        );
    }

    /**
     * @inheritDoc
     */
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        // Strip the volatile keyword for performance reasons.  It's
        // safe to do so since an instance of an instrumented class
        // should never be accessed by more than one thread.
        if (checkDisallowed || (access & Opcodes.ACC_STATIC) == 0)
            access &= ~Opcodes.ACC_VOLATILE;
        return cv.visitField(access,
                name,
                loader.getRefUtil().classDescReference(desc, teamPackageName, checkDisallowed),
                loader.getRefUtil().fieldSignatureReference(signature, teamPackageName, checkDisallowed),
                value);
    }

    /**
     * @inheritDoc
     */
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(loader.getRefUtil().classReference(owner, teamPackageName, checkDisallowed), name, loader.getRefUtil().methodSignatureReference(desc, teamPackageName, checkDisallowed));
    }

    /**
     * @inheritDoc
     */
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(
                loader.getRefUtil().classReference(name, teamPackageName, checkDisallowed),
                loader.getRefUtil().classReference(outerName, teamPackageName, checkDisallowed),
                innerName, access
        );
    }

}
