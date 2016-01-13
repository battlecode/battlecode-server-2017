package battlecode.server;

import battlecode.server.proxy.Proxy;

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
     * The package name of team A.
     */
    private final String teamA;

    /**
     * The URL of the classes of team A.
     * Null if its classes can be found on the system classpath.
     */
    private final URL teamAClasses;

    /**
     * The package name of team B.
     */
    private final String teamB;

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
     * The proxies that the match should write to.
     */
    private final Proxy[] proxies;

    /**
     * Create a GameInfo.
     *
     * @param teamA the A team
     * @param teamB the B team
     * @param maps the maps to play on
     */
    public GameInfo(String teamA, URL teamAClasses,
                    String teamB, URL teamBClasses,
                    String[] maps,
                    Proxy[] proxies,
                    boolean bestOfThree) {
        this.teamA = teamA;
        this.teamAClasses = teamAClasses;
        this.teamB = teamB;
        this.teamBClasses = teamBClasses;
        this.maps = maps;
        this.bestOfThree = bestOfThree;
        this.proxies = proxies;
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
    public String getTeamA() {
        return teamA;
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
    public String getTeamB() {
        return teamB;
    }

    /**
     * @return the URL of the classes of team B, or null if they can be found on the system classpath
     */
    public URL getTeamBClasses() {
        return teamBClasses;
    }

    /**
     * @return whether the game is best of three
     */
    public boolean isBestOfThree() {
        return bestOfThree;
    }

    /**
     * @return the proxies that the game should write to.
     *         DO NOT MODIFY THE RETURNED ARRAY.
     */
    public Proxy[] getProxies() {
        return proxies;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append(teamA);
        if (teamAClasses != null) {
            b.append(" (");
            b.append(teamAClasses);
            b.append(") ");
        }

        b.append(" vs ");

        b.append(teamB);
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
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private GameInfo() {
        this(null, null, null, null, null, null, false);
    }
}
