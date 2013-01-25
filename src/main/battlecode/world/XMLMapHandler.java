package battlecode.world;

import battlecode.common.*;
import battlecode.engine.ErrorReporter;
import battlecode.server.Config;
import battlecode.world.GameMap.MapProperties;
import battlecode.world.signal.MineSignal;
import battlecode.world.signal.NodeBirthSignal;
import battlecode.world.signal.NodeConnectionSignal;
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

            public TerrainData create(Attributes att) {
                String type = getRequired(att, "terrain");
                return new TerrainData(TerrainTile.valueOf(type));
            }
        };
        private TerrainTile tile;

        public TerrainData(TerrainTile tile) {
            this.tile = tile;
        }

        public TerrainTile tile() {
            return tile;
        }

        public void createGameObject(GameWorld world, MapLocation loc) {
        }

        public boolean equalsMirror(SymbolData data) {
            if (!(data instanceof TerrainData))
                return false;
            TerrainData d = (TerrainData) data;
            return d.tile == tile;
        }
    }
    
    private static class MineData implements SymbolData {

        public static final SymbolDataFactory factory = new SymbolDataFactory() {

            public MineData create(Attributes att) {
                String type = getRequired(att, "team");
                return new MineData(Team.valueOf(type));
            }
        };
        private Team team;

        public MineData(Team t) {
            this.team = t;
        }

        public TerrainTile tile() {
            return TerrainTile.LAND;
        }

        public void createGameObject(GameWorld world, MapLocation loc) {
        	world.addMine(team, loc);
        	world.addSignal(new MineSignal(loc, team, true));
        }

        public boolean equalsMirror(SymbolData data) {
            if (!(data instanceof MineData))
                return false;
            MineData d = (MineData) data;
            return d.team == team;
        }
    }

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

        public RobotData(RobotType type, Team team, Team mine) {
            this.type = type;
            this.team = team;
            this.mine = mine;
        }

        public TerrainTile tile() {
            return TerrainTile.LAND;
        }

        public void createGameObject(GameWorld world, MapLocation loc) {
            InternalRobot robot = GameWorldFactory.createPlayer(world, type, loc, team, null, false);
            if (mine!=null)
            {
            	world.addMine(mine, loc);
            	world.addSignal(new MineSignal(loc, mine, true));
            }
        }

        public boolean equalsMirror(SymbolData data) {
            if (!(data instanceof RobotData))
                return false;
            RobotData d = (RobotData) data;
            return type == d.type && team == d.team.opponent() && mine == d.mine;
        }

        public String toString() {
            return String.format("%s:%s:%s", type, team, mine);
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

        public NodeData(Team t, Team mine) {
            team = t;
            this.mine = mine;
        }

        public TerrainTile tile() {
            return TerrainTile.LAND;
        }

        public void createGameObject(GameWorld world, MapLocation loc) {
            if (team == Team.NEUTRAL) {
//            	new InternalRobot(world, RobotType.ENCAMPMENT, loc, Team.NEUTRAL, false);
//            	new InternalEncampment(world, loc);
            	world.visitSignal(new NodeBirthSignal(loc));
            } else {
            	InternalRobot r = GameWorldFactory.createPlayer(world, RobotType.HQ, loc, team, null, false);
                world.setHQ(r, team);
            }
            if (mine!=null)
            {
            	world.addMine(mine, loc);
            	world.addSignal(new MineSignal(loc, mine, true));
            }
        }

        public boolean equalsMirror(SymbolData data) {
            return data instanceof NodeData && mine == ((NodeData)data).mine;
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
        factories.put("ENCAMPMENT", NodeData.factory);
        factories.put("HQ", NodeData.factory);
        factories.put("MINE", MineData.factory);
    }

    /**
     * Used to pass to the GameMap constructor.
     */
    private Map<MapProperties, Integer> mapProperties = new HashMap<MapProperties, Integer>();
    private int currentRow = 0;
    private int currentCol = 0;

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
                    fail("map height '" + mapHeight + "' exceeds limits", "Check that the map file defines a height that is consistent with GameConstants.MAP_MAX_HEIGHT and GameConstants.MAP_MIN_HEIGHT.\n");

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

        TerrainTile[][] mapTiles = new TerrainTile[map.length][];
        for (int i = 0; i < map.length; i++) {
            mapTiles[i] = new TerrainTile[map[i].length];
            for (int j = 0; j < map[i].length; j++)
                mapTiles[i][j] = map[i][j].tile();
        }

        GameMap gm = new GameMap(mapProperties, mapTiles);
        //gm.setTheme(theme);
        GameWorld gw = new GameWorld(gm, teamA, teamB, archonMemory);

        gw.reserveRandomIDs(100);

        MapLocation origin = gm.getMapOrigin();

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++)
                map[i][j].createGameObject(gw, new MapLocation(origin.x + i, origin.y + j));
        }
        
        gw.endRandomIDs();

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
            }
            return n_marked;
        }

        public void add(int x, int y) {
            if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight)
                return;
            if (map[x][y].tile() != TerrainTile.LAND)
                return;
            MapLocation loc = new MapLocation(x, y);
            if (marked.contains(loc))
                return;
            queue.push(loc);
            marked.add(loc);
            n_marked++;
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

    // TODO this needs to be recoded
    // TODO CORY FIX IT
    public boolean isTournamentLegal() {
        LegalityWarning warn = new LegalityWarning();
        int x, y, mx, my;
        SymbolData d, md;
        boolean baseBad = false, archonsBad = false;
        // check that the map is symmetric
        for (y = 0, my = mapHeight - 1; my >= y; y++, my--)
            for (x = 0, mx = mapWidth - 1; (my > y) ? (mx >= 0) : (mx >= x); x++, mx--) {
                if (!map[x][y].equalsMirror(map[mx][my])) {
                    warn.warnf("%d,%d does not match %d,%d", x, y, mx, my);
                }
            }
        int grounds = 0, gx = 0, gy = 0;
        int nodes = 0, archonsA = 0, baseAx = -1, baseAy = -1;
        for (y = 0; y < mapHeight; y++) {
            for (x = 0; x < mapWidth; x++) {
                d = map[x][y];
                if (d instanceof RobotData) {
                    RobotData rd = (RobotData) d;
                    switch (rd.type) {
//                        case ARCHON:
//                            if (rd.team == Team.NEUTRAL) {
//                                warn.warnUnit(rd);
//                            }
//                            if (rd.team == Team.A)
//                                archonsA++;
//                            break;
                        default:
                            warn.warnUnit(rd);
                    }
                } else if (d instanceof NodeData) {
                    nodes++;
                    if (((NodeData) d).team == Team.A) {
                        if (baseAx != -1) {
                            warn.warn("Team A has more than one power core.");
                            baseBad = true;
                        } else {
                            baseAx = x;
                            baseAy = y;
                        }
                    }
                }
                if (d.tile() == TerrainTile.LAND) {
                    grounds++;
                    gx = x;
                    gy = y;
                }
            }
        }

        if (baseAx == -1) {
            baseBad = true;
            warn.warn("Team A does not have a power core.");
        }

//        if (!(baseBad || archonsBad)) {
//            for (y = baseAy - 1; y <= baseAy + 1; y++)
//                for (x = baseAx - 1; x <= baseAx + 1; x++) {
//                    if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
//                        d = map[x][y];
//                        if (d instanceof RobotData) {
//                            RobotData rd = (RobotData) d;
//                            if (rd.type == RobotType.ARCHON && rd.team == Team.A)
//                                archonsA--;
//                        }
//                    }
//                }
//            if (archonsA != 0)
//                warn.warn("Team A has an archon that is not next to its power core.");
//        }

        // check that the ground squares are connected
        if (grounds == 0) {
            warn.warn("There are no land squares on the entire map!");
        } else {
            int reachable = new FloodFill(gx, gy).size();
            if (reachable != grounds) {
                warn.warn(String.format("There are %d land squares but only %d are reachable from %d,%d", grounds, reachable, gx, gy));
            }
        }
        // check for walls that are passable diagonally
        // these aren't illegal, but we try to avoid them
        for (y = 1; y < mapHeight; y++)
            for (x = 1; x < mapWidth; x++) {
                TerrainTile ul = map[x - 1][y - 1].tile();
                TerrainTile ur = map[x][y - 1].tile();
                TerrainTile dl = map[x - 1][y].tile();
                TerrainTile dr = map[x][y].tile();
                if (ul == TerrainTile.VOID && dr == TerrainTile.VOID && ur == TerrainTile.LAND && dl == TerrainTile.LAND)
                    System.err.format("Warning: diagonal passageway at %d, %d\n", x - 1, y);
                if (ul == TerrainTile.LAND && dr == TerrainTile.LAND && ur == TerrainTile.VOID && dl == TerrainTile.VOID)
                    System.err.format("Warning: diagonal passageway at %d, %d\n", x, y);
            }
        int rounds = mapProperties.get(MapProperties.MAX_ROUNDS);
        if (rounds < GameConstants.ROUND_MIN_LIMIT)
            warn.warn("The round limit is too small.");
        else if (rounds > GameConstants.ROUND_MAX_LIMIT)
            warn.warn("The round limit is too large.");

        connected:
        {
            HashMap<MapLocation, UnionFindNode> nodeMap = new HashMap<MapLocation, UnionFindNode>();
            UnionFindNode n0, n1;
            int components = nodes;
            for (MapLocation[] link : nodeLinks) {
                if (!isNode(link[0])) {
                    warn.warnf("Nodelink contains %d,%d but there is no node there", link[0].x, link[0].y);
                    break connected;
                }
                if (!isNode(link[1])) {
                    warn.warnf("Nodelink contains %d,%d but there is no node there", link[1].x, link[1].y);
                    break connected;
                }
                if (!nodeMap.containsKey(link[0]))
                    nodeMap.put(link[0], new UnionFindNode());
                if (!nodeMap.containsKey(link[1]))
                    nodeMap.put(link[1], new UnionFindNode());
                n0 = nodeMap.get(link[0]);
                n1 = nodeMap.get(link[1]);
                if (n0.find() != n1.find())
                    components--;
                n0.union(n1);
            }
            if (components != 1)
                warn.warn("The power node graph is disconnected.");
        }
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
        String mapPath = Config.getGlobalConfig().get("bc.game.map-path");
        for (String str : s) {
            isTournamentLegal(str, mapPath);
        }
    }
}
