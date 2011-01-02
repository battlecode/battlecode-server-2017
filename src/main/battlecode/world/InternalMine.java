package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.RobotLevel;
import battlecode.common.Team;

public class InternalMine extends InternalObject implements Mine {

    private int roundsLeft = GameConstants.MINE_ROUNDS;

    public InternalMine(GameWorld gw, MapLocation loc) {
        super(gw, loc, RobotLevel.MINE, Team.NEUTRAL);
    }

    public int getRoundsLeft() {
        return roundsLeft;
    }

    public double mine() {
        roundsLeft--;
        if (roundsLeft > 0)
            return GameConstants.MINE_RESOURCES;
        else
            return Math.max(GameConstants.MINE_MINIMUM, GameConstants.MINE_RESOURCES + roundsLeft / GameConstants.MINE_DEPLETION_RATE * 0.01);
    }
}
