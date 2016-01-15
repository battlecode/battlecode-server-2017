package battlecode.common;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class ZombieSpawnScheduleTest {

    @Test
    public void testBasic() {
        ZombieSpawnSchedule z = new ZombieSpawnSchedule();
        z.add(100, RobotType.STANDARDZOMBIE, 5);
        z.add(100, RobotType.RANGEDZOMBIE, 123);
        z.add(100, RobotType.STANDARDZOMBIE, 1); // a repeat
        z.add(150, RobotType.STANDARDZOMBIE, 10);
        z.add(150, RobotType.STANDARDZOMBIE, 15);
        z.add(150, RobotType.BIGZOMBIE, 123);
        z.add(0, RobotType.FASTZOMBIE, 1);
        z.add(0, RobotType.BIGZOMBIE, 5);
        z.add(10000, RobotType.FASTZOMBIE, 10);
        z.add(10000, RobotType.FASTZOMBIE, 15);

        int[] rounds = z.getRounds();
        assertThat(Arrays.asList(rounds), contains(0, 100, 150, 10000));

        ZombieCount[] round0 = z.getScheduleForRound(0);
        assertThat(Arrays.asList(round0), containsInAnyOrder(new ZombieCount
                (RobotType.FASTZOMBIE, 10), new ZombieCount(RobotType
                .BIGZOMBIE, 5)));
        ZombieCount[] round100 = z.getScheduleForRound(100);
        assertThat(Arrays.asList(round100), containsInAnyOrder(new ZombieCount
                (RobotType.STANDARDZOMBIE, 6), new ZombieCount(RobotType
                .RANGEDZOMBIE, 123)));
        ZombieCount[] round150 = z.getScheduleForRound(150);
        assertThat(Arrays.asList(round100), containsInAnyOrder(new ZombieCount
                (RobotType.STANDARDZOMBIE, 25), new ZombieCount(RobotType
                .BIGZOMBIE, 123)));
        ZombieCount[] round10000 = z.getScheduleForRound(10000);
        assertThat(Arrays.asList(round100), containsInAnyOrder(new ZombieCount
                (RobotType.FASTZOMBIE, 25)));
        ZombieCount[] round2 = z.getScheduleForRound(2);
        assertEquals(round2.length, 0);

        // Make sure that modifying rounds[] doesn't change anything.
        // This assumes that the copy constructor works, which is tested in
        // the next unit test.
        ZombieSpawnSchedule original = new ZombieSpawnSchedule(z);
        rounds[0] = 100;
        rounds[2] = -1;
        assertEquals(original, z);
        // Make sure that modifying the counts doesn't change anything
        round0[0] = new ZombieCount(RobotType.BIGZOMBIE, 888);
        assertEquals(original, z);
    }

    /**
     * Tests the equals() method.
     */
    @Test
    public void testEquals() {
        ZombieSpawnSchedule z0 = new ZombieSpawnSchedule();
        ZombieSpawnSchedule z1 = new ZombieSpawnSchedule();
        ZombieSpawnSchedule z2 = new ZombieSpawnSchedule();

        z0.add(100, RobotType.STANDARDZOMBIE, 5);
        z0.add(100, RobotType.STANDARDZOMBIE, 2);
        z0.add(100, RobotType.RANGEDZOMBIE, 6);
        z0.add(100, RobotType.BIGZOMBIE, 1234);
        z0.add(100, RobotType.FASTZOMBIE, 1245);
        z0.add(10, RobotType.FASTZOMBIE, 5);
        z0.add(1000, RobotType.BIGZOMBIE, 1);

        // Same ones but different ordering
        z1.add(100, RobotType.STANDARDZOMBIE, 3);
        z1.add(100, RobotType.BIGZOMBIE, 1234);
        z1.add(100, RobotType.RANGEDZOMBIE, 6);
        z1.add(100, RobotType.FASTZOMBIE, 1222);
        z1.add(100, RobotType.FASTZOMBIE, 23);
        z1.add(100, RobotType.STANDARDZOMBIE, 4);
        z1.add(1000, RobotType.BIGZOMBIE, 1);
        z1.add(10, RobotType.FASTZOMBIE, 5);

        // Same ones but a little different
        z2.add(100, RobotType.STANDARDZOMBIE, 5);
        z2.add(100, RobotType.STANDARDZOMBIE, 2);
        z2.add(100, RobotType.RANGEDZOMBIE, 6);
        z2.add(100, RobotType.BIGZOMBIE, 1234);
        z2.add(100, RobotType.FASTZOMBIE, 1245);
        z2.add(11, RobotType.FASTZOMBIE, 5);
        z2.add(1000, RobotType.BIGZOMBIE, 1);

        assertEquals(z0, z1);
        assertNotEquals(z0, z2);
        assertNotEquals(z0, z2);
    }

    /**
     * Make sure that modifying the copy constructor returns a deep copy.
     */
    @Test
    public void testCopyConstructor() {
        ZombieSpawnSchedule z = new ZombieSpawnSchedule();
        z.add(100, RobotType.STANDARDZOMBIE, 5);
        z.add(100, RobotType.RANGEDZOMBIE, 123);
        z.add(100, RobotType.STANDARDZOMBIE, 1); // a repeat
        z.add(150, RobotType.STANDARDZOMBIE, 10);
        z.add(150, RobotType.STANDARDZOMBIE, 15);
        z.add(150, RobotType.BIGZOMBIE, 123);
        z.add(0, RobotType.FASTZOMBIE, 1);
        z.add(0, RobotType.BIGZOMBIE, 5);
        z.add(10000, RobotType.FASTZOMBIE, 10);
        z.add(10000, RobotType.FASTZOMBIE, 15);

        ZombieSpawnSchedule copy = new ZombieSpawnSchedule(z);
        copy.add(123, RobotType.STANDARDZOMBIE, 7);
        assertNotEquals(copy, z);
    }
}
