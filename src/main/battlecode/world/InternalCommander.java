package battlecode.world;

import battlecode.common.CommanderSkillType;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class InternalCommander extends InternalRobot {
    private int xp;

    @SuppressWarnings("unchecked")
    public InternalCommander(GameWorld gw, RobotType type, MapLocation loc, Team t, boolean spawnedRobot,
            int buildDelay) {
        super(gw, type, loc, t, spawnedRobot, buildDelay);

        xp = 0;
    }

    public int getXP() {
        return xp;
    }

    public void giveXP(int amt) {
        xp += amt;
    }

    public boolean hasSkill(CommanderSkillType type) {
        return myGameWorld.hasSkill(getTeam(), type);
    }
}
