package battlecode.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class RobotTypeTest {
    public final double EPSILON = 1.0e-9;

    /**
     * Make sure that outbreak multipliers are computed properly.
     */
    @Test
    public void testOutbreakMultiplier() {
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(0), 1.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(1), 1.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(50), 1.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(299), 1.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(300), 1.1, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(599), 1.1, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(600), 1.2, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(900), 1.3, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(1200), 1.5, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(1500), 1.7, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(1800), 2.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(2100), 2.3, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(2400), 2.6, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(2700), 3.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(2999), 3.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(3000), 4.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(3300), 5.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(3600), 6.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(3899), 6.0, EPSILON);
        assertEquals(RobotType.GUARD.getOutbreakMultiplier(4000), 7.0, EPSILON);
    }

    /**
     * Make sure that outbreak attack and health levels are computed properly.
     */
    @Test
    public void testOutbreakStats() {
        assertEquals(RobotType.RANGEDZOMBIE.attackPower(0), RobotType
                .RANGEDZOMBIE.attackPower, EPSILON);
        assertEquals(RobotType.FASTZOMBIE.attackPower(1000), RobotType
                .FASTZOMBIE.attackPower * 1.3, EPSILON);
        assertEquals(RobotType.BIGZOMBIE.attackPower(2000), RobotType
                .BIGZOMBIE.attackPower * 2.0, EPSILON);
        assertEquals(RobotType.STANDARDZOMBIE.attackPower(3000), RobotType
                .STANDARDZOMBIE.attackPower * 4.0, EPSILON);
        assertEquals(RobotType.STANDARDZOMBIE.maxHealth(0), RobotType
                .STANDARDZOMBIE.maxHealth, EPSILON);
        assertEquals(RobotType.BIGZOMBIE.maxHealth(1000), RobotType
                .BIGZOMBIE.maxHealth * 1.3, EPSILON);
        assertEquals(RobotType.FASTZOMBIE.maxHealth(2000), RobotType
                .FASTZOMBIE.maxHealth * 2.0, EPSILON);
        assertEquals(RobotType.RANGEDZOMBIE.maxHealth(3000), RobotType
                .RANGEDZOMBIE.maxHealth * 4.0, EPSILON);

        // non-zombies should be unaffected by multiplier
        assertEquals(RobotType.GUARD.attackPower(1000), RobotType.GUARD
                .attackPower, EPSILON);
        assertEquals(RobotType.SOLDIER.attackPower(1000), RobotType.SOLDIER
                .attackPower, EPSILON);
        assertEquals(RobotType.TURRET.attackPower(1000), RobotType.TURRET
                .attackPower, EPSILON);
        assertEquals(RobotType.TTM.attackPower(1000), RobotType.TTM
                .attackPower, EPSILON);
        assertEquals(RobotType.VIPER.attackPower(1000), RobotType.VIPER
                .attackPower, EPSILON);
    }
}
