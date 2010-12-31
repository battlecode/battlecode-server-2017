package battlecode.world;

import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.WeaponController;
import battlecode.world.signal.AttackSignal;

public class Beam extends BaseComponent implements WeaponController {

	public static final double [] damage = { 0.,0.,0.,0.,0.,1.,1.,1.,1.,2.,2.,2.,3.,3.,4.};
	public static final double MAX_DAMAGE = 5.;
	private MapLocation lastLoc;
	private RobotLevel lastHeight;
	private int consecutive;

	public Beam(ComponentType type, InternalRobot robot) {
		super(type,robot);
	}

	public void attackSquare(MapLocation loc, RobotLevel height) throws GameActionException {
		assertEquipped();
		assertInactive();
		assertNotNull(loc);
		assertNotNull(height);
		if(loc.equals(lastLoc)&&height.equals(lastHeight))
			consecutive++;
		else
			consecutive=0;
		double dmg;
		if(consecutive<damage.length)
			dmg = damage[consecutive];
		else
			dmg = MAX_DAMAGE;
		robot.addAction(new AttackSignal(robot,type,loc,height,dmg));
	}

}
