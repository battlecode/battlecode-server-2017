package battlecode.engine.instrumenter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipFile;

import battlecode.engine.ErrorReporter;

import org.objectweb.asm.*;
import static org.objectweb.asm.ClassWriter.*;

public abstract class InstrumentingClassLoader extends ClassLoader {
		
	// whether classes instrumented by this ICL should have their System.out silenced

	protected final boolean silenced;
	protected final boolean debugMethodsEnabled;

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
		try{
			if(className.startsWith("instrumented/"))
				cr = new ClassReader(className.substring(13));
			else
				cr = new ClassReader(className);
		}catch(IOException ioe) {
			ErrorReporter.report("Can't find the class \"" + className + "\"", "Make sure the team name is spelled correctly.\nMake sure the .class files are in the right directory (teams/teamname/*.class)");
			throw new InstrumentationException();
		}
		ClassWriter cw = new ClassWriter(COMPUTE_MAXS); // passing true sets maxLocals and maxStack, so we don't have to
		ClassVisitor cv = new RoboAdapter(cw, teamPackageName, debugMethodsEnabled, silenced, checkDisallowed);
		cr.accept(cv, 0);		//passing false lets debug info be included in the transformation, so players get line numbers in stack traces
		byte[] bytes = cw.toByteArray();
		return bytes;
	}

	public abstract Class<?> saveAndDefineClass(String name, byte [] classBytes);

}