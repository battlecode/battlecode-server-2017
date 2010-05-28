package battlecode.contrib.match.json;

import org.json.JSONException;
import org.json.JSONObject;

import battlecode.common.MapLocation;
import battlecode.common.TerrainTile.TerrainType;

public class JSONUtils {

	public static JSONObject loc(MapLocation location) throws JSONException {
		try {
			JSONObject o = new JSONObject();
			o.put("x", location.getX());
			o.put("y", location.getY());
			return o;
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static String type(TerrainType type) {
		return type == TerrainType.LAND ? " " : "#";
	}
	
}
