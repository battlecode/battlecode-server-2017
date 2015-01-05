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
    public final int xp;
    public final int missileCount;

    public RobotInfo(int ID, Team team, RobotType type, MapLocation location, double coreDelay, double weaponDelay, double health, double supplyLevel, int xp, int missileCount) {
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
    }

    public int hashCode() {
        return ID;
    }
}
