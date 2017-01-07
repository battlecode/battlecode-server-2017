package battlecode.instrumenter;

/**
 * Used to verify that a team's submission will pass the instrumenter when it's run.
 * <p/>
 * Usage:<br>
 * <code>java Verifier teamXXX</code><br>
 * If the submission passes, the Java process will return 0, and there will be no output to stderr.  If the
 * submission fails, the Java process will return 1, and there will be error messages in stderr.
 *
 * @author adamd
 */
public class Verifier {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: battlecode.instrumenter.Verifier ${team} ${team.url}, where team is a package" +
                    "containing a RobotPlayer and team.url is a folder or jar containing all of the player-defined class files" +
                    "for that RobotPlayer");
        }
        if (!verify(args[0], args[1])) System.exit(1);
    }

    public static boolean verify(String teamPackageName, String teamURL) {
        try {
            TeamClassLoaderFactory.Loader icl = new TeamClassLoaderFactory(teamURL).createLoader();
            icl.loadClass(teamPackageName + ".RobotPlayer");
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            return false;
        }
    }
}
