package battlecode.world;

import battlecode.common.BroadcastController;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.Message;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.TurnOnSignal;

import com.google.common.primitives.Ints;
import java.util.Arrays;
import java.util.ArrayList;

public class Radio extends BaseComponent implements BroadcastController {

    public Radio(ComponentType type, InternalRobot robot) {
        super(type, robot);
    }

    public void broadcast(Message m) throws GameActionException {
        assertInactive();
        assertNotNull(m);
        spendResources(GameConstants.BROADCAST_FIXED_COST + GameConstants.BROADCAST_COST_PER_BYTE * m.getNumBytes());
        activate(new BroadcastSignal(robot, type().range, m));
    }

    public void broadcastTurnOn(int[] ids) throws GameActionException {
        assertInactive();
        assertNotNull(ids);
        int[] turnOnIDs = new int[ids.length];
        int i, j;
        for (i = 0, j = 0; i < ids.length; i++) {
            InternalRobot ir = gameWorld.getRobotByID(ids[i]);
            if (ir != null && ir.getTeam() == robot.getTeam() && checkWithinRange(ir.getLocation()) && ir.chassis != Chassis.DUMMY)
                turnOnIDs[j++] = ir.getID();
        }
        gameWorld.visitSignal(new TurnOnSignal(Arrays.copyOf(turnOnIDs, j), robot, true));
    }

    public void broadcastTurnOnAll() throws GameActionException {
        assertInactive();
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (InternalObject o : gameWorld.allObjects()) {
            if (o.getTeam() == robot.getTeam() && checkWithinRange(o) && o instanceof InternalRobot && ((InternalRobot) o).chassis != Chassis.DUMMY)
                ids.add(o.getID());
        }
        gameWorld.visitSignal(new TurnOnSignal(Ints.toArray(ids), robot, true));
    }
}
