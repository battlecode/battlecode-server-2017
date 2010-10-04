package battlecode.world;

import battlecode.common.AuraType;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class InternalAura extends InternalWorker {

    AuraType lastAura;
    AuraType currentAura;
    AuraType nextAura;

    public InternalAura(GameWorld gw, RobotType type, MapLocation loc, Team t, boolean wakeDelay) {
        super(gw, type, loc, t, wakeDelay);
    }

    @Override
    public AuraType getAura() {
        return currentAura;
    }

    public void setAura(AuraType t) {
        lastAura = t;
        nextAura = t;
    }

    @Override
    public AuraType getLastAura() {
        return lastAura;
    }

    @Override
    public void processBeginningOfRound() {
        super.processBeginningOfRound();
        if (nextAura == null) return;
        switch (nextAura) {
            case OFF:
                myGameWorld.applyAuraDamageDealt(this);
                break;
            case DEF:
                myGameWorld.applyAuraDamageReceived(this);
                break;
            case MOV:
                myGameWorld.applyAuraMovement(this);
                break;
        }
        currentAura = nextAura;
        nextAura = null;
    }
}
