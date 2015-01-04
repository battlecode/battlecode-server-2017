package battlecode.world;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.CommanderSkillType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.GenericRobot;
import battlecode.engine.signal.Signal;
import battlecode.server.Config;
import battlecode.world.signal.AttackSignal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.SelfDestructSignal;
import battlecode.world.signal.SpawnSignal;

public class InternalCommander extends InternalRobot {
    private int xp;

    @SuppressWarnings("unchecked")
    public InternalCommander(GameWorld gw, RobotType type, MapLocation loc, Team t, boolean spawnedRobot, int buildDelay) {
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
