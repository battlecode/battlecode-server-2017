package battlecode.world;

import battlecode.common.BuildMappings;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;

import battlecode.world.signal.EquipSignal;
import battlecode.world.signal.SpawnSignal;

public class Builder extends BaseComponent implements BuilderController {

    public Builder(ComponentType type, InternalRobot robot) {
        super(type, robot);
    }

    public void build(ComponentType type, MapLocation loc, RobotLevel level) throws GameActionException {
        assertNotNull(level);
        assertNotNull(type);
        assertCanBuild(type);
        assertInactive();
        assertWithinRange(loc);
        InternalRobot ir = alliedRobotAt(loc, level);
        assertHasRoomFor(ir, type);
        if (ir.getChassis() != Chassis.BUILDING && (type == ComponentType.ARMORY || type == ComponentType.FACTORY || type == ComponentType.RECYCLER))
            cantBuildOnThat(type, ir.getChassis());
        if (ir.getChassis() == Chassis.BUILDING && type == ComponentType.JUMP)
            cantBuildOnThat(type, ir.getChassis());
        spendResources(type.cost);
        activate(new EquipSignal(ir, robot, type));
    }

    public void cantBuildOnThat(ComponentType component, Chassis chassis) throws GameActionException {
        throw new GameActionException(GameActionExceptionType.CANT_BUILD_THAT, "You cannot build a " + component + " on a " + chassis);
    }

    public void build(Chassis type, MapLocation loc) throws GameActionException {
        assertNotNull(type);
        assertCanBuild(type);
        assertInactive();
        assertWithinRange(loc);
        if (!gameWorld.canMove(type.level, loc))
            throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "That square is occupied.");
        spendResources(type.cost*(robot.getBuffs().getSpawnCostMultiplier()+1));
        activate(new SpawnSignal(loc, type, robot.getTeam(), robot));
    }

    public static boolean canBuild(ComponentType myType, ComponentType type) {
        return BuildMappings.canBuild(myType, type);
    }

    public static boolean canBuild(ComponentType myType, Chassis chassis) {
        return BuildMappings.canBuild(myType, chassis);
    }

    public void assertCanBuild(ComponentType type) throws GameActionException {
        if (!canBuild(this.type, type))
            throw new GameActionException(GameActionExceptionType.CANT_BUILD_THAT, this.type + " cannot build " + type);
    }

    public void assertCanBuild(Chassis type) throws GameActionException {
        if (!canBuild(this.type, type))
            throw new GameActionException(GameActionExceptionType.CANT_BUILD_THAT, this.type + " cannot build " + type);
    }

    public void assertHasRoomFor(InternalRobot r, ComponentType c) throws GameActionException {
        if (!r.hasRoomFor(c))
            throw new GameActionException(GameActionExceptionType.NO_ROOM_IN_CHASSIS, "Target has no room for component");
    }

    public boolean canBuild(Direction dir, RobotLevel level) {
        assertValidDirection(dir);
		if(type()==ComponentType.DUMMY)
			throw new IllegalStateException("DUMMY cannot use canBuild.");
		else if(level==RobotLevel.IN_AIR&&!BuildMappings.canBuild(type,Chassis.FLYING))
			return false;
        return gameWorld.canMove(level, getLocation().add(dir));
    }

	public boolean canBuild(Chassis chassis, MapLocation loc) {
		if(type()==ComponentType.DUMMY)
			throw new IllegalStateException("DUMMY cannot use canBuildChassis.");
		assertNotNull(chassis);
		assertNotNull(loc);
		if(!BuildMappings.canBuild(type,chassis)) return false;
		if(!withinRange(loc)) return false;
		return gameWorld.canMove(chassis.level, loc);
	}
}
