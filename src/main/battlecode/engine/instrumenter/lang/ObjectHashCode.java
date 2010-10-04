package battlecode.engine.instrumenter.lang;

import battlecode.engine.ErrorReporter;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

public class ObjectHashCode {

	static final Method objectHashCode;
	static final Method enumHashCode;
	static final Method characterHashCode;

	static {
		Method tmpo=null, tmpe=null, tmpc=null;
		try {
			tmpo = Object.class.getMethod("hashCode");
			tmpe = Enum.class.getMethod("hashCode");
			tmpc = Character.class.getMethod("hashCode");
		} catch(Exception e) {
			ErrorReporter.report(e);
		}
		objectHashCode = tmpo;
		enumHashCode = tmpe;
		characterHashCode = tmpc;
	}

	static int lastHashCode=-1;

	static WeakHashMap<Object,Integer> codes = new WeakHashMap<Object,Integer>();

	static public Integer hashCode(Object o, Class<?> cl) {
		try {
			Method hashCodeMethod=cl.getMethod("hashCode");
			if(hashCodeMethod.equals(enumHashCode)) {
				return ((Enum)o).ordinal();
			}
			if(hashCodeMethod.equals(objectHashCode)||
			   hashCodeMethod.equals(characterHashCode)) {
				Integer code = codes.get(o);
				if(code==null) {
					code=++lastHashCode;
					codes.put(o,code);
				}
				return code;
			}
		} catch(Exception e) {
			ErrorReporter.report(e);
		}
		return null;
	}

	static public int identityHashCode(Object o) {
		Integer code = codes.get(o);
		if(code==null) {
			codes.put(o,++lastHashCode);
			return lastHashCode;
		}
		else
			return code.intValue();
	}

	private ObjectHashCode() {}

}