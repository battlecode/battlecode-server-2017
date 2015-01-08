package battlecode.engine.instrumenter;

// Should produce the exact same results as RoboMethodAdapter.  I wrote
// this in an attempt to fix a strange bug.  RoboMethodTree is more
// extensible than RoboMethodAdapter, but also less well tested. -dgulotta

import battlecode.common.GameConstants;
import battlecode.engine.ErrorReporter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.tree.AbstractInsnNode.*;

public class RoboMethodTree extends MethodNode implements Opcodes {

    private final String methodName;
    private final String teamPackageName;
    private final String className;    // the class to which this method belongs
    private final boolean debugMethodsEnabled;
    private final boolean silenced;
    private final boolean checkDisallowed;
    private final String methodDesc;    // the description of this method, e.g., "()V"
    private boolean codeVisited = false;    // tells whether visitCode() has been called

    // all the exception handlers we've seen in the code
    private final Set<LabelNode> exceptionHandlers = new HashSet<LabelNode>();
    private final Set<LabelNode> tryCatchStarts = new HashSet<LabelNode>();

    private static final Set<String> instrumentedStringFuncs = new HashSet<String>();

    static {
        instrumentedStringFuncs.add("matches");
        instrumentedStringFuncs.add("replaceAll");
        instrumentedStringFuncs.add("replaceFirst");
        instrumentedStringFuncs.add("split");
    }

    private LabelNode startLabel;

    private int bytecodeCtr = 0;

    private MethodVisitor methodWriter;

    private static boolean checkedFastHash = false, usingFastHash;

    public RoboMethodTree(final MethodVisitor mv, final String className, final int access, final String methodName, final String methodDesc, final String signature, final String[] exceptions, final String teamPackageName, final boolean debugMethodsEnabled, boolean silenced, boolean checkDisallowed) {
        super(Opcodes.ASM5, access, methodName, methodDesc, signature, exceptions);
        this.methodName = methodName;
        this.teamPackageName = teamPackageName;
        this.className = className;
        this.debugMethodsEnabled = debugMethodsEnabled;
        this.silenced = silenced;
        this.checkDisallowed = checkDisallowed;
        this.methodDesc = methodDesc;
        methodWriter = mv;
        if (!checkedFastHash) {
            usingFastHash = Boolean.getBoolean(battlecode.server.Config.getGlobalConfig().get("bc.server.fast-hash"));
        }
    }

    protected String classReference(String name) {
        return ClassReferenceUtil.classReference(name, teamPackageName, silenced, checkDisallowed);
    }

    protected String classDescReference(String name) {
        return ClassReferenceUtil.classDescReference(name, teamPackageName, silenced, checkDisallowed);
    }

    protected String methodDescReference(String name) {
        return ClassReferenceUtil.methodDescReference(name, teamPackageName, silenced, checkDisallowed);
    }

    protected String fieldSignatureReference(String name) {
        return ClassReferenceUtil.fieldSignatureReference(name, teamPackageName, silenced, checkDisallowed);
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
        if (methodName.startsWith("debug_") && methodDesc.endsWith("V") && debugMethodsEnabled) {
            addDebugHandler();
        }
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

    private void addRobotDeathHandler() {
        LabelNode robotDeathLabel = new LabelNode(new Label());
        LabelNode firstTryCatch = null;
        for(AbstractInsnNode node : instructions.toArray()) {
            if(node.getType()==AbstractInsnNode.LABEL&&tryCatchStarts.contains((LabelNode)node)) {
                firstTryCatch = (LabelNode)node;
                break;
            }
        }
        tryCatchBlocks.add(0, new TryCatchBlockNode(firstTryCatch, robotDeathLabel, robotDeathLabel, "java/lang/VirtualMachineError"));
        instructions.add(robotDeathLabel);
        instructions.add(new FrameNode(F_FULL, 0, new Object[0], 1, new Object[]{"java/lang/VirtualMachineError"}));
        instructions.add(new InsnNode(ATHROW));
    }

    private void addDebugHandler() {
        LabelNode debugEndLabel = new LabelNode(new Label());
        tryCatchBlocks.add(new TryCatchBlockNode(startLabel, debugEndLabel, debugEndLabel, null));
        instructions.insertBefore(nextInstruction(instructions.getFirst()), new MethodInsnNode(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "incrementDebugLevel", "()V", false));
        instructions.add(debugEndLabel);
        instructions.add(new FrameNode(F_FULL, 0, new Object[0], 1, new Object[]{"java/lang/Throwable"}));
        instructions.add(new MethodInsnNode(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "decrementDebugLevel", "()V", false));
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
                if (methodName.startsWith("debug_") && methodDesc.endsWith("V")) {
                    instructions.insertBefore(n, new MethodInsnNode(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "decrementDebugLevel", "()V", false));
                }
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
        
        if(n.getOpcode() == INVOKE_DYNAMIC_INSN) {
        // TODO: Handle Invoke Dynamic correctly to add support for jruby/jython/etc. Checking submissions on CS for any disqualifications from this.
        //throw new RuntimeException("Invoke Dynamic probably not instrumented correctly by Battlecode Engine. Plz don't use for now");
        }
        
        if (n.name.equals("hashCode") && n.desc.equals("()I") && n.getOpcode() != INVOKESTATIC) {
            bytecodeCtr++;
            endOfBasicBlock(n);
            // replace hashCode with deterministic version
            // send the object, its hash code, and the hash code method owner to
            // ObjectHashCode for analysis
            instructions.insertBefore(n, new InsnNode(DUP));
            instructions.insertBefore(n, new MethodInsnNode(n.getOpcode(), classReference(n.owner), "hashCode", "()I", n.itf));
            instructions.insertBefore(n, new InsnNode(SWAP));
            if (n.getOpcode() == INVOKESPECIAL) {
                instructions.insertBefore(n, new LdcInsnNode(Type.getObjectType(n.owner)));
            } else {
                instructions.insertBefore(n, new InsnNode(DUP));
                instructions.insertBefore(n, new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
            }
            n.name = usingFastHash ? "fastHashCode" : "hashCode";
            n.owner = "battlecode/engine/instrumenter/lang/ObjectHashCode";
            n.desc = "(ILjava/lang/Object;Ljava/lang/Class;)I";
            n.itf = false;
            n.setOpcode(INVOKESTATIC);
            return;
        }

        if (n.name.equals("identityHashCode") && n.owner.equals("java/lang/System")) {
            n.owner = "battlecode/engine/instrumenter/lang/ObjectHashCode";
            return;
        }

        if (n.owner.equals("java/util/Random") && n.name.equals("<init>") &&
                n.desc.equals("()V")) {
            instructions.insertBefore(n, new MethodInsnNode(INVOKESTATIC, "battlecode/engine/instrumenter/lang/RoboRandom", "getMapSeed", "()J", false));
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
                illegalMethod(n, "Illegal method: Object." + n.name + "() cannot be called by a player.");
            }

            if (n.owner.equals("java/lang/Class") && n.name.equals("forName")) {
                illegalMethod(n, "Illegal method in" + className + ": Class.forName() may not be called by a player.");
            }

            if (n.owner.equals("java/io/PrintStream") && n.name.equals("<init>") && n.desc.startsWith("(Ljava/lang/String;")) {
                illegalMethod(n, "Illegal method in" + className + ": You may not use PrintStream to open files.");
            }

            if (n.owner.equals("java/lang/String") && n.name.equals("intern")) {
                illegalMethod(n, "Illegal method in " + className + ": String.intern() cannot be called by a player.");
            }

        }

        boolean isDebugMethod = n.name.startsWith("debug_") && n.desc.endsWith("V") && n.owner.startsWith(teamPackageName);
        boolean endBasicBlock = n.owner.startsWith(teamPackageName) || classReference(n.owner).startsWith("instrumented") || n.owner.startsWith("battlecode");

        if (!isDebugMethod)
            bytecodeCtr++;

        MethodCostUtil.MethodData data = MethodCostUtil.getMethodData(n.owner, n.name);
        if (data != null) {
            bytecodeCtr += data.cost;
            endBasicBlock = data.shouldEndRound;
        }

        // do various function replacements

        // instrument string regex functions
        if (n.owner.equals("java/lang/String") && instrumentedStringFuncs.contains(n.name)) {
            n.setOpcode(INVOKESTATIC);
            n.desc = "(Ljava/lang/String;" + n.desc.substring(1);
            n.owner = "instrumented/battlecode/engine/instrumenter/lang/InstrumentableFunctions";
        } else if ((n.owner.equals("java/lang/Math") || n.owner.equals("java/lang/StrictMath")) && n.name.equals("random")) {
            n.owner = "instrumented/battlecode/engine/instrumenter/lang/InstrumentableFunctions";
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
            instructions.insertBefore(n, new FieldInsnNode(GETSTATIC, "battlecode/engine/instrumenter/lang/System", "out", "Ljava/io/PrintStream;"));
            n.desc = "(Ljava/io/PrintStream;)V";
        } else {
            // replace class names
            n.owner = classReference(n.owner);
            n.desc = methodDescReference(n.desc);

            // debug methods
            /*
               if(name.startsWith("debug_")) {
                   System.out.println("debug "+className+" "+methodName+" "+owner+" "+name+" "+checkDisallowed);
               }
               */
            if (isDebugMethod) {
                if (debugMethodsEnabled) {
                    // we need to end the basic block BEFORE the debug level is incremented
                    //endOfBasicBlock(n);
                    //instructions.insertBefore(n, new MethodInsnNode(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "incrementDebugLevel", "()V"));
                    // don't call endOfBasicBlock twice
                    //endBasicBlock=false;
                } else {
                    // if debug methods aren't enabled, we remove the call to the debug method
                    // first, pop the arguments from the stack
                    // arguments are popped in reverse order, so add instructions to newInsns
                    // using insert, not add
                    InsnList newInsns = new InsnList();
                    for (Type t : Type.getArgumentTypes(n.desc)) {
                        switch (t.getSize()) {
                            case 1:
                                //System.out.println("pop "+className+" "+methodName);
                                newInsns.insert(new InsnNode(POP));
                                break;
                            case 2:
                                newInsns.insert(new InsnNode(POP2));
                                break;
                            default:
                                ErrorReporter.report("Illegal type size: not 1 or 2", true);
                                throw new InstrumentationException();
                        }
                    }
                    // next, pop the class on which the method would be called
                    if (n.getOpcode() != INVOKESTATIC)
                        newInsns.add(new InsnNode(POP));
                    // if we remove the method call and don't add any instructions then we could end up with a FrameNode that does not have an instruction following it.  asm seems not to like this.
                    if (newInsns.getFirst() == null)
                        newInsns.add(new InsnNode(NOP));
                    instructions.insertBefore(n, newInsns);
                    instructions.remove(n);
                    // no function was called so don't end the basic block
                    endBasicBlock = false;
                }
            }
        }

        if (endBasicBlock)
            endOfBasicBlock(n);

    }

    private void illegalMethod(MethodInsnNode n, String message) {
        if (InstrumentingClassLoader.lazy()) {
            instructions.insertBefore(n, new LdcInsnNode(message));
            instructions.insertBefore(n, new MethodInsnNode(INVOKESTATIC, "battlecode/engine/instrumenter/RoboMethodTree", "reportIllegalMethod", "(Ljava/lang/String;)V", false));
        } else {
            ErrorReporter.report(message, false);
            throw new InstrumentationException();
        }
    }

    public static void reportIllegalMethod(String message) {
        ErrorReporter.report(message, false);
        throw new RobotDeathException();
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

    private void visitFrameNode(FrameNode n) {
        replaceVars(n.local);
        replaceVars(n.stack);
    }

    private void endOfBasicBlock(AbstractInsnNode n) {
        if (bytecodeCtr == 0)
            return;
        instructions.insertBefore(n, new LdcInsnNode(new Integer(bytecodeCtr)));
        instructions.insertBefore(n, new MethodInsnNode(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "incrementBytecodes", "(I)V", false));
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
        ClassReader cr = null;

        try {
            cr = new ClassReader(owner);
        } catch (IOException ioe) {
            ErrorReporter.report("Can't find the class \"" + owner + "\", and this wasn't caught until the RobotMethodAdapter.isSuperClass stage.", true);
            throw new InstrumentationException();
        }

        InterfaceReader ir = new InterfaceReader();
        cr.accept(ir, ClassReader.SKIP_DEBUG);
        return Arrays.asList(ir.getInterfaces()).contains(superclass);
    }

}
