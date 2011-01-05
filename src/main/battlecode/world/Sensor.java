package battlecode.world;

import battlecode.common.Chassis;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.MineInfo;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;


public class Sensor extends BaseComponent implements SensorController {

    public Sensor(ComponentType type, InternalRobot robot) {
        super(type, robot);
    }

    public GameObject senseObjectAtLocation(MapLocation loc, RobotLevel height) throws GameActionException {
        assertEquipped();
        assertNotNull(loc);
        assertNotNull(height);
        assertWithinRange(loc);
        return gameWorld.getObject(loc, height);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameObject> T[] senseNearbyGameObjects(final Class<T> type) {
        Predicate<InternalObject> p = new Predicate<InternalObject>() {

            public boolean apply(InternalObject o) {
                return checkWithinRange(o) && (type.isInstance(o)) && (!o.equals(robot));
            }
        };
        return Iterables.toArray((Iterable<T>)Iterables.filter(gameWorld.allObjects(), p), type);
    }

    public RobotInfo senseRobotInfo(Robot r) throws GameActionException {
        assertEquipped();
        InternalRobot ir = castInternalRobot(r);
        assertWithinRange(ir);
        ComponentType[] components;
        if (type() == ComponentType.SATELLITE || robot.getTeam() == ir.getTeam())
            components = ir.getComponentTypes();
        else
            components = null;
        MapLocation loc;
        if (ir.container() != null)
            loc = ir.container().getLocation();
        else
            loc = ir.getLocation();

        Chassis ch = ir.getChassis();
        if (ir.getChassis() == Chassis.DUMMY && ir.getTeam() != this.getRobot().getTeam()) {
            ch = Chassis.MEDIUM;
            if (type() == ComponentType.SATELLITE) {
                components = new ComponentType[] {ComponentType.MEDIUM_MOTOR};
            }

        }

        return new RobotInfo(ir, ir.getLocation(), ir.getEnergonLevel(), ir.getMaxEnergon(),
                ir.getDirection(), ir.isOn(), components, ch);

    }

	public boolean canSenseComponents(InternalRobot ir) {
		return type() == ComponentType.SATELLITE || robot.getTeam() == ir.getTeam();
	}

    public MineInfo senseMineInfo(Mine m) throws GameActionException {
        InternalMine im = castInternalMine(m);
        assertWithinRange(im);
        return new MineInfo(im, im.getRoundsLeft());
    }

    public MapLocation senseLocationOf(GameObject o) throws GameActionException {
        InternalObject io = castInternalObject(o);
        assertWithinRange(io);
        return io.getLocation();
    }

    public boolean canSenseObject(GameObject o) {
        assertEquipped();
        return checkWithinRange(castInternalObject(o));
    }

	public boolean canSenseSquare(MapLocation loc) {
		return checkWithinRange(loc);
	}
}
