package battlecode.engine.signal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * A signal handler that delegates to other signal handling methods, which it discovers
 * automatically.  The following criteria are used for determining if a method should handle signals:
 * <p/>
 * - If the method has a {@link DiscoverSignal} annotation, that is respected.
 * - Otherwise, a method is discovered if it is public, its name starts with "visit", and it takes a single
 * parameter that is a subclass of {@link Signal} (but not Signal itself).
 * Currently it uses reflection, which is kind of inelegant and slow (but probably not slow enough to matter).  I guess we could avoid reflection and instead create classes on the fly.
 */
public class AutoSignalHandler implements SignalHandler {

    static HashMap<Class, HashMap<Class, Method>> metaMap = new HashMap<Class, HashMap<Class, Method>>();
    HashMap<Class, Method> methodMap;
    Object myObject;

    public AutoSignalHandler() {
        myObject = this;
        discoverMethods(this.getClass());
    }

    public AutoSignalHandler(Object o) {
        myObject = o;
        discoverMethods(o.getClass());
    }

    protected void discoverMethods(Class cls) {
        synchronized (metaMap) {
            methodMap = metaMap.get(cls);
            if (methodMap != null) return;
            assert Modifier.isPublic(cls.getModifiers());
            methodMap = new HashMap<Class, Method>();
            for (Method method : cls.getMethods()) {
                boolean shouldAdd;
                Class<?>[] parameters = method.getParameterTypes();
                DiscoverSignal annotation = method.getAnnotation(DiscoverSignal.class);
                if (annotation != null)
                    shouldAdd = annotation.value();
                else
                    shouldAdd = method.getName().startsWith("visit") &&
                            parameters.length == 1 &&
                            Signal.class.isAssignableFrom(parameters[0]) &&
                            !parameters[0].equals(Signal.class);
                if (shouldAdd) {
                    //System.out.println("Adding signal handler "+method);
                    Method old = methodMap.put(parameters[0], method);
                    assert old == null;
                }
            }
            metaMap.put(cls, methodMap);
        }
    }

    public void handleException(Throwable e) {
        if (e instanceof RuntimeException)
            throw (RuntimeException) e;
        else if (e instanceof Error)
            throw (Error) e;
        else
            throw new RuntimeException("Exception in signal handler", e);
    }

    @SuppressWarnings("unchecked")
    public void visitSignal(Signal signal) {
        Class<?> cls = signal.getClass();
        do {
            Method method = methodMap.get(cls);
            if (method != null)
                try {
                    method.invoke(myObject, signal);
                } catch (Exception e) {
                    if (e instanceof InvocationTargetException)
                        handleException(e.getCause());
                    else
                        handleException(e);
                }
            cls = cls.getSuperclass();
        }
        while (Signal.class.isAssignableFrom(cls));
    }

}
