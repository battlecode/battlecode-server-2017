package battlecode.server;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;
import battlecode.engine.signal.Signal;
import battlecode.serial.*;
import battlecode.serial.notification.PauseNotification;
import battlecode.serial.notification.ResumeNotification;
import battlecode.serial.notification.RunNotification;
import battlecode.serial.notification.StartNotification;
import battlecode.server.serializer.JavaSerializerFactory;
import battlecode.server.serializer.Serializer;
import battlecode.server.serializer.SerializerFactory;
import battlecode.server.serializer.XStreamSerializerFactory;
import battlecode.world.GameMap;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;
import battlecode.world.signal.*;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by james on 7/28/15.
 */
public class SerializerTest {
    static final Map<GameMap.MapProperties, Integer> properties = new HashMap<>();
    static {
        properties.put(GameMap.MapProperties.HEIGHT, 3);
        properties.put(GameMap.MapProperties.WIDTH, 3);
        properties.put(GameMap.MapProperties.MAX_ROUNDS, 2000);
        properties.put(GameMap.MapProperties.SEED, 12345);
    }

    static final TerrainTile[][] tiles = new TerrainTile[][]{
            new TerrainTile[] {TerrainTile.NORMAL, TerrainTile.OFF_MAP, TerrainTile.VOID},
            new TerrainTile[] {TerrainTile.NORMAL, TerrainTile.UNKNOWN, TerrainTile.VOID},
            new TerrainTile[] {TerrainTile.NORMAL, TerrainTile.VOID, TerrainTile.VOID},
    };

    static final int[][] ores = new int[][] {
            new int[] {0, 1, 2},
            new int[] {3, 4, 5},
            new int[] {6, 7, 8},
    };

    static final GameMap gameMap = new GameMap(properties, tiles, ores, "Test Map");

    static final long[][] teamMemories = new long[][] {
            new long[] {1, 2, 3, 4, 5},
            new long[] {1, 2, 3, 4, 5},
    };

    static final GameWorld gameWorld = new GameWorld(gameMap, "Team 1", "Team 2", teamMemories);

    static final InternalRobot robot = new InternalRobot(gameWorld, RobotType.DRONE, new MapLocation(0,0), Team.A, false, 0);

    // An array with a sample object from every type of thing we could ever want to serialize / deserialize.
    static final Object[] serializeableObjects = new Object[]{
            PauseNotification.INSTANCE,
            ResumeNotification.INSTANCE,
            RunNotification.forever(),
            StartNotification.INSTANCE,
            new MatchInfo("Team 1", "Team 2", new String[] {"Map 1", "Map 2"}),
            new MatchHeader(gameMap, teamMemories, 0, 3),
            new RoundDelta(new Signal[] {
                    new AttackSignal(robot, new MapLocation(1,1)),
                    new BashSignal(robot, new MapLocation(1,1)),
                    new BroadcastSignal(robot, new HashMap<Integer, Integer>()),
                    new BuildSignal(robot, robot, 2), // This is technically incorrect but whatever
                    new BytecodesUsedSignal(new InternalRobot[]{robot}),
                    new CastSignal(robot, new MapLocation(-75, -75)),
                    new ControlBitsSignal(0, 0),
                    new DeathSignal(robot),
                    new HealthChangeSignal(new InternalRobot[]{robot}),
                    new IndicatorDotSignal(robot, new MapLocation(0,0), 0,0,0),
                    new IndicatorLineSignal(robot, new MapLocation(0,0), new MapLocation(1,1), 0,0,0),
                    new IndicatorStringSignal(robot, 0, "Test Indicator String"),
                    new LocationOreChangeSignal(new MapLocation(0,0), -1.0),
                    new MatchObservationSignal(robot, "test"),
                    new MineSignal(new MapLocation(0,0), 0),
                    new MissileCountSignal(0, 1),
                    new MovementOverrideSignal(0, new MapLocation(10000, 10000)),
                    new MovementSignal(robot, new MapLocation(0, 0), true),
                    new RobotInfoSignal(new InternalRobot[] {robot}),
                    new SelfDestructSignal(robot, new MapLocation(0,0), 1000),
                    new SpawnSignal(robot, robot, 1000),
                    new TeamOreSignal(new double[] {10000, 21293}),
                    new XPSignal(0, 1000)
            }),
            new MatchFooter(Team.A, teamMemories),
            new RoundStats(100, 100),
            new GameStats(),
            DominationFactor.BARELY_BARELY_BEAT,
            new ExtensibleMetadata()
    };

    @Test
    public void testJavaRoundTrip() throws IOException {
        testRoundTrip(new JavaSerializerFactory());
    }

    @Test
    public void testXStreamRoundTrip() throws IOException {
        testRoundTrip(new XStreamSerializerFactory());
    }

    /**
     * Runs all the objects we're going to serialize through a serializer-deserializer pair.
     *
     * @param serializerFactory The factory to create serializers with.
     * @throws IOException
     */
    public void testRoundTrip(final SerializerFactory serializerFactory) throws IOException {

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final Serializer serializer = serializerFactory.createSerializer(output, null);
        for (int i = 0; i < serializeableObjects.length; i++) {
            try {
                serializer.serialize(serializeableObjects[i]);
            } catch (final IOException e) {
                fail("Couldn't serialize object of class: " + serializeableObjects[i].getClass().getCanonicalName() + ": " + e);
            }
        }
        serializer.close();
        output.close();

        final ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        final Serializer deserializer = serializerFactory.createSerializer(null, input);

        for (int i = 0; i < serializeableObjects.length; i++) {
            final Object result;
            try {
                result = deserializer.deserialize();
            } catch (final IOException e) {
                fail("Couldn't deserialize object of class: " + serializeableObjects[i].getClass().getCanonicalName() + ": " + e);
                return; // To satisfy "might not have been initialized"
            }

            // TODO assertEquals(serializeableObjects[i], result);
            // For this to work, we'll need to override Object.equals on any class we want to serialize.
        }
    }
}
