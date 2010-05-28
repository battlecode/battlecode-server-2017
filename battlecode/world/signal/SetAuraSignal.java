package battlecode.world.signal;

import battlecode.world.InternalRobot;
import battlecode.common.AuraType;

public class SetAuraSignal extends Signal {

	private static final long serialVersionUID = 3576184779L;

	private final AuraType aura;
	private final int robotID;
	
	public SetAuraSignal(InternalRobot r, AuraType t) {
		robotID = r.getID();
		aura = t;
	}

	public int getRobotID() {
		return robotID;
	}
	
	public AuraType getAura() {
		return aura;
	}

}
