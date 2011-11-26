package battlecode.engine.instrumenter.lang;

import battlecode.engine.instrumenter.ClassReferenceUtil;

public class Reflect {

	public static Class classForName(String s) throws ClassNotFoundException {
		//System.out.println("CFN "+s);
		return classForName(s,true,Reflect.class.getClassLoader());
	}
	
	public static Class classForName(String s, boolean b, ClassLoader l) throws ClassNotFoundException {
		//System.out.println(Reflect.class.getClassLoader());
		//System.out.println("CFNL "+s+" ");
		//throw new ClassNotFoundException();
		try {
			return Class.forName(ClassReferenceUtil.classReference(s.replace('.','/'),"",false,false).replace('/','.'));
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
