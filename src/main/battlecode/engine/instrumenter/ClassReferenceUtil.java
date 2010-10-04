package battlecode.engine.instrumenter;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import battlecode.engine.ErrorReporter;

/**
 * ClassReferenceUtil is a singleton used to keep track of class references during instrumentation.
 * <p>
 * Whenever a class reference is encountered while instrumenting a class, that reference should be registered with
 * ClassReferenceUtil (e.g., using <code>classReference(...)</code>).  This does two things.  First, the class reference
 * may be replaced with a reference to a different class (e.g., replacing Random with RoboRandom).  Second,
 * ClassReferenceUtil remembers new references.  New class references can be retrieved using flushNewlyReferencedClasses.
 *
 * @author adamd
 */
class ClassReferenceUtil {
	// packages for which the player is allowed to use any of the contained classes; loaded from AllowedPackages.txt in idata
	private final static Set<String> allowedPackages;
		
	// a set of classes the player is not allowed to use; loaded from DisallowedClasses.txt in idata
	private final static Set<String> disallowedClasses;

	// We can't instrument these classes because they have native methods.  Java won't allow us
	// to create an instrumented class that has the same prefix as a builtin class, so we have to
	// change the name.  But when we rename the class, it can't use the old class's native methods any more.
	// This might have issues so for now I'm just not instrumenting java.io
	// private final static Set<String> uninstrumentedClasses;
		
	private ClassReferenceUtil() {}

	// the static constructor basically loads the whitelist files and caches them in allowedPackages and disallowedClasses
	static {
		BufferedReader reader;
		String line;
		ZipFile zfile;
		
		// load our zip file
		try{
			zfile = new ZipFile("idata");
		} catch(Exception e) {
			ErrorReporter.report("Error loading idata", "Check that the 'idata' file is in its proper place");
			throw new InstrumentationException();
		}
		
		// load allowed packages
		try{
			allowedPackages = new HashSet<String>();
			reader = new BufferedReader(new InputStreamReader(zfile.getInputStream(zfile.getEntry("AllowedPackages.txt"))));
			while((line = reader.readLine()) != null) {
				allowedPackages.add(line);
			}
		}catch(Exception e) {
			ErrorReporter.report("Error loading idata", "Check that the 'idata' file is not corrupted");
			throw new InstrumentationException();
		}
			
		// load disallowed classes
		try{
			disallowedClasses = new HashSet<String>();
			reader = new BufferedReader(new InputStreamReader(zfile.getInputStream(zfile.getEntry("DisallowedClasses.txt"))));
			while((line = reader.readLine()) != null) {
				disallowedClasses.add(line);
			}
		}catch(Exception e) {
			ErrorReporter.report("Error loading idata", "Check that the 'idata' file is not corrupted");
			throw new InstrumentationException();
		}

		/*
		uninstrumentedClasses = new HashSet<String>();
		uninstrumentedClasses.add("java/io/FileInputStream");
		uninstrumentedClasses.add("java/io/FileOutputStream");
		uninstrumentedClasses.add("java/io/ObjectInputStream");
		uninstrumentedClasses.add("java/io/ObjectOutputStream");
		uninstrumentedClasses.add("java/io/ObjectStreamClass");
		uninstrumentedClasses.add("java/io/ObjectStreamField");
		// instrumenting OutputStream will cause a type mismatch error when the player
		// tries to use System.out.println
		uninstrumentedClasses.add("java/io/OutputStream");
		uninstrumentedClasses.add("java/io/RandomAccessFile");
		// Serializable doesn't have any native methods but we don't want to instrument it anyway
		uninstrumentedClasses.add("java/io/Serializable");
		*/
	}

	protected static boolean isInAllowedPackage(String className) {
		int dotIndex = className.lastIndexOf('/');
		if(dotIndex==-1) return false;
		return allowedPackages.contains(className.substring(0,dotIndex));
	}

	private static boolean shouldAddInstrumentedPrefix(String className) {
		if(className.startsWith("battlecode/")) {
			if(className.equals("battlecode/engine/instrumenter/lang/InstrumentableString"))
				return true;
			else
				return false;
		}
		//if(className.startsWith("java/lang/"))
		//	return false;
		if(className.startsWith("instrumented/"))
			return false;
		if(className.startsWith("java/util/")||
		   className.startsWith("java/math/"))
			return true;
		// We get a type mismatch if we instrument PrintStream but not System
		//if(uninstrumentedClasses.contains(className))
		//	return false;
		//if(isInAllowedPackage(className))
		//	return true;
		if(className.startsWith("sun/")||
		   className.startsWith("com/")||
		   className.startsWith("java/"))
			return false;
		//if(className.startsWith("sun/")||
		//   className.startsWith("com/"))
		//	return false;
		return true;
	}
	
	/**
	 * Registers a class reference, and may replace the reference with a reference to a different class.  This method always returns
	 * a class that should be referenced, even if the return value is the same as the given <code>className</code>.  If this class has not been
	 * referenced previously, the next call to flushNewlyReferencedClasses will return an array containing the given class (among others).
	 * 
	 * If cR = classReference(cN,tPN,s,cD), then it should always be the case that
	 * cR == classReference(cR,tPN,s,cD).  If cR starts with instrumented/, then it should
	 * also always be the case that cR == classReference(cR.substring(13),tPN,s,cD).
	 *
	 * @param className the name of the class that was referenced, in fully qualified form (e.g., "team666/navigation/Navigator")
	 * @param teamPackageName the name of the team thaht referenced the given class
	 *
	 * @throws InstrumentationException if the class reference is not allowed
	 *
	 * @return the name of the class that should replace this reference, in fully qualified form
	 */
	public static String classReference(String className, String teamPackageName, boolean silenced, boolean checkDisallowed) {
		String ans = classReferenceX(className, teamPackageName, silenced, checkDisallowed);
		//System.out.println("CR "+className+":"+ans);
		return ans;
	}
   
    public static String classReferenceX(String className, String teamPackageName, boolean silenced, boolean checkDisallowed) {
		if(className == null) return null;
		if(className.charAt(0) == '[') {
			int arrayIndex = className.lastIndexOf('[');
			//System.out.println("what do I do with "+className);
			if(className.charAt(arrayIndex+1) == 'L') {
				return className.substring(0,arrayIndex+2)+classReference(className.substring(arrayIndex+2),teamPackageName,silenced,checkDisallowed);
			}
			else {
				return className;
			}
		}
		else if(className.startsWith(teamPackageName+"/"))
			return className;
		else if(className.equals("java/lang/System"))
			return (silenced ? "battlecode/engine/instrumenter/lang/SilencedSystem" : "battlecode/engine/instrumenter/lang/System");
		else if(className.equals("java/util/concurrent/atomic/AtomicLong"))
			return "battlecode/engine/instrumenter/lang/AtomicLong";
		//else if(className.equals("java/util/Random"))
			//if(checkDisallowed)
		//	return "battlecode/engine/instrumenter/lang/RoboRandom";
		//else
		//		return "instrumented/java/util/Random";
		if(checkDisallowed) {
			if(disallowedClasses.contains(className)||!isInAllowedPackage(className)) {
				return illegalClass(className, teamPackageName);
			}
		}
		if(className.equals("java/security/SecureRandom"))
			return "instrumented/java/util/Random";
		if(shouldAddInstrumentedPrefix(className))
			return "instrumented/"+className;
		else
			return className;
	}

	/**
	 * Registers a class reference (see <code>classReference(...)</code>), but with the class name in a different format (descriptor, instead
	 * of binary form).
	 *
	 * @param classDesc descriptor of the class that was referenced (e.g., "Lteam666/navigation/Navigator;")
	 * @param teamPackageName the name of the team that referenced the given class
	 * 
	 * @throws InstrumentationException if the class reference is not allowed.
	 */

	public static String classDescReference(String classDesc, String teamPackageName, boolean silenced, boolean checkDisallowed) {
		String ans = classDescReferenceX(classDesc, teamPackageName, silenced, checkDisallowed);
		//System.out.println("CDR "+classDesc+":"+ans);
		return ans;
	}

	public static String classDescReferenceX(String classDesc, String teamPackageName, boolean silenced, boolean checkDisallowed) {
		if(classDesc==null)
			return null;
		if(classDesc.charAt(0) == 'L') {
			return "L" + classReference(classDesc.substring(1,classDesc.length()-1), teamPackageName, silenced, checkDisallowed) + ";";
		}
		else if(classDesc.charAt(0) == '[') {
			int arrayIndex = classDesc.lastIndexOf('[');
			return classDesc.substring(0,arrayIndex+1) + classDescReference(classDesc.substring(arrayIndex+1,classDesc.length()), teamPackageName, silenced, checkDisallowed);
		}
		else {
			if(classDesc.length()>1)
				System.out.println("unrecognized CDR "+classDesc);
			return classDesc;
		}
	}
	
	/**
	 * Registers all the class references in a method descriptor, and replaces references as if classReference were called on each individual
	 * reference.
	 * 
	 * @param methodDesc descriptor for the method that was referenced (e.g., "(Ljava/util/Map;Z)Ljava/util/Set;")
	 * @param teamPackageName the name of the team that referenced the given method
	 * 
	 * @throws InstrumentationException if any of the class references contained the the method descriptor are not allowed.
	 */
	public static String methodDescReference(String methodDesc, String teamPackageName, boolean silenced, boolean checkDisallowed) {
		String ret = "(";

		Type[] argTypes = Type.getArgumentTypes(methodDesc);
		for(int i = 0; i < argTypes.length; i++) {
			if(argTypes[i].getSort() == Type.ARRAY || argTypes[i].getSort() == Type.OBJECT)
				// HACK: whitelistSystem is set to true here b/c we're only replacing Object; once the whole library is replaced, this should be changed
				ret = ret + classDescReference(argTypes[i].toString(), teamPackageName, silenced, checkDisallowed);
			else
				ret = ret + argTypes[i].toString();
		}
		
		ret = ret + ")";

		Type returnType = Type.getReturnType(methodDesc);
		if(returnType.getSort() == Type.ARRAY || returnType.getSort() == Type.OBJECT)
			// HACK: whitelistSystem is set to true here b/c we're only replacing Object; once the whole library is replaced, this should be changed
			ret = ret + classDescReference(returnType.toString(), teamPackageName, silenced, checkDisallowed);
		else
			ret = ret + returnType.toString();
		
		//System.out.println("mdr "+ret);
		return ret;
	}
	
	public static String methodSignatureReference(String signature, String teamPackageName, boolean silenced, boolean checkDisallowed) {
		if(signature==null) return null;
		//System.out.println("meth "+signature);
		BattlecodeSignatureWriter writer = new BattlecodeSignatureWriter(teamPackageName, silenced, checkDisallowed);
		SignatureReader reader = new SignatureReader(signature);
		reader.accept(writer);
		//System.out.println("meth "+writer.toString());
		return writer.toString();
	}

	public static String fieldSignatureReference(String signature, String teamPackageName, boolean silenced, boolean checkDisallowed) {
		if(signature==null) return null;
		//System.out.println("field "+signature);
		BattlecodeSignatureWriter writer = new BattlecodeSignatureWriter(teamPackageName, silenced, checkDisallowed);
		SignatureReader reader = new SignatureReader(signature);
		reader.acceptType(writer);
		//System.out.println("field "+writer.toString());
		return writer.toString();
	}
	
	// called whenever an illegal class is found; throws an InstrumentationException
	private static String illegalClass(String className, String teamPackageName) {
		ErrorReporter.report("Illegal class: " + className + "\nThis class cannot be referenced by player " + teamPackageName, false);
		InstrumentationException e = new InstrumentationException();
		e.printStackTrace();
		throw e;
	}


}
