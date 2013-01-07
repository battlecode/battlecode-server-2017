package battlecode.doc;

import battlecode.common.Upgrade;
import com.sun.javadoc.*;
import com.sun.tools.doclets.Taglet;

import java.util.Map;

public class UpgradeDoc implements Taglet {
    public static void register(Map<String, Taglet> map) {
        map.put("upgrade", new UpgradeDoc());
    }

    public String getName() {
        return "upgrade";
    }

    public boolean inConstructor() {
        return false;
    }

    public boolean inField() {
        return true;
    }

    public boolean inMethod() {
        return false;
    }

    public boolean inOverview() {
        return false;
    }

    public boolean inPackage() {
        return false;
    }

    public boolean inType() {
        return false;
    }

    public boolean isInlineTag() {
        return false;
    }

    public String toString(Tag tag) {
        throw new IllegalArgumentException("The component tag may not be used inline.");
    }

    String[] members;
    int n;

    static public void append(StringBuilder builder, String label, String value) {
        builder.append(String.format("<strong>%s:</strong> %s<br />", label, value));
    }

    public String toString(String comp) {
        Upgrade ug;
        try {
            ug = Upgrade.valueOf(comp);
        } catch (IllegalArgumentException e) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        try {
						append(builder, "Rounds", String.format("%d", ug.numRounds));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public String toString(Tag[] tags) {
        if (members == null) members = System.getProperty("battlecode.doc.members").split("\n");
        String[] member;
        if (n < members.length)
            member = members[n++].split("\\.");
        else
            return null;
        if ("Upgrade".equals(member[0]))
            return toString(member[1]);
        else
            return null;
    }
}
