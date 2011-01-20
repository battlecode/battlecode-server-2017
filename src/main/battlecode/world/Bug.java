package battlecode.world;

import battlecode.common.BugController;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.MineInfo;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.world.signal.AttackSignal;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Bug extends Sensor implements BugController {

    InternalRobot buggedRobot;
    int expiration;

    public Bug(ComponentType type, InternalRobot robot) {
        super(type, robot);
    }

    public MapLocation buggedLocation() {
        if (buggedRobot == null)
            return null;
        else
            return buggedRobot.getLocation();
    }

    @Override
    public void processEndOfTurn() {
        super.processEndOfTurn();
        if (buggedRobot != null && gameWorld.getCurrentRound() >= expiration)
            buggedRobot.setBugged(null);
    }

    // Called by InternalRobot when the robot gets bugged by someone else.
    public void removeBug() {
        buggedRobot = null;
    }

    @Override
    public boolean checkWithinRange(MapLocation loc) {
        if (buggedRobot == null)
            return false;
        return buggedRobot.getLocation().distanceSquaredTo(loc) <= GameConstants.BUG_SENSOR_RANGE;
    }

	public boolean withinRange(MapLocation loc) {
		return super.checkWithinRange(loc);
	}

    public void attackSquare(MapLocation loc, RobotLevel height) throws GameActionException {
        assertEquipped();
        assertInactive();
        assertNotNull(loc);
        assertNotNull(height);
        if (robot.getLocation().distanceSquaredTo(loc) > type.range)
            outOfRange();
        activate(new AttackSignal(robot, type, loc, height));
        InternalRobot ir = gameWorld.getRobot(loc, height);
        if (ir != null) {
            if (buggedRobot != null)
                buggedRobot.setBugged(null);
            buggedRobot = ir;
        }
    }

    public double seneseIncome(Robot r) throws GameActionException {
        throw new GameActionException(GameActionExceptionType.CANT_SENSE_THAT, "Bug cannot sense income");
    }
}
