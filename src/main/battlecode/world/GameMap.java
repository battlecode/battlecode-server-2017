package battlecode.world;

import java.io.Serializable;

import java.util.*;

import battlecode.common.*;

import battlecode.serial.GenericGameMap;

/**
 * The class represents the map in the game world on which
 * objects interact.
 */
public class GameMap implements GenericGameMap {
	
	private static final long serialVersionUID = -2068896916199851260L;

	/** The default game seed. */
	public static final int GAME_DEFAULT_SEED = 6370;

	/** The default game maxiumum number of rounds. */
	public static final int GAME_DEFAULT_MAX_ROUNDS = 10000;
	
	/** The default game minimum number of points. */
	public static final int GAME_DEFAULT_MIN_POINTS = 5000;
	
	/** The width and height of the map. */    
    private final int mapWidth, mapHeight;

	/** The tiles on the map. */
    private final InternalTerrainTile[][] mapTiles; 

	/** The coordinates of the origin. */
	private final int mapOriginX, mapOriginY;
	
	private int[][] initialBlockMap;
	private int[][] blockMap;

	/** The name of the map theme. */
	private String mapTheme;

	/** The random seed contained in the map file */
	private final int seed;

	/** The maximum number of rounds in the game */
	private final int maxRounds;
	
	/** The minimum number of points needed to win the game */
	private final int minPoints;
	  
	/**
	 * Represents the various integer properties a GameMap
	 * can have.
	 */
	static enum MapProperties {	WIDTH, HEIGHT, SEED, MAX_ROUNDS, THEME, MIN_POINTS }
	
	public GameMap(GameMap gm){
		this.mapWidth = gm.mapWidth;
		this.mapHeight = gm.mapHeight;
		this.mapTiles = new InternalTerrainTile[this.mapWidth][this.mapHeight];
		for(int i = 0; i < this.mapWidth; i++){
			for(int j = 0; j < this.mapHeight; j++){
				this.mapTiles[i][j] = new InternalTerrainTile(gm.mapTiles[i][j]);
			}
			
		}
		this.mapOriginX = gm.mapOriginX;
		this.mapOriginY = gm.mapOriginY;
		this.mapTheme = gm.mapTheme;
		this.seed = gm.seed;
		this.maxRounds = gm.maxRounds;
		this.minPoints = gm.minPoints;
	
	}
	/**
	 * Creates a new GameMap from the given properties, tiles, and territory
	 * locations.
	 *
	 * @param mapProperties a map of MapProperties to their integer values containing dimensions, etc.
	 * @param mapTiles a matrix of TerrainTypes representing the map
	 * @param territoryLocations an array of the MapLocations of the territories
	 */
	GameMap(Map<MapProperties, Integer> mapProperties,  InternalTerrainTile[][] mapTiles, int[][] blockMap) {

		if (mapProperties.containsKey(MapProperties.WIDTH))
			this.mapWidth = mapProperties.get(MapProperties.WIDTH);
		else this.mapWidth = mapTiles[0].length;

		if (mapProperties.containsKey(MapProperties.HEIGHT))
			this.mapHeight = mapProperties.get(MapProperties.HEIGHT);
		else this.mapHeight = mapTiles.length;

		if (mapProperties.containsKey(MapProperties.SEED))
			this.seed = mapProperties.get(MapProperties.SEED);
		else this.seed = GAME_DEFAULT_SEED;

		if (mapProperties.containsKey(MapProperties.MAX_ROUNDS))
			this.maxRounds = mapProperties.get(MapProperties.MAX_ROUNDS);
		else this.maxRounds = GAME_DEFAULT_MAX_ROUNDS;
		
		if (mapProperties.containsKey(MapProperties.MIN_POINTS))
			this.minPoints = mapProperties.get(MapProperties.MIN_POINTS);
		else this.minPoints = GAME_DEFAULT_MIN_POINTS;
		
		Random rand = new Random(this.seed);
		this.mapOriginX = rand.nextInt(65000);
		this.mapOriginY = rand.nextInt(65000);

		this.mapTiles = mapTiles;
		this.blockMap = blockMap;
		
		this.initialBlockMap = new int[blockMap.length][blockMap[0].length];
		for (int i = 0; i < blockMap.length; i++) {
			for (int j = 0; j < blockMap[0].length; j++) {
				initialBlockMap[i][j] = blockMap[i][j];
			}
		}
	}

	public void setTheme(String theme) {
		this.mapTheme = theme;
	}

	/**
	 * Returns the width of this map.
 	 *
	 * @return the width of this map
	 */
	public int getWidth() {
		return mapWidth;
	}

	/**
	 * Returns the height of this map.
	 *
	 * @return the height of this map
	 */
	public int getHeight() {
		return mapHeight;
	}

	/**
	 * Returns the name of the suggested map
	 * theme to use when displaying the map.
	 *
	 * @return the string name of the map theme
	 */
	public String getThemeName() {
		return mapTheme;
	}
	
	public int getMinPoints() {
		return minPoints;
	}

	/**
	 * Determines whether or not the location at the specified 
	 * unshifted coordinates is on the map.
	 * 
	 * @param x the (shifted) x-coordinate of the location
	 * @param y the (shifted) y-coordinate of the location
	 * @return true if the given coordinates are on the map,
	 * false if they're not
	 */
	private boolean onTheMap(int x, int y) {
		return (x >= mapOriginX && y >= mapOriginY && x < mapOriginX + mapWidth && y < mapOriginY + mapHeight);
	}

	/**
	 * Determines whether or not the specified location is on the map.
	 *
	 * @param location the MapLocation to test
	 * @return true if the given location is on the map,
	 * false if it's not
	 */
	public boolean onTheMap(MapLocation location) {
		return onTheMap(location.getX(), location.getY());
	}

	/**
	 * Determines the type of the terrain on the map at the
	 * given location.
 	 *
	 * @param location the MapLocation to test
	 * @return the TerrainTile at the given location
	 * of the map, and TerrainTile.OFF_MAP if the given location is
	 * off the map.
	 */
	public TerrainTile getTerrainTile(MapLocation location) {
		if (!onTheMap(location))
			return TerrainTile.OFF_MAP;

		return mapTiles[location.getX() - mapOriginX][location.getY() - mapOriginY];
	}

	public int getFlux(MapLocation location) {
		if (!onTheMap(location))
			return 0;

		return mapTiles[location.getX() - mapOriginX][location.getY() - mapOriginY].getFlux();
	}

	public int mineFlux(MapLocation location) {
		if (!onTheMap(location))
			return 0;

		return mapTiles[location.getX() - mapOriginX][location.getY() - mapOriginY].mineFlux();
	}
	
	public int getLocationHeight(MapLocation location) {
		if (!onTheMap(location))
			return 0;
		
		return getTerrainTile(location).getHeight() + getNumBlocks(location);
	}
	
	/**
	 * Determines the number of blocks at the given location.
 	 *
	 * @param location the MapLocation at which to look
	 * @return an integer representing the number of blocks
	 * at the given location. Will return 0 if the given location is
	 * off the map
	 */
	public int getNumBlocks(MapLocation location) {
		if (!onTheMap(location))
			return 0;
		
		return blockMap[location.getX() - mapOriginX][location.getY() - mapOriginY];
	}
	
	/**
	 * Determines the block delta at the given location.
 	 *
	 * @param location the MapLocation at which to look
	 * @return an integer representing the block delta
	 * at the given location. Will return 0 if the given location is
	 * off the map
	 */
	public int getBlockDelta(MapLocation location) {
		if (!onTheMap(location))
			return 0;
		
		int x = location.getX() - mapOriginX;
		int y = location.getY() - mapOriginY;
		
		return blockMap[x][y] - initialBlockMap[x][y];
	}
	
	/**
	 * Removes a block from the specified location. Does not
	 * do any checking to ensure non-negative values.
 	 *
	 * @param location the MapLocation at which to look
	 */
	public void removeBlock(MapLocation location) {
		if (!onTheMap(location))
			return;
		
		blockMap[location.getX() - mapOriginX][location.getY() - mapOriginY]--;
	}
	
	/**
	 * Adds a block to the specified location. Does not
	 * do any checking to ensure non-negative values.
 	 *
	 * @param location the MapLocation at which to look
	 */
	public void addBlock(MapLocation location) {
		if (!onTheMap(location))
			return;
		
		blockMap[location.getX() - mapOriginX][location.getY() - mapOriginY]++;
	}

	/**
	 * Returns a two-dimensional array of terrain data for this map.
	 * 
	 * @return the map's terrain in a 2D array
	 */
	public InternalTerrainTile[][] getTerrainMatrix() {
		return mapTiles;
	}
	
	/**
	 * Returns a two-dimensional array of block heights for this map.
	 * 
	 * @return the block map's terrain in a 2D array
	 */
	public int[][] getBlockMapMatrix() {
		return blockMap;
	}
	
	/**
	 * Returns a two-dimensional array of the initial block heights for this map.
	 * 
	 * @return the block map's terrain in a 2D array
	 */
	public int[][] getInitialBlockMapMatrix() {
		return initialBlockMap;
	}

	/**
	 * Gets the maximum number of rounds for this game.
	 * 
 	 * @return the maximum number of rounds for this game
	 */
	public int getMaxRounds() {
		return maxRounds + (int)Math.round(1.0 / GameConstants.POINTS_DECREASE_PER_ROUND_FACTOR);
	}

        public int getStraightMaxRounds(){
            return maxRounds;
        }
	
	public int getSeed() {
		return seed;
	}
	
	/**
	 * Gets the origin (i.e., upper left corner) of the map
	 *
	 * @return the origin of the map
	 */
	public MapLocation getMapOrigin() {
		return new MapLocation(mapOriginX, mapOriginY);
	}
	
	
	public static class MapMemory {
		
		// should be ge the max of all robot sensor ranges
		private final static int BUFFER = 11;
		
		private final boolean data[][];
		private final GameMap map;
		private final int Xwidth;
		private final int Ywidth;
		
		public MapMemory(GameMap map) {
			this.map = map;
			Xwidth = map.mapWidth+(2*BUFFER);
			Ywidth = map.mapHeight+(2*BUFFER);
			data = new boolean[Xwidth][Ywidth];
		}
		
		public void rememberLocations(MapLocation loc, int[] offsetsX, int[] offsetsY) {
			int X = loc.getX() - map.mapOriginX+BUFFER;
			int Y = loc.getY() - map.mapOriginY+BUFFER;
			
			for(int i = 0; i < offsetsX.length; i++) {
				data[X+offsetsX[i]][Y+offsetsY[i]] = true;
			}
		}
		
		public TerrainTile recallTerrain(MapLocation loc) {
			int X = loc.getX() - map.mapOriginX + BUFFER;
			int Y = loc.getY() - map.mapOriginY + BUFFER;
			
			if(X >= 0 && X < Xwidth && Y >= 0 && Y < Ywidth && data[X][Y])
				return map.getTerrainTile(loc);
			else
				return null;
		}
		
		
	}

	public static int [][] computeOffsets360(int radiusSquared) {
		int [] XOffsets = new int [400];
		int [] YOffsets = new int [400];
		int nOffsets = 0;
		for (int y = 0; y*y <= radiusSquared; y++) {
			XOffsets[nOffsets]=0;
			YOffsets[nOffsets]=y;
			nOffsets++;
			if(y>0) {
				XOffsets[nOffsets]=0;
				YOffsets[nOffsets]=-y;
				nOffsets++;
			}
			for (int x = 1; x*x + y*y <= radiusSquared; x++) {
				MapLocation loc = new MapLocation(x, y);
				XOffsets[nOffsets]=x;
				YOffsets[nOffsets]=y;
				nOffsets++;
				XOffsets[nOffsets]=-x;
				YOffsets[nOffsets]=y;
				nOffsets++;
				if(y>0) {
					XOffsets[nOffsets]=x;
					YOffsets[nOffsets]=-y;
					nOffsets++;
					XOffsets[nOffsets]=-x;
					YOffsets[nOffsets]=-y;
					nOffsets++;
				}
			}
		}
		return new int [][] { Arrays.copyOf(XOffsets,nOffsets), Arrays.copyOf(YOffsets,nOffsets) };
	}

	public static Map<ComponentType,int[][][]> computeVisibleOffsets() {
		final int MAX_RANGE = 6;
		final MapLocation CENTER = new MapLocation(0, 0);
		Map<ComponentType,int[][][]> offsets = new EnumMap<ComponentType,int[][][]> (ComponentType.class);
		int [][][] offsetsForType;
		int [] XOffsets = new int [169];
		int [] YOffsets = new int [169];
		int nOffsets;
		for (ComponentType type : ComponentType.values()) {
			offsetsForType = new int [9][][];
			offsets.put(type,offsetsForType);
			if((type.angle >= 360.0)) {
				// Same range of vision independent of direction;
				// save memory by using the same array each time
				int [][] tmpOffsets = computeOffsets360(type.range);
				for (int i = 0; i < 8; i ++) {
					offsetsForType[i] = tmpOffsets;
				}
			}
			else {
				for (int i = 0; i < 8; i ++) {
					Direction dir = Direction.values()[i];
					nOffsets = 0;
					for (int y = -MAX_RANGE; y <= MAX_RANGE; y++) {
						for (int x = -MAX_RANGE; x <= MAX_RANGE; x++) {
							MapLocation loc = new MapLocation(x, y);
							if (CENTER.distanceSquaredTo(loc) <= type.range
								&& GameWorld.inAngleRange(CENTER, dir, loc, type.cosHalfAngle)) {
								XOffsets[nOffsets]=x;
								YOffsets[nOffsets]=y;
								nOffsets++;
							}
						}
					}
					
					offsetsForType[i] = new int [][] { Arrays.copyOf(XOffsets,nOffsets), Arrays.copyOf(YOffsets,nOffsets) };
					
				}
			}
		}
		return offsets;
    }
	
}
