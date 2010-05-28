package battlecode.common;

/**
 * Struct that stores basic information that was 'sensed' of another Robot. This
 * info is ephemeral and there is no guarantee any of it will remain the same
 * between rounds.
 * 
 * @author Teh Devs
 */
public class RobotInfo {
	
	/** ID of this Robot */
	public final int id;
    /** Type of this Robot */
    public final RobotType type;
    /** Team of this Robot */
    public final Team team;
    /** Location of this Robot */
    public final MapLocation location;
    /** Energon level of this Robot */
    public final double energonLevel;
    /** Energon reserve level of this Robot */
    public final double energonReserve;
    /** Max energon level of this Robot */
    public final double maxEnergon;
    /** Eventual energon level of this Robot if reserve were allow to
     * completely transfer.
     * Math.min(energonLevel + energonReserve, maxEnergon)
     */
    public final double eventualEnergon;
    /** Rounds until this Robot becomes attack idle */
    public final int roundsUntilAttackIdle;
    /** Rounds until this Robot becomes movement idle */
    public final int roundsUntilMovementIdle;
    /** The direction this Robot is facing */
    public final Direction directionFacing;
    /** The number of blocks in this unit's cargo. Zero for units other than workers. */
    public final double flux;
    /**
     * if this Robot is a turret, this will tell you if it is deployed.
     */
    public final boolean deployed;
    /**
     * Whether the Robot is tleporting
     */
    public final boolean teleporting;
    public final AuraType aura;

    public RobotInfo(int id, RobotType type, Team team, MapLocation location,
            double energonLevel, double energonReserve, double maxEnergon,
            int roundsUntilAttackIdle, int roundsUntilMovementIdle,
            Direction directionFacing, double flux, boolean deployed, boolean teleporting, AuraType aura) {
        super();
		this.id = id;
        this.type = type;
        this.team = team;
        this.location = location;
        this.energonLevel = energonLevel;
        this.energonReserve = energonReserve;
        this.maxEnergon = maxEnergon;
        this.eventualEnergon = Math.min(energonLevel + energonReserve, maxEnergon);
        this.roundsUntilAttackIdle = roundsUntilAttackIdle;
        this.roundsUntilMovementIdle = roundsUntilMovementIdle;
        this.directionFacing = directionFacing;
        this.flux = flux;
        this.deployed = deployed;
        this.teleporting = teleporting;
        this.aura = aura;
    }

	public int hashCode() {
		return id;
	}
}
