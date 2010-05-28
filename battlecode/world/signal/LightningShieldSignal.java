/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.world.signal;

import battlecode.world.InternalRobot;

/**
 *
 * @author Sasa
 */
public class LightningShieldSignal extends Signal {



    private final int robotID, rounds, radius;
    private final double power;
    static final long serialVersionUID = 470287976094363872L;

    public LightningShieldSignal(InternalRobot r, int rounds, int radius, double power) {
        robotID = r.getID();
        this.rounds = rounds;
        this.radius = radius;
        this.power = power;
    }

    public double getPower() {
        return power;
    }

    public int getRadius() {
        return radius;
    }

    public int getRounds() {
        return rounds;
    }

    /**
     * Returns the ID of the robot that is draining
     *
     * @return the messaging robot's ID
     */
    public int getRobotID() {
        return robotID;
    }
}
