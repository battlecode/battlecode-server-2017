package battlecode.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Utility class for determining the java version.
 *
 * @author james
 */
public class Version {
    /**
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            final BufferedReader r = new BufferedReader(new InputStreamReader(
                            Version.class.getClassLoader().getResourceAsStream("battlecode-version")
            ));
            System.out.println(r.readLine());
        } catch (Exception e) {
            System.err.println("Can't determine version!");
            e.printStackTrace();
        }
    }
}
