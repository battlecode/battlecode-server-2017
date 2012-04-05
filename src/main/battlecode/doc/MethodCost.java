package battlecode.doc;

import battlecode.engine.instrumenter.MethodCostUtil;
import com.sun.javadoc.*;
import com.sun.tools.doclets.Taglet;

import java.util.Map;

public class MethodCost implements Taglet {

    public static void register(Map<String, Taglet> map) {
        map.put("methodcost", new MethodCost());
    }

    public String getName() {
        return "methodcost";
    }

    public boolean inConstructor() {
        return false;
    }

    public boolean inField() {
        return false;
    }

    public boolean inMethod() {
        return true;
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
        throw new IllegalArgumentException("The methodcost tag may not be used inline.");
    }

    String[] methods;
    int n;

    public String toString(Tag[] tags) {
        System.getProperty("battlecode.doc.methods");
        if (methods == null) methods = System.getProperty("battlecode.doc.methods").split("\n");
        MethodCostUtil.MethodData data = MethodCostUtil.getMethodDataRaw(methods[n++]);
        if (data == null || data.cost == 0) return null;
        StringBuilder builder = new StringBuilder();
        builder.append("<dt><strong>Bytecode cost:</strong></dt><dd>");
        builder.append(data.cost);
        builder.append("</dd>");
        return builder.toString();
    }
}
