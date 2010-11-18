package battlecode.engine.instrumenter;

import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import battlecode.engine.ErrorReporter;

import org.objectweb.asm.*;
import static org.objectweb.asm.ClassReader.*;

/**
 * MethodCostUtil is a singleton used for looking up MethodData associated with some methods.
 * <p>
 *
 * @author adamd
 */
public class MethodCostUtil {
	
	private MethodCostUtil() {}

	/** This is a map from method names (in the format 'ClassName/methodName'), to the MethodData associated with each method. */
	private final static Map<String, MethodData> methodCosts;
		
	/** This is a map from binary class names, to all the classes/interfaces that the class transitively implements/extends. */
	private final static Map<String, String[]> interfacesMap;

	/**
	 * A struct that stores data about a method -- what its lookup bytecode cost is, and whether it should end the basic block or not.
	 */
	public static class MethodData {
		public final int cost;
		public final boolean shouldEndRound;
			
		public MethodData(int cost, boolean shouldEndRound) {
			this.cost = cost;
			this.shouldEndRound = shouldEndRound;
		}
	}
	
	// static initializer loads the MethodCosts file from idata, and stores it in the methodCosts field
	// also initializes interfacesMap
	static {
		BufferedReader reader;
		String line;
		ZipFile zfile;
		
		// load our zip file
		try{
			zfile = new ZipFile("idata");
		} catch(Exception e) {
			ErrorReporter.report(e, "Check that the 'idata' file is in its proper place");
			throw new InstrumentationException();
		}
					
		// load method costs
		try{
			methodCosts = new HashMap<String, MethodData>();
			reader = new BufferedReader(new InputStreamReader(zfile.getInputStream(zfile.getEntry("MethodCosts.txt"))));
			while((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				if(st.countTokens() != 3)
					throw new RuntimeException("Malformed MethodCosts.txt file");
				methodCosts.put(st.nextToken(), new MethodData(Integer.parseInt(st.nextToken()), Boolean.parseBoolean(st.nextToken())));
			}
		} catch (Exception e) {
			ErrorReporter.report("Error loading idata", "Check that the 'idata' file is not corrupted");
			throw new InstrumentationException();
		}
		
		interfacesMap = new HashMap<String, String[]>();
	}

	public static MethodData getMethodDataRaw(String fullName) {
		return methodCosts.get(fullName);
	}
	
	/**
	 * Returns the MethodData associated with the given method, or null if no MethodData exists for the given method.
	 * @param className the binary name of the class to which the given method belongns
	 * @param methodName the name of the given class
	 */
	public static MethodData getMethodData(String className, String methodName) {
		if(className.charAt(0) == '[')
			return null;
		String key = className + "/" + methodName;

		if(methodCosts.containsKey(key))
			return methodCosts.get(key);
		
		String[] interfaces = null;
		if(interfacesMap.containsKey(className))
			interfaces = interfacesMap.get(className);
		else {
			interfaces = new InterfaceReader(className).getInterfaces();
			interfacesMap.put(className, interfaces);
		}
		
		for(int i = 0; i < interfaces.length; i++) {
			key = interfaces[i] + "/" + methodName;
			if(methodCosts.containsKey(key))
				return methodCosts.get(key);
		}
				
		return null;
	}



}
