Battlecode 2013 Gameplay Specs
==============================

Plot
--------

After being foiled yet another year by the power of fun gamers, the evil Professor Mordemort was captured and placed in the most secured cell in the Galactic Hold. Now, it is one year after those terrible events, and peace reigns supreme in the galaxy, new and faster methods of reaching far off planets being provided by Vanqeri Technologies, and the Interuniverse Defense League is stronger than ever. However, there are rumors of an unknown force that is attacking planets on the outer rim of the galaxy, an enemy that is rumored to be using ancient technology and trench warfare to gain the upper-hand. The IDL is calling on Coders throughout the galaxy to help combat this unknown enemy before the devastation they are causing reaches the more populated worlds.


Objective
----------------

The objective this year is simple. Violently eliminate the opponent's HQ through a constant barrage of soldiers. There are many encampments across the map that may aid in this endeavor, allowing you to fortify positions and take map control. Should you need them, you may also research a number of upgrades that provide you with a large tactical advantage on the battlefield.

Good luck!


Changes from 2012
------------------

There are a number of large changes from previous battlecode years, so veterans should bear these in mind:

- Archons are out. There is one HQ that acts as the archon replacement.
- Robots no longer face a direction. There is no turning, and no concept of forwards and backwards. Instead, robots can just move to any adjacent square.
- There are no longer voids on the map. Every square on the map is traversable, and there is no longer any distinction between ground and air units.
- Both teams start off knowing exactly what the map looks like. They know where all the map edges are and know where the enemy spawns. 
- There are no movement or attack delays(though some actions do still incur a cooldown). Soldiers move and attack once every turn.
- Units always have shared vision and can sense anything within the vision radius of any allied unit.
- Flux has been renamed to power; now it is globally shared among all of a team's units, so flux transfer has been removed.
- Broadcasting has been completely altered. Both teams now write to a global array of integers. Broadcasting no longer relies on robots being within a broadcast radius.

NOTE: Numbers in these specs are provided mainly for readability and are subject to change in future patches. Check the API for details.


Robot Overview
-----------------

Robots are the central part of the Battlecode world. There are two types of basic robots. Note that we use the terms 'robot' and 'unit' interchangeably.

### HQ
The HQ is your main base, and by far the most important unit you have. Each team starts the game off with one HQ. You lose the game if your HQ is destroyed. The HQ can spawn SOLDIERs and research TECH upgrades. If the game reaches the round limit, the HQs will also begin taking end-of-round damage to break ties. The HQ can never regain energon, and there is no way to build additional HQs.

### SOLDIER
SOLDIERs form the core of your army. They are the only mobile unit, and are created by the HQ. They allow you to expand your map control by capturing additional encampments. They can also lay mines and defuse mines. They automatically attack enemies adjacent to them every turn.

Encampments
------------

In addition to the two basic units, there are units called ENCAMPMENTS that soldiers can create on top of unoccupied encampment squares.

If a soldier is located on an encampment square, it has the option to capture the square. If it does so, it sits still for `GameConstants.CAPTURE_DELAY` turns and then turns into one of the following encampments (it must choose which encampment to create before the delay):

1. **MEDBAY**: Heals itself and all adjacent allied units for 2 (`MEDBAY.attackPower`) health per turn. It cannot heal the HQ.

2. **SHIELDS**: The shield encampment gives 5 (`SHIELDS.attackPower`) shields per turn to itself and all adjacent allied units. This shield absorbs damage from artillery fire.

3. **ARTILLERY**: The artillery encampment allows a unit to attack any location within its attack range `ARTILLERY.attackRadiusMaxSquared`, causing 40 (`ARTILLERY.attackPower`) damage to the attacked square, and 20 (`ARTILLERY.attackPower*GameConstants.ARTILLERY_SPLASH_RATIO`) splash damage to the adjacent squares. The artillery can damage friendly units, so watch where you fire. It can fire once every 20 (`ARTILLERY.attackDelay`) turns.

4. **GENERATOR**: Each generator provides the team with 10 (`GameConstants.GENERATOR_RESOURCE_PRODUCTION`) extra power per turn.

5. **SUPPLIER**: Each supplier allows the HQ to spawn soldiers at a faster rate. See the spawning section for details.

The other team may retake an encampment square if the existing encampment on it is destroyed. Capturing has a cost, however. The first capture costs `GameConstants.CAPTURE_POWER_COST` to start, and each new capture costs `GameConstants.CAPTURE_POWER_COST` per encampment owned or in the process of capturing. Starting a capture costs `GameConstants.CAPTURE_POWER_COST*( 1 + ENCAMPMENTS_OWNED + ENCAMPMENTS_BEING_CAPTURED)`.


Robot Resources
------------------

There are three types of resources, POWER and ENERGON, and SHIELDS.

### Energon
Energon is a robot's health. When a robot's energon hits zero, it is immediately removed from the game. Energon can only be regained by healing at a MEDBAY.

### Power

Power is shared globally across the team and is generated by the HQ at 40 power per round (`GameConstants.HQ_RESOURCE_PRODUCTION`).  10 Additional power can be generated by GENERATOR encampments (`GENERATOR_RESOURCE_PRODUCTION`).

Units pay 2 power every turn for upkeep. If enough power is not available, the unit must pay its upkeep using 5 (`GameConstants.UNIT_ENERGON_UPKEEP`) energon. If it doesn't have this much energon, it dies instantly.

At the end of a robot's turn, if the bot did not use its full bytecode allocation, a fraction of the power is refunded based on bytecodes used, according to the formula `POWER_REFUND = (GameConstants.POWER_COST_PER_BYTECODE)*(GameConstants.BYTECODE_LIMIT-BYTECODES_SPENT)`. If the robot uses no bytecodes, it will only spend 1 power in upkeep this turn. If it uses 2000 bytecodes, it will spend 1.2 power in upkeep. If it uses 7500 bytecodes, it will spend 1.75 power in upkeep.

Unused power is stockpiled and carried over into future rounds. However, at the end of each round, 20% (or 1% if fusion has been upgraded) of each team's power stockpile is removed due to power decay. 

### Shields

Shields are generated from the SHIELD encampment. They protect soldiers from artillery shots and mine damage. One shield point blocks one damage from an artillery shot, either direct damage or splash damage. Shields also block up to 75% (`GameConstants.MINE_DAMAGE_RATIO_ABSORBED_BY_SHIELD`) of mine damage. When a soldier takes mine damage, up to 75% of the damage taken will be reduced from shields, and the remaining mine damage will be reduced from energon.

Shields decay at a constant rate of 0.5 (`GameConstants.SHIELD_DECAY_RATE`) per turn. A soldier's shield pool is bound between 0 and 100000000 (`GameConstants.SHIELD_CAP`).

Mines
-----

Soldiers can lay mines throughout the map to help defend territory and catch enemy robots off guard. All soldiers have the ability to lay a mine on their current square, taking 25 (`GameConstants.MINE_LAY_DELAY`) turns to do so. During this time, it cannot perform any other actions (defuse, capture, move) and does not auto-attack. If it is killed during this time, the mine does not get planted. Mines belong to a team, and will not damage robots of that team.

Once a mine is planted, they stay there until they are defused. Mines do not "blow up" when they do damage. Enemy robots on the mined square will take 10 (`GameConstants.MINE_DAMAGE`) damage per turn, every turn they end on the mine. Only one mine can be in one square at a time; they cannot be stacked. You can try to mine squares that are already mined (either by you or the enemy), but it will just be a waste. Encampment squares can be mined, and the HQ squares can be mined also. However, only SOLDIERs take damage from mines.

Enemy mines are not visible until they are stepped on. If you try to sense a mine your opponent just laid, they will sense that there is no mine there. Once you enter the square with any soldier, all your soldiers will be able to sense that an enemy mine is there. Allied mines are always visible. Mines do not provide any sight, but you always know which squares you have mines on, and you can tell when they get defused by repeatedly sensing that square for mines.

With the PICKAXE upgrade, mining is upgraded to simultaneously mining on the soldier's current square as well as the four squares orthogonally adjacent to it. This can even allow you to mine squares containing enemy robots (even their HQ).

### Defusion

A mine must be defused to be removed from the map. To defuse a mine, a soldier must be adjacent to the mine. It must take 12 (`GameConstants.MINE_DEFUSE_DELAY`) turns to defuse the mine, during which it cannot perform other actions and cannot auto-attack. If the soldier is killed during this time, the mine is not defused. Soldiers can only defuse one mine at a time, and two soldiers trying to defuse the same mine won't defuse it any faster. Once the defusion upgrade is researched, the time it takes to defuse the mine is reduced to 5 (`GameConstants.MINE_DEFUSE_DEFUSION_DELAY`), and the soldier can defuse any mine within its sensor range.

How defusion actually works is that soldiers target a square to defuse. They do not have to know there is a mine there to defuse. After the defusion time, any mine in that location will be removed, even if they aren't able to sense it, and even if the mine was not there when the soldier started defusing. You can accidentally defuse your own mines. If you defuse any mine, your opponent will be able to sense that there is no longer any mine there. 

### Neutral mines

The map often starts off with lots of neutral mines. These belong to the neutral team. These are just like mines planted by either team in terms of damaging robots that move across. They can be defused just like any other mine. Both teams start off knowing the locations of all neutral mines on the map, and do not need to enter the square to be able to sense them. 

Note that you can sense exactly where the neutral mines on the map are. This means when the enemy defuses neutral mines, you can tell, although this is fairly expensive in bytecodes.

Upgrades
--------

Upgrades are researchable from your HQ. The following upgrades are available:

1. **Pickaxe**: When a soldier mines, in addition to mining the square it is on, it also mines each of the four orthogonally adjacent squares.
2. **Defusion**: Soldiers can defuse mines not only in adjacent squares, but in all squares in its personal sight radius. They also defuse mines significantly faster. They still must defuse one mine at a time.
3. **Vision**: Increases the personal sensor radius on all robots from 14 to 33 units squared.
4. **Fusion**: The team's power decay rate is adjusted from 20% `RESOURCE_DECAY_RATE` to 1% `RESOURCE_DECAY_RATE_FUSION`.
5. **Nuke**: You immediately win.

An upgrade must be fully researched before its abilities kick in. The HQ can put one point into an upgrade per turn (unless it is spawning soldiers), and the upgrade is considered completed when `numRounds` of points have been put into it.


Victory Conditions
------------------

A team wins by destroying the enemy's HQ, or by researching the nuke upgrade. We expect most games to end by HQ destruction. The nuke upgrade is intended to be used in extreme stalemate situations. After exactly 2000 rounds (`GameConstants.ROUND_LIMIT`), the HQ starts taking damage at a rate of 1 (`GameConstants.TIME_LIMIT_DAMAGE`) energon per turn. This means the game must be over after 2500 rounds. If both teams lose their HQs from end of round damage on the same turn, then the following tiebreakers are applied in order to determine the winner:

- Total # Encampments
- Total # Energon across all robots
- Total # Mines
- Total Team Power
- Lowest ID


Robot Actions
--------------

Robots are equipped with a variety of high tech equipments and can perform the following actions during their turn.

### Sensors

All robots are equipped with personal sensors. These sensors have a sensor range of 14 units squared, which is the same for all robots, and which can be upgraded with the VISION upgrade to 33 units squared. Other than hidden enemy mines, robots can sense everything in their personal sensor range, as well the personal sensor range of every other robot on its team.

- The info on all allied robots can be sensed.
- The info on visible enemy robots can be sensed.
- The positions of detected enemy mines can be sensed. Mines are detected after an allied unit has stepped on it. These mines do not have to be in sensing range after they have been detected.
- The positions of all neutral mines on the map are automatically known at game start, and can be accurately sensed at any time, even if they are out of the range of all of the sensor range of all allied robots.
- The positions of allied mines can be accurately sensed, even if they are out of the range of all of the sensor range of all allied robots.
- The locations of the both HQs can be sensed.
- Your own team's upgrade progress can be sensed, but only by the HQ.
- The HQ can sense whether the enemy's nuke progress has reached the halfway mark.

### Messaging

In Battlecode, robots do not have the ability to access each others' internal memory. Each robot runs on its own thread and cannot access the other robot objects directly. How different robots on a team communicate is by messaging.

This year, there is a global message board accessible to all robots that supports read/write operations. It works as follows:

* The message board is essentially an array of ints. Each position in this array is called a 'channel'. The channels are numbered from 0 to 65535 (`GameConstants.BROADCAST_MAX_CHANNELS`), inclusive. Robots can 'broadcast' to these channels, which is essentially just storing an int in a specified position of this array.
* There is only _one_ global message board and all robots from both teams access the same one
* During each robot's turn, it may write to any channel via `rc.broadcast(channel)` and read what has last been broadcasted on any channel via `rc.readBroadcast(channel)`.
* There is a power cost associated with both reading (`GameConstants.BROADCAST_SEND_COST`) and writing (`GameConstants.BROADCAST_READ_COST`) to the board. A robot can read and write to the message board as many times as it wants in one turn, as long as the team can pay this power cost. 
* Once a message is written to the board, it persists until it is overwritten, or until the end of the game.
* At the beginning of a game, all channels are initialized to 0.

This message board is globally accessible by _any_ robot and is used for communicating whatever the AIs wish to communicate. It can be used to coordinate your army, distribute computation, or call for reinforcements. Since both teams use the same message board, robots' broadcasts may interfere with each other, either by accident or on purpose. Because of this, it is not guaranteed that a broadcast will be able to reach other robots, as the data in a channel may be overwritten before the intended recipient robot gets a turn to read the channel.

### Autoattack

SOLDIERs, SHIELDs, and MEDBAYs autoattack. This attacking cannot be disabled by the player; it will happen at the end of the turn if a robot is still alive and not in the middle of performing an action (mining, defusing, capturing). 

SOLDIERs will deal 6 (`SOLDIER.attackPower`) damage to enemies per turn automatically, if it ends the turn adjacent to any enemies. This damage is split evenly between all the adjacent enemies, so if it is adjacent to four enemies, it will deal 1.5 damage to each. Any enemies whose energon gets reduced to 0 or below after this are immediately removed from the game. Overkill damage is wasted, so if a soldier is adjacent to two enemy robots with 1 energon and 40 energon left, one robot will die and the other will be reduced to 37 energon.

SHIELDs automatically add 5 (`SHIELD.attackPower`) shields to adjacent allies and itself every turn. This is 5 per robot, so it can potentially shield multiple robots for a total of 45 shields added. Shield decay still happens while a robot is next to a SHIELD, so a SHIELD will add 5 shields to itself and then lose 1 (`GameConstants.SHIELD_DECAY_RATE`) shield to decay every turn, ending with a net gain of 4. SHIELDs can shield each other, and multiple SHIELDs can add shields to the same robot on the same round.

MEDBAYs automatically replenish 2 (`MEDBAY.attackPower`) energon to adjacent allies and itself every turn. This is 2 energon per robot, so it can potentially heal multiple robots for a total of 18 energon healed. This healing does not affect the HQ. Energon cannot exceed maximum energon. MEDBAYs can heal each other, and multiple MEDBAYs can heal the same robot on the same round.

### Movement

Only the SOLDIER has the ability to move. Moving has no power cost.

Every turn, SOLDIERs may move to any unoccupied adjacent square, provided they are not in delay from performing any other action (mining, defusing, capturing). Soldiers can auto-attack on the same turn they move. They cannot mine, defuse, or capture on the same turn they move.

SOLDIERs can move diagonally at the same speed as they can move orthogonally.

### Spawning

The HQ has the ability to continuously spawn SOLDIERs. The action of spawning soldiers does not cost power by itself, but the spawned soldiers will start to consume power via upkeep. After the HQ spawns a SOLDIER, the HQ is unable to do any other actions (spawning or researching) for a brief time, determined by how many SUPPLIERs the team currently has alive.

SOLDIERs can be spawned in any adjacent square that does not already have a robot on it. In the beginning, the HQ may spawn one SOLDIER per 10 (`GameConstants.HQ_SPAWN_DELAY`) rounds. This spawn rate is reduced by SUPPLIERS according to the formula a=r(`GameConstants.HQ_SPAWN_DELAY`*`GameConstants.HQ_SPAWN_DELAY_CONSTANT`/(`GameConstants.HQ_SPAWN_DELAY_CONSTANT`+b)), where a is the number of turns it takes to spawn one unit, b is the number of suppliers you have alive, and r() rounds a number to the nearest positive integer.  Units that are spawned are immediately placed on the field and may perform actions like any other robot. The HQ cannot spawn or research while it is in spawning cool-down.

### Suicide

Calling suicide() immediately kills the robot and removes it from the game. It will no longer consume upkeep in future rounds. If an encampment suicides, then the square is freed up for a potentially different encampment to be created there. If a robot suicides, no power is refunded to the team.

### Team Memory

Official matches will usually be sets of multiple games. Each team can save a small amount of information (`GameConstants.TEAM_MEMORY_LENGTH` longs) for the next game using the function `setTeamMemory()`. This information may be retrieved using `getTeamMemory()`. If there was no previous game in the match, or no information was saved, then the memory will be filled with zeros.

### Control Bits, Indicator Strings, and Breakpoints

There are several ways that the user can interact with robots. First, any robot can use `setIndicatorString(int,String)` to set a string that is visible to the user when selecting the robot. Second, the user can manually set a long for each robot, which the robot can query using `getControlBits()`. Finally, a robot can call `breakpoint()`, which flags the game engine to pause computation at the end of the round. These methods are for debug purposes only. During tournaments and scrimmages, the user will not be able to interact with the robots. For more information on these debugging interfaces, check out Debugging below.

### Ending turn

Calling `yield()` and `suicide()` instantly end the turn of a robot. Otherwise a turn ends naturally when the bytecode limit is hit. Every turn a robot gets 10000 bytecodes to run code. At the end of a robot's turn, if the robot yielded, unused bytecodes will return power back into the team's global power pool. 


Maps
-----

Battlecode maps consist of a grid of squares, each with a pair of integer coordinates. Locations on the map are represented as instances of `MapLocation` objects. 

Maps are always rectangular. All of the squares in the map have the same terrain type, while squares out of bounds will have a different terrain type.
Map coordinates are represented similarly to the pixels on a computer screen: x-coordinates increase moving to the right (East), and y-coordinates increase moving down (South).  The most NORTHWEST square of the map is guaranteed to be coordinate (0,0), and the most SOUTHEAST square will be (WIDTH-1, HEIGHT-1).

Maps specify the spawn points of both teams, as well as where all the encampment squares are. They also specify the locations of all the neutral mines.

### Map Files

Maps are specified by XML files, and can be found in the maps folder of the release archive. The schema for the files should be fairly intuitive, so if you'd like to add your own maps you can use the provided maps as a basis. Each map has an associated random number seed, which the RNG uses to generate random numbers in games played on that map.

### Map Constraints

Official maps used in scrimmages and tournaments must all satisfy the following conditions.

- Maps are completely symmetric either by reflection or 180 degree rotation.
- The width and height of the map are guaranteed to be between 20 and 70, inclusive.
- The map cannot have neutral mines on the 4 squares orthogonally adjacent to either HQ.
- There will be a minimum of 5 encampment squares on the map. 
- It will be possible for a soldier to get adjacent to the enemy HQ by turn 200, even if you only make one soldier and research/capture nothing, and the opposing team does nothing.
- The distance between the spawn points will be at least 10 units (Euclidean distance). 


Writing a Player
------------------------

### Introduction

Your player program must reside in a Java package named `teamXXX`, where `XXX` is your three-digit team number, with leading zeros included. You may have whatever sub-packages you like. You must define `teamXXX.RobotPlayer`, which must have a public static `run` method that takes one argument of type `battlecode.common.RobotController`. Whenever a new robot is created, the game calls the run method with the robots RobotController as its argument. If this method ever finishes, either because it returned or because of an uncaught exception, the robot dies and is removed from the game. You are encouraged to wrap your code in loops and exception handlers so that this does not happen.

###. RobotController

The RobotController argument to the RobotPlayer constructor is very important -- this is how you will control your robot. RobotController has methods for sensing (e.g. `senseRobotInfo(Robot)`) and performing actions (e.g., `move()`). If you're not sure how to get your robot to do something, the Javadocs for RobotController are a good place to start.

### Example: examplefuncsplayer


```java
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/** The example funcs player is a player meant to demonstrate basic usage of the most common commands.
 * Robots will move around randomly, occasionally mining and writing useless messages.
 * The HQ will spawn soldiers continuously. 
 */
public class RobotPlayer {
    public static void run(RobotController rc) {
    while (true) {
      try {
        if (rc.getType() == RobotType.HQ) {
          if (rc.isActive()) {
            // Spawn a soldier
            Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
            if (rc.canMove(dir))
              rc.spawn(dir);
          }
        } else if (rc.getType() == RobotType.SOLDIER) {
          if (rc.isActive()) {
            if (Math.random()<0.005) {
              // Lay a mine 
              if(rc.senseMine(rc.getLocation())==null)
                rc.layMine();
            } else { 
              // Choose a random direction, and move that way if possible
              Direction dir = Direction.values()[(int)(Math.random()*8)];
              if(rc.canMove(dir)) {
                rc.move(dir);
                rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
              }
            }
          }
          
          if (Math.random()<0.01 && rc.getTeamPower()>5) {
            // Write the number 5 to a position on the message board corresponding to the robot's ID
            rc.broadcast(rc.getRobot().getID()%GameConstants.BROADCAST_MAX_CHANNELS, 5);
          }
        }

        // End turn
        rc.yield();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}

```


Notice the while(true) loop, which prevents the run method from returning. While the robot is alive, it will be continually cycling through this loop. The try/catch block inside the loop prevents the robot from throwing an uncaught exception and dying. 




Execution Order
------------------

The game is comprised of a number of rounds. During each round, all robots get a turn in order of their IDs. Robots that were created earlier have lower IDs. Newly spawned robots will have a turn on the same round they were created.

The following is a detailed list of a robot's execution order within a single turn. If it dies halfway through, the remainder of the list does not get executed. In particular, note that changes to a robot's state do not happen while player code is being executed. All actions instead get sent to an action queue, and they are executed after the player code is run. For example, if a SOLDIER calls move() and then getLocation(), it will not reflect the location of the robot yet.

1. Robot's upkeep cost, is subtracted from the power pool, or if there is insufficient power in the pool, 'GameConstants.UNIT_ENERGON_UPKEEP' is paid in energon (if robot dies, it does not get a turn, and no power is refunded).
2. Robot executes up to `GameConstants.BYTECODE_LIMIT` of player code. Power costs for action calls in the player code are checked based on the available power at this point.
3. Power is refunded based on remaining bytecodes by a factor of `GameConstants.POWER_COST_PER_BYTECODE`, even if it paid its upkeep with energon.
4. Channels are updated with new broadcasts
5. Actions are performed

    a. If the robot is on the last turn of mining, the mines are placed on the map (SOLIDER Only)

    b. If the robot is on the last turn of defusing, the mines are removed from the map (SOLIDER Only)

    c. If the robot is on the last turn of capturing, then the encampment is created, and the robot is destroyed (SOLIDER Only)

    d. The robot moves (SOLIDER Only)

    e. Targeted attacks happen (ARTILLERY Only)

    f. Research is updated OR a unit is spawned (HQ Only)

6. Mine damage is applied (SOLDIER Only)
7. Automatic attacks are performed

    a. Robot auto-attacks adjacent enemies (SOLIDER Only)
    
    b. Robot heals itself and adjacent allies (MEDBAY Only).
    
    c. Robot adds shields to itself and adjacent allies (SHIELDS Only).

8. Shield decay is applied



Timing
------------------------

Each robot is allowed a certain amount of computation each round. Computation is measured in terms of Java bytecodes, the atomic instructions of compiled Java code. Individual bytecodes are simple instructions such as "subtract" or "get field", and a single line of code generally contains several bytecodes. (For details see http://en.wikipedia.org/wiki/Java_bytecode) Each round, every player runs a number of bytecodes determined by `GameConstants.BYTECODE_LIMIT`. When a robot hits the bytecode limit, its computation is paused while other robots get to do their computation for the same round or the next round. On the next round, the robot's computation is resumed exactly where it left off. Thus, to the robot's code, the round change is invisible. Nothing will jump out and shout at you when a round ends.

Because the round can change at the end of any bytecode, unexpected things can happen. For instance, consider the following example:

```java
Robot[] nearbyRobots = myRC.senseNearbyGameObjects(Robot.class);
MapLocation loc = myRC.senseRobotInfo(nearbyRobots[0]);
```

In the first line, the robot gets a list of all other robots in its sensor range. In the second line, the robot senses the RobotInfo of the first robot in the list. However, what happens if the round changes between the first and second line? A robot that was in sensor range when line 1 was executed might be out of sensor range when line 2 is executed, resulting in an exception. Because of this, your code should be written defensively. Think of this as a "real-world" robot, where things can fail at any time, and you have to be prepared to handle it.

However, there are ways of dealing with this, as we'll see in the next section.

### Yielding

One way to deal with timing complexities is to use `yield()` judiciously. Calling `RobotController.yield()` ends the robot's computation for the current round. This has two advantages.

First, robots consume power based on how many bytecodes they use every turn. A player that uses fewer bytecodes in a turn will be able to support more robots.

Second, after a call to `RobotController.yield()`, subsequent code is executed at the beginning of a new round. Then, you have the full amount of bytecodes for your robot to do computations before the round changes. For instance, let's modify the example above to be:

```java
myRC.yield();
Robot[] nearbyRobots = myRC.senseNearbyGameObjects(Robot.class);
MapLocation loc = myRC.senseRobotInfo(nearbyRobots[0]);
```

Since yield is called in line 1, line 2 will be executed at the beginning of a new round. Since `senseNearbyGameObjects()` does not take very many bytecodes, it is pretty much guaranteed that there won't be a round change between lines 2 and 3.

A common paradigm is to have a main loop, with a `yield()` at the bottom of the loop. Thus, the top of the loop is always executed at the beginning of the round. If all the robot's computation for one iteration of the loop can fit in one round, then there should be minimal problems with unexpected round changes. Note that team000 above does this.

### Monitoring

The Clock class provides a way to identify the current round ( `Clock.getRoundNum()` ), and how many bytecodes have been executed during the current round ( `Clock.getBytecodeNum()` ).

### GameActionExceptions

GameActionExceptions are thrown when an ability cannot be performed. It is often the result of uncertainty about the game world, or an unexpected round change in your code. Thus, you must write your player defensively and handle GameActionExceptions judiciously. Each GameActionException has a GameActionExceptionType, which tells roughly what went wrong. You should also be prepared for any ability to fail and make sure that this has as little effect as possible on the control flow of your program.

Exceptions cause a bytecode penalty of `GameConstants.EXCEPTION_BYTECODE_PENALTY`.


Mechanics
--------------------

This section deals with some of the mechanics of how your players are run in the game engine, including bytecode-counting, library restrictions, etc.

### Java Language Usage

Players may use classes from any of the packages listed in AllowedPackages.txt, except for classes listed in DisallowedPackages.txt.

Furthermore, the following restrictions apply:

`Object.wait`, `Object.notify`, `Object.notifyAll`, `Class.forName`, and `String.intern` are not allowed.
`java.lang.System` only supports `out`, `arraycopy`, and `getProperty`. Furthermore, `getProperty` can only be used to get properties with names beginning with "bc.testing."
`java.io.PrintStream` may not be used to open files.
Scala functions such as `scala.Console.readByte()` that attempt to read from standard input will always throw an `EOFException`.

Note that violating any of the above restrictions will cause the robots to self-destruct when run, even if the source files compile without problems.

### Bytecode costs

Classes in `java.util`, `java.math`, and scala and their subpackages are bytecode counted as if they were your own code. The following functions in `java.lang` are also bytecode counted as if they were your own code.

```
Math.random
StrictMath.random
String.matches
String.replaceAll
String.replaceFirst
String.split
```

The function `System.arraycopy` costs one bytecode for each element copied. All other functions have a fixed bytecode cost. These costs are listed in the `MethodCosts.txt` file. Functions not listed in `MethodCosts.txt` are free. The bytecode costs of battlecode.common functions are also listed in the javadoc.

### Memory Usage

Robots must keep their memory usage reasonable. If a robot uses more than 8 Mb of heap space during a tournament or scrimmage match, the robot may be killed.

### Exceptions

Throwing exceptions of any kind incurs a bytecode penalty given by `GameConstants.EXCEPTION_BYTECODE_PENALTY`, so unnecessary throwing of exceptions should be avoided.

### Virtual Machine Errors

Your player cannot catch virtual machine errors such as `StackOverflowError` and `OutOfMemoryError`. If your robot throws one of these exceptions, it will die.

### Java version

Our scrimmage and tournament servers will be running Java 6 and Scala 2.9. The Battlecode software should run on Java 7, but please be aware that our compile server will not recognize Java 7 specific language features.



Debugging
-------------------

This section describes some of the features of the game engine intended to make debugging somewhat less painful. Debug mode reveals valuable information about robots at development time but will be turned off for scrimmages and real tournaments.

### System.out

Any output that your robots print to System.out is directed to the output stream of the Battlecode engine, prefixed with information about the robot.

### Setting Indicator Strings

You'll find that your primary source of debugging is setting one of 3 indicator strings that are viewable in the client.  Unlike System.out which is not synchronized to match execution (as the engine precomputes the game faster than the client views it), Indicator strings are synchronized to the round number and can be used for debugging complex robot behaviors. 

Use `setIndicatorString(int,String)` to change a robot's indicator string. The are viewable in the top right corner of the client when the robot is selected. Indicator strings maintain value until they are changed.

### Debug Methods

The game engine has a feature that allows you to separate out debugging code that is unimportant to your player's performance in the tournament. Methods that have names beginning with debug_ and that have a void return type are given special status. By default, these methods are skipped during execution of the player. When the System property debugMethodsEnabled is set to true, however, the methods are executed normally except that they do not count against your robot's bytecode limit. Code that prepares the arguments to such a method may consume bytecodes, but the body of the method and any methods that it invokes are not counted.

### System Properties

Your robot can read system properties whose names begin with "bc.testing.". You can set a property by adding a line to bc.conf like this:

```
bc.testing.team-a-strategy=experimental
```

You can check the value of the property like this:

```java
String strategy = System.getProperty("bc.testing.team-a-strategy");
```

### Breakpoints

Breakpoints allow you to pause the game engine's calculations. If breakpoints are enabled (see the software page), and a robot calls RobotController.breakpoint(), the game engine will stop computing at the end of the round. This gives you a chance to see exactly what's going on in the game when your robot hits a certain point in its code. You can resume the game engine's computation in the client, by hitting the "resume" button. If the match is being dumped straight to a file (i.e., there is no client to resume the game), breakpoints are ignored.

Note that when a robot calls breakpoint(), computation will be stopped at the end of the round, not immediately when breakpoint() is called. Depending on the circumstances, you might want to use breakpoint(); yield(); instead.


Tournaments & Course Credit
---------------------------

There are five tournaments: the Sprint, Seeding, Qualifying, Final, and Newbie tournaments. Check the Calendar page for dates and locations. Here, we'll explain the mechanics of how the tournaments are run.

The Sprint Tournament is a single elimination tournament. Contestants are seeded based on scrimmage ranking, and play continues until there is only one undefeated team.

The Seeding Tournament is a double elimination tournament. Contestants are seeded based on scrimmage ranking, and play continues until there is only one undefeated team. The results of this tournament are used to determine seeds for the Qualifying and Newbie tournaments. Teams are ranked by the following criteria, in order:

- Furthest round achieved
- Bayesian Elo rating for the tournament (computed on a per-game basis, not a per-match basis)

The Qualifying Tournament is a double elimination tournament (see e.g. http://en.wikipedia.org/wiki/Image:NSB-doubleelim-draw-2004.png). Play continues until there are 8 teams remaining. These teams move on to the Final Tournament. Teams are seeded for the final tournament as follows:

- The four teams that did not lose a match receive the top four seeds.
- Teams that did not lose a single game are ranked by their qualifying seeds.
- The remaining teams are ranked by Bayesian Elo rating for the tournament (computed on a per-game basis).
- The Final Tournament is another double elimination tournament. The Final Tournament starts with a blank state, i.e., any losses in the Qualifying Tournament are erased.

The Newbie Tournament will run concurrently with the Qualifying Tournament. All teams consisting entirely of MIT students who have not participated in Battlecode before will automatically be entered into the Newbie tournament in addition to the other tournaments. We will announce the format of the Newbie tournament soon.

In order to receive credit for the course, or to be eligible for the newbie tournament, you must register with an mit.edu e-mail address. If you already registered with a different e-mail address, please let us know.

You can receive credit for the course by defeating the reference player. You must defeat Teh Devs in an unranked scrimmage on a certain set of maps. These maps will be announced approximately two weeks into the course. If your player beats the reference player, everyone on your team receives 6 credits.

If your submission does not beat the reference player, then you can get credit an alternate way, by sending us a 2-page report on your player: its code design, how it works, an explanation of any AI paradigms you used, etc. We will look over your source code and your report, and if both show a significant amount of effort, thought, and good design techniques, we will give you 6 credits.

We give prizes for the best strategy reports, so we encourage you to submit a report even if you defeat the reference player.

Also, note that you are allowed to drop 6.370 without penalty very late into IAP.


Getting Help
-------------

We have both a forum (https://www.battlecode.org/contestants/forum/) and an IRC Channel (#battlecode on irc.freenode.net). Hang out and chat with us -- we're friendly!


Disclaimers
-------------

We have done our best to test and balance the properties of the Battlecode world. Inevitably, however, we will need to make adjustments in the interest of having a fair competition that allows a variety of creative strategies. We will endeavor to keep these changes to a minimum, and release them as early as possible. All changes will be carefully documented in the Changelog.

Despite our best efforts, there may be bugs or exploits that allow players to operate outside the spirit of the competition. Using such exploits for the tournament or scrimmage will result in immediate disqualification, at the discretion of the directors. Such exploits might include, but are not limited to, robot communication without messages, bypassing the bytecode limit, or terminating the game engine. If you are not sure what qualifies as "in the spirit of the competition", ask the devs before submitting your code.


Changelog
-------------
* **1.0.0** (1/7/2013) - Initial specs released
* **1.0.1** (1/7/2013) - Bug hotfix. Example players no longer throw exceptions, encampment capture cost is made obvious in the spec. Misc. spec typos fixed.
* **1.1.0** (1/8/2013) - Note Backwards Incompatible **API CHANGES**
    * Most RobotLevel stuff removed
    * Capture cost calculation made robust.
    * Sensing of mines, robots, encampments made easier, API refactored and intentionally broken to make this obvious.
    * Map max size reduced to 70, and constraints made tighter (200 max rush distance). Lots of new maps added.
    * Nuke sensing rebalanced, only 50% detectable
    * Spec document mostly rewritten to include detailed information regarding mines, broadcasting, etc.
* **1.1.1** (1/8/2013) - Fixing typos
* **1.1.2** (1/9/2013) - Fixing bug in mine sensing. Tiebreaker conditions fixed. Specs updated to describe autoattacks better
* **1.1.3** (1/9/2013) - Fixed neutral mine detection
* **1.1.4** (1/10/2013) - Added runmatch to automate replays.
* **1.1.5** (1/11/2013) - Broadcast read cost reduced. Correct ranges displayed. Shields improved. Fixed specs. Increased number of channels.
* **1.2.0** (1/17/2013) - Post-sprint release. Updates to the sprites. Added new maps. Additional Client options.
* **1.3.0** (1/25/2013) - Final Release Candidate. Added new maps. Additional client fixes. Balance changes:
    * Artillery does 60 damage
    * Artillery ratio 0.25
    * Shield increased to 10/turn
    * Shield decay increased to 1/turn
    * Shields absorb 90% of mine damage
    * Broadcast send power cost reduced to 0.03


Appendices
------------

### Appendix A: Javadocs and Game Constants

Javadocs can be found here, and they are also included in the software distribution.

The javadocs include the values of the game constants and robot attributes.

### Appendix B: Energon Health Warning

Energon intake is not for everyone. Please consult a physician before use. 6.370 Battlecode Corporation is not responsible in the event of injury due to energon use. Energon's side-effects include loss of limb, death, unbearable pain, tendencies to procrastinate and an unnatural senseless rage. Handle with care. Energon consumption has not been approved by any health agency and you USE IT AT YOUR OWN RISK. For this reason, please be careful when scrimmaging. Energon is the same thing as health.
