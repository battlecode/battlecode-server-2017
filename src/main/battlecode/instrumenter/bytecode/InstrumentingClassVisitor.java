package battlecode.instrumenter.bytecode;

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
     */
    public InstrumentingClassVisitor(final ClassVisitor cv, final String teamPackageName, boolean silenced, boolean checkDisallowed) {
        super(Opcodes.ASM5, cv);
        this.teamPackageName = teamPackageName;
        this.silenced = silenced;
        this.checkDisallowed = checkDisallowed;
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
        className = ClassReferenceUtil.classReference(name, teamPackageName, checkDisallowed);
        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = ClassReferenceUtil.classReference(interfaces[i], teamPackageName, checkDisallowed);
        }
        String newSuperName;
        newSuperName = ClassReferenceUtil.classReference(superName, teamPackageName, checkDisallowed);
        super.visit(version, access, className, ClassReferenceUtil.methodSignatureReference(signature, teamPackageName, checkDisallowed), newSuperName, interfaces);
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
                exceptions[i] = ClassReferenceUtil.classReference(exceptions[i], teamPackageName, checkDisallowed);
            }
        }
        MethodVisitor mv = cv.visitMethod(access,
                name,
                ClassReferenceUtil.methodDescReference(desc, teamPackageName, checkDisallowed),
                ClassReferenceUtil.methodSignatureReference(signature, teamPackageName, checkDisallowed),
                exceptions);
        // create a new InstrumentingMethodVisitor, and let it loose on this method
        return mv == null ? null : new InstrumentingMethodVisitor(mv, className, access, name, desc, signature, exceptions, teamPackageName, silenced, checkDisallowed);
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
                ClassReferenceUtil.classDescReference(desc, teamPackageName, checkDisallowed),
                ClassReferenceUtil.fieldSignatureReference(signature, teamPackageName, checkDisallowed),
                value);
    }

    /**
     * @inheritDoc
     */
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(ClassReferenceUtil.classReference(owner, teamPackageName, checkDisallowed), name, ClassReferenceUtil.methodSignatureReference(desc, teamPackageName, checkDisallowed));
    }

    /**
     * @inheritDoc
     */
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(ClassReferenceUtil.classReference(name, teamPackageName, checkDisallowed), ClassReferenceUtil.classReference(outerName, teamPackageName, checkDisallowed), innerName, access);
    }

}
