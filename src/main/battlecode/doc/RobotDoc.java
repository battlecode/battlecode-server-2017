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
            append(builder, "Max Energon", String.format("%1.0f", rt.maxEnergon));
            if (rt.spawnCost > 0)
                append(builder, "Spawn Cost", String.format("%1.0f", rt.spawnCost));
            if (rt.moveDelayOrthogonal > 0) {
                append(builder, "Move Delay Orthogonal", String.format("%d", rt.moveDelayOrthogonal));
                append(builder, "Move Delay Diagonal", String.format("%d", rt.moveDelayDiagonal));
            }
            if (rt.sensorAngle > 0) {
                append(builder, "Sensor Radius Squared", String.format("%d", rt.sensorRadiusSquared));
                append(builder, "Sensor Angle", String.format("%1.0f", rt.sensorAngle));
            }
            if (rt.canAttackAir || rt.canAttackGround) {
                append(builder, "Min Attack Radius Squared", String.format("%d", rt.attackRadiusMinSquared));
                append(builder, "Max Attack Radius Squared", String.format("%d", rt.attackRadiusMaxSquared));
                append(builder, "Attack Power", String.format("%1.1f", rt.attackPower));
                append(builder, "Attack Delay", String.format("%d", rt.attackDelay));
                String attacks;
                if (rt.canAttackAir && rt.canAttackGround)
                    attacks = "Air, Ground";
                else if (rt.canAttackAir)
                    attacks = "Air";
                else
                    attacks = "Ground";
                append(builder, "Attacks", attacks);
            }
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
