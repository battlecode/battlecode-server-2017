package battlecode.util;

import battlecode.common.RobotType;
import battlecode.schema.BodyType;

/**
 * TODO document
 *
 * @author james
 */
public class FlatHelpers {
    public static RobotType getRobotTypeFromBodyType(byte bodyType) {
        switch (bodyType) {
            case BodyType.ARCHON:
                return RobotType.ARCHON;
            case BodyType.GARDENER:
                return RobotType.GARDENER;
            case BodyType.LUMBERJACK:
                return RobotType.LUMBERJACK;
            case BodyType.RECRUIT:
                return RobotType.RECRUIT;
            case BodyType.SCOUT:
                return RobotType.SCOUT;
            case BodyType.SOLDIER:
                return RobotType.SOLDIER;
            case BodyType.TANK:
                return RobotType.TANK;
            default:
                throw new RuntimeException("No robot type for: "+bodyType);
        }
    }

    public static byte getBodyTypeFromRobotType(RobotType type) {
        switch (type) {
            case ARCHON:
                return BodyType.ARCHON;
            case GARDENER:
                return BodyType.GARDENER;
            case LUMBERJACK:
                return BodyType.LUMBERJACK;
            case RECRUIT:
                return BodyType.RECRUIT;
            case SCOUT:
                return BodyType.SCOUT;
            case SOLDIER:
                return BodyType.SOLDIER;
            case TANK:
                return BodyType.TANK;
            default:
                throw new RuntimeException("No body type for: "+type);
        }
    }
}
