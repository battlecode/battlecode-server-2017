package battlecode.instrumenter;

import battlecode.instrumenter.inject.RobotMonitor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for IndividualClassLoader.
 *
 * @author james
 */
public class IndividualClassLoaderTest {
    private IndividualClassLoader l1;
    private IndividualClassLoader l2;

    @Before
    public void resetIndividualClassLoader() throws Exception {
        IndividualClassLoader.reset();

        l1 = new IndividualClassLoader("instrumentertest");
        l2 = new IndividualClassLoader("instrumentertest");

        // Set up noop RobotMonitors.

        SandboxedRobotPlayer.Pauser pauser = () -> {};
        SandboxedRobotPlayer.Killer killer = () -> {};

        final Class<?> monitor1 = l1.loadClass(RobotMonitor.class.getName());
        monitor1.getMethod("init",
                SandboxedRobotPlayer.Pauser.class,
                SandboxedRobotPlayer.Killer.class,
                int.class)
                .invoke(null, pauser, killer, 0);
        monitor1.getMethod("setBytecodeLimit", int.class)
                .invoke(null, Integer.MAX_VALUE);

        final Class<?> monitor2 = l2.loadClass(RobotMonitor.class.getName());
        monitor2.getMethod("init",
                SandboxedRobotPlayer.Pauser.class,
                SandboxedRobotPlayer.Killer.class,
                int.class)
                .invoke(null, pauser, killer, 0);
        monitor2.getMethod("setBytecodeLimit", int.class)
                .invoke(null, Integer.MAX_VALUE);

    }

    // Should always give the same result for loadClass(string)
    @Test
    public void testLoadsClassesRepeatedly() throws ClassNotFoundException {
        final List<String> classNames = new ArrayList<>();

        classNames.add("java.lang.Object");

        classNames.add("instrumentertest.Outer");
        classNames.add("instrumentertest.Outer$Inner");

        classNames.addAll(IndividualClassLoader.alwaysRedefine);

        final List<Class<?>> loadedClasses = new ArrayList<>();

        // Reloading consecutively should work
        for (String className : classNames) {
            final Class<?> loadedClass = l1.loadClass(className);
            final Class<?> reLoadedClass = l1.loadClass(className);

            assertEquals(loadedClass, reLoadedClass);

            loadedClasses.add(loadedClass);
        }

        // Reloading in a different order should work
        for (int i = 0; i < classNames.size(); i++) {
            assertEquals(l1.loadClass(classNames.get(i)), loadedClasses.get(i));
        }
    }

    // Should reload player classes between instances.
    @Test
    public void testReloadsPlayerClasses() throws ClassNotFoundException {
        assertNotEquals(
                l1.loadClass("instrumentertest.Outer"),
                l2.loadClass("instrumentertest.Outer")
        );
    }

    // Should reload always-reloadable classes between instances.
    @Test
    public void testReloadsAlwaysReloadClasses() throws ClassNotFoundException {
        for (String alwaysRedefine : IndividualClassLoader.alwaysRedefine) {
            assertNotEquals(
                l1.loadClass(alwaysRedefine),
                l2.loadClass(alwaysRedefine)
            );
        }
    }

    // Classes that don't need to be reloaded. Should be from AllowedPackages.txt.
    private static final Class<?>[] NEVER_RELOAD = new Class<?>[] {
            java.lang.Object.class,
            battlecode.common.Direction.class,
            java.math.BigInteger.class,
            java.util.Map.class,
            java.util.regex.Matcher.class,
            java.io.InputStream.class
    };

    // Should give already-loaded system classes for most things.
    @Test
    public void testNoUnnecessaryReloads() throws ClassNotFoundException {
        final IndividualClassLoader l
                = new IndividualClassLoader("instrumentertest");

        for (Class<?> theClass : NEVER_RELOAD) {
            assertEquals(theClass, l.loadClass(theClass.getName()));
        }
    }

    // If a player class overrides hashCode, hashCode should work normally.
    // If a player class *doesn't* override hashCode, we should replace calls to it
    // with a deterministic hash code function.
    @Test
    public void testHashCodeInstrumentation() throws Exception {
        final Class<?> overridesClass = l1.loadClass("instrumentertest.OverridesHashCode");
        final Method getHashCodeOverrides = overridesClass.getMethod("getHashCode");

        final Object overrides = overridesClass.newInstance();

        assertEquals(57, getHashCodeOverrides.invoke(overrides));
        assertEquals(57, getHashCodeOverrides.invoke(overrides));


        final Class<?> notOverridesClass1 = l1.loadClass("instrumentertest.DoesntOverrideHashCode");
        final Method getHashCodeNotOverrides1 = notOverridesClass1.getMethod("getHashCode");
        final Object notOverrides1a = notOverridesClass1.newInstance();
        final Object notOverrides1b = notOverridesClass1.newInstance();

        assertEquals(getHashCodeNotOverrides1.invoke(notOverrides1a),
                getHashCodeNotOverrides1.invoke(notOverrides1a));
        assertEquals(getHashCodeNotOverrides1.invoke(notOverrides1b),
                getHashCodeNotOverrides1.invoke(notOverrides1b));

        final Class<?> notOverridesClass2 = l2.loadClass("instrumentertest.DoesntOverrideHashCode");
        final Method getHashCodeNotOverrides2 = notOverridesClass2.getMethod("getHashCode");
        final Object notOverrides2a = notOverridesClass2.newInstance();
        final Object notOverrides2b = notOverridesClass2.newInstance();

        assertEquals(getHashCodeNotOverrides2.invoke(notOverrides2a),
                getHashCodeNotOverrides2.invoke(notOverrides2a));
        assertEquals(getHashCodeNotOverrides2.invoke(notOverrides2b),
                getHashCodeNotOverrides2.invoke(notOverrides2b));

        // hashCode should be deterministic across loaders (assuming it is called
        // in the same order.)
        assertEquals(getHashCodeNotOverrides1.invoke(notOverrides1a),
                getHashCodeNotOverrides2.invoke(notOverrides2a));
        assertEquals(getHashCodeNotOverrides1.invoke(notOverrides1b),
                getHashCodeNotOverrides2.invoke(notOverrides2b));
    }
}
