package battlecode.world;

import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;
import battlecode.world.signal.MovementSignal;

public class JumpCore extends BaseComponent implements JumpController {

    public JumpCore(ComponentType type, InternalRobot robot) {
        super(type, robot);
    }

    public void setDirection(Direction d) throws GameActionException {
        throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "This component cannot turn");
    }

    public TerrainTile senseTerrainTile(MapLocation loc) {
        return rc.senseTerrainTile(loc);
    }

    public void assertCanMove(MapLocation loc) throws GameActionException {

        if (!gameWorld.canMove(robot.myHeight, loc) )
            throw new GameActionException(GameActionExceptionType.CANT_MOVE_THERE, "Cannot move to the location: " + loc);
    }

    public void jump(MapLocation loc) throws GameActionException {
        assertInactive();
        assertWithinRange(loc);
        assertCanMove(loc);
        activate(new MovementSignal(robot, loc, false, 0));
    }
}
