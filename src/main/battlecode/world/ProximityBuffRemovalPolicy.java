/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.world;

import battlecode.common.MapLocation;

/**
 *
 * @author Sasa
 */
public class ProximityBuffRemovalPolicy extends BuffRemovalPolicy {

    private final InternalRobot target;
    private final double radius;

    public ProximityBuffRemovalPolicy(InternalBuff buff, InternalRobot target, double radius) {
        super(buff);
        this.target = target;
        this.radius = radius;
    }

    @Override
    public boolean remove() {
        MapLocation mt = target.getLocation(),
                mm = getBuff().getRobot().getLocation();
        return (Math.sqrt(Math.pow(mt.getX() - mm.getX(), 2) + Math.pow(mt.getY() - mm.getY(), 2)) >= radius);
    }
}
