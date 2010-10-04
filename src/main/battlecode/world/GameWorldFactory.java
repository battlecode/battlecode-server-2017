package battlecode.world;

import static battlecode.common.GameConstants.MAP_MAX_HEIGHT;
import static battlecode.common.GameConstants.MAP_MAX_WIDTH;
import static battlecode.common.GameConstants.MAP_MIN_HEIGHT;
import static battlecode.common.GameConstants.MAP_MIN_WIDTH;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.ErrorReporter;
import battlecode.engine.PlayerFactory;
import battlecode.world.GameMap.MapProperties;
import battlecode.world.signal.MapOriginSignal;
import battlecode.world.signal.Signal;

/*
TODO:
- make the parser more robust, and with better failure modes
- maybe take out the locations, objects, and terrain nodes
- comments & javadoc
 */
public class GameWorldFactory {
    /**
     * This class is the actual SAX handler that responds
     * to element and character data events.
     */
    private static class XMLMapHandler extends DefaultHandler {

        /** Implements a stack for keeping track of XML elements. */
        private LinkedList<String> xmlStack = new LinkedList<String>();
        /** Stores all the map properties. */
        private int mapWidth, mapHeight;
        private TerrainTile.TerrainType[][] mapTypes = null;
        private int[][] mapHeights = null;
        private InternalTerrainTile[][] mapTiles = null;
        private int[][] blockMap = null;
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
            public boolean isRobot() {
                switch (this) {
                    case SOLDIER:
                    case ARCHON:
                    case CHAINER:
                    case WOUT:
                    case TURRET:
                    case AURA:
                    case TELEPORTER:
                    case COMM:
                        return true;
                    default:
                        return false;
                }
            }
        }

        private static class SymbolData {

            final public SymbolType type;
            final public Team team;
            final public TerrainTile.TerrainType terrainType;
            final public double amount;

            public SymbolData(SymbolType type, Team team, TerrainTile.TerrainType terrainType, double amount) {
                this.type = type;
                this.amount = amount;

                if (team == null)
                    this.team = Team.A;
                else
                    this.team = team;

                if (terrainType == null)
                    this.terrainType = TerrainTile.TerrainType.LAND;
                else
                    this.terrainType = terrainType;
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
                GameWorldFactory.fail("node missing required attribute '" + property + "'", "Check that all the nodes in the map file have all their required attributes.\n");
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
                GameWorldFactory.fail("<" + child + "> allowed only as a child of <" + parent + ">", "Make sure that all nodes in the map file have the right parent nodes, according to the map file specs.\n");
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
                    GameWorldFactory.fail("<map> must be the root element of the map document", "Check that <map> is the root node.\nCheck that <map> occurs nowhere else in the map file.\n");

                // Check the bounds of the map height.
                mapHeight = Integer.parseInt(getRequired(attributes, "height"));
                if (mapHeight < MAP_MIN_HEIGHT || mapHeight > MAP_MAX_HEIGHT)
                    GameWorldFactory.fail("map height '" + mapHeight + "' exceeds limits", "Check that the map file defines a height that is consistent with GameConstants.MAP_MAX_HEIGHT and GameConstants.MAP_MIN_HEIGHT.\n");

                // Check the bounds of the map width.
                mapWidth = Integer.parseInt(getRequired(attributes, "width"));
                if (mapWidth < MAP_MIN_WIDTH || mapWidth > MAP_MAX_WIDTH)
                    GameWorldFactory.fail("map width '" + mapWidth + "' exceeds limits", "Check that the map file defines a width that is consistent with GameConstants.MAP_MAX_WIDTH and GameConstants.MAP_MIN_WIDTH.\n");

                /* String result;
                result = getOptional(attributes,"theme");
                if (result != null) theme = result; */

                // Update the map properties Map.
                mapProperties.put(MapProperties.HEIGHT, mapHeight);
                mapProperties.put(MapProperties.WIDTH, mapWidth);

                // Allocate map tiles based on the width and height.
                mapTiles = new InternalTerrainTile[mapWidth][mapHeight];
                mapTypes = new TerrainTile.TerrainType[mapWidth][mapHeight];
                mapHeights = new int[mapWidth][mapHeight];
                map = new char[mapWidth][mapHeight];
                blockMap = new int[mapWidth][mapHeight];

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
                    GameWorldFactory.fail("invalid 'character' attribute '" + character + "' -- 'character' must have length 1", "Check that all 'character' attributes are just one character.\n");

                String type = getRequired(attributes, "type");
                SymbolType symbolType;
                try {
                    symbolType = SymbolType.valueOf(type);
                } catch (IllegalArgumentException iae) {
                    GameWorldFactory.fail("invalid symbol type '" + type + "'", "Check that all symbol nodes have a type attribute defined in the map file specs.\n");
                    return;
                }

                Team team;
                TerrainTile.TerrainType terrainType;
                double amount;
                String teamString = getOptional(attributes, "team"), terrainString = getOptional(attributes, "terrain"), amountString = getOptional(attributes, "amount");

                if (teamString == null)
                    team = null;
                else {
                    try {
                        team = Team.valueOf(teamString);
                    } catch (IllegalArgumentException iae) {
                        GameWorldFactory.fail("invalid team '" + teamString + "'", "Check that all team attributes are 'A' or 'B'.\n");
                        return;
                    }
                }

                if (terrainString == null)
                    terrainType = null;
                else {
                    try {
                        terrainType = TerrainTile.TerrainType.valueOf(terrainString);
                    } catch (IllegalArgumentException iae) {
                        GameWorldFactory.fail("invalid terrain type '" + terrainString + "'", "Check that all symbol nodes have a terrain attribute defined in the map file specs.\n");
                        return;
                    }
                }

                if (amountString == null)
                    amount = 0.0;
                else {
                    try {
                        amount = Double.valueOf(amountString);
                    } catch (IllegalArgumentException iae) {
                        GameWorldFactory.fail("invalid amount '" + amountString + "'", "Check that all amounts are valid doubles.\n");
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
                GameWorldFactory.fail("unrecognized map element '<" + qName + ">'", "Check that all nodes are spelled correctly.\n");

            // Put this element on the XML element stack.
            xmlStack.addLast(qName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(char[] ch, int start, int length) {

            // Only parse if we're in "data" or "height" -- ignores comments and other junk.
            if (!xmlStack.getLast().equals("data")
                    && !xmlStack.getLast().equals("height")
                    && !xmlStack.getLast().equals("blocks"))
                return;
            if (xmlStack.getLast().equals("height")) {
                //if this is a "height" cdata block, dispatch to the heights() method
                heights(ch, start, length);
                return;
            }

            if (xmlStack.getLast().equals("blocks")) {
                //if this is a "blocks" cdata block, dispatch to the blocks() method
                //blocks(ch, start, length);
                return;
            }

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
                            GameWorldFactory.fail("row " + currentRow + " in <data> has too few characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");
                    }
                    currentRow++;
                    currentCol = 0;
                    continue;
                }
                if (currentRow < 0)
                    fail("spurious character in <data> node", "Check that the first row of map characters starts on the line after <data><![CDATA[ (see example maps).\n");
                if (currentCol >= mapWidth)
                    GameWorldFactory.fail("row " + currentRow + " in <data> has too many characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");
                if (!symbolMap.containsKey(c))
                    GameWorldFactory.fail("unrecognized symbol in map: '" + c + "'", "Check that '" + c + "' is defined as one of the symbols in the map file.\n");

                map[currentCol][currentRow] = c;
                mapTypes[currentCol][currentRow] = symbolMap.get(c).terrainType;
                currentCol++;
            }

            typesHaveBeenSet = true;
            constructMapIfWeHaveEverything();
        }
        private boolean heightsHaveBeenSet = false;
        private boolean blocksHaveBeenSet = false;
        private boolean typesHaveBeenSet = false;

        private void constructMapIfWeHaveEverything() {
            if (heightsHaveBeenSet && typesHaveBeenSet)
                for (int i = 0; i < mapWidth; i++)
                    for (int j = 0; j < mapHeight; j++)
                        mapTiles[i][j] = new InternalTerrainTile(mapHeights[i][j], mapTypes[i][j]);
            /*else if (typesHaveBeenSet && !heightsHaveBeenSet){
            for (int i = 0; i < mapWidth; i++)
            for (int j = 0; j < mapHeight; j++)
            mapTiles[i][j] = TerrainTile.createTerrainTile(0, mapTypes[i][j]);
            }*/
        }

        /**
         * {@inheritDoc}
         */
        public void heights(char[] ch, int start, int length) {

            // Only parse if we're in "data" -- ignores comments and other junk.
            if (!xmlStack.getLast().equals("height"))
                return;

            if (currentRow >= mapHeight) {
                // if we have an extra row at the end, check if it's only whitespace
                for (int i = start; i < length; i++) {
                    // if it isn't whitespace, fail
                    if (!Character.isWhitespace(ch[i]) && ch[i] != '\n')
                        fail("the <height> node has too many rows", "Check that the number of rows is consistent with the 'height' attribute of <map>.\n");
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
                            GameWorldFactory.fail("row " + currentRow + " in <height> has too few characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");
                    }
                    currentRow++;
                    currentCol = 0;
                    continue;
                }
                if (currentRow < 0)
                    fail("spurious character in <height> node", "Check that the first row of map characters starts on the line after <data><![CDATA[ (see example maps).\n");
                if (currentCol >= mapWidth)
                    GameWorldFactory.fail("row " + currentRow + " in <data> has too many characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");

                String s = "" + c;
                int height = -1;
                if (c == '.' || c == ' ') //parse '.' as 0
                    height = 0;
                else
                    try {
                        height = Integer.parseInt(s, 36);
                    } catch (NumberFormatException e) {
                        fail("unusable height in <height> node data", "Check to make sure you only use heights 0-9 only, " + c + " is unsupported right now.");
                    }
                mapHeights[currentCol][currentRow] = height;
                currentCol++;
            }
            heightsHaveBeenSet = true;
            constructMapIfWeHaveEverything();
        }

        /**
         * {@inheritDoc}
         */
        public void blocks(char[] ch, int start, int length) {

            // Only parse if we're in "data" -- ignores comments and other junk.
            if (!xmlStack.getLast().equals("blocks"))
                return;

            if (currentRow >= mapHeight) {
                // if we have an extra row at the end, check if it's only whitespace
                for (int i = start; i < length; i++) {
                    // if it isn't whitespace, fail
                    if (!Character.isWhitespace(ch[i]) && ch[i] != '\n')
                        fail("the <height> node has too many rows", "Check that the number of rows is consistent with the 'height' attribute of <map>.\n");
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
                            GameWorldFactory.fail("row " + currentRow + " in <height> has too few characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");
                    }
                    currentRow++;
                    currentCol = 0;
                    continue;
                }
                if (currentRow < 0)
                    fail("spurious character in <height> node", "Check that the first row of map characters starts on the line after <data><![CDATA[ (see example maps).\n");
                if (currentCol >= mapWidth)
                    GameWorldFactory.fail("row " + currentRow + " in <data> has too many characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");

                String s = "" + c;
                int blocks = 0;
                if (c == '.' || c == ' ') //parse '.' as 0
                    blocks = 0;
                else
                    try {
                        blocks = Integer.parseInt(s, 36);
                    } catch (NumberFormatException e) {
                        fail("unusable # of blocks in <block> node data", "Check to make sure you only use 0-35 blocks only, " + c + " is unsupported right now.");
                    }
                blockMap[currentCol][currentRow] = blocks;
                currentCol++;
            }
            blocksHaveBeenSet = true;
            constructMapIfWeHaveEverything();
        }

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

            GameMap gm = new GameMap(mapProperties, mapTiles, blockMap);
            //gm.setTheme(theme);
            GameWorld gw = new GameWorld(gm, teamA, teamB, archonMemory);

            MapLocation origin = gm.getMapOrigin();

            Signal s = new MapOriginSignal(origin);
            s.accept(gw);

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
                    switch (data.type) {
                        case SOLDIER:
                            PlayerFactory.createPlayer(gw, RobotType.SOLDIER, loc, data.team, null, false);
                            break;
                        case CHAINER:
                            PlayerFactory.createPlayer(gw, RobotType.CHAINER, loc, data.team, null, false);
                            break;
                        case TURRET:
                            PlayerFactory.createPlayer(gw, RobotType.TURRET, loc, data.team, null, false);
                            break;
                        case ARCHON:
                            PlayerFactory.createPlayer(gw, RobotType.ARCHON, loc, data.team, null, false);
                            break;
                        case WOUT:
                            PlayerFactory.createPlayer(gw, RobotType.WOUT, loc, data.team, null, false);
                            break;
                        case COMM:
                            PlayerFactory.createPlayer(gw, RobotType.COMM, loc, data.team, null, false);
                            break;
                        case AURA:
                            PlayerFactory.createPlayer(gw, RobotType.AURA, loc, data.team, null, false);
                            break;
                        case TELEPORTER:
                            PlayerFactory.createPlayer(gw, RobotType.TELEPORTER, loc, data.team, null, false);
                            break;
                        default:
                            break;
                    }
                }
            }
            return gw;
        }
    }

    /**
     * My favoritist method of them all!
     *
     */
    private static void fail(String reason, String thingsToTry) {
        ErrorReporter.report("Malformed map file: " + reason, thingsToTry);
        throw new MapFileError();
    }

    public static GameWorld createGameWorld(String teamA, String teamB, String mapName, String mapPath, long[][] archonMemory) throws Exception {
        // Create a new XMLMapHandler.
        XMLMapHandler handler = new XMLMapHandler();

        // Create a new SAX parser.
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        // Create an input stream from the file.
        if (!mapPath.endsWith("/")) mapPath += "/";
        String fileName = mapPath + mapName + ".xml";
        FileInputStream file;
        try {
            file = new FileInputStream(fileName);
        } catch (Exception e) {
            fail("can't load '" + fileName + "' because of an exception:\n" + e.getMessage(), "Check that the map name is spelled correctly.\nCheck that the map file is located in the right directory.\nCheck that the map file isn't in use by another application.\n");
            return null;
        }

        // Parse the file using the handler.
        parser.parse(file, handler);

        return handler.createGameWorld(teamA, teamB, archonMemory);
    }
    
}
