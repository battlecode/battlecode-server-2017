package battlecode.world;

import battlecode.common.*;

/*
TODO:
- add a mortar registry in GameWorld??
- comments
 */
public class InternalMortar {

    private final InternalRobot myMortar;
    private final MapLocation mySource;
    private final MapLocation myTarget;
    private final GameWorld myGameWorld;
    private final double myDamage;
    private int roundsUntilImpact = 5;

    public InternalMortar(GameWorld gw, MapLocation target, InternalRobot mortar, double damage) {
        myMortar = mortar;
        myGameWorld = gw;
        mySource = mortar.getLocation();
        myTarget = target;
        myDamage = damage;
        gw.notifyAddingNewMortar(this);
    }

    public void process() {
        if (!myGameWorld.isExistant(myMortar)) {
            // Terminate damage early
            myGameWorld.removeMortar(this);
            return;
        }

        InternalRobot targetRobot = myGameWorld.getRobot(myTarget, RobotLevel.ON_GROUND);
        if (targetRobot != null) {
            double damage = myDamage / 5;
            targetRobot.changeEnergonLevelFromAttack(-damage);
        }

        if (--roundsUntilImpact == 0) {
            myGameWorld.removeMortar(this);
        }
    }

    public MapLocation getSource() {
        return mySource;
    }

    public MapLocation getTarget() {
        return myTarget;
    }

    public int getRoundsUntilImpact() {
        return roundsUntilImpact;
    }
}
