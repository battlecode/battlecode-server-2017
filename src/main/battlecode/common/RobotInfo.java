package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of another Robot. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 */
public class RobotInfo {

    public final int ID;
    public final Team team;
    public final RobotType type;
    public final MapLocation location;
    public final double coreDelay;
    public final double weaponDelay;
    public final double health;
    public final double supplyLevel;

    /**
     * COMMANDER only.
     */
    public final int xp;

    /**
     * LAUNCHER only.
     */
    public final int missileCount;

    /**
     * For structures being built, this will be the location of the unit building the structure (null if N/A).
     */
    public final MapLocation builder;

    /**
     * For units that are building a structure, this will be the location of the structure being built (null if N/A).
     */
    public final MapLocation buildingLocation;

    public RobotInfo(int ID, Team team, RobotType type, MapLocation location, double coreDelay, double weaponDelay, double health, double supplyLevel, int xp, int missileCount, MapLocation builder, MapLocation buildingLocation) {
        super();
        this.ID = ID;
        this.team = team;
        this.type = type;
        this.location = location;
        this.coreDelay = coreDelay;
        this.weaponDelay = weaponDelay;
        this.health = health;
        this.supplyLevel = supplyLevel;
        this.xp = xp;
        this.missileCount = missileCount;
        this.builder = builder;
        this.buildingLocation = buildingLocation;
    }

    public int hashCode() {
        return ID;
    }
}
