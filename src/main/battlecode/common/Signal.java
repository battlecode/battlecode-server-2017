package battlecode.common;

public class Signal {

    private MapLocation location;
    private int ID;
    private Team team;
    private int[] message;

    public Signal(MapLocation ml, int id, Team t) {

        this.location = ml;
        this.ID = id;
        this.team = t;
        this.message = null;

    }

    public Signal(MapLocation ml, int id, Team t, int signal1, int signal2) {

        this.location = ml;
        this.ID = id;
        this.team = t;
        this.message = new int[]{signal1, signal2};

    }

    public int getID() {
        return ID;
    }

    public MapLocation getLocation() {
        return location;
    }

    public Team getTeam() {
        return team;
    }

    /**
     * Returns the message associated with this signal, or null if the signal
     * was a basic (regular) signal. If it's not null, the integer array will
     * always have length 2.
     *
     * @return the message associated with this signal, or null if the signal
     * had no message.
     */
    public int[] getMessage() {
        return message;
    }

}
