package battlecode.world;

import battlecode.common.ComponentType;
import battlecode.common.RobotLevel;

public class Miner extends Builder {

	InternalMine mine;

	@SuppressWarnings("unchecked")
	public Miner(InternalRobot robot) {
		super(ComponentType.RECYCLER,robot);
		mine = (InternalMine) gameWorld.getObject(robot.getLocation(),RobotLevel.MINE);
	}

	public void processEndOfTurn() {
		super.processEndOfTurn();
		if(mine!=null)
			gameWorld.adjustResources(robot.getTeam(),mine.mine());
	}

}
