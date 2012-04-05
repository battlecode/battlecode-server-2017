package battlecode.world;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Team;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class Util {

    private Util() {
    }

    private static class WithinDistance implements Predicate<MapLocation> {

        private MapLocation loc;
        private int distance;

        public WithinDistance(MapLocation loc, int distance) {
            this.loc = loc;
            this.distance = distance;
        }

        public boolean apply(MapLocation l) {
            return l.distanceSquaredTo(loc) <= distance;
        }
    }

    private static class WithinAngle implements Predicate<MapLocation> {

        private double cosHalfTheta;
        private MapLocation loc;
        private Direction dir;

        public WithinAngle(MapLocation loc, Direction dir, double cosHalfTheta) {
            this.loc = loc;
            this.dir = dir;
            this.cosHalfTheta = cosHalfTheta;
        }

        public boolean apply(MapLocation l) {
            return GameWorld.inAngleRange(loc, dir, l, cosHalfTheta);
        }
    }

    public static Predicate<InternalObject> objectWithinDistance(MapLocation loc, int distance) {
        return Predicates.compose(withinDistance(loc, distance), objectLocation);
    }

    public static Predicate<InternalObject> robotWithinDistance(MapLocation loc, int distance) {
        return Predicates.and(isRobot, objectWithinDistance(loc, distance));
    }

    public static Predicate<MapLocation> withinDistance(MapLocation loc, int distance) {
        return new WithinDistance(loc, distance);
    }

    public static Predicate<MapLocation> withinAngle(MapLocation loc, Direction dir, double cosHalfTheta) {
        if (cosHalfTheta <= -.99999)
            return Predicates.alwaysTrue();
        else
            return new WithinAngle(loc, dir, cosHalfTheta);
    }

    public static Predicate<MapLocation> withinWedge(MapLocation loc, int range, Direction dir, double cosHalfTheta) {
        return Predicates.and(withinDistance(loc, range), withinAngle(loc, dir, cosHalfTheta));
    }

    static final Function<InternalObject, MapLocation> objectLocation = new Function<InternalObject, MapLocation>() {

        public MapLocation apply(InternalObject o) {
            return o.getLocation();
        }
    };

    public static Predicate<InternalObject> isAllied(final Team tm) {
        return new Predicate<InternalObject>() {

            public boolean apply(InternalObject o) {
                return o.getTeam() == tm;
            }
        };
    }

    static final Predicate<Object> isRobot = Predicates.instanceOf(InternalRobot.class);
}
