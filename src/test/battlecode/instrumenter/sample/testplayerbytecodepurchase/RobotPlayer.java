package battlecode.instrumenter.sample.testplayerbytecodepurchase;

import battlecode.common.Clock;
import battlecode.common.RobotController;

/**
 * Created by nmccoy on 1/20/17.
 */
public class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws Exception {
        System.out.println("Initial bytecode: "+Clock.getBytecodesLeft());
        rc.purchaseBytecodes(10);
        System.out.println("Final bytecode: "+Clock.getBytecodesLeft());
    }
}
