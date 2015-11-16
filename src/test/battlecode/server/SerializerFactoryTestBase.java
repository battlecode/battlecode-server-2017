package battlecode.server;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.signal.Signal;
import battlecode.serial.*;
import battlecode.serial.notification.PauseNotification;
import battlecode.serial.notification.ResumeNotification;
import battlecode.serial.notification.RunNotification;
import battlecode.serial.notification.StartNotification;
import battlecode.server.serializer.Serializer;
import battlecode.server.serializer.SerializerFactory;
import battlecode.world.GameMap;
import battlecode.world.GameWorld;
import battlecode.world.InternalRobot;
import battlecode.world.ZombieSpawnSchedule;
import battlecode.world.signal.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 7/28/15.
 */
public abstract class SerializerFactoryTestBase {
    static final Map<GameMap.MapProperties, Integer> properties = new HashMap<>();
    static {
        properties.put(GameMap.MapProperties.HEIGHT, 3);
        properties.put(GameMap.MapProperties.WIDTH, 3);
        properties.put(GameMap.MapProperties.ROUNDS, 2000);
        properties.put(GameMap.MapProperties.SEED, 12345);
    }

    static final ZombieSpawnSchedule zSchedule = new ZombieSpawnSchedule();
    static {
        zSchedule.add(5, RobotType.RANGEDZOMBIE, 10);
        zSchedule.add(10, RobotType.FASTZOMBIE, 4);
    }

    static final int[][] parts = new int[][] {
            new int[] {10, 11, 12},
            new int[] {13, 14, 15},
            new int[] {16, 17, 18},
    };

    static final int[][] rubble = new int[][] {
            new int[] {0, 1, 2},
            new int[] {3, 4, 5},
            new int[] {6, 7, 8},
    };

    static final GameMap gameMap = new GameMap(properties, rubble, parts, zSchedule, "Test Map");

    static final long[][] teamMemories = new long[][] {
            new long[] {1, 2, 3, 4, 5},
            new long[] {1, 2, 3, 4, 5},
    };

    static final GameWorld gameWorld = new GameWorld(gameMap, "Team 1", "Team 2", teamMemories);

    static final InternalRobot robot = new InternalRobot(gameWorld, RobotType.ARCHON, new MapLocation(0,0), Team.A, false, 0);

    // An array with a sample object from every type of thing we could ever want to serialize / deserialize.
    static final Object[] serializeableObjects = new Object[]{
            PauseNotification.INSTANCE,
            ResumeNotification.INSTANCE,
            RunNotification.forever(),
            StartNotification.INSTANCE,
            new MatchInfo("Team 1", "Team 2", new String[] {"Map 1", "Map 2"}),
            new MatchHeader(gameMap, teamMemories, 0, 3),
            new RoundDelta(new Signal[] {
                    new AttackSignal(robot.getID(), new MapLocation(1,1)),
                    new BashSignal(robot.getID(), new MapLocation(1,1)),
                    new BroadcastSignal(robot.getID(), robot.getTeam(), new HashMap<Integer, Integer>()),
                    new BuildSignal(57, new MapLocation(1,1), RobotType.GUARD, Team.A, 50),
                    new BytecodesUsedSignal(new InternalRobot[]{robot}),
                    new CastSignal(robot.getID(), new MapLocation(-75, -75)),
                    new ControlBitsSignal(0, 0),
                    new DeathSignal(robot.getID()),
                    new HealthChangeSignal(new InternalRobot[]{robot}),
                    new IndicatorDotSignal(robot.getID(), robot.getTeam(), new MapLocation(0,0), 0, 0, 0),
                    new IndicatorLineSignal(robot.getID(), robot.getTeam(), new MapLocation(0,0), new MapLocation(1,1), 0, 0, 0),
                    new IndicatorStringSignal(robot.getID(), 0, "Test Indicator String"),
                    new LocationOreChangeSignal(new MapLocation(0,0), -1.0),
                    new MatchObservationSignal(robot.getID(), "test"),
                    new MovementOverrideSignal(0, new MapLocation(10000, 10000)),
                    new MovementSignal(robot.getID(), new MapLocation(0, 0), true, 0),
                    new RobotDelaySignal(new InternalRobot[] {robot}),
                    new SpawnSignal(robot, robot, 1000),
                    new TeamOreSignal(Team.A, 100),
                    new XPSignal(0, 1000)
            }),
            new MatchFooter(Team.A, teamMemories),
            new RoundStats(100, 100),
            new GameStats(),
            DominationFactor.BARELY_BEAT,
            new ExtensibleMetadata()
    };

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
                throw new IOException("Couldn't serialize object of class: " +
                        serializeableObjects[i].getClass().getCanonicalName(), e);
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
                throw new IOException("Couldn't deserialize object of class: " +
                        serializeableObjects[i].getClass().getCanonicalName(), e);
            }

            // TODO assertEquals(serializeableObjects[i], result);
            // For this to work, we'll need to override Object.equals on any class we want to serialize.
        }
    }
}
