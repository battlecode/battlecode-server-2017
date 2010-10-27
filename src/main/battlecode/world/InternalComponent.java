package battlecode.world;

import battlecode.common.Component;
import battlecode.common.ComponentType;
import battlecode.common.MapLocation;
import battlecode.common.Team;

public class InternalComponent extends InternalObject implements Component
{
	private int roundsUntilIdle;
	private final ComponentType type;
	private BaseComponent controller;

	public boolean isActive() { return roundsUntilIdle>0; }

	public int roundsUntilIdle() { return roundsUntilIdle; }

	public ComponentType type() { return type; }

	public void activate() {
		activate(type.delay);
	}

	public void activate(int rounds) {
		if(rounds>roundsUntilIdle)
			roundsUntilIdle = rounds;
	}
	
	public void processEndOfTurn() {
		if(roundsUntilIdle>0)
			roundsUntilIdle--;	
	}

	public InternalComponent(GameWorld gw, MapLocation loc, ComponentType type) {
		super(gw,loc,null,null);
		this.type = type;
	}

	public BaseComponent getController() {
		return controller;
	}

	public void setController(BaseComponent c) {
		controller = c;
	}

	public boolean canBeEquipped() {
		return controller==null||!controller.getRobot().isOn();
	}
}
