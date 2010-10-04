package battlecode.contrib.match.xml;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import battlecode.common.MapLocation;
import battlecode.world.signal.*;

public class XMLSignalHandler extends AutoSignalHandler<String> {
	
	public String visitAttackSignal(AttackSignal s) {
        String str = "<attackSignal "
                + "robot='" + s.getRobotID() + "' "
                + "loc='" + loc(s.getTargetLoc()) + "' "
                + "level='" + s.getTargetHeight() + "' />";
        return str;
    }

    public String visitBroadcastSignal(BroadcastSignal s) {
        String str = "<broadcastSignal "
                + "robot='" + s.getRobotID() + "' />";
        return str;
    }
    
    public String visitConvexHullSignal(ConvexHullSignal s) {
		String str = "<convexHullSignal "
            + "team='" + s.getTeam() + "'>";
		for (MapLocation[] locs : s.getConvexHulls()) {
			String[] strLocs = new String[locs.length];
			for (int i = 0; i < locs.length; i++) {
				strLocs[i] = loc(locs[i]);
			}
			str += "<hull locs='" + StringUtils.join(strLocs, ";") + "' />";
		}
		str += "</convexHullSignal>";
		return str;
	}

    public String visitDeathSignal(DeathSignal s) {
        String str = "<deathSignal "
                + "robot='" + s.getObjectID() + "' />";
        return str;
    }
    
    public String visitDeploySignal(DeploySignal s) {
        return "<deploySignal "
                + "robot='" + s.getRobotID() + "' />";
    }
    
    public String visitDoTeleportSignal(DoTeleportSignal s) {
        String str = "<doTeleportSignal "
                + "robot='" + s.getRobotID() + "' "
                + "teleportLoc='" + loc(s.getTeleportLoc()) + "' />";
        return str;
    }

    public String visitEnergonChangeSignal(EnergonChangeSignal s) {
        String str = "<energonChangeSignal "
                + "robots='" + StringUtils.join(ArrayUtils.toObject(s.getRobotIDs()), ",") + "' "
                + "energon='" + StringUtils.join(ArrayUtils.toObject(s.getEnergon()), ",") + "' />";
        return str;
    }

    public String visitEnergonTransferSignal(EnergonTransferSignal s) {
        String str = "<energonTransferSignal "
                + "robot='" + s.getRobotID() + "' "
                + "level='" + s.getTargetHeight() + "' "
                + "loc='" + loc(s.getTargetLoc()) + "' "
                + "amt='" + d(s.getAmount()) + "' />";
        return str;
    }
    
    public String visitFluxChangeSignal(FluxChangeSignal s) {
        String str = "<fluxChangeSignal "
                + "robots='" + StringUtils.join(ArrayUtils.toObject(s.getRobotIDs()), ",") + "' "
                + "flux='" + StringUtils.join(ArrayUtils.toObject(s.getFlux()), ",") + "' />";
        return str;
    }

    public String visitFluxTransferSignal(FluxTransferSignal s) {
        String str = "<fluxTransferSignal "
                + "robot='" + s.getRobotID() + "' "
                + "level='" + s.getTargetHeight() + "' "
                + "loc='" + loc(s.getTargetLoc()) + "' "
                + "amt='" + s.getAmount() + "' />";
        return str;
    }

    public String visitEvolutionSignal(EvolutionSignal s) {
        String str = "<evolutionSignal "
                + "robot='" + s.getRobotID() + "' "
                + "type='" + s.getType() + "' />";
        return str;
    }

    public String visitIndicatorStringSignal(IndicatorStringSignal s) {
    	return null;
    	/*
        String indicatorString = s.getNewString();
        indicatorString = (indicatorString != null) ? indicatorString : "";
        indicatorString = indicatorString.replaceAll("<", "&lt;");
        indicatorString = indicatorString.replaceAll(">", "&gt;");
        indicatorString = indicatorString.replaceAll("\"", "&quot;");
        indicatorString = indicatorString.replaceAll("\'", "#039;");
        indicatorString = indicatorString.replaceAll("&", "&amp;");

        String str = "<indicatorStringSignal "
                + "robot='" + s.getRobotID() + "' "
                + "index='" + s.getStringIndex() + "' "
                + "string='" + indicatorString + "' />";
        return str;
        */
    }
    
    public String visitMineFluxSignal(MineFluxSignal s) {
    	return null;
    }

    public String visitMovementSignal(MovementSignal s) {
        String str = "<movementSignal "
                + "robot='" + s.getRobotID() + "' "
                + "loc='" + loc(s.getNewLoc()) + "' "
                + "forward='" + s.isMovingForward() + "' />";
        return str;
    }

    public String visitSetAuraSignal(SetAuraSignal s) {
        String str = "<setAuraSignal "
                + "robot='" + s.getRobotID() + "' "
                + "aura='" + s.getAura() + "' />";
        return str;
    }

    public String visitSetDirectionSignal(SetDirectionSignal s) {
        String str = "<setDirectionSignal "
                + "robot='" + s.getRobotID() + "' "
                + "dir='" + s.getDirection() + "' />";
        return str;
    }

    public String visitStartTeleportSignal(StartTeleportSignal s) {
        String str = "<startTeleportSignal "
                + "robot='" + s.getRobotID() + "' "
                + "fromTeleporterID='" + s.getFromTeleporterID() + "' "
                + "toTeleporterID='" + s.getToTeleporterID() + "' "
                + "teleportLoc='" + loc(s.getTeleportLoc()) + "' />";
        return str;
    }

    public String visitSpawnSignal(SpawnSignal s) {
        String str = "<spawnSignal "
                + "robot='" + s.getRobotID() + "' "
                + "parent='" + s.getParentID() + "' "
                + "team='" + s.getTeam() + "' "
                + "type='" + s.getType() + "' "
                + "dir='" + s.getDirection() + "' "
                + "loc='" + loc(s.getLoc()) + "' />";
        return str;
    }

    public String visitUndeploySignal(UndeploySignal s) {
        return "<undeploySignal "
                + "robot='" + s.getRobotID() + "' />";
    }

    /////////////////////////////////////////////////
    /////////// PRIVATE HELPER METHODS //////////////
    /////////////////////////////////////////////////
    private String loc(MapLocation location) {
        return location.getX() + "," + location.getY();
    }

    private String d(double val) {
        return Double.toString(Math.floor(val * 100) / 100);
    }

}
