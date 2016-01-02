package battlecode.common;

import battlecode.common.*;
import battlecode.world.signal.Signal;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.TypeChangeSignal;

import java.util.HashMap;
import java.util.Optional;
import java.util.*;


public class Message {
	
	private MapLocation location;
	private int ID;
	private Team team;
	
	public Message(MapLocation ml, int id, Team t) {
		
		this.location = ml;
		this.ID = id;
		this.team = t;
		
	}
	
    public int getID() {
        return ID;
    }

    public MapLocation getLocation() {
        return location;
    }

    public Team getTeam() {
        return team;
    }

	

}
