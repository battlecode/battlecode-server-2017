package battlecode.world;

import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.WeaponController;
import battlecode.world.signal.AttackSignal;
import battlecode.engine.instrumenter.RobotDeathException;

public class Weapon extends BaseComponent implements WeaponController {

    private MapLocation3D lastHit = null;
    private int roundsHit = 0;

    public Weapon(ComponentType type, InternalRobot robot) {
        super(type, robot);
    }

    public void attackSquare(MapLocation loc, RobotLevel height) throws GameActionException {
        assertEquipped();
        assertInactive();
        assertNotNull(loc);
        assertNotNull(height);
        assertWithinRange(loc);
        //check no air target
        if (this.type == ComponentType.HAMMER && height == RobotLevel.IN_AIR){
            throw new GameActionException(GameActionExceptionType.OUT_OF_RANGE, "Cannot attack air");
        }
        //Beam calculations
        if (this.type == ComponentType.BEAM) {
            if ((new MapLocation3D(loc, height)).equals(lastHit)) roundsHit++;
            else {
                lastHit = new MapLocation3D(loc, height);
                roundsHit = 0;
            }
            activate(new AttackSignal(robot, type, loc, height,
                    GameConstants.BEAM_RAMP[Math.min(roundsHit, GameConstants.BEAM_RAMP.length - 1)]));
        } else {
            activate(new AttackSignal(robot, type, loc, height));
        }
		// if this robot killed itself, its turn should end
		if(robot.getEnergonLevel()<0) {
			throw new RobotDeathException();
		}
    }
}
