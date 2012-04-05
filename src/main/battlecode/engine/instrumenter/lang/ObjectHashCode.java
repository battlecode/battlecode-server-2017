package battlecode.engine.instrumenter.lang;

import battlecode.engine.ErrorReporter;
import org.hibernate.search.util.WeakIdentityHashMap;

import java.lang.reflect.Method;
import java.util.HashMap;

public class ObjectHashCode {

    static final Method objectHashCode;
    static final Method enumHashCode;
    static final Method characterHashCode;

    static {
        Method tmpo = null, tmpe = null, tmpc = null;
        try {
            tmpo = Object.class.getMethod("hashCode");
            tmpe = Enum.class.getMethod("hashCode");
            tmpc = Character.class.getMethod("hashCode");
        } catch (Exception e) {
            ErrorReporter.report(e);
        }
        objectHashCode = tmpo;
        enumHashCode = tmpe;
        characterHashCode = tmpc;
    }

    static int lastHashCode = -1;

    static WeakIdentityHashMap<Object, Integer> codes = new WeakIdentityHashMap<Object, Integer>();
    // reflection is slow so cache the results
    static HashMap<Class, Boolean> usesOHC = new HashMap<Class, Boolean>();

    static public int hashCode(int hash, Object o, Class<?> cl) throws NoSuchMethodException {
        int idHash = java.lang.System.identityHashCode(o);
        if (hash != idHash)
            return hash;
        if (usesObjectHashCode(cl))
            return identityHashCode(o);
        else
            return hash;
    }

    // Assumes that if hashCode and System.identityHashCode are
    // the same then hashCode has been reimplemented.  Chance
    // of a collision is 1 in 2^32.
    static public int fastHashCode(int hash, Object o, Class<?> cl) {
        int idHash = java.lang.System.identityHashCode(o);
        if (hash != idHash)
            return hash;
        return identityHashCode(o);
    }

    static private boolean usesObjectHashCode(Class<?> cl) throws NoSuchMethodException {
        Boolean b = usesOHC.get(cl);
        if (b == null) {
            Method hashCodeMethod = cl.getMethod("hashCode");
            b = hashCodeMethod.equals(enumHashCode) ||
                    hashCodeMethod.equals(objectHashCode) ||
                    hashCodeMethod.equals(characterHashCode);
            usesOHC.put(cl, b);
        }
        return b;
    }

    static public int identityHashCode(Object o) {
        Integer code = codes.get(o);
        if (code == null) {
            codes.put(o, ++lastHashCode);
            return lastHashCode;
        } else
            return code;
    }

    private ObjectHashCode() {
    }

}
