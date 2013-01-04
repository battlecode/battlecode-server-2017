package battlecode.analysis;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.engine.signal.Signal;
import battlecode.serial.MatchHeader;
import battlecode.serial.RoundDelta;
import battlecode.server.proxy.Proxy;
import battlecode.server.proxy.ProxyFactory;
import battlecode.server.proxy.XStreamProxy;
import battlecode.world.GameMap;
import battlecode.world.signal.*;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

public class AwesomenessAnalyzer {
    private static final float TOWER_AWESOMENESS = 500;
    private static final float ARCHON_DEATH_AWESOMENESS = 250;
    private static final float ARCHON_AWESOMENESS = 100;
    private static final float DEATH_AWESOMENESS = 50;
    private static final float ATTACK_AWESOMENESS = 10;
    private static final float SPAWN_AWESOMENESS = 5;
    private static final float EQUIP_AWESOMENESS = 1;
    private static final float EVOLVE_AWESOMENESS = 5;
    private static final float ACTIVE_AWESOMENESS = 1;

    private static final float ARCHON_AWESOMENESS_MULTIPLIER = 2.f;
    private static final float WOUT_AWESOMENESS_MULTIPLIER = .3f;

    private static final float RADIUS_IN_STDEVS = 1.4f;

    private String filename;
    private ArrayList<GameData> games;


    private static Options options() {
        Options options = new Options();
        options.addOption("directory", true, "run mode");
        return options;
    }

    public AwesomenessAnalyzer(String filename) {
        this.filename = filename;
        games = new ArrayList<GameData>();
    }

    private class Event {
        public float awesomeness;
        public float x;
        public float y;

        public Event(float awesomeness, MapLocation location) {
            this.awesomeness = awesomeness;
            x = location.x + 0.5f; // Center of square
            y = location.y + 0.5f; // Center of square
        }
    }

    private class RobotStat {
        public static final int COUNTDOWN = 20;

        public MapLocation location;
        public int idleCountdown;
        public float robotAwesomeness;
        public RobotType type;

        public RobotStat(MapLocation location, RobotType type) {
            this.location = location;
            idleCountdown = COUNTDOWN;
            transform(type);
        }

        // TODO this needs to be recoded
        // TODO CORY FIX IT
        public void transform(RobotType type) {
            this.type = type;
//            if (type.equals(RobotType.ARCHON))
//                robotAwesomeness = ARCHON_AWESOMENESS_MULTIPLIER;
//                //else if(type.equals(RobotType.WOUT))
//                //	robotAwesomeness=WOUT_AWESOMENESS_MULTIPLIER;
//            else
                robotAwesomeness = 1.f;
        }

        public void resetCountDown() {
            idleCountdown = COUNTDOWN;
        }
    }

    private class GameData {
        public ArrayList<Object> data;
        public ArrayList<AwesomenessSignal> stats;
        public float totalAwesomeness;

        // Maps robotID to location
        private HashMap<Integer, RobotStat> robots;
        private float centerX;
        private float centerY;
        private float radius;

        public GameData() {
            data = new ArrayList<Object>();
            stats = new ArrayList<AwesomenessSignal>();
            robots = new HashMap<Integer, RobotStat>();
        }

        public void addData(Object o) {
            data.add(o);

            if (o instanceof MatchHeader) {
                visitHeader((MatchHeader) o);
                System.out.println("Center: " + centerX + " " + centerY + " " + radius);
            } else if (o instanceof RoundDelta) {
                stats.add(visitRound((RoundDelta) o));
            }
        }

        public void postProcess() {
            final int WAKE_DELAY = 400;

            // Wait some rounds before moving camera
            for (int i = 0; i < WAKE_DELAY && i < stats.size(); i++) {
                AwesomenessSignal s = stats.get(i);
                s.centerX = (i * s.centerX + (WAKE_DELAY - i) * centerX) / WAKE_DELAY;
                s.centerY = (i * s.centerY + (WAKE_DELAY - i) * centerY) / WAKE_DELAY;
                s.radius = (i * s.radius + (WAKE_DELAY - i) * radius) / WAKE_DELAY;
            }
        }

        public void reduceJitter(int begin, int end) {
            // Ramer-Douglas-Peucker curve simplification algorithm
            AwesomenessSignal first = stats.get(begin);
            AwesomenessSignal last = stats.get(end);
            int farthest = -1;

            double dist, farthestDist = 10.;
            int i;
            for (i = begin + 1; i < end; i++) {
                double x = (first.centerX * (end - i) + last.centerX * (i - begin)) / (end - begin);
                double y = (first.centerY * (end - i) + last.centerY * (i - begin)) / (end - begin);
                double r = (first.radius * (end - i) + last.radius * (i - begin)) / (end - begin);
                AwesomenessSignal s = stats.get(i);
                x -= s.centerX;
                y -= s.centerY;
                r -= s.radius;
                // we care more about getting the right camera
                // angle when there is more stuff going on
                dist = s.totalAwesomeness * s.totalAwesomeness * (x * x + y * y + r * r / 2.) / (s.radius * s.radius);
                //System.out.println(dist);
                if (dist > farthestDist) {
                    farthestDist = dist;
                    farthest = i;
                }
            }

            //System.out.println("reduceJitter "+begin+"-"+end+" farthestDist "+farthestDist);

            if (farthest == -1) {
                for (i = begin + 1; i < end; i++) {
                    AwesomenessSignal s = stats.get(i);
                    s.centerX = (first.centerX * (end - i) + last.centerX * (i - begin)) / (end - begin);
                    s.centerY = (first.centerY * (end - i) + last.centerY * (i - begin)) / (end - begin);
                    s.radius = (first.radius * (end - i) + last.radius * (i - begin)) / (end - begin);
                }
            } else {
                reduceJitter(begin, farthest);
                reduceJitter(farthest, end);
            }
        }

        public void smoothStats() {

            // exponential smoothing

            float[] old1 = new float[stats.size()];
            float[] oldX = new float[stats.size()];
            float[] oldY = new float[stats.size()];
            float[] oldR2 = new float[stats.size()];

            float[] new1 = new float[stats.size()];
            float[] newX = new float[stats.size()];
            float[] newY = new float[stats.size()];
            float[] newR2 = new float[stats.size()];

            float[] tmp;

            float xOffset = stats.get(0).centerX;
            float yOffset = stats.get(0).centerY;

            // convert from awesomeness stats to sums

            final float decay = .95f;
            final float norm = (1.f - decay) / 2.f;

            int i, j;
            for (i = 0; i < stats.size(); i++) {
                AwesomenessSignal s = stats.get(i);
                old1[i] = s.totalAwesomeness;
                float dx = s.centerX - xOffset;
                float dy = s.centerY - yOffset;
                oldX[i] = dx * s.totalAwesomeness;
                oldY[i] = dy * s.totalAwesomeness;
                float stdev = s.radius / RADIUS_IN_STDEVS;
                oldR2[i] = s.totalAwesomeness * (stdev * stdev + dx * dx + dy * dy);
            }

            // If we do n passes, then we get a convolution function
            // that has continuous n-1st derivative
            for (j = 0; j < 2; j++) {
                float s1 = 0.f, sx = 0.f, sy = 0.f, sr2 = 0.f;
                for (i = 0; i < stats.size(); i++) {
                    s1 += old1[i] * norm;
                    sx += oldX[i] * norm;
                    sy += oldY[i] * norm;
                    sr2 += oldR2[i] * norm;
                    new1[i] = s1;
                    newX[i] = sx;
                    newY[i] = sy;
                    newR2[i] = sr2;
                    s1 *= .95;
                    sx *= .95;
                    sy *= .95;
                    sr2 *= .95;
                }
                s1 = 0.f;
                sx = 0.f;
                sy = 0.f;
                sr2 = 0.f;
                for (i = stats.size() - 1; i >= 0; i--) {
                    s1 += old1[i] * norm;
                    sx += oldX[i] * norm;
                    sy += oldY[i] * norm;
                    sr2 += oldR2[i] * norm;
                    new1[i] += s1;
                    newX[i] += sx;
                    newY[i] += sy;
                    newR2[i] += sr2;
                    s1 *= .95;
                    sx *= .95;
                    sy *= .95;
                    sr2 *= .95;
                }
                tmp = old1;
                old1 = new1;
                new1 = tmp;
                tmp = oldX;
                oldX = newX;
                newX = tmp;
                tmp = oldY;
                oldY = newY;
                newY = tmp;
                tmp = oldR2;
                oldR2 = newR2;
                newR2 = tmp;
            }

            // now back to awesomeness

            for (i = 0; i < stats.size(); i++) {
                AwesomenessSignal s = stats.get(i);
                s.totalAwesomeness = old1[i];
                s.centerX = oldX[i] / old1[i] + xOffset;
                s.centerY = oldY[i] / old1[i] + yOffset;
                //System.out.println(old1[i]+" "+oldX[i]+" "+oldY[i]+" "+oldR2[i]);
                s.radius = RADIUS_IN_STDEVS * (float) Math.sqrt((oldR2[i] - (oldX[i] * oldX[i] + oldY[i] * oldY[i]) / old1[i]) / old1[i]);
            }

            /*
                 for(i=0;i<stats.size();i++) {
                 System.out.println(i+" "+stats.get(i).radius);
                 }
               */

            reduceJitter(0, stats.size() - 1);

            /*
                 for(i=0;i<stats.size();i++) {
                 System.out.println(i+" "+stats.get(i).radius);
                 }
               */

            /*

                 float total = 0.0f;
                 float min = Float.MAX_VALUE;
                 float max = Float.MIN_VALUE;
                 int awesomeRound = 0;

                 final int LOOK_BACK = 5;
                 final int LOOK_FORWARD = 10;
                 final AwesomenessSignal[] rounds = new AwesomenessSignal[stats.size()];
                 stats.toArray(rounds);
                 for (int i = 0; i < rounds.length; i++) {
                 int n = 0;
                 float sum = 0;
                 float centerXSum = 0;
                 float centerYSum = 0;
                 float radiusSum = 0;

                 // Sum elements before i
                 for (int j = i - LOOK_BACK; j < i; j++) {
                 if (j >= 0) {
                 n++;
                 AwesomenessSignal s = rounds[j];
                 float awesomeness = s.totalAwesomeness;
                 sum += awesomeness;
                 centerXSum += s.centerX * awesomeness;
                 centerYSum += s.centerY * awesomeness;
                 radiusSum += s.radius * awesomeness;
                 }
                 }

                 // Sum elements after i
                 for (int j = i + 1; j <= i + LOOK_FORWARD && j < rounds.length; j++) {
                 n++;
                 AwesomenessSignal s = rounds[j];
                 float awesomeness = s.totalAwesomeness;
                 sum += awesomeness;
                 centerXSum += s.centerX * awesomeness;
                 centerYSum += s.centerY * awesomeness;
                 radiusSum += s.radius * awesomeness;
                 }

                 // Weight current value higher than others
                 AwesomenessSignal s = rounds[i];
                 float newAwesomeness = (sum + s.totalAwesomeness * n) / (2 * n);
                 s.updateAwesomeness(newAwesomeness);
                 s.centerX = (centerXSum + s.centerX * sum) / (2 * sum);
                 s.centerY = (centerYSum + s.centerY * sum) / (2 * sum);
                 s.radius = (radiusSum + s.radius * sum) / (2 * sum);

                 total += newAwesomeness;
                 if (newAwesomeness < min)
                 min = newAwesomeness;
                 if (newAwesomeness > max) {
                 max = newAwesomeness;
                 awesomeRound = i;
                 }
                 }

                 //System.out.println("Smooth: " + total + " " + totalAwesomeness + " " + min + " " + max);
                 //System.out.println("AwesomeRound: " + (awesomeRound + 1));

                 for(int i=0;i<stats.size();i++) {
                 System.out.println(i+" "+stats.get(i).radius);
                 }

               */

        }

        public List<Object> getOutput() {
            // Copy data
            ArrayList<Object> output = new ArrayList<Object>(data);

            // Insert awesomeness stats into RoundDelta
            int statNum = 0;
            for (ListIterator<Object> iter = output.listIterator(); iter.hasNext(); ) {
                Object round = iter.next();
                if (round instanceof RoundDelta) {
                    RoundDelta rd = (RoundDelta) round;
                    //iter.add(stats.get(statNum++));
                    //Signal[] oldSignals = rd.getSignals();
                    Signal[] oldSignals = stripIndicatorStrings(rd.getSignals());
                    final int len = oldSignals.length;
                    Signal[] newSignals = new Signal[len + 1];
                    System.arraycopy(oldSignals, 0, newSignals, 1, len);
                    newSignals[0] = stats.get(statNum++);
                    rd.setSignals(newSignals);
                }
            }

            return output;
        }

        public void renormalize() {
            final AwesomenessSignal[] rounds = new AwesomenessSignal[stats.size()];
            stats.toArray(rounds);

            // Calculate average awesomeness
            float ave = 0.0f;
            for (int i = 0; i < rounds.length; i++) {
                ave += rounds[i].totalAwesomeness;
            }
            ave = ave / rounds.length;

            // Renormalize
            for (int i = 0; i < rounds.length; i++) {
                rounds[i].renormalize(ave);
            }
            System.out.println("Renormalizing: " + ave);
        }

        public String toString() {
            return "AwesomenessSignal: ave=" + (totalAwesomeness / stats.size());
        }

        private Signal[] stripIndicatorStrings(Signal[] signals) {
            ArrayList<Signal> strippedSignals = new ArrayList<Signal>(signals.length);

            for (Signal s : signals)
                if (!(s instanceof IndicatorStringSignal))
                    strippedSignals.add(s);

            Signal[] out = new Signal[strippedSignals.size()];
            strippedSignals.toArray(out);

            return out;
        }

        private void visitHeader(MatchHeader header) {
            final GameMap map = (GameMap) header.getMap();
            final float halfWidth = ((float) map.getWidth()) / 2.0f;
            final float halfHeight = ((float) map.getHeight()) / 2.0f;
            final MapLocation origin = map.getMapOrigin();

            centerX = origin.x + halfWidth;
            centerY = origin.y + halfHeight;
            radius = (float) Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight);

            hulls = new ConvexHullData[]{new ConvexHullData(), new ConvexHullData()};
        }

        private class ConvexHullData {

            public ConvexHullData() {
            }

            public ConvexHullData(MapLocation[][] hulls) {
                int i;
                long dx, dy;
                for (MapLocation[] hull : hulls) {
                    MapLocation oldLoc = hull[hull.length - 1];
                    for (MapLocation newLoc : hull) {
                        dx = newLoc.x - oldLoc.x;
                        dy = newLoc.y - oldLoc.y;
                        area_2 += (newLoc.y + oldLoc.y) * dx;
                        sum_X_6 += -(newLoc.x * newLoc.x + newLoc.x * oldLoc.x + oldLoc.x * oldLoc.x) * dy;
                        sum_Y_6 += (newLoc.y * newLoc.y + newLoc.y * oldLoc.y + oldLoc.y * oldLoc.y) * dx;
                        oldLoc = newLoc;
                    }
                }
            }

            // area times 2
            public long area_2;
            // integral of X times 6
            public long sum_X_6;
            // integral of Y times 6
            public long sum_Y_6;
        }

        private ConvexHullData[] hulls;

        private AwesomenessSignal visitRound(RoundDelta round) {
            final Signal[] signals = round.getSignals();
            ArrayList<Event> events = new ArrayList<Event>(signals.length);

            RobotStat r;
            // Add awesomeness for events
            for (int i = 0; i < signals.length; i++) {
                Signal signal = signals[i];
                if (signal instanceof SpawnSignal) {
                    SpawnSignal s = (SpawnSignal) signal;
                    final int parent = s.getParentID();
                    if (parent != 0) {
                        robots.get(parent).resetCountDown();
                    }

                    MapLocation loc = s.getLoc();
                    if (loc != null) {
                        r = new RobotStat(loc, s.getType());
                        robots.put(s.getRobotID(), r);
                        events.add(new Event(SPAWN_AWESOMENESS * r.robotAwesomeness, loc));
                    }
                } else if (signal instanceof MovementSignal) {
                    MovementSignal s = (MovementSignal) signal;
                    RobotStat robot = robots.get(s.getRobotID());
                    robot.resetCountDown();

                    robot.location = s.getNewLoc();
                } else if (signal instanceof AttackSignal) {
                    AttackSignal s = (AttackSignal) signal;
                    r = robots.get(s.getRobotID());
                    r.resetCountDown();
                    MapLocation loc = s.getTargetLoc();
                    if (loc != null) {
                        events.add(new Event(ATTACK_AWESOMENESS * r.robotAwesomeness, s.getTargetLoc()));
                    }
                } else if (signal instanceof DeathSignal) {
                    DeathSignal s = (DeathSignal) signal;
                    r = robots.remove(s.getObjectID());
                    float awesomeness = DEATH_AWESOMENESS * r.robotAwesomeness;
                    if (r.location != null) {
                        events.add(new Event(awesomeness, r.location));
                    }
                }
            }

            // Add awesomeness for active robots
            for (RobotStat robot : robots.values()) {
                if (robot.idleCountdown-- > 0) {
                    events.add(new Event(ACTIVE_AWESOMENESS, robot.location));
                }
            }

            // Calculate stats
            float sum = 0;
            float centerX = 0;
            float centerY = 0;
            float radius = 0;
            if (events.size() <= 0) {
                centerX = this.centerX;
                centerY = this.centerY;
                radius = this.radius;
            } else {
                // Calculate sum, center
                for (Event event : events) {
                    sum += event.awesomeness;
                    centerX += event.x;
                    centerY += event.y;
                }
                centerX /= events.size();
                centerY /= events.size();

                // Calculate std dev
                for (Event event : events) {
                    float diffX = event.x - centerX;
                    float diffY = event.y - centerY;
                    radius += event.awesomeness * (diffX * diffX + diffY * diffY);
                }
                radius = (float) Math.sqrt(radius / sum);
            }

            // Convert std dev to actual radius
            radius *= RADIUS_IN_STDEVS;

            // Enforce min radius
            if (radius < 4) {
                radius = 4;
                //System.err.println("Warning: Std dev too small: " + radius);
            }

            // Enforce max radius
            if (radius > this.radius) {
                radius = this.radius;
            }

            //System.out.println(sum);
            totalAwesomeness += sum;
            return new AwesomenessSignal(sum, centerX, centerY, radius);
        }
    }

    public void analyze() {
        ObjectInputStream input = null;
        try {
            input = XStreamProxy.getXStream().createObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));
        } catch (Exception e) {
            System.err.println("Error: couldn't open match file " + filename);
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Analyzing: " + filename);


        try {
            GameData gameData = new GameData();
            games.add(gameData);

            // Initialize first Game
            Object o = input.readObject();
            if (o == null || !(o instanceof MatchHeader)) {
                System.err.println("Error: Missing MatchHeader.");
                System.exit(-2);
            }
            gameData.addData(o);

            while ((o = input.readObject()) != null) {
                if (o instanceof MatchHeader) {
                    // New Game
                    gameData = new GameData();
                    games.add(gameData);
                }

                gameData.addData(o);
            }
        } catch (EOFException e) {
            // Done parsing
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-2);
        }

        for (GameData game : games) {
            game.postProcess();
        }

        System.out.println(games);
    }

    public void smoothStats() {
        for (GameData game : games) {
            game.smoothStats();
        }
    }

    public void dumpFile() {
        try {
            Proxy output = ProxyFactory.createProxyFromFile(filename + ".analyzed");
            output.open();
            for (GameData game : games) {
                game.renormalize();
                for (Object data : game.getOutput()) {
                    output.writeObject(data);
                }
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-2);
        }
    }

    public static void analyze(String file) {
        AwesomenessAnalyzer analyzer = new AwesomenessAnalyzer(file);
        analyzer.analyze();
        analyzer.smoothStats();
        //for (int i = 0; i < 16; i++)
        //	  analyzer.smoothStats();
        analyzer.dumpFile();
    }

    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();
        JFileChooser chooser = new JFileChooser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options(), args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        String directory = cmd.getOptionValue("directory");
        System.out.println("selected dir: " + directory);
        if (cmd.hasOption("directory")) {
            File dir = new File(directory);
            String[] children = dir.list();
            if (children == null) {
                // Either dir does not exist or is not a directory
            } else {
                for (int i = 0; i < children.length; i++) { // Get filename of file or directory
                    String filename = children[i];
                    File file = new File(directory + "/" + filename);
                    //String type = chooser.getTypeDescription(file);
                    //System.out.println(directory + "/" + filename + " " + type);
                    if (filename.endsWith(".rms"))
                    //if(type.compareTo("RMS File")==0)
                    {
                        System.out.println("is an rms file");
                        analyze(file.getAbsolutePath());
                    }
                }
            }

        } else {
            if (args.length < 1) {
                System.err.println("Error: No filenames specified in arguments");
                System.exit(-1);
            }

            for (String arg : args) {
                analyze(arg);
            }
        }
    }
}
