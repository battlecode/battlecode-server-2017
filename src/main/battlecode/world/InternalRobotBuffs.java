/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.world;

import battlecode.common.*;
import battlecode.server.Config;

import java.util.Iterator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author Sasa
 */
public class InternalRobotBuffs {

    private final EnumMap<BuffType, InternalBuff> buffs = new EnumMap<BuffType, InternalBuff>(BuffType.class);
    private final InternalRobot robot;
    private double damageDealtMultiplier, damageReceivedMultiplier, damageDealtAdder;
	private int movementDelayAdder;
    private double spawnCostMultiplier;
	private int behaviorIntervalStart;
	private int behaviorIntervalLength;
    public static double DAMAGE_DEALT_MULTIPLIER_A, DAMAGE_RECIEVED_MULTIPLIER_A, DAMAGE_DEALT_ADDER_A,SPAWN_COST_MULTIPLIER_A;
    public static double DAMAGE_DEALT_MULTIPLIER_B, DAMAGE_RECIEVED_MULTIPLIER_B, DAMAGE_DEALT_ADDER_B,SPAWN_COST_MULTIPLIER_B;
    

    InternalRobotBuffs(InternalRobot robot) {
        this.robot = robot;
        Config options = Config.getGlobalConfig();

        if(options.get("drw.BEHAVIOR_INTERVAL_START")!=null)
        {
        	this.behaviorIntervalStart = new Integer(options.get("drw.BEHAVIOR_INTERVAL_START")).intValue();
        }
        
        if(options.get("drw.BEHAVIOR_INTERVAL_LENGTH")!=null)
        {
        	this.behaviorIntervalLength = new Integer(options.get("drw.BEHAVIOR_INTERVAL_LENGTH")).intValue();
        }
        
        if(options.get("drw.SPAWN_COST_MULTIPLIER_A")!=null)
        {
        	this.SPAWN_COST_MULTIPLIER_A = new Double(options.get("drw.SPAWN_COST_MULTIPLIER_A")).doubleValue();
        }
    
        if(options.get("drw.SPAWN_COST_MULTIPLIER_B")!=null)
        {
        	this.SPAWN_COST_MULTIPLIER_B = new Double(options.get("drw.SPAWN_COST_MULTIPLIER_B")).doubleValue();
        }
        
        if(options.get("drw.DAMAGE_DEALT_MULTIPLIER_A")!=null)
        {
        	this.DAMAGE_DEALT_MULTIPLIER_A = new Double(options.get("drw.DAMAGE_DEALT_MULTIPLIER_A")).doubleValue();
        }
    
        if(options.get("drw.DAMAGE_DEALT_MULTIPLIER_B")!=null)
        {
        	this.DAMAGE_DEALT_MULTIPLIER_B = new Double(options.get("drw.DAMAGE_DEALT_MULTIPLIER_B")).doubleValue();
        }
        
        if(options.get("drw.DAMAGE_RECIEVED_MULTIPLIER_A")!=null)
        {
        	this.DAMAGE_RECIEVED_MULTIPLIER_A = new Double(options.get("drw.DAMAGE_RECIEVED_MULTIPLIER_A")).doubleValue();
        }
    
        if(options.get("drw.DAMAGE_RECIEVED_MULTIPLIER_B")!=null)
        {
        	this.DAMAGE_RECIEVED_MULTIPLIER_B = new Double(options.get("drw.DAMAGE_RECIEVED_MULTIPLIER_B")).doubleValue();
        }
        
        if(options.get("drw.DAMAGE_DEALT_ADDER_A")!=null)
        {
        	this.DAMAGE_DEALT_ADDER_A= new Double(options.get("drw.DAMAGE_DEALT_ADDER_A")).doubleValue();
        }
    
        if(options.get("drw.DAMAGE_DEALT_ADDER_B")!=null)
        {
        	this.DAMAGE_DEALT_ADDER_B = new Double(options.get("drw.DAMAGE_DEALT_ADDER_B")).doubleValue();
        }
       //print();
    }
    
    public void print()
    {
    	System.out.println("this.DAMAGE_DEALT_ADDER_A: " + this.DAMAGE_DEALT_ADDER_A);
    	System.out.println("this.DAMAGE_DEALT_ADDER_B: " + this.DAMAGE_DEALT_ADDER_B);
    	System.out.println("this.DAMAGE_DEALT_MULTIPLIER_A: " + this.DAMAGE_DEALT_MULTIPLIER_A);
    	System.out.println("this.DAMAGE_DEALT_MULTIPLIER_B: " + this.DAMAGE_DEALT_MULTIPLIER_B);
    	System.out.println("this.DAMAGE_RECIEVED_MULTIPLIER_A: " + this.DAMAGE_RECIEVED_MULTIPLIER_A);
    	System.out.println("this.DAMAGE_RECIEVED_MULTIPLIER_B: " + this.DAMAGE_RECIEVED_MULTIPLIER_B);
    	System.out.println("this.SPAWN_COST_MULTIPLIER_A: " + this.SPAWN_COST_MULTIPLIER_A);
    	System.out.println("this.SPAWN_COST_MULTIPLIER_B: " + this.SPAWN_COST_MULTIPLIER_B);
    	System.out.println("this.BEHAVIOR_INTERVAL_START: " + this.behaviorIntervalStart);
    	System.out.println("this.BEHAVIOR_INTERVAL_LENGTH: " + this.behaviorIntervalLength);
    	System.out.println("is behavior on: " + this.isBehaviorOn() + " round: " + robot.myGameWorld.getCurrentRound());
    }

    public boolean addBuff(InternalBuff b) {
    	boolean a = b.verifyAdd(robot);
        if (!a) return false;
        buffs.put(b.type(), b);
        return true;
    }

    public boolean containsBuff(BuffType t) {
        return buffs.containsKey(t);
    }

    public void removeBuff(BuffType t) {
        buffs.remove(t);
    }

    public void processBeginningOfRound() {
        //zero out all modifiers
  
        damageDealtMultiplier = (robot.getTeam() == Team.A) ? DAMAGE_DEALT_MULTIPLIER_A : DAMAGE_DEALT_MULTIPLIER_B;
        damageReceivedMultiplier = (robot.getTeam() == Team.A) ? DAMAGE_RECIEVED_MULTIPLIER_A : DAMAGE_RECIEVED_MULTIPLIER_B;
        damageDealtAdder = (robot.getTeam() == Team.A) ? DAMAGE_DEALT_ADDER_A : DAMAGE_DEALT_ADDER_B;
        spawnCostMultiplier= (robot.getTeam()== Team.A) ? SPAWN_COST_MULTIPLIER_A:SPAWN_COST_MULTIPLIER_B;
        //recalculate all modifiers
        for (InternalBuff buff : buffs.values()) {
            buff.processBeginningOfRound(this);
        }
    }

    public void processEndOfRound() {
        //java.util.List<InternalBuff> rl = null;
        InternalBuff buff;
        for (Map.Entry<BuffType, InternalBuff> entry : buffs.entrySet()) {
            buff = entry.getValue();
            buff.processEndOfRound(this);
            if (buff.getRemovalPolicy().remove()) {
                buffs.remove(entry.getKey());
            }
        }
    }

    //FOLLOWING ITEMS ONLY TO BE USED BY BUFFS**********************************
 
    public double getDamageDealtMultiplier() {
        return returnValue(damageDealtMultiplier);
    }

    // accumulating auras is overpowered so just take the largest one
    public void modifyDamageDealtMultiplier(double damageDealtMultiplier) {
        this.damageDealtMultiplier += damageDealtMultiplier;
    }

    public double getDamageReceivedMultiplier() {
        return returnValue(damageReceivedMultiplier);
    }

    public void modifyDamageReceivedMultiplier(double damageReceivedMultiplier) {
        this.damageReceivedMultiplier += damageReceivedMultiplier;
    }

    public double getDamageDealtAdder() {
        return returnValue(damageDealtAdder);
    }

    public void modifyDamageDealtAdder(double damageDealtAdder) {
        this.damageDealtAdder += damageDealtAdder;
    }
    
    public void modifySpawnCostMultiplier(double spawnCostMultiplier)
    {
    	this.spawnCostMultiplier = spawnCostMultiplier;
    }
    
    public int getMovementDelayAdder()
    {
    	return movementDelayAdder;
    }
    
    public void modifyMovementDelayAdder(int movementDelayAdder)
    {
    	this.movementDelayAdder += movementDelayAdder;
    }
    
    public double getSpawnCostMultiplier()
    {
    	return returnValue(this.spawnCostMultiplier);
    }
    
    public boolean isBehaviorOn()
    {
    	int startTime = robot.myGameWorld.getCurrentRound() - this.behaviorIntervalStart;
    	double alternatedCount;
    	if(this.behaviorIntervalLength==0)
    		return true;
    	if(startTime>=0)
    	{
    		alternatedCount = Math.floor(new Double(startTime).doubleValue() / new Double(this.behaviorIntervalLength).doubleValue()); 
    		if(alternatedCount%2==0)
    			return true;
    	}
    	
    	return false;
    }
    
    public double returnValue(double value)
    {
    	if(isBehaviorOn())
    	{
    		return value;
    	}
    	return 0.0;
    }

	
}
