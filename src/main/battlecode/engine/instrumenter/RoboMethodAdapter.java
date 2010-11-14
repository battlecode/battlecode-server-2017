package battlecode.engine.instrumenter;

import static battlecode.common.GameConstants.EXCEPTION_BYTECODE_PENALTY;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.objectweb.asm.ClassReader.*;

import battlecode.engine.ErrorReporter;
import battlecode.server.Config;

/**
 * Instruments a method.  See InstrumenterASMImpl for more info on what this instrumentation does.
 *
 * @author adamd
*/
public class RoboMethodAdapter extends MethodAdapter implements Opcodes {

	private final String methodName;
	private final String teamPackageName;
	private final String className;	// the class to which this method belongs
	private final boolean debugMethodsEnabled;
	private final boolean silenced;
	private final boolean checkDisallowed;
	private final String methodDesc;	// the description of this method, e.g., "()V"
	private Label debugStartLabel;	// a Label that marks the top of the method, if the given method is a debug method
	private boolean codeVisited = false;	// tells whether visitCode() has been called

	// all the exception handlers we've seen in the code
	private final Set<Label> exceptionHandlers;

	// this gives the running count of how many bytecodes we've seen in the basic block.  At the end of the basic block, this number is passed
	// to RobotMonitor.incrementBytecodeCtr, and bytecodeCtr is reset to 0 for the next basic block.
	private int bytecodeCtr = 0;

	private static HashSet<String> instrumentedStringFuncs;


	static {
		instrumentedStringFuncs = new HashSet<String>();
		instrumentedStringFuncs.add("matches");
		instrumentedStringFuncs.add("replaceAll");
		instrumentedStringFuncs.add("replaceFirst");
		instrumentedStringFuncs.add("split");
	}

	private class LabelPair {
		public final Label first;
		public final Label second;

		public LabelPair(Label first, Label second) {
			this.first=first;
			this.second=second;
		}

		public boolean equals(LabelPair p) {
			return first.equals(p.first)&&second.equals(p.second);
		}

		public int hashCode() {
			return first.hashCode()^second.hashCode();
		}
	}

	HashSet<LabelPair> tryCatchEncountered = new HashSet<LabelPair>();
	Label robotDeathLabel;
	
	/**
	 * Creates a new RoboMethodAdapter to instrument a given method.
	 * @param mv the MethodVisitor used to read the method
	 * @param className the binary name of the class to which the given method belongs
	 * @param methodName the binary name of the method that is being instrumented
	 * @param methodDesc the description (e.g., "()V") of the method that is being instrumented
	 * @param teamPackageName the package name of the team for which this method is being instrumented
	 * @param debugMethodsEnabled whether debug methods should be enabled
	 * @param silenced whether System.out should be silenced
	 */
    public RoboMethodAdapter(final MethodVisitor mv, final String className, final String methodName, final String methodDesc, final String teamPackageName, final boolean debugMethodsEnabled, boolean silenced, boolean checkDisallowed) {
        super(mv);
        this.methodName = methodName;
		this.teamPackageName = teamPackageName;
		this.className = className;
		this.debugMethodsEnabled = debugMethodsEnabled;
		this.silenced = silenced;
		this.checkDisallowed = checkDisallowed;
		this.methodDesc = methodDesc;
		exceptionHandlers = new HashSet<Label>();
    }
    
	public void visitCode() {
		// if we're in a debug method, create a start label
		if(methodName.startsWith("debug_") && methodDesc.endsWith("V")) {
			debugStartLabel = new Label();
			super.visitLabel(debugStartLabel);
		}
		codeVisited = true;
	}
	
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		incrementBytecodeCtr();
		super.visitFieldInsn(opcode, ClassReferenceUtil.classReference(owner, teamPackageName, silenced, checkDisallowed), name, ClassReferenceUtil.classDescReference(desc, teamPackageName, silenced, checkDisallowed));
	}
	
	public void visitIincInsn(int var, int increment) {
		incrementBytecodeCtr();
		super.visitIincInsn(var, increment);
	}
	
	public void visitInsn(int opcode) {
		incrementBytecodeCtr();
		// any "return" bytecode means the end of a basic block
		switch(opcode) {
		case IRETURN:
		case LRETURN:
		case FRETURN:
		case DRETURN:
		case ARETURN:
		case RETURN:
			endOfBasicBlock();
			if(methodName.startsWith("debug_") && methodDesc.endsWith("V")) {
				super.visitMethodInsn(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "decrementDebugLevel", "()V");
			}
			super.visitInsn(opcode);
			break;
		case MONITORENTER:
		case MONITOREXIT:
			if(checkDisallowed && !InstrumentingClassLoader.lazy()) {
				ErrorReporter.report("synchronized() may not be used by a player.",false);
				throw new InstrumentationException();
			}
			// Allowing non-contestant code to use monitorenter/monitorexit
			// shouldn't cause any problems since there's no reason for java
			// to lock anything in battlecode.common.  But we may as well
			// remove them for performance reasons.
			super.visitInsn(POP);
			break;
		default:
			super.visitInsn(opcode);
		}
	}
	
	public void visitIntInsn(int opcode, int operand) {
		incrementBytecodeCtr();
		super.visitIntInsn(opcode, operand);
	}
	
	public void visitJumpInsn(int opcode, Label label) {
		incrementBytecodeCtr();
		// all jump bytecodes mean the end of a basic block
		endOfBasicBlock();
		super.visitJumpInsn(opcode, label);
	}
		
	public void visitLdcInsn(Object cst) {
		incrementBytecodeCtr();
		if(cst instanceof Type) {
			cst = Type.getType(ClassReferenceUtil.classDescReference(cst.toString(),teamPackageName,silenced,checkDisallowed));
		}
		/* This is the code that replaces constant pool Strings with fake Strings.  Uncomment it once fake
		    Strings have been written.  This code assumes that fake Strings have a constructor that takes a regular
		    String. */
/*		// TODO: test this
		if(cst instanceof String) {
			System.out.println("found: " + cst.toString());
			super.visitTypeInsn(NEW,"battlecode/java/lang/String");
			super.visitInsn(DUP);
			super.visitLdcInsn(cst);
			super.visitMethodInsn(INVOKESPECIAL,"battlecode/java/lang/String","<init>","(Ljava/lang/String;)V");
		} else {*/
			super.visitLdcInsn(cst);
//		}
	}
	
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		incrementBytecodeCtr();
		// switches mean the end of a basic block
		endOfBasicBlock();
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {

		if(name.equals("hashCode")&&desc.equals("()I")&&opcode!=INVOKESTATIC) {
			incrementBytecodeCtr();
			endOfBasicBlock();
			owner = ClassReferenceUtil.classReference(owner, teamPackageName, silenced, checkDisallowed);
			// replace hash code with deterministic version
			// First, we check if the hash code is the same as
			// the identity hash code.  If they are different, we know
			// hashCode was reimplemented.  If they are the same and
			// bc.server.fast-hash is set, then we assume that hashCode
			// was not reimplemented.  (Chance of accidental collision
			// is 1/2^32.)  If bc.server.fast-hash is not set, then
			// we check if hashCode was reimplemented using reflection.
			// (Around 30x slower, but guaranteed correct.)
			super.visitInsn(DUP);
			super.visitMethodInsn(opcode,owner,name,desc);
			super.visitInsn(SWAP);
			super.visitInsn(DUP2);
			super.visitMethodInsn(INVOKESTATIC,"java/lang/System","identityHashCode","(Ljava/lang/Object;)I");
			Label doneLabel = new Label();
			Label sameLabel = new Label();
			super.visitJumpInsn(IF_ICMPEQ,sameLabel);
			super.visitInsn(POP);
			super.visitJumpInsn(GOTO,doneLabel);
			super.visitLabel(sameLabel);
			if(InstrumentingClassLoader.fastHash()) {
				super.visitInsn(SWAP);
				super.visitInsn(POP);
				super.visitMethodInsn(INVOKESTATIC,"battlecode/engine/instrumenter/lang/ObjectHashCode","identityHashCode","(Ljava/lang/Object;)I");
			}
			else {
				if(opcode==INVOKESPECIAL) {
					super.visitLdcInsn(Type.getObjectType(owner));
				}
				else {
					super.visitInsn(DUP);
					super.visitMethodInsn(opcode,owner,"getClass","()Ljava/lang/Class;");
				}
				super.visitMethodInsn(INVOKESTATIC,"battlecode/engine/instrumenter/lang/ObjectHashCode","hashCode","(ILjava/lang/Object;Ljava/lang/Class;)I");
			}
			super.visitLabel(doneLabel);
			return;
		}

		// I hate BigInteger so much for this.
		if(owner.equals("sun/misc/Unsafe")&&name.equals("getUnsafe")) {
			endOfBasicBlock();
			visitInsn(RETURN);
		}

		if(owner.equals("java/util/Random")&&name.equals("<init>")&&
			desc.equals("()V")) {
			super.visitMethodInsn(INVOKESTATIC,"battlecode/engine/instrumenter/lang/RoboRandom","getMapSeed","()J");
			super.visitMethodInsn(INVOKESPECIAL,"instrumented/java/util/Random","<init>","(J)V");
			return;
		}

		if(checkDisallowed) {
			// do wait/notify monitoring
			if((desc.equals("()V") && (name.equals("wait") || name.equals("notify") || name.equals("notifyAll")))
			   || (name.equals("wait") && (desc.equals("(J)V") || desc.equals("(JI)V")))) {
				forbidden("Illegal method: Object." + name + "() cannot be called by a player");
			}

			if(owner.equals("java/lang/Class")&&name.equals("forName")) {
				forbidden("Illegal method in "+className+": You may not use Class.forName().");
			}

			if(owner.equals("java/io/PrintStream")&&name.equals("<init>")&&desc.startsWith("(Ljava/lang/String;")) {
				forbidden("Illegal method in "+className+": You may not use PrintStream to open files.");
			}
			
			if(owner.equals("java/lang/String")&&instrumentedStringFuncs.contains(name)) {
				opcode = INVOKESTATIC;
				desc = "(Ljava/lang/String;"+desc.substring(1);
				owner = "battlecode/engine/instrumenter/lang/InstrumentableString";
			}
			
			// do random monitoring
			else if(owner.equals("java/lang/Math") && name.equals("random")) {
				forbidden("Illegal method in " + className + ": Math.random() cannot be called by a player.  Use java.util.Random instead.");
			}
			else if(owner.equals("java/util/Collections") && name.equals("shuffle") && desc.equals("(Ljava/util/List;)V")) {
				forbidden("Illegal method in " + className + ": You must supply Collections.shuffle() with a Random.");
			}
			else if(owner.equals("java/lang/StrictMath") && name.equals("random")) {
				forbidden("Illegal method in " + className + ": StrictMath.random() cannot be called by a player.  Use java.util.Random instead.");
			}
			else if(owner.equals("java/lang/String") && name.equals("intern")) {
				forbidden("Illegal method in " + className + ": String.intern() cannot be called by a player.");
			}
		}
		
		// do debug method craziness
		boolean isDebugMethod;
		Label start = null, end = null, handler = null;
		if(name.startsWith("debug_") && desc.endsWith("V")) {
			isDebugMethod = true;
			if(!debugMethodsEnabled) {
				// if debug methods aren't enabled, we remove the call to the debug method
				// first, pop the arguments from the stack
				List<Type> typeList = Arrays.asList(Type.getArgumentTypes(desc));
				Collections.reverse(typeList); //take arguments off in reverse order
				for(Type t : typeList) {
					switch(t.getSize()) {
						case 1:
							super.visitInsn(POP);
							break;
						case 2:
							super.visitInsn(POP2);
							break;
						default:
							ErrorReporter.report("Illegal type size: not 1 or 2", true);
							throw new InstrumentationException();
					}
				}
				// next, pop the class on which the method would be called
				if(opcode != INVOKESTATIC)
					super.visitInsn(POP);
				// if we don't write any instructions then we might have a frame that is not followed by an instruction.  asm seems not to like this.
				else
					super.visitInsn(NOP);
				return;
			}
		}
		else
			isDebugMethod = false;

		// if it's not a debug method, increment one for the INVOKE bytecode
		if(!isDebugMethod)
			incrementBytecodeCtr();
				
		// get the lookup data for this method call
		MethodCostUtil.MethodData data = MethodCostUtil.getMethodData(owner, name);
		if(data == null) {
			// if we don't have lookup data for this method, it just ends the basic block
			endOfBasicBlock();
		} else {
			// if we do have lookup data, add the bytecode cost
			incrementBytecodeCtr(data.cost);
			if(data.shouldEndRound == true)
				endOfBasicBlock();
		}
		
		// insert a call to RobotMonitor.incrementDebugLevel before any call to a debug method		
		if(isDebugMethod && debugMethodsEnabled)
			super.visitMethodInsn(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "incrementDebugLevel", "()V");
		
		
		
		//hax the e.printStackTrace() method calls
		if (name.equals("printStackTrace") && desc.equals("()V") &&
				(owner == null || owner.equals("java/lang/Throwable")|| isSuperClass(owner, "java/lang/Throwable"))){
			super.visitFieldInsn(GETSTATIC,"battlecode/engine/instrumenter/lang/System","out","Ljava/io/PrintStream;");
			 super.visitMethodInsn(INVOKEVIRTUAL,
					   "java/lang/Throwable",
					   "printStackTrace",
					   "(Ljava/io/PrintStream;)V");
		} else
			super.visitMethodInsn(opcode, ClassReferenceUtil.classReference(owner, teamPackageName, silenced, checkDisallowed), name, ClassReferenceUtil.methodDescReference(desc, teamPackageName, silenced, checkDisallowed));
	}

	/**
	 * Tests whether the class referenced by <code>owner</code> extends or implements <code>superclass</code>.
	 * e.g. isSuperClass("battlecode/common/GameActionException", "java/lang/Throwable") => true
	 * @param owner - class to test
	 * @param superclass - interface or superclass to test as an ancestor
	 * @throws InstrumentationException if class <code>owner</code> cannot be found
	 */
	private static boolean isSuperClass(String owner, String superclass) {
		ClassReader cr = null;
		
		try{
			cr = new ClassReader(owner);
		}catch(IOException ioe) {
			ErrorReporter.report("Can't find the class \"" + owner + "\", and this wasn't caught until the RobotMethodAdapter.isSuperClass stage.", true);
			throw new InstrumentationException();
		}
		
		InterfaceReader ir = new InterfaceReader();
		cr.accept(ir, SKIP_DEBUG);
		return Arrays.asList(ir.getInterfaces()).contains(superclass);
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
		incrementBytecodeCtr();
		super.visitMultiANewArrayInsn(ClassReferenceUtil.classDescReference(desc, teamPackageName, silenced, checkDisallowed), dims);
	}
	
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		incrementBytecodeCtr();
		// a switch means the end of the basic block
		endOfBasicBlock();
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}
	
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// dgulotta - Found a fix that doesn't leak memory.  We create a
		// new handler for RobotDeathExceptions that won't re-catch them.
		// FWIW I think the re-catching thing may be due to the compiler using
		// the same code for synchronized() and finally.  With synchronized()
		// you really do want to re-catch exceptions so you can make sure you
		// release the lock.  Normally an exception wouldn't occur, but
		// Thread.stop() can cause an exception anywhere.  Older compilers
		// generated code that did not re-catch exceptions; see for
		// example chapter 7 of the second edition of the JVM spec.
		//amrik - fix for the finally clause. the engine stalled on a specific game and the error was traced down
		// to a RobotMonitor.switchRunner call in Scheduler.passToNextThread, where throwing a RobotDeathException
		// caused an infinite loop. The exception gets thrown, the finally clause executed but no other method above
		// in the stack trace could catch/rethrow it.
		//
		/*adamd -
		 *I figured out why the game engine is stalling.  Apparently, in Java if an exception is thrown in the very
		 *first bytecode of a finally clause, it is caught by the same try-finally clause.  Normally, the first 
		 *bytecode of a finally clause just stores the exception to a local variable, which almost never throws an
		 *exception, so you never notice it.  But in our case, when we instrument contestants' try-finally clauses,
		 *we rethrow anyRobotDeathExceptions in the finally clause, but this gets caught by the sametry-finally,
		 *hence the infinite loop.  I can't understand why Java adds sillyhandlers like "if an exception occurs
		 *between A and B, goto A", but oh well.
		 */
		/*
		if (start == handler)
			return;
		*/
		
		//-- end fix

		if(robotDeathLabel == null)
			robotDeathLabel = new Label();

		LabelPair p = new LabelPair(start,end);
		if(!tryCatchEncountered.contains(p)) {
			// don't let player recover from robot death / out of memory / stack overflow 
			super.visitTryCatchBlock(start,end,robotDeathLabel,"java/lang/VirtualMachineError");
			tryCatchEncountered.add(p);
		}
	
		exceptionHandlers.add(handler);
		super.visitTryCatchBlock(start, end, handler, (type==null ? null : ClassReferenceUtil.classReference(type, teamPackageName, silenced, checkDisallowed)));
	}
	
	
	public void visitLabel(Label label) {
		
		endOfBasicBlock();
		super.visitLabel(label);
		
		if(exceptionHandlers.contains(label)) {
			// I don't know if this is ever necessary (it would only be
			// needed if some non-instrumented code calls back to
			// instrumented code from inside a try-catch loop that catches
			// Errors), but just in case...
			super.visitMethodInsn(INVOKESTATIC,"battlecode/engine/instrumenter/RobotMonitor","checkForRobotDeath","()V");
			incrementBytecodeCtr(EXCEPTION_BYTECODE_PENALTY);
			endOfBasicBlock();
		}
	}

	public void visitLocalVariable(String name, String desc, String signature,
								   Label start, Label end, int index) {
		super.visitLocalVariable(name,
								 ClassReferenceUtil.classDescReference(desc, teamPackageName, silenced, checkDisallowed),
								 ClassReferenceUtil.fieldSignatureReference(signature, teamPackageName, silenced, checkDisallowed),
								 start,
								 end,
								 index);
	} 

	public void visitTypeInsn(int opcode, String desc) {
		incrementBytecodeCtr();
		super.visitTypeInsn(opcode, ClassReferenceUtil.classReference(desc, teamPackageName, silenced, checkDisallowed));
	}
	
	public void visitVarInsn(int opcode, int var) {
		incrementBytecodeCtr();

		if(opcode == RET)
			endOfBasicBlock();
		super.visitVarInsn(opcode, var);
	}
	
	public void visitMaxs(int maxStack, int maxLocals) {
		// these values are filled in automatically by ClassAdapter, so what we pass here doesn't really matter
		super.visitMaxs(0, 0);
	}
	
	public void visitEnd() {
		// if the method we're instrumenting is a debug method, and it's non-static (i.e., we've visited its code), then wrap the body in a
		// try-finally{RobotMonitory.decrementDebugLevel();}
		if(codeVisited) {
			endOfBasicBlock();
			if(methodName.startsWith("debug_") && methodDesc.endsWith("V")) {
				Label end = new Label();
				Label after = new Label();
				Label handler = new Label();
				super.visitMethodInsn(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "decrementDebugLevel", "()V");
				super.visitLabel(end);
				super.visitJumpInsn(GOTO, after);
				super.visitLabel(handler);
				super.visitMethodInsn(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "decrementDebugLevel", "()V");
				super.visitInsn(ATHROW);
				super.visitLabel(after);
				super.visitInsn(RETURN);
				super.visitTryCatchBlock(debugStartLabel, end, handler, "java/lang/Throwable");
			}
			
			if(robotDeathLabel!=null) {
				super.visitLabel(robotDeathLabel);
				super.visitInsn(ATHROW);
			}

		}
			
		super.visitEnd();
	}
	
	/******Utility Methods******/
	
	private void forbidden(String reason) {
		if(InstrumentingClassLoader.lazy()) {
			super.visitLdcInsn(reason);
			super.visitLdcInsn(false);
			super.visitMethodInsn(INVOKESTATIC, "battlecode/engine/ErrorReporter", "report", "(Ljava/lang/String;Z)V");
			super.visitTypeInsn(NEW, "battlecode/engine/instrumenter/InstrumentationException");
			super.visitInsn(DUP);
			super.visitMethodInsn(INVOKESPECIAL, "battlecode/engine/instrumenter/InstrumentationException", "<init>", "()V");
			super.visitInsn(ATHROW);
		}
		else {
			ErrorReporter.report(reason,false);
			throw new InstrumentationException();
		}
	}

	private void incrementBytecodeCtr() {
		bytecodeCtr++;
	}
	
	private void incrementBytecodeCtr(int amount) {
		bytecodeCtr += amount;
	}
	
	private void endOfBasicBlock() {
		if(methodName.equals("<clinit>")&&!checkDisallowed) {
			// Don't charge for static initializers of
			// builtin classes
			return;
		}
		switch(bytecodeCtr) {
			case 0:
				return;
			case 1:
				super.visitInsn(ICONST_1);
				break;
			case 2:
				super.visitInsn(ICONST_2);
				break;
			case 3:
				super.visitInsn(ICONST_3);
				break;
			case 4:
				super.visitInsn(ICONST_4);
				break;
			case 5:
				super.visitInsn(ICONST_5);
				break;
			default:
				super.visitLdcInsn(new Integer(bytecodeCtr));
				break;
		}
		super.visitMethodInsn(INVOKESTATIC, "battlecode/engine/instrumenter/RobotMonitor", "incrementBytecodes", "(I)V");
		bytecodeCtr = 0;
	}
	
}
