package battlecode.world;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class Util {

	private Util() {}

	public static class WithinWedge implements Predicate<MapLocation> {

		private double cosHalfTheta;
		private MapLocation loc;
		private int distance;
		private Direction dir;

		public WithinWedge(MapLocation loc, Direction dir, int distance, double cosHalfTheta) {
			this.loc = loc;
			this.dir = dir;
			this.distance = distance;
			this.cosHalfTheta = cosHalfTheta;
		}

		public boolean apply(MapLocation l) {
			if(loc.distanceSquaredTo(l)>distance)
				return false;
			return GameWorld.inAngleRange(loc,dir,l,cosHalfTheta);
		}

	}

	static final Function<InternalObject,MapLocation> objectLocation = new Function<InternalObject,MapLocation>() {
		public MapLocation apply(InternalObject o) {
			return o.getLocation();
		}
	};

	static final Function<BaseComponent,InternalComponent> controllerToComponent = new Function<BaseComponent,InternalComponent>() {
		public InternalComponent apply(BaseComponent c) {
			return c.getComponent();
		}
	};

	static final Predicate<Object> isRobot = Predicates.instanceOf(InternalRobot.class);
	static final Predicate<Object> isComponent = Predicates.instanceOf(InternalComponent.class);

}
