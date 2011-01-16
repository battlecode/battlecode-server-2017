package battlecode.doc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import battlecode.common.ComponentClass;
import battlecode.common.ComponentType;

import battlecode.world.Builder;

import com.sun.javadoc.*;
import com.sun.tools.doclets.Taglet;

public class Components implements Taglet {
	public static void register(Map<String, Taglet> map) { map.put("components", new Components()); }

	public String getName() { return "components"; }

	public boolean inConstructor() { return false; }

	public boolean inField() { return true; }

	public boolean inMethod() { return false; }

	public boolean inOverview() { return false; }

	public boolean inPackage() { return false; }

	public boolean inType() { return false; }

	public boolean isInlineTag() { return false; }

	public String toString(Tag tag) {
		throw new IllegalArgumentException("The component tag may not be used inline.");
	}
	
	static List<ComponentType> builders = new ArrayList<ComponentType>();

	static {
		for(ComponentType t : ComponentType.values()) {
			if(t.componentClass==ComponentClass.BUILDER)
				builders.add(t);
		}
	}


	String [] members;
	int n;

	public String toString(String comp) {
		ComponentType ct;
		try {
			ct = ComponentType.valueOf(comp);
		} catch(IllegalArgumentException e) { return null; }
		StringBuilder builder = new StringBuilder();
		builder.append("<dt><strong>Controller interface:</strong></dt><dd>");
		builder.append("<a href=\"../../battlecode/common/");
		builder.append(ct.controller.getSimpleName());
		builder.append(".html\">");
		builder.append(ct.controller.getSimpleName());
		builder.append("</a></dd>");
		builder.append("<dt><strong>Built by:</strong></dt>");
		for(ComponentType b : builders) {
			if(Builder.canBuild(b,ct)) {
				builder.append("<dd><a href=\"../../battlecode/common/ComponentType.html#");
				builder.append(b.name());
				builder.append("\">");
				builder.append(b.name());
				builder.append("</a></dd>");
			}
		}
		builder.append(String.format("<dt><strong>Weight:</strong> %d <strong>Cost:</strong> %d <strong>Delay:</strong> %d <strong>Range (r^2):</strong> %d <strong>Angle:</strong> %1.0f",ct.weight, ct.cost, ct.delay, ct.range, ct.angle));
		if(ct.attackPower!=0) {
			builder.append(" <strong>Attack power:</strong> "+ct.attackPower);
		}
		builder.append("</dt>");
		return builder.toString();

	}

	public String toString(Tag [] tags) {
		if(members==null) members = System.getProperty("battlecode.doc.members").split("\n");
		String [] member;
		if(n<members.length)
			member = members[n++].split("\\.");
		else
			return null;
		if("ComponentType".equals(member[0]))
			return toString(member[1]);
		else
			return null;
	}
}
