package battlecode.server;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;
import battlecode.serial.notification.*;
import battlecode.world.signal.InternalSignal;
import battlecode.serial.*;
import battlecode.server.serializer.Serializer;
import battlecode.server.serializer.SerializerFactory;
import battlecode.world.GameMap;
import battlecode.common.ZombieSpawnSchedule;
import battlecode.world.signal.*;
import org.junit.Ignore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author james
 */
@Ignore
public abstract class SerializerFactoryTestBase {
    private static final Map<GameMap.MapProperties, Integer> properties = new HashMap<>();
    static {
        properties.put(GameMap.MapProperties.HEIGHT, 3);
        properties.put(GameMap.MapProperties.WIDTH, 3);
        properties.put(GameMap.MapProperties.ROUNDS, 2000);
        properties.put(GameMap.MapProperties.SEED, 12345);
    }

    private static final ZombieSpawnSchedule zSchedule = new ZombieSpawnSchedule();
    static {
        zSchedule.add(5, RobotType.RANGEDZOMBIE, 10);
        zSchedule.add(10, RobotType.FASTZOMBIE, 4);
    }

    private static final double[][] parts = new double[][] {
            new double[] {10, 11, 12},
            new double[] {13, 14, 15},
            new double[] {16, 17, 18},
    };

    private static final double[][] rubble = new double[][] {
            new double[] {0, 1, 2},
            new double[] {3, 4, 5},
            new double[] {6, 7, 8},
    };

    private static final GameMap.InitialRobotInfo[] initialRobots = new GameMap.InitialRobotInfo[] {
            new GameMap.InitialRobotInfo(10, 100, RobotType.ARCHON, Team.B),
            new GameMap.InitialRobotInfo(100, 10, RobotType.ARCHON, Team.A)
    };

    private static final GameMap gameMap = new GameMap(properties,
            rubble,
            parts,
            zSchedule,
            initialRobots,
            "Test Map");

    private static final long[][] teamMemories = new long[][] {
            new long[] {1, 2, 3, 4, 5},
            new long[] {1, 2, 3, 4, 5},
    };

    // An array with a sample object from every type of thing we could ever want to serialize / deserialize.
    private static final ServerEvent[] serverEvents = new ServerEvent[] {
            new MatchHeader(gameMap, teamMemories, 0, 3),
            new RoundDelta(new InternalSignal[] {
                    new AttackSignal(57, new MapLocation(1,1)),
                    new BroadcastSignal(57, new Signal(new MapLocation(1, 1),
                            57, Team.A), 24),
                    new BuildSignal(57, new MapLocation(1,1), RobotType.GUARD, Team.A, 50),
                    new BytecodesUsedSignal(new int[] {5, 6}, new int[] {17, 32}),
                    new ClearRubbleSignal(57, new MapLocation(1, 1), 5),
                    new ControlBitsSignal(0, 0),
                    new DeathSignal(57),
                    new HealthChangeSignal(new int[] {5, 6}, new double[] {17.21, 32}),
                    new IndicatorDotSignal(57, Team.B, new MapLocation(0,0), 0, 0, 0),
                    new IndicatorLineSignal(57, Team.B, new MapLocation(0,0), new MapLocation(1,1), 0, 0, 0),
                    new IndicatorStringSignal(57, 0, "Test Indicator String"),
                    new InfectionSignal(new int[] {5, 6}, new int[] {1, 0},
                            new int[] {10, 5}),
                    new MatchObservationSignal(57, "test"),
                    new MovementOverrideSignal(0, new MapLocation(10000, 10000)),
                    new MovementSignal(57, new MapLocation(0, 0), 0),
                    new RepairSignal(57, 120),
                    new RubbleChangeSignal(new MapLocation(0, 0), 5),
                    new RobotDelaySignal(new int[] {5, 6}, new double[] {17, 32}, new double[] {10, 2.5}),
                    new SpawnSignal(120, SpawnSignal.NO_ID, new MapLocation(5, 6), RobotType.ZOMBIEDEN, Team.ZOMBIE, 0),
                    new TeamResourceSignal(Team.A, 100),
                    new TypeChangeSignal(57, RobotType.TTM)
            }),
            new MatchFooter(Team.A, teamMemories),
            new GameStats(),
            new InjectDelta(true, new InternalSignal[0]),
            new PauseEvent(),
            new ExtensibleMetadata()

    };

    private static final Notification[] notifications = new Notification[] {
            PauseNotification.INSTANCE,
            ResumeNotification.INSTANCE,
            RunNotification.forever(),
            StartNotification.INSTANCE,
            new InjectNotification(new MovementOverrideSignal(0, new MapLocation(0, 0))),
            new GameNotification(new GameInfo("teama", "teamb", new String[] {"map-1"}))
    };

    /**
     * Runs all the objects we're going to serialize through a serializer-deserializer pair.
     *
     * @param factory The factory to create serializers with
     * @throws IOException
     */
    public void testRoundTrip(final SerializerFactory factory) throws IOException {
        testRoundTripForType(factory, serverEvents, ServerEvent.class);
        testRoundTripForType(factory, notifications, Notification.class);
    }

    /**
     * Test a round trip for a list of objects.
     *
     * @param factory the factory to create serializers with
     * @param messages the messages to serialize
     * @param messageClass the type of messages to serialize
     * @param <T> the type of messages to serialize
     * @throws IOException
     */
    private <T> void testRoundTripForType(final SerializerFactory factory,
                                         final T[] messages,
                                         final Class<T> messageClass)
            throws IOException{

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final Serializer<T> serializer = factory.createSerializer(
                output, null, messageClass
        );

        for (T message : messages) {
            try {
                serializer.serialize(message);
            } catch (final IOException e) {
                throw new IOException("Couldn't serialize object of class: " +
                        message.getClass().getName(), e);
            }
        }
        serializer.close();
        output.close();

        System.out.printf("Factory %s output size: %d bytes (for %ss)\n",
                factory.getClass().getSimpleName(),
                output.size(),
                messageClass.getSimpleName()
        );

        final ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        final Serializer<T> deserializer = factory.createSerializer(
                null, input, messageClass
        );

        for (int i = 0; i < messages.length; i++) {
            final T result;
            try {
                result = deserializer.deserialize();
            } catch (final IOException e) {
                throw new IOException("Couldn't deserialize object of class: " +
                        messages[i].getClass().getName(), e);
            }

            // TODO assertEquals(serializeableObjects[i], result);
            // For this to work, we'll need to override Object.equals on any class we want to serialize.
        }
    }
}
