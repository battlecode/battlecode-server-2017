package battlecode.world;

import static battlecode.common.GameConstants.MAP_MAX_HEIGHT;
import static battlecode.common.GameConstants.MAP_MAX_WIDTH;
import static battlecode.common.GameConstants.MAP_MIN_HEIGHT;
import static battlecode.common.GameConstants.MAP_MIN_WIDTH;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.ComponentType;
import battlecode.common.Chassis;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.ErrorReporter;
import battlecode.engine.PlayerFactory;
import battlecode.world.GameMap.MapProperties;
import battlecode.world.signal.EquipSignal;
import battlecode.world.signal.SpawnSignal;
import battlecode.engine.signal.Signal;

/**
 * This class is the actual SAX handler that responds
 * to element and character data events.
 */
class XMLMapHandler extends DefaultHandler {

        /** Implements a stack for keeping track of XML elements. */
        private LinkedList<String> xmlStack = new LinkedList<String>();
        /** Stores all the map properties. */
        private int mapWidth, mapHeight;
        private SymbolData[][] map = null;
        private Map<Character, SymbolData> symbolMap = new HashMap<Character, SymbolData>();

		private interface SymbolData {
			public TerrainTile tile();
			/* Returns {@code true} if createGameObject() does anything. */
			public void createGameObject(GameWorld world, MapLocation loc);
			public boolean equalsMirror(SymbolData data);
		}

		private interface SymbolDataFactory {
			public SymbolData create(Attributes att);
		}

		private static class TerrainData implements SymbolData {

			public static final SymbolDataFactory factory = new SymbolDataFactory() {
				public TerrainData create(Attributes att)
				{
					String type = getRequired(att,"terrain");
					return new TerrainData(TerrainTile.valueOf(type));
				}
			};

			private TerrainTile tile;
			public TerrainData(TerrainTile tile) { this.tile = tile; }
			public TerrainTile tile() { return tile; }
			public void createGameObject(GameWorld world, MapLocation loc) {}
			public boolean equalsMirror(SymbolData data) {
				if (!(data instanceof TerrainData)) return false;
				TerrainData d = (TerrainData) data;
				return d.tile==tile;
			}
			
		}

		private static class RobotData implements SymbolData {

			public static final SymbolDataFactory factory = new SymbolDataFactory() {
				public RobotData create(Attributes att) {
					String stype = getRequired(att,"type");
					Chassis chassis;
					ComponentType [] components;
					if("RECYCLER".equals(stype)) {
						chassis = Chassis.BUILDING;
						components = new ComponentType [] { ComponentType.RECYCLER };
					}
					else if ("CONSTRUCTOR".equals(stype)) {
						chassis = Chassis.LIGHT;
						components = new ComponentType [] { ComponentType.CONSTRUCTOR, ComponentType.SIGHT };
					}
					else {
						chassis = Chassis.valueOf(stype);
						String comp = getOptional(att,"components");
						if(comp == null || comp.isEmpty()) {
							components = new ComponentType [0];
						}
						else {
							String [] s = comp.split(",");
							components = new ComponentType [s.length];
							for(int i=0;i<s.length;i++)
								components[i] = ComponentType.valueOf(s[i]);
						}
					}
					Team team = Team.valueOf(getRequired(att,"team"));
					return new RobotData(chassis,components,team);
				}
			};

			private Chassis type;
			private ComponentType [] components;
			private Team team;
			public RobotData(Chassis type, ComponentType [] components, Team team) {
				this.type = type;
				this.components = components;
				this.team = team;
			}
			public TerrainTile tile() { return TerrainTile.LAND; }
			public void createGameObject(GameWorld world, MapLocation loc) {
				InternalRobot robot = GameWorldFactory.createPlayer(world,type,loc,team,null,false);
				for(ComponentType t : components) {
					if(t==ComponentType.RECYCLER)
						world.createMine(loc);
					world.visitSignal(new EquipSignal(robot,null,t));
				}
			}
			public boolean equalsMirror(SymbolData data) {
				if(!(data instanceof RobotData)) return false;
				RobotData d = (RobotData) data;
				return type==d.type&&Arrays.equals(components,d.components)&&team==d.team.opponent();
			}

		}

		private static class MineData implements SymbolData {
	
			public static final SymbolDataFactory factory = new SymbolDataFactory() { 
				public MineData create(Attributes att) {
					String tm = getOptional(att,"team");
					Team team;
					return new MineData();
				}
			};

			public MineData() {}
			public TerrainTile tile() { return TerrainTile.LAND; }
			public void createGameObject(GameWorld world, MapLocation loc) {
				world.createMine(loc);
			}
			public boolean equalsMirror(SymbolData data) {
				return data instanceof MineData;
			}
		}

		private class SymbolTile {
	
			SymbolData data;
			MapLocation loc;

			public SymbolTile(SymbolData data, MapLocation loc) {
				this.data = data;
				this.loc = loc;
			}

			public void createGameObject(GameWorld world) {
				data.createGameObject(world,loc);
			}

		}

		private static final Map<String, SymbolDataFactory> factories = new HashMap<String, SymbolDataFactory>();
		private final ArrayList<SymbolTile> objectsToCreate = new ArrayList<SymbolTile>(); 

		static {
			factories.put("TERRAIN",TerrainData.factory);
			factories.put("RECYCLER",RobotData.factory);
			factories.put("CONSTRUCTOR",RobotData.factory);
			for(Chassis ch : Chassis.values()) {
				factories.put(ch.name(),RobotData.factory);
			}
			factories.put("MINE",MineData.factory);
		}

        /** Used to pass to the GameMap constructor. */
        private Map<MapProperties, Integer> mapProperties = new HashMap<MapProperties, Integer>();
        private int currentRow = 0;
        private int currentCol = 0;

        /**
         * This method validates a given attribute, returning its value
         * if it is present and failing if it does not.
         *
         * @param attributes the SAX Attributes instance
         * @param property the property to check for
         * @return the String value of the specified attribute
         */
        private static String getRequired(Attributes attributes, String property) {

            String result = getOptional(attributes, property);

            if (result == null) {
                fail("node missing required attribute '" + property + "'", "Check that all the nodes in the map file have all their required attributes.\n");
                return null;
            } else return result;

        }

        /**
         * This method returns the value of an attribute without
         * validating its existence.
         *
         * @param attributes the SAX Attributes instance
         * @param property the property to get the value of
         * @return the String value of the specified attribute, or
         * <code>null</code> if it doesn't exist
         */
        private static String getOptional(Attributes attributes, String property) {

            int propertyIndex = attributes.getIndex(property);
            if (propertyIndex < 0) return null;
            else return attributes.getValue(propertyIndex);

        }

        /**
         * This method validates that an XML element is the
         * current parent element (i.e. is on the top of the
         * stack); if it isn't, it fails.
         *
         * @param qName the name of the parent element to check for
         */
        private void requireElement(String child, String parent) {
            if (!xmlStack.getLast().equals(parent))
                fail("<" + child + "> allowed only as a child of <" + parent + ">", "Make sure that all nodes in the map file have the right parent nodes, according to the map file specs.\n");
        }

        //private String theme = null;
        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            // Separately handle each case of element name.
            if (qName.equals("map")) {

                // Ensure that the <map> element is the document root.
                if (xmlStack.size() > 0)
                    fail("<map> must be the root element of the map document", "Check that <map> is the root node.\nCheck that <map> occurs nowhere else in the map file.\n");

                // Check the bounds of the map height.
                mapHeight = Integer.parseInt(getRequired(attributes, "height"));
                if (mapHeight < MAP_MIN_HEIGHT || mapHeight > MAP_MAX_HEIGHT)
                    fail("map height '" + mapHeight + "' exceeds limits", "Check that the map file defines a height that is consistent with GameConstants.MAP_MAX_HEIGHT and GameConstants.MAP_MIN_HEIGHT.\n");

                // Check the bounds of the map width.
                mapWidth = Integer.parseInt(getRequired(attributes, "width"));
                if (mapWidth < MAP_MIN_WIDTH || mapWidth > MAP_MAX_WIDTH)
                    fail("map width '" + mapWidth + "' exceeds limits", "Check that the map file defines a width that is consistent with GameConstants.MAP_MAX_WIDTH and GameConstants.MAP_MIN_WIDTH.\n");

                /* String result;
                result = getOptional(attributes,"theme");
                if (result != null) theme = result; */

                // Update the map properties Map.
                mapProperties.put(MapProperties.HEIGHT, mapHeight);
                mapProperties.put(MapProperties.WIDTH, mapWidth);

                // Allocate map tiles based on the width and height.
                map = new SymbolData[mapWidth][mapHeight];

            } else if (qName.equals("game")) {

                // Ensure that <game> only occurs under <map>.
                requireElement(qName, "map");

                String result;

                // Fetch some optional attributes and update the map properties.
                result = getOptional(attributes, "seed");
                if (result != null)
                    mapProperties.put(MapProperties.SEED, Integer.parseInt(result));

                result = getOptional(attributes, "rounds");
                if (result != null)
                    mapProperties.put(MapProperties.MAX_ROUNDS, Integer.parseInt(result));

                result = getOptional(attributes, "points");
                if (result != null)
                    mapProperties.put(MapProperties.MIN_POINTS, Integer.parseInt(result));

            } else if (qName.equals("symbols")) {

                requireElement(qName, "map");

            } else if (qName.equals("symbol")) {

                requireElement(qName, "symbols");

                String character = getRequired(attributes, "character");
                if (character.length() != 1)
                    fail("invalid 'character' attribute '" + character + "' -- 'character' must have length 1", "Check that all 'character' attributes are just one character.\n");

                String type = getRequired(attributes, "type");
				SymbolDataFactory factory = factories.get(type);
                if(factory==null) {
                    fail("invalid symbol type '" + type + "'", "Check that all symbol nodes have a type attribute defined in the map file specs.\n");
					return;
                }

                SymbolData data;
				try {
					data = factory.create(attributes);
				} catch(IllegalArgumentException e) {
					fail(e.getMessage(),"Check that all parameters are spelled correctly.");
					return;
				}

                symbolMap.put(character.charAt(0), data);

            } else if (qName.equals("data")) {

                // Ensure that <data> only occurs under <map>.
                requireElement(qName, "map");

                // Reset the row tracker variable.
                currentRow = -1;

                // The actual map data will be parsed by characters()...

            } else {
                //fail("unrecognized map element '<" + qName + ">'", "Check that all nodes are spelled correctly.\n");
			}

            // Put this element on the XML element stack.
            xmlStack.addLast(qName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(char[] ch, int start, int length) {

            // Only parse if we're in "data" or "height" -- ignores comments and other junk.
            if (!xmlStack.getLast().equals("data"))
                return;

            if (currentRow >= mapHeight) {
                // if we have an extra row at the end, check if it's only whitespace
                for (int i = start; i < length; i++) {
                    // if it isn't whitespace, fail
                    if (!Character.isWhitespace(ch[i]) && ch[i] != '\n')
                        fail("the <data> node has too many rows", "Check that the number of rows is consistent with the 'height' attribute of <map>.\n");
                }
                return;		// if it is whitespace, just ignore it
            }

            // Parse each character into TerrainTypes.
            for (int i = start; i < length; i++) {
                char c = ch[i];
                // ignore tabs
                if (c == '\t')
                    continue;
                // if its a newline, update currentRow and currentCol
                if (c == '\n') {
                    if (currentRow != -1) {
                        if (currentCol < mapWidth)
                            fail("row " + currentRow + " in <data> has too few characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");
                    }
                    currentRow++;
                    currentCol = 0;
                    continue;
                }
                if (currentRow < 0)
                    fail("spurious character in <data> node", "Check that the first row of map characters starts on the line after <data><![CDATA[ (see example maps).\n");
                if (currentCol >= mapWidth)
                    fail("row " + currentRow + " in <data> has too many characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");
                if (!symbolMap.containsKey(c))
                    fail("unrecognized symbol in map: '" + c + "'", "Check that '" + c + "' is defined as one of the symbols in the map file.\n");

                map[currentCol][currentRow] = symbolMap.get(c);
                currentCol++;
            }

            typesHaveBeenSet = true;
        }

        private boolean typesHaveBeenSet = false;

        /**
         * {@inheritDoc}
         */
        @Override
        public void endElement(String uri, String localName, String qName) {
            // check that we got enough rows in <data>
            if (qName.equals("data")) {
                if (currentRow < mapHeight)
                    fail("the <data> node has too few rows", "Check that the number of rows is consistent with the 'height' attribute of <map>.\n");
            }

            // Pop an element off the stack.
            xmlStack.removeLast();

        }

        public GameWorld createGameWorld(String teamA, String teamB, long[][] archonMemory) {

            System.out.println("Creating a game%%%%%%%%%");

			TerrainTile [][] mapTiles = new TerrainTile [map.length][];
			for(int i=0;i<map.length;i++) {
				mapTiles[i] = new TerrainTile [map[i].length];
				for(int j=0;j<map[i].length;j++)
					mapTiles[i][j] = map[i][j].tile();
			}

            GameMap gm = new GameMap(mapProperties, mapTiles);
            //gm.setTheme(theme);
            GameWorld gw = new GameWorld(gm, teamA, teamB, archonMemory);

			gw.reserveRandomIDs(100);

            MapLocation origin = gm.getMapOrigin();

			for(int i=0;i<map.length;i++) {
				for(int j=0;j<map[i].length;j++)
					map[i][j].createGameObject(gw,new MapLocation(origin.x+i,origin.y+j));
			}

			gw.endRandomIDs();

            return gw;
        }
	
	/**
     * My favoritist method of them all!
     *
     */
    private static void fail(String reason, String thingsToTry) {
        ErrorReporter.report("Malformed map file: " + reason, thingsToTry);
		RuntimeException e = new IllegalArgumentException();
		//e.printStackTrace();
        throw e;
    }
	
	public boolean isTournamentLegal() {
		boolean asymmetric = false;
		int x, y, mx, my;
		SymbolData d, md;
		// check that the map is symmetric
		for(y = 0, my = mapHeight-1; my>=y; y++, my--)
			for (x = 0, mx = mapWidth-1 ; (my>y)?(mx>=0):(mx>=x); x++, mx--) {
				if(!map[x][y].equalsMirror(map[mx][my])) {
					if(!asymmetric) {
						System.err.println("found asymmetry:");
						asymmetric = true;
					}
					System.err.format("%d,%d does not match %d,%d\n",x,y,mx,my);
				}
			}
		return !asymmetric;
	}

	public static boolean isTournamentLegal(String mapName, String mapPath) {
		System.out.format("checking map %s for legality\n",mapName);
		XMLMapHandler handler;
		try {
			handler = loadMap(mapName,mapPath);
		} catch(IllegalArgumentException e) {
			return false;
		}
		return handler.isTournamentLegal();
	}
	
	public static XMLMapHandler loadMap(String mapName, String mapPath) {
	    // Create a new XMLMapHandler.
        XMLMapHandler handler = new XMLMapHandler();

        // Create a new SAX parser.
        SAXParser parser;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch(Exception e) {
			ErrorReporter.report(e,false);
			return null;
		}

        // Create an input stream from the file.
        if (!mapPath.endsWith("/")) mapPath += "/";
        String fileName = mapPath + mapName + ".xml";
		FileInputStream file;
		try {
        	file = new FileInputStream(fileName);
		}
		catch(FileNotFoundException e) {
			fail("can't load '" + fileName + "' because of an exception:\n" + e.getMessage(), "Check that the map name is spelled correctly.\nCheck that the map file is located in the right directory.\nCheck that the map file isn't in use by another application.\n");
			return null;
		}

        // Parse the file using the handler.
		try {
        	parser.parse(file, handler);
		}
		catch(Exception e) {
			e.printStackTrace();
			fail("can't load '" + fileName + "' beacause of an exception:\n" + e.getMessage(), "Check that the map is valid XML.\n");
			return null;
		}
		return handler;
	}
}

