/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package battlecode.world;

import battlecode.common.MapLocation;

/**
 * @author Sasa
 */
public class LocationBuffRemovalPolicy extends BuffRemovalPolicy {
    private final MapLocation loc;

    public LocationBuffRemovalPolicy(InternalBuff buff) {
        super(buff);
        loc = this.getBuff().getRobot().getLocation();
    }

    @Override
    public boolean remove() {
        return this.getBuff().getRobot().getLocation() != loc;
    }

}
