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

    public static Predicate<InternalObject> objectWithinDistance(MapLocation loc, int distance) {
        return Predicates.compose(withinDistance(loc, distance), objectLocation);
    }

    public static Predicate<InternalObject> robotWithinDistance(MapLocation loc, int distance) {
        return Predicates.and(isRobot, objectWithinDistance(loc, distance));
    }

    public static Predicate<MapLocation> withinDistance(MapLocation loc, int distance) {
        return new WithinDistance(loc, distance);
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
