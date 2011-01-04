package battlecode.world.signal;

import battlecode.common.MapLocation;
import battlecode.world.InternalMine;
import battlecode.engine.signal.Signal;

public class MineDepletionSignal extends Signal
{

	public final int id;
        public final int roundsAvaliable;

	public MineDepletionSignal(InternalMine mine, int roundsLeft) {
		id = mine.getID();
                roundsAvaliable = roundsLeft;
	}

}
