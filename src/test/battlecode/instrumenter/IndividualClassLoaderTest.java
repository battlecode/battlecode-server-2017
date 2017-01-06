package battlecode.instrumenter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for IndividualClassLoader.
 *
 * @author james
 */
public class IndividualClassLoaderTest {
    private static URL extFolderURL;
    private IndividualClassLoader.Cache sharedCache;
    private IndividualClassLoader l1;
    private IndividualClassLoader l2;

    @BeforeClass
    public static void writeCache() throws Exception {
        extFolderURL = URLUtils.toTempFolder(
            "instrumentertest/CallsIllegalMethods.class",
            "instrumentertest/CallsMathRandom.class",
            "instrumentertest/DoesntOverrideHashCode.class",
            "instrumentertest/IllegalMethodReference.class",
            "instrumentertest/LegalMethodReference.class",
            "instrumentertest/Nothing.class",
            "instrumentertest/Outer.class",
            "instrumentertest/Outer$Inner.class",
            "instrumentertest/OverridesHashCode.class",
            "instrumentertest/Reflection.class",
            "instrumentertest/StringFormat.class",
            "instrumentertest/UsesEnumMap.class",
            "instrumentertest/UsesLambda.class",
            "java/lang/Double.class"
        );
    }

    public IndividualClassLoader setupLoader(String packageName,
                                             IndividualClassLoader.Cache cache) throws Exception {
        IndividualClassLoader result = new IndividualClassLoader(packageName, cache);

        // Set up noop RobotMonitors.
        // Necessary for... reasons.

        SandboxedRobotPlayer.Pauser pauser = () -> {};
        SandboxedRobotPlayer.Killer killer = () -> {};

        final Class<?> monitor1 = result
                .loadClass("battlecode.instrumenter.inject.RobotMonitor");
        monitor1.getMethod("init",
                SandboxedRobotPlayer.Pauser.class,
                SandboxedRobotPlayer.Killer.class,
                int.class)
                .invoke(null, pauser, killer, 0);
        monitor1.getMethod("setBytecodeLimit", int.class)
                .invoke(null, Integer.MAX_VALUE);

        return result;
    }


    @Before
    public void setupDefaultCache() throws Exception {
        sharedCache = new IndividualClassLoader.Cache(extFolderURL);
        l1 = setupLoader("instrumentertest", sharedCache);
        l2 = setupLoader("instrumentertest", sharedCache);
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
        for (Class<?> theClass : NEVER_RELOAD) {
            assertEquals(theClass, l1.loadClass(theClass.getName()));
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

    @Test
    public void testIllegalMethodsFail() throws Exception {
        final String[] classNames = new String[] {
                "instrumentertest.CallsIllegalMethods$CallsWait",
                "instrumentertest.CallsIllegalMethods$CallsClassForName",
                "instrumentertest.CallsIllegalMethods$CallsStringIntern",
                "instrumentertest.CallsIllegalMethods$CallsSystemNanoTime",
                "instrumentertest.CallsIllegalMethods$CreatesFilePrintStream",
        };

        for (String className : classNames) {
            try {
                l1.loadClass(className);
            } catch (InstrumentationException e) {
                // Reset teamsWithErrors.
                continue;
            }

            fail("Didn't outlaw illegal class: "+className);
        }
    }

    @Test
    public void testLambdas() throws Exception {
        final Class<?> c = l1.loadClass("instrumentertest.UsesLambda");

        c.getMethod("run").invoke(null);
    }

    @Test
    public void testStringFormat() throws Exception {
        final Class<?> c = l1.loadClass("instrumentertest.StringFormat");

        c.getMethod("run").invoke(null);
    }

    @Test(expected=InstrumentationException.class)
    public void testCantReflect() throws Exception {
        l1.loadClass("instrumentertest.Reflection");
    }

    @Test(expected=InstrumentationException.class)
    public void testCantReferenceIllegalMethod() throws Exception {
        l1.loadClass("instrumentertest.IllegalMethodReference");
    }

    @Test
    public void testCanUseLambda() throws Exception {
        l1.loadClass("instrumentertest.LegalMethodReference");
    }

    @Test
    public void testMathRandom() throws Exception {
        l1.loadClass("instrumentertest.CallsMathRandom");
    }

    @Test
    public void testCanUseEnumMap() throws Exception {
        l1.loadClass("instrumentertest.UsesEnumMap");
    }

    @Test
    public void testLoadFromJar() throws Exception {
        URL jar = URLUtils.toTempJar("instrumentertest/Nothing.class");
        IndividualClassLoader loader = setupLoader(
                "instrumentertest",
                new IndividualClassLoader.Cache(jar)
        );

        Class<?> jarClass = loader.loadClass("instrumentertest.Nothing");

        URL jarClassLocation = jarClass.getResource("Nothing.class");

        // EXTREMELY scientific

        assertTrue(jarClassLocation.toString().startsWith("jar:"));
        assertTrue(jarClassLocation.toString().contains(jar.toURI().toURL().toString()));
    }

    @Test(expected = InstrumentationException.class)
    public void testOverrideLangClass() throws Exception {
        URL folder = URLUtils.toTempFolder(
            new String[] {
                    // Put it at java/lang/double in the jar
                    "java/lang/Double.class"
            },
            new URL[] {
                    // load it from there
                    IndividualClassLoaderTest.class.getResource("resources/java.lang.Double.class")
            }
        );
        IndividualClassLoader loader = setupLoader(
                "instrumentertest",
                new IndividualClassLoader.Cache(folder)
        );

        loader.loadClass("java.lang.Double");
    }

    @Test
    public void testNoIncorrectPlayerPackages() {
        for (String pack : new String[] { "battlecode", "java", "com.sun"}) {
            try {
                new IndividualClassLoader(pack, sharedCache);
                fail("No error on player package: "+pack);
            } catch (InstrumentationException e) {}
        }
    }

    @Test
    public void testNoCollisions() throws Exception {
        URL folderA = URLUtils.toTempFolder(
            new String[] {
                    "Value.class"
            },
            new URL[] {
                    IndividualClassLoaderTest.class.getResource("resources/ValueA.class")
            }
        );
        URL folderB = URLUtils.toTempFolder(
            new String[] {
                    "Value.class"
            },
            new URL[] {
                    IndividualClassLoaderTest.class.getResource("resources/ValueB.class")
            }
        );
        IndividualClassLoader loaderA = setupLoader("instrumentertest",
                new IndividualClassLoader.Cache(folderA));
        IndividualClassLoader loaderB = setupLoader("instrumentertest",
                new IndividualClassLoader.Cache(folderB));

        assertEquals(
                'A',
                loaderA.loadClass("Value").getMethod("getValue").invoke(null)
        );

        assertEquals(
                'B',
                loaderB.loadClass("Value").getMethod("getValue").invoke(null)
        );
    }
}
