package battlecode.common;

import java.io.Serializable;

/**
 * Represents persistent data of the terrain of a location on the map.
 * Ephemeral data goes in InternalTerrainTile.
 * This data consists of <br>
 * 1. The type of the terrain (<tt>LAND</tt>, <tt>VOID</tt> or <tt>OFF_MAP</tt>)<br>
 * 2. The height of the terrain (an <tt>int</tt>)
 * 
 */
public class TerrainTile implements Serializable {

    private static final long serialVersionUID = 785608353848029236L;

	public static final int MIN_HEIGHT = 1;
	public static final int MAX_HEIGHT = 31;
    /** The type of terrain in a tile **/
    public enum TerrainType {

        LAND, VOID, OFF_MAP;
    }
    public static final TerrainTile OFF_MAP = new TerrainTile(-1, TerrainType.OFF_MAP);

    /**
     * Determines whether this type of terrain is traversable by
     * objects that move at the specified.
     *
     * @param height the height to use to determine traversability
     * @return true if objects at the given height can traverse this
     * terrain type, false if they can't
     */
    public boolean isTraversableAtHeight(RobotLevel height) {
        switch (type) {
            case OFF_MAP:
                return false;
            case VOID:
                return height == RobotLevel.IN_AIR;
            case LAND:
                return true;
            default:
                throw new RuntimeException("TerrainTile.isTraversibleAtHeight: unknown TerrainType!");
        }
    }
    /** Raw height of this tile **/
    protected final int height;

    /** Raw terrain type of this tile **/
    protected final TerrainType type;
    private final int hashCode;
    protected TerrainTile(TerrainTile tile){
    	this.height = tile.height;
    	this.type = tile.type;
    	this.hashCode = tile.hashCode;
    }

    protected TerrainTile(int height, TerrainType type) {
		if(type==TerrainType.OFF_MAP)
			this.height=-1;
		else if(type==TerrainType.VOID)
			this.height=0;
		else if(height > MAX_HEIGHT)
			this.height = MAX_HEIGHT;
		else
			this.height = height;//= MAX_HEIGHT-height;
		//this.height = height;
        this.type = type;
        if (type.equals(TerrainType.OFF_MAP) && height != -1)
            throw new RuntimeException("battlecode.common.TerrainTile: tried to construct OFF_MAP with a height != -1");
        this.hashCode = type.hashCode() + height;                
    }

    /** Factory method for constructing terrain tiles **/
    public static TerrainTile createTerrainTile(int height, TerrainType type) {
        if (type == TerrainType.OFF_MAP)
            return OFF_MAP;
        else {
            return new TerrainTile(height, type);
        }
    }

    /**
     * Returns the physical height of this terrain tile.
     * Right now, this is only the growth delay
     * @return the height of this terrain tile
     */
    public int getHeight() {
    	return height;
    }

    /**
     * Returns the {@link TerrainType} of this {@link TerrainTile}.
     *
     * @return the {@link TerrainType} of this {@link TerrainTile}.
     */
    public TerrainType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TerrainTile))
            return false;
        else {
            TerrainTile t = (TerrainTile) o;
            return (t.getType() == type && this.height == t.getHeight());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("Type: ");
        sb.append(type.toString());
        sb.append(", ");
        sb.append("Height: ");
        sb.append(height);
        sb.append(']');
        return sb.toString();
    }
}
