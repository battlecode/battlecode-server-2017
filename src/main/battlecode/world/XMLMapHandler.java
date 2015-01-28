package battlecode.world;

import battlecode.common.*;
import battlecode.engine.ErrorReporter;
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
 * This class is the actual SAX handler that responds
 * to element and character data events.
 */
class XMLMapHandler extends DefaultHandler {

    /**
     * Implements a stack for keeping track of XML elements.
     */
    private LinkedList<String> xmlStack = new LinkedList<String>();
    /**
     * Stores all the map properties.
     */
    private int mapWidth, mapHeight;
    private String mapName = "";
    private SymbolData[][] map = null;
    private Map<Character, SymbolData> symbolMap = new HashMap<Character, SymbolData>();

    private interface SymbolData {

        public void setValue(double value);

        public double floatData();

        public TerrainTile tile();
        /* Returns {@code true} if createGameObject() does anything. */

        public void createGameObject(GameWorld world, MapLocation loc);

        public boolean equalsMirror(SymbolData data);

        public SymbolData copy();
    }

    private interface SymbolDataFactory {

        public SymbolData create(Attributes att);
    }

    private static class TerrainData implements SymbolData {

        public static final SymbolDataFactory factory = new SymbolDataFactory() {

            public TerrainData create(Attributes att) {
                String type = getRequired(att, "terrain");
                return new TerrainData(TerrainTile.valueOf(type));
            }
        };
        private TerrainTile tile;
        private double value;

        public TerrainData(TerrainTile tile) {
            this.tile = tile;
            this.value = -1;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public TerrainTile tile() {
            return tile;
        }

        public double floatData() {
            return this.value;
        }

        public void createGameObject(GameWorld world, MapLocation loc) {
        }

        public boolean equalsMirror(SymbolData data) {
            if (!(data instanceof TerrainData))
                return false;
            TerrainData d = (TerrainData) data;
            return d.tile == tile && d.value == value;
        }

        public SymbolData copy() {
            TerrainData t = new TerrainData(this.tile);
            t.setValue(this.value);
            return t;
        }
    }

    // TODO(axc): we should allow different tiles other than normal
    private static class RobotData implements SymbolData {

        public static final SymbolDataFactory factory = new SymbolDataFactory() {

            public RobotData create(Attributes att) {
                String stype = getRequired(att, "type");
                RobotType chassis;
                chassis = RobotType.valueOf(stype);
                Team team = Team.valueOf(getRequired(att, "team"));
                String smine = getOptional(att, "mine");
                Team mine = smine==null ? null : Team.valueOf(smine);
                return new RobotData(chassis, team, mine);
            }
        };
        public final RobotType type;
        public final Team team;
        public final Team mine;
        public double value;

        public RobotData(RobotType type, Team team, Team mine) {
            this.type = type;
            this.team = team;
            this.mine = mine;
            this.value = -1;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public double floatData() {
            return this.value;
        }

        public TerrainTile tile() {
            return TerrainTile.NORMAL;
        }

        public void createGameObject(GameWorld world, MapLocation loc) {
            InternalRobot robot = GameWorldFactory.createPlayer(world, type, loc, team, null, false, 0);

	    if (this.type == RobotType.TOWER) {
		world.addTower(robot, this.team);
	    }
        }

        public boolean equalsMirror(SymbolData data) {
            if (!(data instanceof RobotData))
                return false;
            RobotData d = (RobotData) data;
            return type == d.type && team == d.team.opponent() && mine == d.mine && d.value == value;
        }

        public String toString() {
            return String.format("%s:%s:%s", type, team, mine);
        }

        public SymbolData copy() {
            RobotData r = new RobotData(this.type, this.team, this.mine);
            r.setValue(this.value);
            return r;
        }
    }

    private static class NodeData implements SymbolData {

        public static final SymbolDataFactory factory = new SymbolDataFactory() {

            public NodeData create(Attributes att) {
                Team team = Team.valueOf(getRequired(att, "team"));
                String smine = getOptional(att, "mine");
                Team mine = smine==null ? null : Team.valueOf(smine);
                return new NodeData(team, mine);
            }
        };

        public final Team team;
        public final Team mine;
        public double value;

        public NodeData(Team t, Team mine) {
            team = t;
            this.mine = mine;
            this.value = -1;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public double floatData() {
            return this.value;
        }

        public TerrainTile tile() {
            return TerrainTile.NORMAL;
        }

        public void createGameObject(GameWorld world, MapLocation loc) {
            if (team == Team.NEUTRAL) {
            } else {
            	InternalRobot r = GameWorldFactory.createPlayer(world, RobotType.HQ, loc, team, null, false, 0);
                world.setHQ(r, team);
            }
        }

        public boolean equalsMirror(SymbolData data) {
            if (!(data instanceof NodeData)) {
                return false;
            }
            NodeData d = (NodeData) data;
            return mine == ((NodeData)data).mine && d.value == value;
        }

        public SymbolData copy() {
            NodeData n = new NodeData(this.team, this.mine);
            n.setValue(this.value);
            return n;
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
            data.createGameObject(world, loc);
        }
    }

    private static final Map<String, SymbolDataFactory> factories = new HashMap<String, SymbolDataFactory>();
    private final ArrayList<SymbolTile> objectsToCreate = new ArrayList<SymbolTile>();

    private ArrayList<MapLocation[]> nodeLinks = new ArrayList<MapLocation[]>();

    static {
        factories.put("TERRAIN", TerrainData.factory);
        for (RobotType ch : RobotType.values()) {
            factories.put(ch.name(), RobotData.factory);
        }
        //factories.put("ENCAMPMENT", NodeData.factory);
        factories.put("HQ", NodeData.factory);
    }

    /**
     * Used to pass to the GameMap constructor.
     */
    private Map<MapProperties, Integer> mapProperties = new HashMap<MapProperties, Integer>();
    private int currentRow = 0;
    private int currentCol = 0;

    public void setMapName(String mn) {
        this.mapName = mn;
    }

    /**
     * This method validates a given attribute, returning its value
     * if it is present and failing if it does not.
     *
     * @param attributes the SAX Attributes instance
     * @param property   the property to check for
     * @return the String value of the specified attribute
     */
    private static String getRequired(Attributes attributes, String property) {

        String result = getOptional(attributes, property);

        if (result == null) {
            fail("node missing required attribute '" + property + "'", "Check that all the nodes in the map file have all their required attributes.\n");
            return null;
        } else
            return result;

    }

    /**
     * This method returns the value of an attribute without
     * validating its existence.
     *
     * @param attributes the SAX Attributes instance
     * @param property   the property to get the value of
     * @return the String value of the specified attribute, or
     *         <code>null</code> if it doesn't exist
     */
    private static String getOptional(Attributes attributes, String property) {

        int propertyIndex = attributes.getIndex(property);
        if (propertyIndex < 0)
            return null;
        else
            return attributes.getValue(propertyIndex);

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
            
            if ("false".equals(getOptional(attributes, "constraints")))
            {
//            	special map, don't check size constraints
                mapHeight = Integer.parseInt(getRequired(attributes, "height"));
                mapWidth = Integer.parseInt(getRequired(attributes, "width"));
            } else
            {
            	// Check the bounds of the map height.
                mapHeight = Integer.parseInt(getRequired(attributes, "height"));
                if (mapHeight < GameConstants.MAP_MIN_HEIGHT || mapHeight > GameConstants.MAP_MAX_HEIGHT)
                    fail("map height '" + mapHeight + "' exceeds limits", "Check that the map file defines a height that is consistent with GameConstants.MAP_MAX_HEIGHT (" + GameConstants.MAP_MAX_HEIGHT + ") and GameConstants.MAP_MIN_HEIGHT (" + GameConstants.MAP_MIN_HEIGHT + ").\n");

                // Check the bounds of the map width.
                mapWidth = Integer.parseInt(getRequired(attributes, "width"));
                if (mapWidth < GameConstants.MAP_MIN_WIDTH || mapWidth > GameConstants.MAP_MAX_WIDTH)
                    fail("map width '" + mapWidth + "' exceeds limits", "Check that the map file defines a width that is consistent with GameConstants.MAP_MAX_WIDTH and GameConstants.MAP_MIN_WIDTH.\n");
            }

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

            //result = getOptional(attributes, "points");
            //if (result != null)
            //    mapProperties.put(MapProperties.MIN_POINTS, Integer.parseInt(result));

        } else if (qName.equals("symbols")) {

            requireElement(qName, "map");

        } else if (qName.equals("symbol")) {

            requireElement(qName, "symbols");

            String character = getRequired(attributes, "character");
            if (character.length() != 1)
                fail("invalid 'character' attribute '" + character + "' -- 'character' must have length 1", "Check that all 'character' attributes are just one character.\n");

            String type = getRequired(attributes, "type");
            SymbolDataFactory factory = factories.get(type);
            if (factory == null) {
                fail("invalid symbol type '" + type + "'", "Check that all symbol nodes have a type attribute defined in the map file specs.\n");
                return;
            }

            SymbolData data;
            try {
                data = factory.create(attributes);
            } catch (IllegalArgumentException e) {
                fail(e.getMessage(), "Check that all parameters are spelled correctly.");
                return;
            }

            symbolMap.put(character.charAt(0), data);

        } else if (qName.equals("data")) {

            // Ensure that <data> only occurs under <map>.
            requireElement(qName, "map");

            // Reset the row tracker variable.
            currentRow = -1;

            // The actual map data will be parsed by characters()...

        } else if (qName.equals("nodelinks")) {
            requireElement(qName, "map");
        } else if (qName.equals("nodelink")) {
            requireElement(qName, "nodelinks");
            MapLocation locA = MapLocation.valueOf(getRequired(attributes, "from"));
            MapLocation locB = MapLocation.valueOf(getRequired(attributes, "to"));
            String onedir = getOptional(attributes, "oneway");
            if (onedir != null)
                nodeLinks.add(new MapLocation[]{locA, locB, null});
            else
                nodeLinks.add(new MapLocation[]{locA, locB});
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
            return;        // if it is whitespace, just ignore it
        }

        // Parse each character into TerrainTypes.
        String dataSoFar = "";
        for (int i = start; i < length; i++) {
            char c = ch[i];
            // ignore tabs
            if (c == '\t')
                continue;
            // if it's whitespace, check dataSoFar
            if ((c == '\n' || c == ' ') && dataSoFar.length() > 0) {
                if (!symbolMap.containsKey(dataSoFar.charAt(0)))
                    fail("unrecognized symbol in map: '" + c + "'", "Check that '" + c + "' is defined as one of the symbols in the map file. DEBUG: '" + dataSoFar + "'\n");

                map[currentCol][currentRow] = symbolMap.get(dataSoFar.charAt(0)).copy();
                if (dataSoFar.substring(1).trim().equals("")) {
                    map[currentCol][currentRow].setValue(0);
                } else {
                    map[currentCol][currentRow].setValue(Double.parseDouble(dataSoFar.substring(1)));
                }

                currentCol++;
                dataSoFar = "";
            } else {
                dataSoFar += c;
            }

            // if it's a newline, update currentRow and currentCol
            if (c == '\n') {
                if (currentRow != -1) {
                    if (currentCol < mapWidth)
                        fail("row " + currentRow + " in <data> has too few characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");
                }
                currentRow++;
                currentCol = 0;
                dataSoFar = "";
                continue;
            }
            if (currentRow < 0)
                fail("spurious character in <data> node", "Check that the first row of map characters starts on the line after <data><![CDATA[ (see example maps).\n");
            if (currentCol >= mapWidth)
                fail("row " + currentRow + " in <data> has too many characters", "Check that the number of characters in each row is consistent with the 'width' attribute of <map>.\n");
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

    public GameWorld createGameWorld(String teamA, String teamB, long[][] teamMemory) {

        System.out.println("Creating a game%%%%%%%%%");

        //if (!isTournamentLegal()) {
            //fail("Map is not legal!", "Fix it.");
        //}

        TerrainTile[][] mapTiles = new TerrainTile[map.length][];
        for (int i = 0; i < map.length; i++) {
            mapTiles[i] = new TerrainTile[map[i].length];
            for (int j = 0; j < map[i].length; j++)
                mapTiles[i][j] = map[i][j].tile();
        }

        int[][] intData = new int[map.length][];
        for (int i = 0; i < map.length; i++) {
            intData[i] = new int[map[i].length];
            for (int j = 0; j < map[i].length; j++) {
                intData[i][j] = (int) map[i][j].floatData();
                if (mapTiles[i][j] == TerrainTile.VOID) {
                    intData[i][j] = 0;
                }
            }
        }

        GameMap gm = new GameMap(mapProperties, mapTiles, intData, mapName);
        GameWorld gw = new GameWorld(gm, teamA, teamB, teamMemory);

        gw.reserveRandomIDs(32000);

        MapLocation origin = gm.getMapOrigin();

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] instanceof NodeData) {
                    map[i][j].createGameObject(gw, new MapLocation(origin.x + i, origin.y + j));
                }
            }
        }
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (!(map[i][j] instanceof NodeData)) {
                    map[i][j].createGameObject(gw, new MapLocation(origin.x + i, origin.y + j));
                }
            }
        }

        // by removing this line, you can no longer use IDs to determine execution order
        //gw.endRandomIDs();

        return gw;
    }

    public void checkNodeLink(MapLocation l) {
        if (!(map[l.x][l.y] instanceof NodeData)) {
            fail(String.format("Nodelink contains %d,%d but there is no node there", l.x, l.y), "Make sure the nodelink data is correct.");
        }
    }

    public boolean isNode(MapLocation l) {
        return map[l.x][l.y] instanceof NodeData;
    }

    /**
     * My favoritist method of them all!
     */
    private static void fail(String reason, String thingsToTry) {
        ErrorReporter.report("Malformed map file: " + reason, thingsToTry);
        RuntimeException e = new IllegalArgumentException();
        //e.printStackTrace();
        throw e;
    }

    class FloodFill {

        Stack<MapLocation> queue;
        Set<MapLocation> marked;
        int n_marked;

        public FloodFill(int x, int y) {
            queue = new Stack<MapLocation>();
            marked = new HashSet<MapLocation>();
            add(x, y);
        }

        public int size() {
            MapLocation loc;
            while (!queue.isEmpty()) {
                loc = queue.pop();
                add(loc.x + 1, loc.y);
                add(loc.x - 1, loc.y);
                add(loc.x, loc.y + 1);
                add(loc.x, loc.y - 1);
                add(loc.x - 1, loc.y + 1);
                add(loc.x - 1, loc.y - 1);
                add(loc.x + 1, loc.y - 1);
                add(loc.x + 1, loc.y + 1);
            }
            return n_marked;
        }

        public void add(int x, int y) {
            if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight)
                return;
            if (map[x][y].tile() != TerrainTile.NORMAL)
                return;
            MapLocation loc = new MapLocation(x, y);
            if (marked.contains(loc))
                return;
            queue.push(loc);
            marked.add(loc);
            n_marked++;
        }

        public boolean reachable(int x, int y) {
            return marked.contains(new MapLocation(x, y));
        }
    }


    public static class LegalityWarning {

        public boolean legal = true;

        public void warn(String s) {
            System.err.println(s);
            legal = false;
        }

        public void warnf(String s, Object... obj) {
            warn(String.format(s, obj));
        }

        public void warnUnit(RobotData r) {
            warn("Illegal unit: " + r);
        }

    }

    public boolean isTournamentLegal() {
        LegalityWarning warn = new LegalityWarning();

        int maxOre = 0;

        int x, y, mx, my;
        SymbolData d, md;
        boolean baseBad = false;
        // check that the map is symmetric
        boolean symA = true, symB = true, symC = mapHeight == mapWidth, symD = mapHeight == mapWidth, symE = true;
        for (y = 0; y < mapHeight; y++) {
            for (x = 0; x < mapWidth; x++) {
                maxOre = Math.max(maxOre, (int) map[x][y].floatData());

                symA = symA && (map[x][y].equalsMirror(map[x][mapHeight - y - 1]));
                symB = symB && (map[x][y].equalsMirror(map[mapWidth - x - 1][y]));
                if (mapWidth == mapHeight) {
                    symC = symC && (map[x][y].equalsMirror(map[mapHeight - y - 1][mapWidth - x - 1]));
                    symD = symD && (map[x][y].equalsMirror(map[y][x]));
                }
                symE = symE && (map[x][y].equalsMirror(map[mapWidth - x - 1][mapHeight - y - 1]));
            }
        }
        if (!symA && !symB && !symC && !symD && !symE) {
            warn.warnf("Map is not symmetric in any way!");
        }

        if (maxOre < 10) {
            warn.warn("The max ore on any tile is less than 10.");
        }

        ArrayList<MapLocation> teamATowers = new ArrayList<MapLocation>();
        ArrayList<MapLocation> teamBTowers = new ArrayList<MapLocation>();

        int grounds = 0, gx = 0, gy = 0;
        int baseAx = -1, baseAy = -1, baseBx = -1, baseBy = -1;
        for (y = 0; y < mapHeight; y++) {
            for (x = 0; x < mapWidth; x++) {
                d = map[x][y];
                if (d instanceof RobotData) {
                    RobotData rd = (RobotData) d;
                    switch (rd.type) {
                        case TOWER:
                            if (rd.team == Team.A) teamATowers.add(new MapLocation(x, y));
                            else if (rd.team == Team.B) teamBTowers.add(new MapLocation(x, y));
                            break;
                        default:
                            warn.warnUnit(rd);
                    }
                } else if (d instanceof NodeData) {
                    if (((NodeData) d).team == Team.A) {
                        if (baseAx != -1) {
                            warn.warn("Team A has more than one HQ.");
                            baseBad = true;
                        } else {
                            baseAx = x;
                            baseAy = y;
                        }
                    } else if (((NodeData) d).team == Team.B) {
                        if (baseBx != -1) {
                            warn.warn("Team B has more than one HQ.");
                            baseBad = true;
                        } else {
                            baseBx = x;
                            baseBy = y;
                        }
                    }
                }
                if (d.tile() == TerrainTile.NORMAL) {
                    grounds++;
                    gx = x;
                    gy = y;
                }
            }
        }

        if (baseAx == -1) {
            baseBad = true;
            warn.warn("The HQs are missing");
        }

        // make sure teams don't have more than 6 towers
        if (teamATowers.size() > GameConstants.NUMBER_OF_TOWERS_MAX) {
            warn.warn("Too many towers!");
        }

        // check that the ground squares are connected
        if (grounds == 0) {
            warn.warn("There are no land squares on the entire map!");
        } else {
            FloodFill ff = new FloodFill(baseAx, baseAy);
            int s = ff.size(); // calculate the flood fill

            if (s != grounds) {
                warn.warn("Some tiles are not reachable!");
            }
        }

        // make sure no two things are in attack range
        MapLocation HQA = new MapLocation(baseAx, baseAy);
        MapLocation HQB = new MapLocation(baseBx, baseBy);
        if (HQA.distanceSquaredTo(HQB) <= GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED) {
            warn.warn("The HQs are too close together.");
        }

        for (MapLocation towerA : teamATowers) {
            for (MapLocation towerB : teamBTowers) {
                if (towerA.distanceSquaredTo(towerB) <= RobotType.TOWER.attackRadiusSquared) {
                    warn.warn("Some towers are too close together.");
                }
                if (towerB.distanceSquaredTo(HQA) <= GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED) {
                    warn.warn("Team A can attack team B towers from the start.");
                }
            }
            if (towerA.distanceSquaredTo(HQB) <= GameConstants.HQ_BUFFED_ATTACK_RADIUS_SQUARED) {
                    warn.warn("Team B can attack team A towers from the start.");
                }
        }

        // TODO: weird cases in which all the NORMAL tiles are connected but not actually reachable because
        // some units are blocking the locations. For example, it's possible to surround an HQ with 6 towers and
        // 2 VOIDs to make it impossible to actually do anything.

        int rounds = mapProperties.get(MapProperties.MAX_ROUNDS);
        if (rounds < GameConstants.ROUND_MIN_LIMIT)
            warn.warn("The round limit is too small.");
        else if (rounds > GameConstants.ROUND_MAX_LIMIT)
            warn.warn("The round limit is too large.");

        return warn.legal;
    }

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
            fail("can't load '" + fileName + "' because of an exception:\n" + e.getMessage(), "Check that the map name is spelled correctly.\nCheck that the map file is located in the right directory.\nCheck that the map file isn't in use by another application.\n");
            return null;
        }

        // Parse the file using the handler.
        try {
            parser.parse(file, handler);
        } catch (Exception e) {
            e.printStackTrace();
            fail("can't load '" + fileName + "' beacause of an exception:\n" + e.getMessage(), "Check that the map is valid XML.\n");
            return null;
        }
        return handler;
    }

    public static void main(String[] s) {
        System.out.println("Checking maps for tournament legality...");
        String mapPath = Config.getGlobalConfig().get("bc.game.map-path");
        for (String str : s) {
            isTournamentLegal(str, mapPath);
        }
    }
}
