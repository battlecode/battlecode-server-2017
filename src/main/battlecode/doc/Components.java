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
	public static void register(Map map) { map.put("components", new Components()); }

	public String getName() { return "components"; }

	public boolean inConstructor() { return false; }

	public boolean inField() { return true; }

	public boolean inMethod() { return false; }

	public boolean inOverview() { return false; }

	public boolean inPackage() { return false; }

	public boolean inType() { return false; }

	public boolean isInlineTag() { return false; }

	public String toString(Tag tag) {
		throw new IllegalArgumentException("The methodcost tag may not be used inline.");
	}
	
	static List<ComponentType> builders = new ArrayList<ComponentType>();

	static {
		for(ComponentType t : ComponentType.values()) {
			if(t.componentClass==ComponentClass.BUILDER)
				builders.add(t);
		}
	}


	String [] components;
	int n;

	public String toString(String comp) {
		ComponentType ct = ComponentType.valueOf(comp);
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
		return builder.toString();

	}

	public String toString(Tag [] tags) {
		if(components==null) components = System.getProperty("battlecode.doc.components").split("\n");
		String comp;
		if(n<components.length)
			comp = components[n++];
		else
			return null;
		if("\0".equals(comp))
			return "\0";
		else {
			return toString(comp);
		}
	}
}
