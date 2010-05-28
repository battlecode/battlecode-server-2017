/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package battlecode.world;

import battlecode.common.Team;
import battlecode.server.Config;

import java.util.Iterator;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Sasa
 */
public class InternalRobotBuffs {

    private final EnumMap<BuffType, InternalBuff> buffs = new EnumMap<BuffType, InternalBuff>(BuffType.class);
    private final InternalRobot robot;
    private double energonRegen, damageDealtMultiplier, damageReceivedMultiplier, damageDealtAdder;
    private int movementDelayAdder, attackDelayAdder, turnDelayAdder;
    public static double ENERGON_REGEN_A, DAMAGE_DEALT_MULTIPLIER_A, DAMAGE_RECIEVED_MULTIPLIER_A, DAMAGE_DEALT_ADDER_A;
    public static int MOVEMENT_DELAY_ADDER_A, ATTACK_DELAY_ADDER_A, TURN_DELAY_ADDER_A;
    public static double ENERGON_REGEN_B, DAMAGE_DEALT_MULTIPLIER_B, DAMAGE_RECIEVED_MULTIPLIER_B, DAMAGE_DEALT_ADDER_B;
    public static int MOVEMENT_DELAY_ADDER_B, ATTACK_DELAY_ADDER_B, TURN_DELAY_ADDER_B;

    InternalRobotBuffs(InternalRobot robot) {
        this.robot = robot;
        Config options = Config.getGlobalConfig();

        
        if(options.get("drw.ENERGON_REGEN_A")!=null)
        {
        	this.ENERGON_REGEN_A = new Double(options.get("drw.ENERGON_REGEN_A")).doubleValue();
        }
    
        if(options.get("drw.ENERGON_REGEN_B")!=null)
        {
        	this.ENERGON_REGEN_B = new Double(options.get("drw.ENERGON_REGEN_B")).doubleValue();
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
        
        if(options.get("drw.MOVEMENT_DELAY_ADDER_A")!=null)
        {
        	this.MOVEMENT_DELAY_ADDER_A = new Integer(options.get("drw.MOVEMENT_DELAY_ADDER_A")).intValue();
        }
    
        if(options.get("drw.MOVEMENT_DELAY_ADDER_B")!=null)
        {
        	this.MOVEMENT_DELAY_ADDER_B = new Integer(options.get("drw.MOVEMENT_DELAY_ADDER_B")).intValue();
        }
        
        if(options.get("drw.ATTACK_DELAY_ADDER_A")!=null)
        {
        	this.ATTACK_DELAY_ADDER_A = new Integer(options.get("drw.ATTACK_DELAY_ADDER_A")).intValue();
        }
    
        if(options.get("drw.ATTACK_DELAY_ADDER_B")!=null)
        {
        	this.ATTACK_DELAY_ADDER_B = new Integer(options.get("drw.ATTACK_DELAY_ADDER_B")).intValue();
        }
        
        if(options.get("drw.TURN_DELAY_ADDER_A")!=null)
        {
        	this.TURN_DELAY_ADDER_A = new Integer(options.get("drw.TURN_DELAY_ADDER_A")).intValue();
        }
    
        if(options.get("drw.TURN_DELAY_ADDER_B")!=null)
        {
        	this.TURN_DELAY_ADDER_B = new Integer(options.get("drw.TURN_DELAY_ADDER_B")).intValue();
        }
        
//       print();
    }
    
    public void print()
    {
    	System.out.println("this.ATTACK_DELAY_ADDER_A: " + this.ATTACK_DELAY_ADDER_A);
    	System.out.println("this.ATTACK_DELAY_ADDER_B: " + this.ATTACK_DELAY_ADDER_B);
    	System.out.println("this.DAMAGE_DEALT_ADDER_A: " + this.DAMAGE_DEALT_ADDER_A);
    	System.out.println("this.DAMAGE_DEALT_ADDER_B: " + this.DAMAGE_DEALT_ADDER_B);
    	System.out.println("this.DAMAGE_DEALT_MULTIPLIER_A: " + this.DAMAGE_DEALT_MULTIPLIER_A);
    	System.out.println("this.DAMAGE_DEALT_MULTIPLIER_B: " + this.DAMAGE_DEALT_MULTIPLIER_B);
    	System.out.println("this.DAMAGE_RECIEVED_MULTIPLIER_A: " + this.DAMAGE_RECIEVED_MULTIPLIER_A);
    	System.out.println("this.DAMAGE_RECIEVED_MULTIPLIER_B: " + this.DAMAGE_RECIEVED_MULTIPLIER_B);
    	System.out.println("this.ENERGON_REGEN_A: " + this.ENERGON_REGEN_A);
    	System.out.println("this.ENERGON_REGEN_B: " + this.ENERGON_REGEN_B);
    	System.out.println("this.MOVEMENT_DELAY_ADDER_A: " + this.MOVEMENT_DELAY_ADDER_A);
    	System.out.println("this.MOVEMENT_DELAY_ADDER_B: " + this.MOVEMENT_DELAY_ADDER_B);
    	System.out.println("this.TURN_DELAY_ADDER_A: " + this.TURN_DELAY_ADDER_A);
    	System.out.println("this.TURN_DELAY_ADDER_B: " + this.TURN_DELAY_ADDER_B);
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
        energonRegen = (robot.getTeam() == Team.A) ? ENERGON_REGEN_A : ENERGON_REGEN_B;
        damageDealtMultiplier = (robot.getTeam() == Team.A) ? DAMAGE_DEALT_MULTIPLIER_A : DAMAGE_DEALT_MULTIPLIER_B;
        damageReceivedMultiplier = (robot.getTeam() == Team.A) ? DAMAGE_RECIEVED_MULTIPLIER_A : DAMAGE_RECIEVED_MULTIPLIER_B;
        damageDealtAdder = (robot.getTeam() == Team.A) ? DAMAGE_DEALT_ADDER_A : DAMAGE_DEALT_ADDER_B;
        movementDelayAdder = (robot.getTeam() == Team.A) ? MOVEMENT_DELAY_ADDER_A : MOVEMENT_DELAY_ADDER_B;
        attackDelayAdder = (robot.getTeam() == Team.A) ? ATTACK_DELAY_ADDER_A : ATTACK_DELAY_ADDER_B;
        turnDelayAdder = (robot.getTeam() == Team.A) ? TURN_DELAY_ADDER_A : TURN_DELAY_ADDER_B;
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
    public int getMovementDelayAdder() {
        return movementDelayAdder;
    }

    public void modifyMovementDelayAdder(int movementDelayAdder) {
        this.movementDelayAdder += movementDelayAdder;
    }

    public int getAttackDelayAdder() {
        return attackDelayAdder;
    }

    public void modifyAttackDelayAdder(int attackDelayAdder) {
        this.attackDelayAdder += attackDelayAdder;
    }

    public int getTurnDelayAdder() {
        return turnDelayAdder;
    }

    public void modifyTurnDelayAdder(int turnDelayAdder) {
        this.turnDelayAdder += turnDelayAdder;
    }

    public double getEnergonRegen() {
        return energonRegen;
    }

    public void modifyEnergonRegen(double energonRegen) {
        this.energonRegen += energonRegen;
    }

    public double getDamageDealtMultiplier() {
        return damageDealtMultiplier;
    }

    // accumulating auras is overpowered so just take the largest one
    public void modifyDamageDealtMultiplier(double damageDealtMultiplier) {
        this.damageDealtMultiplier += damageDealtMultiplier;
    }

    public double getDamageReceivedMultiplier() {
        return damageReceivedMultiplier;
    }

    public void modifyDamageReceivedMultiplier(double damageReceivedMultiplier) {
        this.damageReceivedMultiplier += damageReceivedMultiplier;
    }

    public double getDamageDealtAdder() {
        return damageDealtAdder;
    }

    public void modifyDamageDealtAdder(double damageDealtAdder) {
        this.damageDealtAdder += damageDealtAdder;
    }
}
