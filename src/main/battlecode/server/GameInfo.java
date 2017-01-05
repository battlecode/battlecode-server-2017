package battlecode.server;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;

/**
 * Represents a "Game": a series of Matches between two players,
 * on a set of maps.
 */
public class GameInfo implements Serializable {

    private static final long serialVersionUID = 638514128835207033L;

    /**
     * The name of team A.
     */
    private final String teamAName;

    /**
     * The package name of team A.
     */
    private final String teamAPackage;

    /**
     * The URL of the classes of team A.
     * Null if its classes can be found on the system classpath.
     */
    private final URL teamAClasses;

    /**
     * The name of team B.
     */
    private final String teamBName;

    /**
     * The package name of team B.
     */
    private final String teamBPackage;

    /**
     * The URL of the classes of team B.
     * Null if its classes can be found on the system classpath.
     */
    private final URL teamBClasses;

    /**
     * The maps to run matches on.
     */
    private final String[] maps;

    /**
     * Whether the game is running in best-of-three mode.
     * This could be extended to best-of-N if needed.
     */
    private final boolean bestOfThree;

    /**
     * The file the game should be saved to, or null
     * if it shouldn't be saved.
     */
    private final File saveFile;


    /**
     * Create a GameInfo.
     *
     * @param teamAPackage the package of A team
     * @param teamAClasses the location of team A classes - directory or jar,
     *                     or null to use the system classpath
     * @param teamBPackage the B team
     * @param teamBClasses the location of team B classes
     * @param maps the names maps to play on
     * @param saveFile the file to save to if the server is configured to save
     *                 matches, or null to never save
     * @param bestOfThree whether the game is best of three
     */
    public GameInfo(String teamAName, String teamAPackage, URL teamAClasses,
                    String teamBName, String teamBPackage, URL teamBClasses,
                    String[] maps,
                    File saveFile,
                    boolean bestOfThree) {
        this.teamAName = teamAName;
        this.teamAPackage = teamAPackage;
        this.teamAClasses = teamAClasses;
        this.teamBName = teamBName;
        this.teamBPackage = teamBPackage;
        this.teamBClasses = teamBClasses;
        this.maps = maps;
        this.saveFile = saveFile;
        this.bestOfThree = bestOfThree;
    }

    /**
     * @return the maps to run matches on
     */
    public String[] getMaps() {
        return maps;
    }

    /**
     * @return the package name of team A
     */
    public String getTeamAPackage() {
        return teamAPackage;
    }

    /**
     * @return the URL of the classes of team A, or null if they can be found on the system classpath
     */
    public URL getTeamAClasses() {
        return teamAClasses;
    }

    /**
     * @return the package name of team B
     */
    public String getTeamBPackage() {
        return teamBPackage;
    }

    /**
     * @return the URL of the classes of team B, or null if they can be found on the system classpath
     */
    public URL getTeamBClasses() {
        return teamBClasses;
    }

    /**
     * @return the save file this game should be saved to, or null if it shouldn't be saved
     */
    public File getSaveFile() {
        return this.saveFile;
    }

    /**
     * @return whether the game is best of three
     */
    public boolean isBestOfThree() {
        return bestOfThree;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append(teamAPackage);
        if (teamAClasses != null) {
            b.append(" (");
            b.append(teamAClasses);
            b.append(") ");
        }

        b.append(" vs ");

        b.append(teamBPackage);
        if (teamBClasses != null) {
            b.append(" (");
            b.append(teamBClasses);
            b.append(") ");
        }

        b.append(" on ");

        b.append(Arrays.asList(maps));

        if (bestOfThree) {
            b.append(" (best of three)");
        }

        return b.toString();
    }

    /**
     * The name of team A.
     */
    public String getTeamAName() {
        return teamAName;
    }

    /**
     * The name of team B.
     */
    public String getTeamBName() {
        return teamBName;
    }
}
