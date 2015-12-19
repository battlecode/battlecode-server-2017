package battlecode.instrumenter.bytecode;

import battlecode.common.GameConstants;
import battlecode.server.ErrorReporter;
import battlecode.instrumenter.InstrumentationException;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

/**
 * The class where the bulk of instrumentation happens.
 * Takes in the bytecode for a method and modifies it to do a few things:
 *  - Call RobotMonitor.incrementBytecodes() at the end of every basic block
 *  - Overrides class references with our injected / instrumented class references
 *  - Modifies some particularly finnicky method calls so that they behave correctly
 *    (e.g. Object.hashCode(), Math.random(), Throwable.printStackTrace())
 */
public class InstrumentingMethodVisitor extends MethodNode implements Opcodes {
	
    private final String teamPackageName;
    private final String className;    // the class to which this method belongs
    private final boolean checkDisallowed;

    // all the exception handlers we've seen in the code
    private final Set<LabelNode> exceptionHandlers = new HashSet<>();
    private final Set<LabelNode> tryCatchStarts = new HashSet<>();

    private static final Set<String> instrumentedStringFuncs = new HashSet<>();

    static {
        instrumentedStringFuncs.add("matches");
        instrumentedStringFuncs.add("replaceAll");
        instrumentedStringFuncs.add("replaceFirst");
        instrumentedStringFuncs.add("split");
    }

    private LabelNode startLabel;

    private int bytecodeCtr = 0;

    private MethodVisitor methodWriter;

    public InstrumentingMethodVisitor(final MethodVisitor mv, final String className, final int access, final String methodName, final String methodDesc, final String signature, final String[] exceptions, final String teamPackageName, boolean silenced, boolean checkDisallowed) {
        super(ASM5, access, methodName, methodDesc, signature, exceptions);
        this.teamPackageName = teamPackageName;
        this.className = className;
        this.checkDisallowed = checkDisallowed;
        methodWriter = mv;
    }

    protected String classReference(String name) {
        return ClassReferenceUtil.classReference(name, teamPackageName, checkDisallowed);
    }

    protected String classDescReference(String name) {
        return ClassReferenceUtil.classDescReference(name, teamPackageName, checkDisallowed);
    }

    protected String methodDescReference(String name) {
        return ClassReferenceUtil.methodDescReference(name, teamPackageName, checkDisallowed);
    }

    protected String fieldSignatureReference(String name) {
        return ClassReferenceUtil.fieldSignatureReference(name, teamPackageName, checkDisallowed);
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        for (Object o : tryCatchBlocks) {
            visitTryCatchBlockNode((TryCatchBlockNode) o);
        }
        for (AbstractInsnNode node : instructions.toArray()) {
            // node could be taken out of the list
            // or have stuff inserted after it,
            // so node.getNext() might not be valid
            // after we visit node
            switch (node.getType()) {
                case FIELD_INSN:
                    visitFieldInsnNode((FieldInsnNode) node);
                    break;
                case INSN:
                    visitInsnNode((InsnNode) node);
                    break;
                case LDC_INSN:
                    visitLdcInsnNode((LdcInsnNode) node);
                    break;
                case METHOD_INSN:
                    visitMethodInsnNode((MethodInsnNode) node);
                    break;
                case MULTIANEWARRAY_INSN:
                    visitMultiANewArrayInsnNode((MultiANewArrayInsnNode) node);
                    break;
                case TYPE_INSN:
                    visitTypeInsnNode((TypeInsnNode) node);
                    break;
                case VAR_INSN:
                    visitVarInsnNode((VarInsnNode) node);
                    break;
                case LABEL:
                    visitLabelNode((LabelNode) node);
                    break;
                case FRAME:
                    visitFrameNode((FrameNode) node);
                    break;
                case JUMP_INSN:
                case LOOKUPSWITCH_INSN:
                case TABLESWITCH_INSN:
                    bytecodeCtr++;
                    endOfBasicBlock(node);
                    break;
                case IINC_INSN:
                case INT_INSN:
                    bytecodeCtr++;
                    break;
            }
        }
        startLabel = new LabelNode(new Label());
        instructions.insert(startLabel);
        boolean anyTryCatch = tryCatchBlocks.size() > 0;
        if (anyTryCatch) {
            addRobotDeathHandler();
        }
        for (Object o : localVariables) {
            visitLocalVariableNode((LocalVariableNode) o);
        }
        super.visitMaxs(0, 0);
    }

    public void visitEnd() {
        accept(methodWriter);
    }

    private void visitTryCatchBlockNode(TryCatchBlockNode n) {
        exceptionHandlers.add(n.handler);
        tryCatchStarts.add(n.start);
        if (n.type != null) {
            n.type = classReference(n.type);
        }
    }

    private static AbstractInsnNode nextInstruction(AbstractInsnNode n) {
        while (n.getType() == AbstractInsnNode.LINE ||
                n.getType() == AbstractInsnNode.FRAME ||
                n.getType() == AbstractInsnNode.LABEL)
            n = n.getNext();
        return n;
    }
    
    @SuppressWarnings("unchecked")	// This is to fix the warning from the add() to tryCatchBlocks
    private void addRobotDeathHandler() {
        LabelNode robotDeathLabel = new LabelNode(new Label());
        LabelNode firstTryCatch = null;
        for(AbstractInsnNode node : instructions.toArray()) {
            if(node.getType()==AbstractInsnNode.LABEL&&tryCatchStarts.contains(node)) {
                firstTryCatch = (LabelNode)node;
                break;
            }
        }
        tryCatchBlocks.add(0, new TryCatchBlockNode(firstTryCatch, robotDeathLabel, robotDeathLabel, "java/lang/VirtualMachineError"));
        instructions.add(robotDeathLabel);
        instructions.add(new FrameNode(F_FULL, 0, new Object[0], 1, new Object[]{"java/lang/VirtualMachineError"}));
        instructions.add(new InsnNode(ATHROW));
    }

    private void visitFieldInsnNode(FieldInsnNode n) {
        bytecodeCtr++;
        n.owner = classReference(n.owner);
        n.desc = classDescReference(n.desc);
    }

    private void visitInsnNode(InsnNode n) {
        bytecodeCtr++;
        switch (n.getOpcode()) {
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
                endOfBasicBlock(n);
                break;
            case ATHROW:
                endOfBasicBlock(n);
                break;
            case MONITORENTER:
            case MONITOREXIT:
                if (checkDisallowed) {
                    ErrorReporter.report("synchronized() may not be used by a player.", false);
                    throw new InstrumentationException();
                }
                // We need to strip these so we don't leave monitors locked when a robot dies
                instructions.set(n, new InsnNode(POP));
                break;
        }
    }

    private void visitLdcInsnNode(LdcInsnNode n) {
        bytecodeCtr++;
        if (n.cst instanceof Type) {
            n.cst = Type.getType(classDescReference(n.cst.toString()));
        }
    }

    private void visitMethodInsnNode(MethodInsnNode n) {
        // do various function replacements

        if (n.name.equals("hashCode") && n.desc.equals("()I") && n.getOpcode() != INVOKESTATIC) {
            bytecodeCtr++;
            endOfBasicBlock(n);
            // replace hashCode with deterministic version
            // send the object, its hash code, and the hash code method owner to
            // ObjectHashCode for analysis
            n.owner = "battlecode/instrumenter/inject/ObjectHashCode";
            n.desc = "(Ljava/lang/Object;)I";
            n.itf = false;
            n.setOpcode(INVOKESTATIC);
            return;
        }

        if (n.name.equals("identityHashCode") && n.owner.equals("java/lang/System")) {
            n.owner = "battlecode/instrumenter/inject/ObjectHashCode";
            return;
        }

        if (n.owner.equals("java/util/Random") && n.name.equals("<init>") &&
                n.desc.equals("()V")) {
            instructions.insertBefore(n, new MethodInsnNode(INVOKESTATIC, "battlecode/instrumenter/inject/RobotMonitor", "getRandomSeed", "()J", false));
            n.owner = "instrumented/java/util/Random";
            n.desc = "(J)V";
            return;
        }

        if (n.owner.equals("java/lang/String")) {
            if((n.name.equals("<init>")&&n.desc.equals("([B)V"))
                ||(n.name.equals("<init>")&&n.desc.equals("([BII)V"))
                ||(n.name.equals("getBytes")&&n.desc.equals("()[B"))) {
                instructions.insertBefore(n,new LdcInsnNode("UTF-16"));
                n.desc = n.desc.replace(")","Ljava/lang/String;)");
            }
        }

        // check for banned functions
        if (checkDisallowed) {
            // do wait/notify monitoring
            if ((n.desc.equals("()V") && (n.name.equals("wait") || n.name.equals("notify") || n.name.equals("notifyAll")))
                    || (n.name.equals("wait") && (n.desc.equals("(J)V") || n.desc.equals("(JI)V")))) {
                throw new InstrumentationException("Illegal method: Object." + n.name + "() cannot be called by a player.");
            }

            if (n.owner.equals("java/lang/Class") && n.name.equals("forName")) {
                throw new InstrumentationException("Illegal method in" + className + ": Class.forName() may not be called by a player.");
            }

            if (n.owner.equals("java/io/PrintStream") && n.name.equals("<init>") && n.desc.startsWith("(Ljava/lang/String;")) {
                throw new InstrumentationException("Illegal method in" + className + ": You may not use PrintStream to open files.");
            }

            if (n.owner.equals("java/lang/String") && n.name.equals("intern")) {
                throw new InstrumentationException("Illegal method in " + className + ": String.intern() cannot be called by a player.");
            }

        }

        boolean endBasicBlock = n.owner.startsWith(teamPackageName) || classReference(n.owner).startsWith("instrumented") || n.owner.startsWith("battlecode");

        MethodCostUtil.MethodData data = MethodCostUtil.getMethodData(n.owner, n.name);
        if (data != null) {
            bytecodeCtr += data.cost;
            endBasicBlock = data.shouldEndRound;
        }

        // instrument string regex functions
        if (n.owner.equals("java/lang/String") && instrumentedStringFuncs.contains(n.name)) {
            n.setOpcode(INVOKESTATIC);
            n.desc = "(Ljava/lang/String;" + n.desc.substring(1);
            n.owner = "instrumented/battlecode/instrumenter/inject/InstrumentableFunctions";
        } else if ((n.owner.equals("java/lang/Math") || n.owner.equals("java/lang/StrictMath"))
                && n.name.equals("random")) {
            n.owner = "instrumented/battlecode/instrumenter/inject/InstrumentableFunctions";
        }

        //hax the e.printStackTrace() method calls
        // This isn't quite the correct behavior.  If
        // we wanted to do the correct thing always
        // we would use reflection at runtime to
        // figure out whether the method we're
        // calling is Throwable.printStackTrace.
        // But in practice this should be good enough.
        else if (n.name.equals("printStackTrace") && n.desc.equals("()V") &&
                (n.owner == null || n.owner.equals("java/lang/Throwable") || isSuperClass(n.owner, "java/lang/Throwable"))) {
            instructions.insertBefore(n, new FieldInsnNode(GETSTATIC, "battlecode/instrumenter/inject/System", "out", "Ljava/io/PrintStream;"));
            n.desc = "(Ljava/io/PrintStream;)V";
        } else {
            // replace class names
            n.owner = classReference(n.owner);
            n.desc = methodDescReference(n.desc);
        }

        if (endBasicBlock)
            endOfBasicBlock(n);

    }

    private void visitMultiANewArrayInsnNode(MultiANewArrayInsnNode n) {
        bytecodeCtr++;
        n.desc = classDescReference(n.desc);
    }

    private void visitLabelNode(LabelNode n) {
        endOfBasicBlock(n);
        if (exceptionHandlers.contains(n))
            bytecodeCtr += GameConstants.EXCEPTION_BYTECODE_PENALTY;
    }

    private void visitTypeInsnNode(TypeInsnNode n) {
        bytecodeCtr++;
        n.desc = classReference(n.desc);
    }

    private void visitVarInsnNode(VarInsnNode n) {
        bytecodeCtr++;
        if (n.getOpcode() == RET)
            endOfBasicBlock(n);
    }

    private void visitLocalVariableNode(LocalVariableNode n) {
        n.desc = classDescReference(n.desc);
        n.signature = fieldSignatureReference(n.signature);
    }

    private void replaceVars(List<Object> l) {
        if (l == null)
            return;
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) instanceof String) {
                l.set(i, classReference((String) l.get(i)));
            }
        }
    }

    @SuppressWarnings("unchecked")	// n.local and n.stack are both supposed to be List<Object>, but they aren't for some reason?
    private void visitFrameNode(FrameNode n) {
        replaceVars(n.local);
        replaceVars(n.stack);
    }

    private void endOfBasicBlock(AbstractInsnNode n) {
        if (bytecodeCtr == 0)
            return;
        instructions.insertBefore(n, new LdcInsnNode(bytecodeCtr));
        instructions.insertBefore(n, new MethodInsnNode(INVOKESTATIC, "battlecode/instrumenter/inject/RobotMonitor", "incrementBytecodes", "(I)V", false));
        bytecodeCtr = 0;
    }

    /**
     * Tests whether the class referenced by <code>owner</code> extends or implements <code>superclass</code>.
     * e.g. isSuperClass("battlecode/common/GameActionException", "java/lang/Throwable") => true
     *
     * @param owner      - class to test
     * @param superclass - interface or superclass to test as an ancestor
     * @throws InstrumentationException if class <code>owner</code> cannot be found
     */
    private static boolean isSuperClass(String owner, String superclass) {
        ClassReader cr;

        try {
            cr = ClassReaderUtil.reader(owner);
        } catch (IOException ioe) {
            ErrorReporter.report("Can't find the class \"" + owner + "\", and this wasn't caught until the RobotMethodAdapter.isSuperClass stage.", true);
            throw new InstrumentationException();
        }

        InterfaceReader ir = new InterfaceReader();
        cr.accept(ir, ClassReader.SKIP_DEBUG);
        return Arrays.asList(ir.getInterfaces()).contains(superclass);
    }

}
