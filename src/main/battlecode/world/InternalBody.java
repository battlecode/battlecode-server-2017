package battlecode.world;

import battlecode.common.MapLocation;

public abstract class InternalBody {
    private final int ID;
    private final GameWorld gameWorld;
    private final double radius;
    private MapLocation location;
    private byte team;
    
    public InternalBody(int ID, MapLocation location, GameWorld gameWorld, double radius, byte team) {
        this.ID = ID;
        this.location = location;
        this.gameWorld = gameWorld;
        this.radius = radius;
        this.team = team;
    }
    
    public int getID() {
        return ID;
    }
    
    public MapLocation getLocation() {
        return location;
    }
    
    public GameWorld getGameWorld() {
        return gameWorld;
    }
    
    public double getRadius () {
        return radius;
    }
    
    public byte getTeam() {
        return team;
    }
    
    public boolean isNeutral() {
        return team == 0;
    }
    
    public boolean exists() {
        return gameWorld.exists(this);
    }
    
    @Override
    public boolean equals(Object o) {
        return o != null && (o instanceof InternalBody)
                && ((InternalRobot) o).getID() == getID();
    }
    
    @Override
    public int hashCode() {
        return ID;
    }
}
