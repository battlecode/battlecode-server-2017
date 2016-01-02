package battlecode.common;

import battlecode.common.*;
import battlecode.world.signal.Signal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.TypeChangeSignal;

import java.util.HashMap;
import java.util.Optional;
import java.util.*;


public class MessageSignal extends Message {
	
	private int signal1;
	private int signal2;
	
	public MessageSignal(MapLocation ml, int id, Team t, int s1, int s2) {
		
		super(ml, id, t);
		this.signal1 = s1;
		this.signal2 = s2;
	}
	
    public int getSignalValue1() {
        return signal1;
    }

    public int getSignalValue2() {
        return signal2;
    }


}
