package battlecode.common;

public interface PowerNode extends GameObject {

    /**
     * Returns this power node's location.
     */
    public MapLocation getLocation();

    /**
     * Returns the locations of all power nodes that are connected to this power node.
     */
    public MapLocation[] neighbors();

    /**
     * If this node is the power core for a team, returns that team.
     * Otherwise returns null.
     */
    public Team powerCoreTeam();

}
