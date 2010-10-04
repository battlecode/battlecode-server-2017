package battlecode.contrib.match.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import battlecode.common.MapLocation;
import battlecode.world.signal.*;

public class JSONSignalHandler extends AutoSignalHandler<JSONObject> {

	public JSONObject visitAttackSignal(AttackSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "attack");
			o.put("robot", s.getRobotID());
			o.put("loc", JSONUtils.loc(s.getTargetLoc()));
			o.put("level", s.getTargetHeight());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitAwesomenessSignal(AwesomenessSignal s) {
		return null; // unused
	}

	public JSONObject visitBroadcastSignal(BroadcastSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "broadcast");
			o.put("robot", s.getRobotID());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitBytecodesUsedSignal(BytecodesUsedSignal s) {
		return null; // unused
	}

	public JSONObject visitControlBitsSignal(ControlBitsSignal s) {
		return null; // unused
	}

	public JSONObject visitConvexHullSignal(ConvexHullSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "convexHull");
			o.put("team", s.getTeam().toString());
			JSONArray hulls = new JSONArray();
			for (MapLocation[] locsArr : s.getConvexHulls()) {
				JSONArray locs = new JSONArray();
				for (MapLocation loc : locsArr)
					locs.put(JSONUtils.loc(loc));
				hulls.put(locs);
			}
			o.put("hulls", hulls);
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitDeathSignal(DeathSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "death");
			o.put("robot", s.getObjectID());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitDeploySignal(DeploySignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "deploy");
			o.put("robot", s.getRobotID());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitDoTeleportSignal(DoTeleportSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "doTeleport");
			o.put("robot", s.getRobotID());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitEnergonChangeSignal(EnergonChangeSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "energonChange");
			JSONArray robots = new JSONArray();
			for (int robot : s.getRobotIDs())
				robots.put(robot);
			o.put("robots", robots);
			JSONArray energonAmounts = new JSONArray();
			for (double amount : s.getEnergon())
				energonAmounts.put(amount);
			o.put("energon", energonAmounts);
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitEnergonTransferSignal(EnergonTransferSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "energonTransfer");
			o.put("robot", s.getRobotID());
			o.put("level", s.getTargetHeight());
			o.put("loc", JSONUtils.loc(s.getTargetLoc()));
			o.put("amt", s.getAmount());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitEvolutionSignal(EvolutionSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "evolution");
			o.put("robot", s.getRobotID());
			o.put("type", s.getType().toString());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitFluxChangeSignal(FluxChangeSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "fluxChange");
			JSONArray robots = new JSONArray();
			for (int robot : s.getRobotIDs())
				robots.put(robot);
			o.put("robots", robots);
			JSONArray fluxAmounts = new JSONArray();
			for (double amount : s.getFlux())
				fluxAmounts.put(amount);
			o.put("flux", fluxAmounts);
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitFluxTransferSignal(FluxTransferSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "fluxTransfer");
			o.put("robot", s.getRobotID());
			o.put("level", s.getTargetHeight());
			o.put("loc", JSONUtils.loc(s.getTargetLoc()));
			o.put("amt", s.getAmount());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitIndicatorStringSignal(IndicatorStringSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "indicatorString");
			o.put("robot", s.getRobotID());
			o.put("index", s.getStringIndex());
			o.put("string", s.getNewString());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitLightningShieldSignal(LightningShieldSignal s) {
		return null;
	}

	public JSONObject visitMapOriginSignal(MapOriginSignal s) {
		return null;
	}

	public JSONObject visitMatchObservationSignal(MatchObservationSignal s) {
		return null;
	}

	public JSONObject visitMovementOverrideSignal(MovementOverrideSignal s) {
		return null;
	}

	public JSONObject visitMovementSignal(MovementSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "movement");
			o.put("robot", s.getRobotID());
			o.put("loc", JSONUtils.loc(s.getNewLoc()));
			o.put("forward", s.isMovingForward());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitSetAuraSignal(SetAuraSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "setAura");
			o.put("robot", s.getRobotID());
			o.put("aura", s.getAura().toString());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitSetDirectionSignal(SetDirectionSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "setDirection");
			o.put("robot", s.getRobotID());
			o.put("dir", s.getDirection());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitSpawnSignal(SpawnSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "spawn");
			o.put("robot", s.getRobotID());
			o.put("type", s.getType().toString());
			o.put("parent", s.getParentID());
			o.put("team", s.getTeam().toString());
			o.put("dir", s.getDirection().toString());
			o.put("loc", JSONUtils.loc(s.getLoc()));
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitStartTeleportSignal(StartTeleportSignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "startTeleport");
			o.put("robot", s.getRobotID());
			o.put("loc", JSONUtils.loc(s.getTeleportLoc()));
			o.put("toTeleporter", s.getToTeleporterID());
			o.put("fromTeleporter", s.getFromTeleporterID());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}

	public JSONObject visitUndeploySignal(UndeploySignal s) {
		try {
			JSONObject o = new JSONObject();
			o.put("signal", "undeploy");
			o.put("robot", s.getRobotID());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}
	
}
