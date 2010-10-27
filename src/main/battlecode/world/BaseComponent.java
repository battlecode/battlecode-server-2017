package battlecode.world;

import battlecode.common.ComponentClass;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;

import static battlecode.common.GameActionExceptionType.*;

import com.google.common.base.Predicate;

public abstract class BaseComponent implements ComponentController
{
	protected InternalComponent component;
	protected InternalRobot robot;
	protected GameWorld gameWorld;

	public boolean isActive() {
		assertEquipped();
		return component.isActive();
	}

	public int roundsUntilIdle() {
		assertEquipped();
		return component.roundsUntilIdle();
	}

	public ComponentType type() { return component.type(); }

	public ComponentClass componentClass() { return component.type().componentClass; }

	public InternalComponent getComponent() { return component; }

	public InternalRobot getRobot() { return robot; }

	public void unequip() {
		// add unequip signal
	}

	protected Predicate<InternalObject> objectWithinRangePredicate() {
		return new Predicate<InternalObject>() {
			public boolean apply(InternalObject o) {
				return checkWithinRange(o);
			}
		};
	}

	protected Predicate<MapLocation> locWithinRangePredicate() {
		return new Predicate<MapLocation>() {
			public boolean apply(MapLocation o) {
				return checkWithinRange(o);
			}
		};
	}


	protected void assertEquipped() {
		if(component.getController()!=this)
			throw new IllegalStateException("You no longer control this component.");
	}

	public boolean withinRange(MapLocation loc) {
		assertEquipped();
		return checkWithinRange(loc);
	}

	protected boolean checkWithinRange(MapLocation loc) {
		if(robot.getLocation().distanceSquaredTo(loc)>component.type().range)
			return false;
		return GameWorld.inAngleRange(robot.getLocation(),robot.getDirection(),
			loc,component.type().cosHalfAngle);
	}

	protected boolean checkWithinRange(InternalObject obj) {
		return checkWithinRange(obj.getLocation());
	}

	protected static void assertNotNull(Object o) {
		RobotControllerImpl.assertNotNull(o);
	}
	
	protected void assertInactive() throws GameActionException {
		if(component.roundsUntilIdle()>0)
			throw new GameActionException(ALREADY_ACTIVE,"This component is already active.");	
	}

	protected void assertWithinRange(MapLocation loc) throws GameActionException {
		if(!checkWithinRange(loc))
			throw new GameActionException(CANT_SENSE_THAT,"That is not within this component's range.");
	}

	protected void assertWithinRange(InternalObject obj) throws GameActionException {
		assertWithinRange(obj.getLocation());
	}

	protected static InternalRobot castInternalRobot(Robot r) {
		assertNotNull(r);
		if (!(r instanceof InternalRobot))
            throw new IllegalArgumentException("Invalid Robot object (don't extend Robot!)");
		return (InternalRobot) r;
	}

	protected static InternalObject castInternalObject(GameObject o) {
        assertNotNull(o);
		if (!(o instanceof InternalObject))
            throw new IllegalArgumentException("Invalid GameObject (don't extend GameObject!)");
		return (InternalObject) o;
	}

	protected BaseComponent(InternalComponent component, InternalRobot robot) {
		this.component = component;
		this.robot = robot;
		this.gameWorld = robot.getGameWorld();
	}

}
