package battlecode.doc;

import com.sun.javadoc.*;
import com.sun.tools.doclets.Taglet;

import battlecode.engine.instrumenter.MethodCostUtil;

import java.util.Map;

public class MethodCost implements Taglet {

	public static void register(Map map) { map.put("methodcost", new MethodCost()); }
	
	public String getName() { return "methodcost"; }

	public boolean inConstructor() { return false; }

	public boolean inField() { return false; }

	public boolean inMethod() { return true; }

	public boolean inOverview() { return false; }

	public boolean inPackage() { return false; }

	public boolean inType() { return false; }

	public boolean isInlineTag() { return false; }

	protected static MethodDoc lastMethod;
	
	public String toString(Tag tag) {
		throw new IllegalArgumentException("The methodcost tag may not be used inline.");
	}

	public String toString(Tag [] tags) {
		if(tags.length==0) return "";
		MethodDoc doc = (MethodDoc)tags[0].holder();
		lastMethod = null;
		String name = doc.name();
		String cls = doc.containingClass().qualifiedName().replace(".","/");
		MethodCostUtil.MethodData data = MethodCostUtil.getMethodDataNoAsm(cls,name);
		if(data==null||data.cost==0) return "";
		StringBuilder builder = new StringBuilder();
		builder.append("<dt><strong>Bytecode cost:</strong></dt><dd>");
		builder.append(data.cost);
		builder.append("</dd>");
		return builder.toString();
	}
}
