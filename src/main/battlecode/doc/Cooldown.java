package battlecode.doc;

import com.sun.javadoc.*;
import com.sun.tools.doclets.Taglet;

import java.util.Map;

public class Cooldown implements Taglet {
	
	public static void register(Map map) { map.put("cooldown", new Cooldown()); }

	public String getName() { return "cooldown"; }

	public boolean inConstructor() { return false; }

	public boolean inField() { return false; }

	public boolean inMethod() { return true; }

	public boolean inOverview() { return false; }

	public boolean inPackage() { return false; }

	public boolean inType() { return false; }

	public boolean isInlineTag() { return false; }

	public String toString(Tag tag) {
		String text;
		if(tag.text().trim().isEmpty())
			text = "<code>type().delay</code>";
		else
			text = tag.text();
		return String.format("\n<dt><strong>Cooldown:</strong></dt><dd>%s</dd>",text);
	}

	public String toString(Tag [] tags) {
		if(tags.length!=1)
			throw new IllegalArgumentException("Only one cooldown tag is allowed per method.");
		return toString(tags[0]);
	}

}
