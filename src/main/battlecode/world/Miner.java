package battlecode.world;

import battlecode.common.ComponentType;
import battlecode.common.RobotLevel;
import battlecode.world.signal.MineDepletionSignal;

public class Miner extends Builder {

    InternalMine mine;

    @SuppressWarnings("unchecked")
    public Miner(InternalRobot robot) {
        super(ComponentType.RECYCLER, robot);
        mine = (InternalMine) gameWorld.getObject(robot.getLocation(), RobotLevel.MINE);
    }

    public void processBeginningOfTurn() {
        super.processBeginningOfTurn();
        if (mine != null) {
            gameWorld.addSignal(new MineDepletionSignal(mine, mine.getRoundsLeft()));

            gameWorld.adjustResources(robot.getTeam(), mine.mine());
        }
    }
}
