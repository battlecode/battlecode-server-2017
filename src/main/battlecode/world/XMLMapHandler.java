package battlecode.world;

import static battlecode.common.GameConstants.MAP_MAX_HEIGHT;
import static battlecode.common.GameConstants.MAP_MAX_WIDTH;
import static battlecode.common.GameConstants.MAP_MIN_HEIGHT;
import static battlecode.common.GameConstants.MAP_MIN_WIDTH;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
import battlecode.common.Chassis;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.ErrorReporter;
import battlecode.engine.PlayerFactory;
import battlecode.world.GameMap.MapProperties;
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
        private TerrainTile[][] mapTiles = null;
        private char[][] map = null;
        private Map<Character, SymbolData> symbolMap = new HashMap<Character, SymbolData>();

        private static enum SymbolType {

            TERRAIN,
            DEPOSIT,
            SOLDIER,
            ARCHON,
            CHAINER,
            WOUT,
            TURRET,
            AURA,
            TELEPORTER,
            COMM;

			private final Chassis myChassis;

			SymbolType() {
				Chassis type;
				try {
					type = Enum.valueOf(Chassis.class, name());
				} catch(IllegalArgumentException e) {
					type = null;
				}
				myChassis = type;
			}

			public Chassis getChassis() {
				return myChassis;
			}

            public boolean isRobot() {
				return myChassis!=null;
            }
        }

        private static class SymbolData {

            final public SymbolType type;
            final public Team team;
            final public TerrainTile terrainType;
            final public double amount;

            public SymbolData(SymbolType type, Team team, TerrainTile terrainType, double amount) {
                this.type = type;
                this.amount = amount;

                if (team == null)
                    this.team = Team.NEUTRAL;
                else
                    this.team = team;

                if (terrainType == null)
                    this.terrainType = TerrainTile.LAND;
                else
                    this.terrainType = terrainType;
            }

			public boolean isMirrorOf(SymbolData data) {
				return type == data.type && team == data.team.opponent() && terrainType == data.terrainType && amount == data.amount;
			}
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
        private String getRequired(Attributes attributes, String property) {

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
        private String getOptional(Attributes attributes, String property) {

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
                mapTiles = new TerrainTile[mapWidth][mapHeight];
                map = new char[mapWidth][mapHeight];

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
                SymbolType symbolType;
                try {
                    symbolType = SymbolType.valueOf(type);
                } catch (IllegalArgumentException iae) {
                    fail("invalid symbol type '" + type + "'", "Check that all symbol nodes have a type attribute defined in the map file specs.\n");
                    return;
                }

                Team team;
                TerrainTile terrainType;
                double amount;
                String teamString = getOptional(attributes, "team"), terrainString = getOptional(attributes, "terrain"), amountString = getOptional(attributes, "amount");

                if (teamString == null)
                    team = null;
                else {
                    try {
                        team = Team.valueOf(teamString);
                    } catch (IllegalArgumentException iae) {
                        fail("invalid team '" + teamString + "'", "Check that all team attributes are 'A' or 'B'.\n");
                        return;
                    }
                }

                if (terrainString == null)
                    terrainType = null;
                else {
                    try {
                        terrainType = TerrainTile.valueOf(terrainString);
                    } catch (IllegalArgumentException iae) {
                        fail("invalid terrain type '" + terrainString + "'", "Check that all symbol nodes have a terrain attribute defined in the map file specs.\n");
                        return;
                    }
                }

                if (amountString == null)
                    amount = 0.0;
                else {
                    try {
                        amount = Double.valueOf(amountString);
                    } catch (IllegalArgumentException iae) {
                        fail("invalid amount '" + amountString + "'", "Check that all amounts are valid doubles.\n");
                        return;
                    }
                }

                SymbolData data = new SymbolData(symbolType, team, terrainType, amount);

                symbolMap.put(character.charAt(0), data);

            } else if (qName.equals("data")) {

                // Ensure that <data> only occurs under <map>.
                requireElement(qName, "map");

                // Reset the row tracker variable.
                currentRow = -1;

                // The actual map data will be parsed by characters()...

            } else if (qName.equals("height")) {
                //Ensure that <height> only occurs under <map>
                requireElement(qName, "map");

                currentRow = -1;

                // The actual height data gets parsed by heights()

            } else if (qName.equals("blocks")) {
                //Ensure that <blocks> only occurs under <map>
                requireElement(qName, "map");

                currentRow = -1;

                // The actual height data gets parsed by blocks()

            } else
                fail("unrecognized map element '<" + qName + ">'", "Check that all nodes are spelled correctly.\n");

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

                map[currentCol][currentRow] = c;
                mapTiles[currentCol][currentRow] = symbolMap.get(c).terrainType;
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

        private static class MapCoordinate {

            public int x;
            public int y;

            public MapCoordinate(int x, int y) {
                this.x = x;
                this.y = y;
            }
        }

        public GameWorld createGameWorld(String teamA, String teamB, long[][] archonMemory) {

            System.out.println("Creating a game%%%%%%%%%");

            GameMap gm = new GameMap(mapProperties, mapTiles);
            //gm.setTheme(theme);
            GameWorld gw = new GameWorld(gm, teamA, teamB, archonMemory);

            MapLocation origin = gm.getMapOrigin();

            ArrayList<MapCoordinate> robotCoordinates = new ArrayList<MapCoordinate>();
            for (int j = 0; j < mapHeight; j++) {
                for (int i = 0; i < mapWidth; i++) {
                    SymbolType symbolType = symbolMap.get(map[i][j]).type;
                    if (symbolType.isRobot()) {
                        robotCoordinates.add(new MapCoordinate(i, j));
                    }
                }
            }

            //Pad initial IDs up to 100
            for (int i = 100 - robotCoordinates.size(); i > 0; i--) {
                robotCoordinates.add(null);
            }

            // Shuffle order we create Robots
            Collections.shuffle(robotCoordinates, gw.getRandGen());

            for (MapCoordinate coordinate : robotCoordinates) {
                if (coordinate == null) {
                    gw.nextID(); // Allocate next ID to no one
                } else {
                    MapLocation loc = new MapLocation(origin.getX() + coordinate.x, origin.getY() + coordinate.y);
                    SymbolData data = symbolMap.get(map[coordinate.x][coordinate.y]);
					GameWorldFactory.createPlayer(gw,data.type.getChassis(),loc,data.team,null,false);
                }
            }
            return gw;
        }
	
	/**
     * My favoritist method of them all!
     *
     */
    private static void fail(String reason, String thingsToTry) {
        ErrorReporter.report("Malformed map file: " + reason, thingsToTry);
        throw new IllegalArgumentException();
    }
	
	public boolean isTournamentLegal() {
		boolean asymmetric = false;
		int x, y, mx, my;
		SymbolData d, md;
		// check that the map is symmetric
		for(y = 0, my = mapHeight-1; my>=y; y++, my--)
			for (x = 0, mx = mapWidth-1 ; (my>y)?(mx>=0):(mx>=x); x++, mx--) {
				d = symbolMap.get(map[x][y]);
				md = symbolMap.get(map[mx][my]);
				if(!d.isMirrorOf(md)) {
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
			fail("can't load '" + fileName + "' beacause of an exception:\n" + e.getMessage(), "Check that the map is valid XML.\n");
			return null;
		}
		return handler;
	}
}

