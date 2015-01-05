package battlecode.doc;

import battlecode.common.RobotType;
import com.sun.javadoc.*;
import com.sun.tools.doclets.Taglet;

import java.util.Map;

public class RobotDoc implements Taglet {
    public static void register(Map<String, Taglet> map) {
        map.put("robot", new RobotDoc());
    }

    public String getName() {
        return "robot";
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
        RobotType rt;
        try {
            rt = RobotType.valueOf(comp);
        } catch (IllegalArgumentException e) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        try {
            if (rt == RobotType.HQ) {
                    append(builder, "Type", "HQ");
            } else if (rt == RobotType.TOWER) {
                    append(builder, "Type", "Tower");
            } else if (rt.isBuilding) {
                    append(builder, "Type", "Structures"); 
            } else {
                    append(builder, "Type", "Unit");
            }

            if (rt.spawnSource != null) {
                append(builder, "Spawned By", rt.spawnSource.toString());
            }
            if (rt.dependency != null) {
                append(builder, "Dependencies", rt.dependency.toString());
            }
            if (rt.oreCost > 0) {
                append(builder, "Ore Cost", rt.oreCost + "");
            }
            if (rt.buildTurns > 0) {
                append(builder, "Build Turns", rt.buildTurns + "");
            }
            append(builder, "Supply Upkeep", rt.supplyUpkeep + "");
            append(builder, "Max Health", String.format("%1.0f", rt.maxHealth));
            append(builder, "Attack Power", String.format("%1.1f", rt.attackPower));
            append(builder, "Attack Radius Squared", rt.attackRadiusSquared + "");
            append(builder, "Movement Delay", rt.movementDelay + "");
            append(builder, "Attack Delay", rt.attackDelay + "");
            append(builder, "Loading Delay", rt.loadingDelay + "");
            append(builder, "Cooldown Delay", rt.cooldownDelay + "");
            append(builder, "Sensor Radius Squared", rt.sensorRadiusSquared + "");
            append(builder, "Bytecode Limit", rt.bytecodeLimit + "");
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
        if ("RobotType".equals(member[0]))
            return toString(member[1]);
        else
            return null;
    }
}
