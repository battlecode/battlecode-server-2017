package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.server.ErrorReporter;
import battlecode.server.Config;
import battlecode.world.GameMap.MapProperties;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * This class contains the code for reading an XML map file and converting it
 * to a proper GameMap.
 */
public class XMLMapHandler extends DefaultHandler {

    /**
     * An enum to keep track of the two types of terrain that might appear in
     * a map file.
     */
    public enum TerrainType {
        /**
         * Denotes a tile that has a positive number of parts and no rubble.
         */
        PARTS,
        /**
         * Denotes a tile that has no parts and any amount of rubble.
         */
        RUBBLE
    }

    /**
     * Implements a stack for keeping track of XML elements.
     */
    private LinkedList<String> xmlStack = new LinkedList<>();
    /**
     * Contains a SymbolData for each cell on the map.
     */
    private SymbolData[][] map = null;
    /**
     * A mapping from a string to a map element type. These will be used in
     * each XML file to specify the contents of each map cell.
     */
    private Map<String, SymbolData> symbolMap = new HashMap<>();
    /**
     * The width of the map.
     */
    private int mapWidth;
    /**
     * The height of the map.
     */
    private int mapHeight;
    /**
     * The name of the map.
     */
    private String mapName = "";
    /**
     * Used to pass to the GameMap constructor.
     */
    private Map<MapProperties, Integer> mapProperties = new
            HashMap<>();
    /**
     * Zombie spawn schedule for the GameMap constructor.
     */
    private ZombieSpawnSchedule zSchedule = new ZombieSpawnSchedule();

    /**
     * The result of the parsing; created the first time getParsedMap()
     * is called.
     */
    private GameMap resultMap;

    /**
     * Sets the map name.
     *
     * @param mn the map name.
     */
    public void setMapName(String mn) {
        this.mapName = mn;
    }

    /**
     * A class to hold information about a particular map cell.
     */
    private interface SymbolData {
        /**
         * Sets the integer value associated with this cell.
         *
         * @param value the new value for this cell.
         */
        void setValue(double value);

        /**
         * Returns the integer value associated with this cell.
         *
         * @return the integer value associated with this cell.
         */
        double getValue();

        /**
         * Returns the TerrainType associated with this cell.
         *
         * @return the TerrainType associatd with this cell.
         */
        TerrainType tile();

        /**
         * Give the robot that should be placed at a tile for this type of symbol
         * when the game starts.
         *
         * @param originOffsetX the x offset from the origin of the tile
         * @param originOffsetY the y offset from the origin of the tile
         */
        Optional<GameMap.InitialRobotInfo> getRobotAt(int originOffsetX, int originOffsetY);

        /**
         * Returns whether two SymbolDatas are equivalent but represent
         * opposite teams. The opposite of team A is team B (and vice-versa),
         * and the opposite of team ZOMBIE is also team ZOMBIE. This is used
         * to verify that a map is symmetric.
         *
         * @param data the SymbolData to compare this to.
         * @return whether the SymbolData represents a possible counterpart
         * on a symmetric map.
         */
        boolean equalsMirror(SymbolData data);

        /**
         * Returns a copy of this SymbolData.
         *
         * @return a copy of this SymbolData.
         */
        SymbolData copy();
    }

    /**
     * A factory to create symbol data based on XML attributes.
     */
    private interface SymbolDataFactory {

        /**
         * Returns a SymbolData given XML attributes.
         *
         * @param att the XML attributes.
         * @return a SymbolData based on the XML data.
         */
        SymbolData create(Attributes att);
    }

    /**
     * A SymbolData type to represent a general terrain map cell (a cell
     * containing no robots or other game objects).
     */
    private static class TerrainData implements SymbolData {

        /**
         * A factory that returns a TerrainData with the given terrain type.
         */
        public static final SymbolDataFactory factory = att -> {
            String type = getRequired(att, "terrain");
            return new TerrainData(TerrainType.valueOf(type));
        };

        /**
         * Stores the terrain type for this map cell.
         */

        private TerrainType tile;
        /**
         * Stores either the number of parts or the amount of rubble,
         * depending on the terrain type.
         */
        private double value;

        /**
         * Creates a new TerrainData based on a specific terrain.
         * @param tile the terrain to use for this TerrainData.
         */
        public TerrainData(TerrainType tile) {
            this.tile = tile;
            this.value = 0;
        }

        /**
         * Sets the value for this cell.
         *
         * @param value the new value for this cell.
         */
        @Override
        public void setValue(double value) {
            this.value = value;
        }

        /**
         * Returns the value for this cell.
         *
         * @return the value for this cell.
         */
        @Override
        public double getValue() {
            return this.value;
        }

        /**
         * Returns the terrain type for this cell.
         *
         * @return the terrain type for this cell.
         */
        @Override
        public TerrainType tile() {
            return tile;
        }

        /**
         * Returns nothing, because there are no robots on terrain cells.
         */
        @Override
        public Optional<GameMap.InitialRobotInfo> getRobotAt(int originOffsetX, int originOffsetY) {
            return Optional.empty();
        }

        /**
         * Returns whether the other SymbolData is exactly equal to this one.
         *
         * @param data the SymbolData to compare this to.
         * @return whether the other SymbolData is exactly equal to this one.
         */
        @Override
        public boolean equalsMirror(SymbolData data) {
            if (!(data instanceof TerrainData))
                return false;
            TerrainData d = (TerrainData) data;
            return d.tile == tile && d.value == value;
        }

        /**
         * Returns a copy of itself.
         *
         * @return a copy of itself.
         */
        @Override
        public SymbolData copy() {
            TerrainData t = new TerrainData(this.tile);
            t.setValue(this.value);
            return t;
        }
    }

    /**
     * A SymbolData type to represent map cells that contain robots.
     */
    private static class RobotData implements SymbolData {

        /**
         * A factory that produces SymbolData for map cells that contain a
         * specific robot type from a specific team.
         */
        public static final SymbolDataFactory factory = att -> {
            String type = getRequired(att, "type");
            Team team = Team.valueOf(getRequired(att, "team"));
            return new RobotData(RobotType.valueOf(type), team);
        };

        /**
         * The RobotType associated with this map cell.
         */
        public final RobotType type;
        /**
         * The team of the robot associated with this map cell.
         */
        public final Team team;

        /**
         * Creates a new RobotData to represent a map cell containing a robot
         * of a specific type and team.
         *
         * @param type the robot's type.
         * @param team the robot's team.
         */
        public RobotData(RobotType type, Team team) {
            this.type = type;
            this.team = team;
        }

        /**
         * Does nothing.
         *
         * @param value ignored.
         */
        public void setValue(double value) {
        }

        /**
         * Returns 0.
         *
         * @return 0.
         */
        public double getValue() {
            return 0;
        }

        /**
         * Returns the terrain type associated with this map cell (always
         * RUBBLE, because robots do not start on cells with parts).
         * @return RUBBLE.
         */
        public TerrainType tile() {
            return TerrainType.RUBBLE;
        }

        /**
         * Gives a robot of the specific type and team at the
         * given origin offset.
         */
        @Override
        public Optional<GameMap.InitialRobotInfo> getRobotAt(int originOffsetX, int originOffsetY) {
            return Optional.of(
                    new GameMap.InitialRobotInfo(originOffsetX, originOffsetY, type, team)
            );
        }

        /**
         * Returns whether the other map cell represents a robot of the
         * opposite team. The opposite of team A is team B and vice-versa.
         * The opposite of team ZOMBIE is also team ZOMBIE.
         *
         * @param data the SymbolData to compare this to.
         * @return whether the other SymbolData represents a symmetric copy
         * of this one.
         */
        public boolean equalsMirror(SymbolData data) {
            if (!(data instanceof RobotData))
                return false;
            RobotData d = (RobotData) data;
            return type == d.type && team == d.team.opponent();
        }

        /**
         * Returns a copy of itself.
         *
         * @return a copy of itself.
         */
        public SymbolData copy() {
            return new RobotData(this.type, this.team);
        }
    }

    /**
     * A SymbolDataFactory for each possible symbol "type" that can appear in
     * the map file. This will include TERRAIN (cells without units) and all
     * the different robot types.
     */
    private static final Map<String, SymbolDataFactory> factories = new
            HashMap<>();

    static {
        factories.put("TERRAIN", TerrainData.factory);
        for (RobotType ch : RobotType.values()) {
            factories.put(ch.name(), RobotData.factory);
        }
    }
    /**
     * Current row as we are scanning the map data.
     */
    private int currentRow = 0;
    /**
     * Current column as we are scanning the map data.
     */
    private int currentCol = 0;
    /**
     * Stores what has been read of a map cell token so far as we scan the
     * map data.
     */
    private String dataSoFar = "";

    /**
     * This method validates a given attribute, returning its value
     * if it is present and failing if it does not.
     *
     * @param attributes the SAX Attributes instance.
     * @param property   the property to check for.
     * @return the String value of the specified attribute.
     */
    private static String getRequired(Attributes attributes, String property) {
        String result = getOptional(attributes, property);

        if (result == null) {
            fail("XML element in map is missing required attribute '" +
                            property + "'",
                    "Check that all the elements in the map file have all " +
                            "their required attributes.\n");
            return null;
        } else {
            return result;
        }
    }

    /**
     * This method returns the value of an attribute without validating its
     * existence.
     *
     * @param attributes the SAX Attributes instance
     * @param property   the property to get the value of
     * @return the String value of the specified attribute, or
     * <code>null</code> if it doesn't exist
     */
    private static String getOptional(Attributes attributes, String property) {
        int propertyIndex = attributes.getIndex(property);
        if (propertyIndex < 0) {
            return null;
        } else {
            return attributes.getValue(propertyIndex);
        }
    }

    /**
     * This method validates that an XML element is the current parent
     * element (i.e. is on the top of the stack); if it isn't, it fails.
     *
     * @param child the name of the element to check for
     * @param parent the name of the parent element to check for
     */
    private void requireElement(String child, String parent) {
        if (!xmlStack.getLast().equals(parent)) {
            fail("<" + child + "> allowed only as a child of <" + parent +
                    ">", "Make sure that all elements in the map file have " +
                    "the right parent elements, according to the map file " +
                    "specs. \n");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) {
        // Separately handle each case of element name.
        if (qName.equals("map")) {
            // Ensure that the <map> element is the document root.
            if (xmlStack.size() > 0) {
                fail("<map> must be the root element of the map document",
                        "Check that <map> is the root node.\nCheck that <map>" +
                                " occurs nowhere else in the map file.\n");
            }

            if ("false".equals(getOptional(attributes, "constraints"))) {
                // Special map, don't check size constraints.
                mapHeight = Integer.parseInt(getRequired(attributes, "height"));
                mapWidth = Integer.parseInt(getRequired(attributes, "width"));
            } else {
                // Check the bounds of the map height.
                mapHeight = Integer.parseInt(getRequired(attributes, "height"));
                if (mapHeight < GameConstants.MAP_MIN_HEIGHT || mapHeight >
                        GameConstants.MAP_MAX_HEIGHT)
                    fail("map height '" + mapHeight + "' exceeds limits",
                            "Check that the map file defines a height that is" +
                                    " consistent with GameConstants" +
                                    ".MAP_MAX_HEIGHT (" + GameConstants
                                    .MAP_MAX_HEIGHT + ") and GameConstants" +
                                    ".MAP_MIN_HEIGHT (" + GameConstants
                                    .MAP_MIN_HEIGHT + ").\n");

                // Check the bounds of the map width.
                mapWidth = Integer.parseInt(getRequired(attributes, "width"));
                if (mapWidth < GameConstants.MAP_MIN_WIDTH || mapWidth >
                        GameConstants.MAP_MAX_WIDTH)
                    fail("map width '" + mapWidth + "' exceeds limits",
                            "Check that the map file defines a width that is " +
                                    "consistent with GameConstants" +
                                    ".MAP_MAX_WIDTH and GameConstants" +
                                    ".MAP_MIN_WIDTH.\n");
            }

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
                mapProperties.put(MapProperties.ROUNDS, Integer.parseInt
                        (result));
        } else if (qName.equals("zombies")) {
            requireElement(qName, "map");

            String type = getRequired(attributes, "type");
            String count = getRequired(attributes, "count");
            String round = getRequired(attributes, "round");

            int iRound = -1, iCount = -1;
            try {
                iRound = Integer.parseInt(round);
            } catch (NumberFormatException e) {
                fail("invalid numeral found in map file for zombie round " +
                        "number: " + round, "Check that all round numbers are" +
                        " properly formatted integers.\n");
            }
            try {
                iCount = Integer.parseInt(count);
            } catch (NumberFormatException e) {
                fail("invalid numeral found in map file for zombie count " +
                        "number: " + count, "Check that all count numbers are" +
                        " properly formatted integers.\n");
            }
            zSchedule.add(iRound, RobotType.valueOf(type), iCount);
        } else if (qName.equals("symbols")) {
            requireElement(qName, "map");
        } else if (qName.equals("symbol")) {
            requireElement(qName, "symbols");

            String character = getRequired(attributes, "character");

            String type = getRequired(attributes, "type");
            SymbolDataFactory factory = factories.get(type);
            if (factory == null) {
                fail("invalid symbol type '" + type + "'", "Check that all " +
                        "symbol nodes have a type attribute defined in the " +
                        "map file specs.\n");
                return;
            }

            SymbolData data;
            try {
                data = factory.create(attributes);
            } catch (IllegalArgumentException e) {
                fail(e.getMessage(), "Check that all parameters are spelled " +
                        "correctly.");
                return;
            }

            symbolMap.put(character, data);
        } else if (qName.equals("data")) {
            // Ensure that <data> only occurs under <map>.
            requireElement(qName, "map");

            // Reset the row tracker variable.
            currentRow = -1;

            // The actual map data will be parsed by characters()...
        } else {
            fail("unrecognized map element '<" + qName + ">'", "Check that " +
                    "all nodes are spelled correctly.\n");
        }

        // Put this element on the XML element stack.
        xmlStack.addLast(qName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        // Only parse if we're in "data" or "height" -- ignores comments and
        // other junk.
        if (!xmlStack.getLast().equals("data")) {
            return;
        }

        if (currentRow >= mapHeight) {
            // if we have an extra row at the end, check if it's only whitespace
            for (int i = start; i < start + length; i++) {
                // if it isn't whitespace, fail
                if (!Character.isWhitespace(ch[i]) && ch[i] != '\n')
                    fail("the <data> node has too many rows", "Check that the" +
                            " number of rows is consistent with the 'height' " +
                            "attribute of <map>.\n");
            }
            return; // If it is whitespace, just ignore it.
        }

        // Parse each character into TerrainTypes.
        for (int i = start; i < length; i++) {
            char c = ch[i];
            // ignore tabs
            if (c == '\t')
                continue;
            // if it's whitespace, check dataSoFar
            if ((c == '\n' || c == ' ') && dataSoFar.length() > 0) {
                // Each map cell will be represented by a sequence of letters
                // followed by an optional sequence of digits.
                String letters = "";
                int letterIdx = 0;
                while (letterIdx < dataSoFar.length() && Character.isLetter
                        (dataSoFar.charAt(letterIdx))) {
                    letters += dataSoFar.charAt(letterIdx);
                    letterIdx++;
                }

                if (!symbolMap.containsKey(letters))
                    fail("unrecognized symbol in map: '" + letters + "'",
                            "Check " +
                            "that '" + letters + "' is defined as one of the " +
                            "symbols in the map file.\n");

                map[currentCol][currentRow] = symbolMap.get(letters).copy();
                if (dataSoFar.substring(letterIdx).trim().equals("")) {
                    map[currentCol][currentRow].setValue(0);
                } else {
                    map[currentCol][currentRow].setValue(Double.parseDouble(
                            dataSoFar.substring(letterIdx)));
                }

                currentCol++;
                dataSoFar = "";
            } else if (!(c == '\n' || c == ' ')) {
                dataSoFar += c;
            }

            // if it's a newline, update currentRow and currentCol
            if (c == '\n') {
                if (currentRow != -1) {
                    // On some machines, it seems like the initial newline
                    // before the first row of the map cell data gets fed in
                    // twice. This if-statement handles this case, and also
                    // allows people to add arbitrary blank lines between
                    // rows in the map data.
                    if (currentCol == 0 && dataSoFar.equals("")) {
                        continue;
                    }
                    if (currentCol < mapWidth)
                        fail("row " + currentRow + " in <data> has too few " +
                                "characters", "Check that the number of " +
                                "characters in each row is consistent with " +
                                "the 'width' attribute of <map>.\n");
                }
                currentRow++;
                currentCol = 0;
                dataSoFar = "";
                continue;
            }
            if (currentRow < 0)
                fail("spurious character in <data> node", "Check that the " +
                        "first row of map characters starts on the line after" +
                        " <data><![CDATA[ (see example maps).\n");
            if (currentCol >= mapWidth)
                fail("row " + currentRow + " in <data> has too many " +
                        "characters", "Check that the number of characters in" +
                        " each row is consistent with the 'width' attribute " +
                        "of <map>.\n");
        }

        /*
      Whether characters has been called.
     */
        boolean typesHaveBeenSet = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
        // check that we got enough rows in <data>
        if (qName.equals("data")) {
            if (currentRow < mapHeight)
                fail("the <data> node has too few rows", "Check that the " +
                        "number of rows is consistent with the 'height' " +
                        "attribute of <map>.\n");
        }

        // Pop an element off the stack.
        xmlStack.removeLast();
    }

    public GameMap getParsedMap() {
        if (resultMap != null) {
            return resultMap;
        }

        final double[][] rubbleData = new double[map.length][];
        final double[][] partsData = new double[map.length][];
        for (int i = 0; i < map.length; i++) {
            rubbleData[i] = new double[map[i].length];
            partsData[i] = new double[map[i].length];
            for (int j = 0; j < map[i].length; j++) {
                //If a standard tile, use float data as rubble. If a parts
                // tile, use as parts
                if (map[i][j].tile() == TerrainType.PARTS) {
                    rubbleData[i][j] = 0;
                    partsData[i][j] = map[i][j].getValue();
                } else {
                    rubbleData[i][j] = map[i][j].getValue();
                    partsData[i][j] = 0;
                }
            }
        }

        final List<GameMap.InitialRobotInfo> initialRobots = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                final Optional<GameMap.InitialRobotInfo> maybeRobot = map[i][j].getRobotAt(i, j);

                if (maybeRobot.isPresent()) {
                    initialRobots.add(maybeRobot.get());
                }
            }
        }

        resultMap = new GameMap(mapProperties, rubbleData, partsData,
                zSchedule, initialRobots, mapName);

        return resultMap;
    }

    /**
     * Throws a runtime exception and writes an error report.
     *
     * @param reason reason for failure.
     * @param thingsToTry suggestions on how to fix error.
     */
    private static void fail(String reason, String thingsToTry) {
        ErrorReporter.report("Malformed map file: " + reason, thingsToTry);
        throw new IllegalArgumentException();
    }

    /**
     * A class to print warnings to stderr.
     */
    public static class LegalityWarning {
        /**
         * Keeps track of whether any warnings have been created.
         */
        public boolean legal = true;

        /**
         * Prints the warning to stderr.
         * @param s the warning.
         */
        public void warn(String s) {
            System.err.println(s);
            legal = false;
        }

        /**
         * Prints the warning to stderr.
         *
         * @param s the warning with string formatting enabled.
         * @param obj the formatting arguments.
         */
        public void warnf(String s, Object... obj) {
            warn(String.format(s, obj));
        }

        /**
         * Prints a warning about a particular unit to stderr.
         *
         * @param r the unit to warn about.
         */
        public void warnUnit(RobotData r) {
            warn("Illegal unit: " + r);
        }
    }

    /**
     * Returns whether the map is tournament legal. Prints warning statements
     * to stderr. The map checked is whichever one is being processed by this
     * XMLMapHandler.
     *
     * @return whether the map is tournament legal.
     */
    public boolean isTournamentLegal() {
        LegalityWarning warn = new LegalityWarning();

        int x, y;
        SymbolData d;

        // Check for symmetry.
        boolean symA = true, symB = true, symC = mapHeight == mapWidth, symD
                = mapHeight == mapWidth, symE = true;
        for (y = 0; y < mapHeight; y++) {
            for (x = 0; x < mapWidth; x++) {
                symA = symA && (map[x][y].equalsMirror(map[x][mapHeight - y -
                        1]));
                symB = symB && (map[x][y].equalsMirror(map[mapWidth - x -
                        1][y]));
                if (mapWidth == mapHeight) {
                    symC = symC && (map[x][y].equalsMirror(map[mapHeight - y
                            - 1][mapWidth - x - 1]));
                    symD = symD && (map[x][y].equalsMirror(map[y][x]));
                }
                symE = symE && (map[x][y].equalsMirror(map[mapWidth - x -
                        1][mapHeight - y - 1]));
            }
        }
        if (!symA && !symB && !symC && !symD && !symE) {
            warn.warnf("Map is not symmetric in any way!");
        }

        // A tournament map should only start with archons and zombie dens.
        ArrayList<MapLocation> teamAArchons = new ArrayList<>();
        ArrayList<MapLocation> teamBArchons = new ArrayList<>();
        for (y = 0; y < mapHeight; y++) {
            for (x = 0; x < mapWidth; x++) {
                d = map[x][y];
                if (d instanceof RobotData) {
                    RobotData rd = (RobotData) d;
                    switch (rd.type) {
                        case ARCHON:
                            if (rd.team == Team.A)
                                teamAArchons.add(new MapLocation(x, y));
                            else if (rd.team == Team.B)
                                teamBArchons.add(new MapLocation(x, y));
                            break;
                        case ZOMBIEDEN:
                            break;
                        default:
                            warn.warnUnit(rd);
                    }
                }
            }
        }

        if (teamAArchons.size() == 0) {
            warn.warn("No Archons!");
        }

        if (teamAArchons.size() > GameConstants.NUMBER_OF_ARCHONS_MAX) {
            warn.warn("Too many Archons!");
        }

        int rounds = mapProperties.get(MapProperties.ROUNDS);
        if (rounds < GameConstants.ROUND_MIN_LIMIT)
            warn.warn("The round limit is too small.");
        else if (rounds > GameConstants.ROUND_MAX_LIMIT)
            warn.warn("The round limit is too large.");

        return warn.legal;
    }

    /**
     * Checks the specific map for legality. Prints warnings to stderr.
     *
     * @param mapName name of map.
     * @param mapPath path of map.
     * @return whether the map is legal.
     */
    public static boolean isTournamentLegal(String mapName, String mapPath) {
        System.err.format("checking map %s for legality\n", mapName);
        XMLMapHandler handler;
        try {
            handler = loadMap(mapName, mapPath);
        } catch (IllegalArgumentException e) {
            System.err.println("failed to load map");
            return false;
        }
        return handler.isTournamentLegal();
    }

    /**
     * Returns an XMLMapHandler for a specific map.
     *
     * @param mapName name of map.
     * @param mapPath path of map.
     * @return XMLMapHandler for map.
     */
    public static XMLMapHandler loadMap(String mapName, String mapPath) {
        // Create a new XMLMapHandler.
        XMLMapHandler handler = new XMLMapHandler();
        handler.setMapName(mapName);

        // Create a new SAX parser.
        SAXParser parser;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            ErrorReporter.report(e, false);
            return null;
        }

        // Create an input stream from the file.
        if (!mapPath.endsWith("/"))
            mapPath += "/";
        String fileName = mapPath + mapName + ".xml";
        FileInputStream file;
        try {
            file = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fail("can't load '" + fileName + "' because of an exception:\n" +
                    e.getMessage(), "Check that the map name is spelled " +
                    "correctly.\nCheck that the map file is located in the " +
                    "right directory.\nCheck that the map file isn't in use " +
                    "by another application.\n");
            return null;
        }

        // Parse the file using the handler.
        try {
            parser.parse(file, handler);
        } catch (Exception e) {
            e.printStackTrace();
            fail("can't load '" + fileName + "' because of an exception:\n"
                    + e.getMessage(), "Check that the map is valid XML.\n");
            return null;
        }
        return handler;
    }

    /**
     * Checks a set of maps for legality. It will search in bc.game.map-path
     * for the maps, and use the maps whose names are passed in as runtime
     * arguments.
     *
     * @param s name of maps to use.
     */
    public static void main(String[] s) {
        System.out.println("Checking maps for tournament legality...");
        String mapPath = Config.getGlobalConfig().get("bc.game.map-path");
        for (String str : s) {
            isTournamentLegal(str, mapPath);
        }
    }
}
