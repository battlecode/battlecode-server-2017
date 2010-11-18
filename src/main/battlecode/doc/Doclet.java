package battlecode.doc;

import com.sun.javadoc.*;
import com.sun.tools.doclets.standard.Standard;

public class Doclet extends Standard {

	public static boolean start(RootDoc root) {
		// Due to various issues with the javadoc api we have to
		// use some bad hacks to tell the bytecode cost taglet the
		// method names.
		StringBuilder builder = new StringBuilder();
		for(ClassDoc cl : root.classes()) {
			String clname = cl.qualifiedName().replace(".","/");
			for(MethodDoc m : cl.methods()) {
				builder.append(clname);
				builder.append("/");
				builder.append(m.name());
				builder.append("\n");
			}
		}
		System.setProperty("battlecode.doc.methods",builder.toString());
		return Standard.start(root);
	}

}
