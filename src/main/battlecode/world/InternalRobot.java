package battlecode.world;

import static battlecode.common.GameConstants.*;

import java.util.LinkedList;
import java.util.List;

import battlecode.common.ActionType;
import battlecode.common.AuraType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.RobotLevel;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.engine.ErrorReporter;
import battlecode.engine.GenericRobot;
import battlecode.world.signal.BroadcastSignal;
import battlecode.world.signal.DeathSignal;
import battlecode.world.signal.DoTeleportSignal;
import battlecode.engine.signal.Signal;

public class InternalRobot extends InternalObject implements Robot, GenericRobot {

    private volatile double myEnergonLevel;
    private volatile double myEnergonReserve;
    protected volatile Direction myDirection;
    // myActionType is deprecated
    private volatile ActionType myActionType;
    private volatile int roundsUntilAttackIdle;
    private volatile int roundsUntilMovementIdle;
    private volatile RobotType myRobotType;
    private volatile Message broadcastQueue = null;
    private volatile Signal actionQueue = null;
    private volatile boolean energonChanged = true;
    private volatile GameActionException lastException = null;
    protected volatile double myMaxEnergon;
    protected volatile long controlBits;
    protected volatile boolean hasBeenAttacked = false;
    private static boolean upkeepEnabled = true;
    private static boolean spawnRadiusEnforced = true;
    // first index is robot type, second is direction, third is x or y
    private static final int[][][][] offsets = GameMap.computeVisibleOffsets();
    // number of bytecodes used in the most recent round
    private volatile int bytecodesUsed = 0;
    protected volatile InternalRobot fromTeleporter;
    protected volatile InternalRobot toTeleporter;
    protected volatile MapLocation teleportLoc;
    // don't use roundsUntilMovementIdle because
    // roundsUntilMovementIdle could increase
    // if the robot evolves while waiting for
    // the teleport to happen
    protected volatile int roundsUntilTeleport;
    private volatile double myFlux;
    private volatile boolean fluxChanged = false;
    // these used to be final but we want to free their
    // memory when the robot dies
    private List<Message> incomingMessageQueue;
    protected GameMap.MapMemory mapMemory;
    private InternalRobotBuffs buffs = new InternalRobotBuffs(this);

    public InternalRobotBuffs getBuffs() {
        return buffs;
    }

    public InternalRobot(GameWorld gw, RobotType type, MapLocation loc, Team t,
            boolean spawnedRobot) {
        super(gw, loc, (type.isAirborne() ? RobotLevel.IN_AIR
                : RobotLevel.ON_GROUND), t);
        myDirection = Direction.values()[gw.getRandGen().nextInt(8)];



        if (spawnedRobot) {
            setAction(ActionType.WAKING, type.wakeDelay());
        } else {
            setAction(ActionType.IDLE, 0);
        }

        myRobotType = type;
        myEnergonLevel = type.startEnergon();
        myEnergonReserve = 0.0;
        // ~ incomingMessageQueue = Collections.synchronizedList(new
        // LinkedList<Message>());
        // ~ broadcastQueue = Collections.synchronizedList(new
        // LinkedList<Message>());
        // ~ actionQueue = Collections.synchronizedList(new
        // LinkedList<Signal>());
        incomingMessageQueue = new LinkedList<Message>();

        myMaxEnergon = type.maxEnergon();
        mapMemory = new GameMap.MapMemory(gw.getGameMap());
        saveMapMemory(null, loc, false);
        controlBits = 0;
        //indicatorStrings = new String[NUMBER_OF_INDICATOR_STRINGS];
        if (type == RobotType.TELEPORTER) {
            myGameWorld.notifyAddingNewTeleporter(this);
        }
    }

    public static void setUpkeepEnabled(boolean upkeepEnabled) {
        InternalRobot.upkeepEnabled = upkeepEnabled;
    }

    public static void setSpawnRadiusEnforcement(boolean enforce) {
        InternalRobot.spawnRadiusEnforced = enforce;
    }

    public static boolean getSpawnRadiusEnforcement() {
        return InternalRobot.spawnRadiusEnforced;
    }

    @Override
    public void processBeginningOfRound() {
        super.processBeginningOfRound();
        buffs.processBeginningOfRound();

        if (roundsUntilAttackIdle > 0) {
            roundsUntilAttackIdle--;
        }
        if (roundsUntilMovementIdle > 0) {
            roundsUntilMovementIdle--;
        }

        if ((roundsUntilAttackIdle == 0) && (roundsUntilMovementIdle == 0)) {
            myActionType = ActionType.IDLE;
        }
        // Removing since actions already happen at end of round.
        //executeAction();

    }

    public void processBeginningOfTurn() {
        if (teleportLoc != null) {
            if (--roundsUntilTeleport <= 0) {
                if (myGameWorld.isExistant(fromTeleporter) && myGameWorld.isExistant(toTeleporter) && myGameWorld.canMove(myRobotType, teleportLoc)) {
                    new DoTeleportSignal(this, teleportLoc).accept(myGameWorld);
                }
                teleportLoc = null;
            }
        }

        if (myRobotType == RobotType.WOUT) {
            myGameWorld.mineFlux(this);
        }
    }

    @Override
    public void processEndOfTurn() {
        super.processEndOfTurn();

        executeAction();

        if (broadcastQueue != null) {
            Message msg = broadcastQueue;
            // ~ System.out.println("found message!");
            myGameWorld.addSignal(new BroadcastSignal(this));
            myGameWorld.broadcastMessage(this, msg);
            changeEnergonLevel(-(BROADCAST_FIXED_COST + (BROADCAST_COST_PER_BYTE * msg.getNumBytes())));
            broadcastQueue = null;
        }
    }

    @Override
    public void processEndOfRound() {
        super.processEndOfRound();

        if (upkeepEnabled) {
            changeEnergonLevel(-myRobotType.energonUpkeep());
        }

        double buffEnergonMod = buffs.getEnergonRegen();
        if (buffEnergonMod > 0) {
            this.receiveTransfer(buffs.getEnergonRegen());
        } else if (buffEnergonMod < 0) {
            this.changeEnergonLevel(buffEnergonMod);
        }

        if (myEnergonReserve > 0.0) {
            double amountToTransfer = Math.min(ENERGON_TRANSFER_RATE,
                    myMaxEnergon - myEnergonLevel);
            amountToTransfer = Math.min(amountToTransfer,
                    myEnergonReserve);
            myEnergonReserve -= amountToTransfer;
            changeEnergonLevel(amountToTransfer);
        }


        buffs.processEndOfRound();
        if (myEnergonLevel <= 0) {
            suicide();
        }
    }

    public double getEnergonLevel() {
        return myEnergonLevel;
    }

    public double getEnergonReserve() {
        return myEnergonReserve;
    }

    public double getEventualEnergonLevel() {
        return Math.min(myEnergonLevel + myEnergonReserve, myMaxEnergon);
    }

    public void receiveTransfer(double amount) {
        myEnergonReserve += Math.min(amount, ENERGON_RESERVE_SIZE - myEnergonReserve);
    }

    public Direction getDirection() {
        return myDirection;
    }

    public Signal getCurrentActionSignal() {
        return actionQueue;
    }

    public void setAction(ActionType action, int numRounds) {
        myActionType = action;
        if (action.hasAttackCooldown()) {
            roundsUntilAttackIdle = numRounds + buffs.getAttackDelayAdder();
        }
        if (action.hasMovementCooldown()) {
            if (action == action.SETTING_DIRECTION) {
                roundsUntilMovementIdle = numRounds + buffs.getTurnDelayAdder();
            } else if (action == action.MOVING) {
                roundsUntilMovementIdle = numRounds + buffs.getMovementDelayAdder();
            } else {
                roundsUntilMovementIdle = numRounds;
            }
        }
    }

    public void changeEnergonLevelFromAttack(double amount) {
        hasBeenAttacked = true;
        changeEnergonLevel(amount * (buffs.getDamageReceivedMultiplier() + 1));
    }

    public void changeEnergonLevel(double amount) {
        myEnergonLevel += amount;
        if (myEnergonLevel > myMaxEnergon) {
            myEnergonLevel = myMaxEnergon;
        }
        energonChanged = true;
    }

    public boolean clearEnergonChanged() {
        if (energonChanged) {
            energonChanged = false;
            return true;
        } else {
            return false;
        }
    }

    public double getMaxEnergon() {
        return myMaxEnergon;
    }

    public void setDirection(Direction dir) {
        myDirection = dir;
        saveMapMemory(getLocation(), getLocation(), false);
    }

    public int getRoundsUntilAttackIdle() {
        return roundsUntilAttackIdle;
    }

    public int getRoundsUntilMovementIdle() {
        return roundsUntilMovementIdle;
    }

    public void suicide() {
        (new DeathSignal(this)).accept(myGameWorld);
    }

    public RobotType getRobotType() {
        return myRobotType;
    }

    public void setRobotType(RobotType rt) {
        myRobotType = rt;

        double excessEnergon = myEnergonLevel - rt.maxEnergon();
        if (excessEnergon > 0.0) {
            // Cap energon to maxEnergon
            myEnergonLevel = rt.maxEnergon();

            // Give excess energon to reserve
            myEnergonReserve += excessEnergon;

            // Clamp energonReserve to max
            if (myEnergonReserve > ENERGON_RESERVE_SIZE) {
                myEnergonReserve = ENERGON_RESERVE_SIZE;
            }
        }

        myMaxEnergon = rt.maxEnergon();

        // update map memory after transformation
        saveMapMemory(getLocation(), getLocation(), false);

        buffs.removeBuff(BuffType.TURRET_DEPLOY);
    }

    public void enqueueIncomingMessage(Message msg) {
        incomingMessageQueue.add(msg);
    }

    public Message dequeueIncomingMessage() {
        if (incomingMessageQueue.size() > 0) {
            return incomingMessageQueue.remove(0);
        } else {
            return null;
        }
        // ~ return incomingMessageQueue.poll();
    }

    public Message[] dequeueIncomingMessages() {
        Message[] result = incomingMessageQueue.toArray(new Message[incomingMessageQueue.size()]);
        incomingMessageQueue.clear();
        return result;
    }

    public boolean hasBroadcastMessage() {
        return (!(broadcastQueue == null));
    }

    public boolean enqueueMessageToBroadcast(Message msg)
            throws GameActionException {
        // ~ System.out.println("enqueueing message");
        // ~ System.out.println(broadcastQueue.size());
        if (broadcastQueue != null) {
            throw new GameActionException(
                    GameActionExceptionType.DOUBLE_ACTION_ERROR,
                    "You cannot queue two messages to broadcast in the same round.");
        } else {
            broadcastQueue = (Message) msg.clone();
            // ~ System.out.println(broadcastQueue.size());
            return true;

        }
    }

    public double getBroadcastQueueCost() {
        return (broadcastQueue == null ? 0
                : (BROADCAST_FIXED_COST + (BROADCAST_COST_PER_BYTE * broadcastQueue.getNumBytes())));
    }

    public void clearBroadcastQueue() {
        broadcastQueue = null;
    }

    public void queueAction(Signal s) throws GameActionException {
        if (actionQueue != null) {
            throw new GameActionException(
                    GameActionExceptionType.DOUBLE_ACTION_ERROR,
                    "You cannot queue two actions in the same round.");
        }

        actionQueue = s;
    }

    public boolean hasActionQueued() {
        return actionQueue != null;
    }

    public void clearActionQueue() {
        actionQueue = null;
    }

    public void executeAction() {
        // ~ Signal s = actionQueue.poll();
        if (actionQueue != null) {
            Signal s = actionQueue;
            actionQueue = null;
            // ~ System.out.println("execute: " + Clock.getRoundNum());
            // ~ System.out.println("found signal");
            // ~ System.out.println(s.accept(new SignalDisplayHandler()));
            Exception e = s.accept(myGameWorld);
            if (e != null) {
                if (e instanceof GameActionException) {
                    lastException = (GameActionException) e;
                    // TODO: do we need this?
                    clearActionQueue();
                } else {
                    e.printStackTrace();
                    ErrorReporter.report("Unrecognized Exception when action was executed.");
                }
            }
        }
    }

    public GameActionException getLastException() {
        GameActionException result = lastException;
        lastException = null;
        return result;
    }

    public GameMap.MapMemory getMapMemory() {
        return mapMemory;
    }

    public void saveMapMemory(MapLocation oldLoc, MapLocation newLoc,
            boolean fringeOnly) {
        int[][] myOffsets = offsets[myRobotType.ordinal()][myDirection.ordinal()];
        mapMemory.rememberLocations(newLoc, myOffsets[0], myOffsets[1]);
    }

    @Override
    public void setLocation(MapLocation newLoc) {
        MapLocation oldLoc = getLocation();
        super.setLocation(newLoc);
        saveMapMemory(oldLoc, newLoc, false);
    }

    public void setControlBits(long l) {
        controlBits = l;
    }

    public long getControlBits() {
        return controlBits;
    }

    public void setBytecodesUsed(int numBytecodes) {
        bytecodesUsed = numBytecodes;
    }

    public int getBytecodesUsed() {
        return bytecodesUsed;
    }
	
	public int getBytecodeLimit() {
		return BYTECODE_LIMIT_BASE;
	}


    public boolean hasBeenAttacked() {
        return hasBeenAttacked;
    }

    @Override
    public String toString() {
        return getTeam().toString() + ":" + myRobotType.toString() + "#" + getID();
    }

    public double attackPower() {

        
        double attackPower = myRobotType.attackPower();
        
        return (attackPower + buffs.getDamageDealtAdder()) * (1 + buffs.getDamageDealtMultiplier());

    }

    public void setTeleportAction(InternalRobot fromTeleporter, InternalRobot toTeleporter, MapLocation teleportLoc) {
        this.fromTeleporter = fromTeleporter;
        this.toTeleporter = toTeleporter;
        this.teleportLoc = teleportLoc;
        // Don't use setAction, because buffs
        // shouldn't apply to teleporting
        myActionType = ActionType.TELEPORTING;
        if (roundsUntilMovementIdle < TELEPORT_DELAY) {
            roundsUntilMovementIdle = TELEPORT_DELAY;
        }
        roundsUntilTeleport = TELEPORT_DELAY;
    }

    public void incrementFlux(double amount) {
        changeFluxLevel(myFlux + amount);
        //myGameWorld.adjustTeamPoints(this, amount);
    }

    public void changeFluxLevel(double amount) {
        if (amount < 0) amount = 0;
        else if (amount > myRobotType.maxFlux()) amount = myRobotType.maxFlux();
        fluxChanged = true;
        myFlux = amount;
    }

    public boolean clearFluxChanged() {
        if (fluxChanged) {
            fluxChanged = false;
            return true;
        }
        return false;
    }

    public double getFlux() {
        if (myRobotType.isBuilding())
            return myEnergonLevel * ENERGON_TO_FLUX_CONVERSION;
        else
            return myFlux;
    }

    public boolean isDeployed() {
        if (myRobotType == RobotType.TURRET) {
            return buffs.containsBuff(BuffType.TURRET_DEPLOY);
        } else {
            return false;
        }
    }

    public boolean isTeleporting() {
        return teleportLoc != null;
    }

    public AuraType getAura() {
        return null;
    }

    public AuraType getLastAura() {
        return null;
    }

    public void freeMemory() {
        incomingMessageQueue = null;
        mapMemory = null;
        buffs = null;
        lastException = null;
    }
}
