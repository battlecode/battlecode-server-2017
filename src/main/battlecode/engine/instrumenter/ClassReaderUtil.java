package battlecode.engine.instrumenter;

import org.objectweb.asm.ClassReader;

import java.io.IOException;

/**
 * A hack to deal with the fact that the ClassReader constructor uses
 * getSystemResource() rather than getResource() to read class files,
 * and getSystemResource() sometimes fails to find resources that
 * getResource() can find.
 *
 * @author james
 */
public final class ClassReaderUtil {
    /**
     * Create a class reader
     *
     * @param className the name of the class; can use "."s or "/"s to delimit
     *                  packages, but must use "$"s to delimit inner classes
     * @return a ClassReader for the class
     * @throws IOException if the ClassReader cannot be loaded
     */
    public static ClassReader reader(final String className) throws IOException {
        String fileName = className.replace(".", "/") + ".class";

        // read the raw bytes of the file, using this classloader to locate it
        return new ClassReader(ClassReaderUtil.class.getClassLoader()
                .getResourceAsStream(fileName));
    }
}
