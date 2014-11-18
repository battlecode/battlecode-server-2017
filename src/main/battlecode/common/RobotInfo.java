package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of another Robot. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 *
 * @author Teh Devs
 */
public class RobotInfo {

    public final int ID;
    public final Team team;
    public final RobotType type;
    public final MapLocation location;
    public final double turnsUntilMovement;
    public final double turnsUntilAttack;
    public final double health;
    public final double supplyLevel;
    public final int xp;
    public final boolean isBuildingSomething;
    public final RobotType buildingTypeBeingBuilt;
    public final int buildingRoundsRemaining;
    public final int missileCount;

    public RobotInfo(int ID, Team team, RobotType type, MapLocation location, double turnsUntilMovement, double turnsUntilAttack, double health, double supplyLevel, int xp, boolean isBuildingSomething, RobotType buildingTypeBeingBuilt, int buildingRoundsRemaining, int missileCount) {
        super();
        this.ID = ID;
        this.team = team;
        this.type = type;
        this.location = location;
        this.turnsUntilMovement = turnsUntilMovement;
        this.turnsUntilAttack = turnsUntilAttack;
        this.health = health;
        this.supplyLevel = supplyLevel;
        this.xp = xp;
        this.isBuildingSomething = isBuildingSomething;
        this.buildingTypeBeingBuilt = buildingTypeBeingBuilt;
        this.buildingRoundsRemaining = buildingRoundsRemaining;
        this.missileCount = missileCount;
    }

    public int hashCode() {
        return ID;
    }
}
