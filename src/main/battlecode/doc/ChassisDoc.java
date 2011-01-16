package battlecode.doc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import battlecode.common.Chassis;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentType;

import battlecode.world.Builder;

import com.sun.javadoc.*;
import com.sun.tools.doclets.Taglet;

public class ChassisDoc implements Taglet {
	public static void register(Map<String, Taglet> map) { map.put("chassis", new ChassisDoc()); }

	public String getName() { return "chassis"; }

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
		Chassis ct;
		try {
			ct = Chassis.valueOf(comp);
		} catch(IllegalArgumentException e) { return null; }
		StringBuilder builder = new StringBuilder();
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
		try {
		builder.append(String.format("<dt><strong>Capacity:</strong> %d <strong>Cost:</strong> %d <strong>Max HP:</strong> %1.0f <strong>Upkeep:</strong> %1.2f <strong>Move delay orthogonal/diagonal:</strong> %d/%d",ct.weight, ct.cost, ct.maxHp, ct.upkeep, ct.moveDelayOrthogonal, ct.moveDelayDiagonal));
		builder.append("</dt>");
		} catch(Exception e) { e.printStackTrace(); }
		return builder.toString();

	}

	public String toString(Tag [] tags) {
		if(members==null) members = System.getProperty("battlecode.doc.members").split("\n");
		String [] member;
		if(n<members.length)
			member = members[n++].split("\\.");
		else
			return null;
		if("Chassis".equals(member[0]))
			return toString(member[1]);
		else
			return null;
	}
}
