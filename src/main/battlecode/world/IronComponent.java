/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package battlecode.world;

import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.IronController;
import battlecode.world.signal.IronShieldSignal;

/**
 *
 * @author Sasa
 */
public class IronComponent extends BaseComponent implements IronController{

    public IronComponent(ComponentType type, InternalRobot robot) {
        super(type, robot);
    }

    public void activateShield() throws GameActionException {
        assertInactive();
        activate(new IronShieldSignal(robot));
    }

}
