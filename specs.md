Battlecode 2014 Gameplay Specs
==============================

Plot
--------

After Teh Nubs were victorious against the unknown forces at the outer rim, the galaxy felt a little lifeless. The numerous victorious melee robots juked back and forth in triumph, but at what cost! The terrain was strewn with mines and military structures, unfit for industry or agriculture. With the old military government, nothing would be done. But now the Interplanetary Development Committee has ascended to the golden toilet and proclaimed a new interplanetary initiative. Military forces will now be repurposed for herding cattle to produce milk for the galaxy's young and infirm. Two "companies" will be placed on each planet in the spirit of capitalist competition.

Objective
----------------

Transport milk to space. First build PASTRs (Projected Animal Security and Treatment Region) and herd cows into them. The PASTRs automatically send your milk to space. Remember, you can also shoot your competitor's cowboys, destroy his PASTRs, and disperse and steal his cows.

Good luck!

Major Mechanics for 2014
-----------------
- Your company HQ produces the robot cowboys that can herd cows.
- Robots can move to any adjacent square without needing to turn.
- The map is known, though vision is not shared between robots. 
- Movement and attack commands share a delay timer.
- There's more than one way to move: there's running and sneaking. 
- Attacks are ranged and manually targeted.
- To communicate messages among your robots, you can post and read integers to and from a team-shared array. This means message hacking and jamming are no longer possible.  However, broadcasting robots' positions are revealed to all players during the round that they are broadcasting.
- Cows are a scalar field. Sneaking does not disturb the scalar field, but running and shooting cause cows to run away from the source of noise. In the case of several sources of noise, the cows run away from the center of mass of the noise.
- A robot may convert itself into a PASTR, which projects a circular containment field and gathers milk from cows inside the field. Cows that run into a PASTR cannot run out of the pasture unless the PASTR is destroyed. In addition, individual robots gather a smaller quantity of milk from cows on their square.


Robot Overview
-----------------

Robots are the central part of the Battlecode world. There are two types of basic robots. Note that we use the terms 'robot' and 'unit' interchangeably.

### HQ
The HQ is your main base, and by far the most important unit you have. Each team starts the game off with one HQ. The HQ is invincible, and can't be destroyed. Your company HQ produces the robot COWBOYs that can herd cows.

### COWBOY
COWBOYs form the core of your army. They are the only mobile unit, and are created by the HQ. They allow you to expand your map control and herd cows.

### PASTR
Generates a field to get milk from cows inside the field, and keep cows within the field as long as it remains up.

Robot Resources
------------------

Each robot has hitpoints (100). When the hitpoints reach zero, the robot is immediately removed from the game. Hitpoints regenerate slowly over time (.25 per turn when it hasn't been damaged in the last 30 turns). 

In the past, robots used a resource to fuel their movement and computation. This year, the resource is action delay. A robot that does more computation will move more slowly (longer move/attack delay). Because move and attack delay are combined this year, this will also mean less damage per second for computationally intensive robots.

The HQ can produce a robot every few rounds until the number of allied robots (including structures) equals `GameConstants.MAX_ROBOTS`. This production delay increases depending on the number of robots currently controlled. Structures such as PASTRs also count as 'robots'.

Victory Conditions
------------------

A team wins by transporting `GameConstants.WIN_QTY` GigaGallons (GG) to space. If neither team has done so by `GameConstants.ROUND_LIMIT`, the following tiebreakers apply:

- Quantity of milk transported
- Total # cows in PASTRs
- Total # enemy robots killed
- Lowest ID

Robot Actions
--------------

Robots are equipped with a variety of high tech equipments and can perform the following actions during their turn.

### Action Delay and Bytecode
Each robot has an `actiondelay` counter that decrements by 1 every turn. Movement and attacking cannot be performed unless `actiondelay` is less than 1, and they also give a certain amount of `actiondelay`.

Running code uses bytecodes. Each turn, a robot can spend up to 10000 bytecodes on computation. If this limit is reached, the robot's turn is immediately ended and the computation is continued on the next turn. Using `yield()` and `selfdestruct()` can end a turn early, saving bytecodes and ending computation. The former is generally preferred.
For cowboy robots, each bytecode above 2000 gives 0.00005 `actiondelay`.


### Sensors

Info on robots in sight range can be sensed. Vision is not shared between robots. The locations of starting HQs and other map objects are known. 

- The info on all allied robots can be sensed.
- The info on visible enemy robots can be sensed.
- The locations of the both HQs can be sensed.

### Broadcasting
Radio Sensors: When a robot broadcasts to radio, all robots are made aware of the location of the broadcasting robot for for one turn. They can access the positions with a method call like `rc.senseNearbyBroadcastingRobots(Team t)`.

Messages written to the team-shared integer list persist until overwritten. You can't read or write integers from or to the enemy team's shared integer list. 

The cost of transmitting and receiving are in bytecodes, which, as mentioned earlier, affect movement and attack speeds.


### Attack
Cowboy robots can attack any tile within attack range (square range of 10). Attacking and moving share the same cooldown (action delay). Attacking deals 10 damage and gives 2 `actiondelay`.

An attack destroys all cows at the targeted location. In addition, it makes noise that scares cows at long range at the targeted location.

Your HQ shoots depleted uranium girders out of a railgun, dealing overkill area damage to the target (50 and 25 splash in a square range of 2). HQ has square range of 16. Watch out for friendly fire.

Noise Towers can also 'attack' in their attack range. Their attacks create noise (can choose to create noise in square range of 9 or square range of 36) but deal no damage.

### Movement
Cowboy robots can move to any unoccupied adjacent square if their delay is less than one. Using bytecodes adds small fractions to the delay that eventually add up to one, requiring a momentary pause in movement or attack, representing careful thought.

Running is faster (shorter move delay), but creates noise, scaring cows at short range. Sneaking is slower but creates no noise. By sneaking, you can actually move among cows.

Running gives 2 actiondelay and sneaking gives 3 actiondelay for lateral movement. Diagonal movement gives 1.4 times the actiondelay of lateral movement.

### Spawning, Construction, and Robot Count
The HQ can spawn soldiers, subject to a production delay (30 turns plus total number of robots^1.5) and a maximum robot number (25). Cowboys count for one robot, PASTRs count for two robots, and Noise Towers count for three robots.
Cowboy robots can construct structures on the square they are currently on. The robot will become unable to take any action for a certain number of turns (50 for PASTRs, 100 for Noise Towers) and then will be removed and replaced with the constructed structure.

### Suicide
Calling `selfdestruct()` immediately removes the calling robot from the game and deals area damage (30+half of remaining hp to square range of 2). This replaces the `suicide()` method. Structures cannot selfdestruct.

### Team Memory
Official matches will usually be sets of multiple games. Each team can save a small amount of information (`GameConstants.TEAM_MEMORY_LENGTH` longs) for the next game using the function `setTeamMemory()`. This information may be retrieved using `getTeamMemory()`. If there was no previous game in the match, or no information was saved, then the memory will be filled with zeros.

### Control Bits, Indicator Strings, and Breakpoints
Each robot can set strings that are visible when viewing the client, as a debugging tool. 

There are several ways that the user can interact with robots. First, any robot can use `setIndicatorString(int,String)` to set a string that is visible to the user when selecting the robot. Second, the user can manually set a long for each robot, which the robot can query using `getControlBits()`. Finally, a robot can call `breakpoint()`, which flags the game engine to pause computation at the end of the round. These methods are for debug purposes only. During tournaments and scrimmages, the user will not be able to interact with the robots. For more information on these debugging interfaces, check out Debugging below.

### Ending turn

Calling `yield()` and `selfdestruct()` instantly end the turn of a robot, potentially saving bytecodes. Otherwise a turn ends naturally when the bytecode limit is hit. Every turn a robot gets 10000 bytecodes to run code.

### Cows
Cows are a scalar field. Each location on the map has a certain natural cow growth. During each turn, each location gains a number of cows equal to the natural cow growth, and then 0.5% of the cows on that location die a natural death.

Cows can be influenced by noise and attacks. After each turn, cows will run away from the averaged location of all the noises they heard that turn. Short-range noises (running, Noise Tower light attacks) scare cows in range^2 9, and long-range noises (shooting, Noise Tower normal attacks) scare cows in range^2 36. If the direction away from this averaged location points between two locations, the cows will split evenly between those locations.

Cows in a PASTR containment field cannot leave the field, and cows on the same square as a robot will not leave that square due to noise until the robot moves.
In addition, attacking a square (except for Noise Tower attacks) destroys all cows on that square. All weapons used are certified humane.

### Milk
Milk comes from cows. They are automatically milked by either being within the containment field of a PASTR, or by being on the same square as a robot (which milks them in its spare time). Robots only give 5% of the milk that a PASTR would generate. Destroying an enemy PASTR gives 1/10 of `GameConstants.WIN_QTY` milk.


Maps
-----
Battlecode maps are a rectangular grid of squares, each with a pair of integer coordinates. Each tile is an instance of `MapLocation`. Squares outside the map have TerrainType.OFF_MAP. The northwest map square is the origin (0,0). Maps specify the spawn points of the teams.

There are three types of terrain: GROUND, VOID, and ROAD. VOID terrain is not traversable and do not have cows. ROAD terrain discounts movement-related and sneaking-related actiondelays by a factor of 0.7 for robots on the terrain.

### Map Files

Maps are specified by XML files, and can be found in the maps folder of the release archive. The schema for the files should be fairly intuitive, so if you'd like to add your own maps you can use the provided maps as a basis. Each map has an associated random number seed, which the RNG uses to generate random numbers in games played on that map.

### Map Constraints
Official maps used in scrimmages and tournaments must all satisfy the following conditions.
- Maps are completely symmetric either by reflection or 180 degree rotation.
- The width and height of the map are guaranteed to be between 20 and 100, inclusive.
- The distance between the spawn points will be at least 10 units (Euclidean distance). 


Writing a Player
------------------------

### Introduction

Your player program must reside in a Java package named `teamXXX`, where `XXX` is your three-digit team number, with leading zeros included. You may have whatever sub-packages you like. You must define `teamXXX.RobotPlayer`, which must have a public static `run` method that takes one argument of type `battlecode.common.RobotController`. Whenever a new robot is created, the game calls the run method with the robots RobotController as its argument. If this method ever finishes, either because it returned or because of an uncaught exception, the robot dies and is removed from the game. You are encouraged to wrap your code in loops and exception handlers so that this does not happen.

###. RobotController

The RobotController argument to the RobotPlayer constructor is very important -- this is how you will control your robot. RobotController has methods for sensing (e.g. `senseRobotInfo(Robot)`) and performing actions (e.g., `move()`). If you're not sure how to get your robot to do something, the Javadocs for RobotController are a good place to start.

### Example: examplefuncsplayer


```java
package examplefuncsplayer;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
	static Random rand;
	
	public static void run(RobotController rc) {
		rand = new Random();
		Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		
		while(true) {
			if (rc.getType() == RobotType.HQ) {
				try {					
					//Check if a robot is spawnable and spawn one if it is
					if (rc.isActive() && rc.senseRobotCount() <= 25) {
						Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
						if (rc.senseObjectAtLocation(rc.getLocation().add(toEnemy)) == null) {
							rc.spawn(toEnemy);
						}
					}
				} catch (Exception e) {
					System.out.println("HQ Exception");
				}
			}
			
			if (rc.getType() == RobotType.SOLDIER) {
				try {
					if (rc.isActive()) {
						int action = (rc.getRobot().getID()*rand.nextInt(101) + 50)%101;
						//Construct a PASTR
						if (action < 1 && rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 2) {
							rc.construct(RobotType.PASTR);
						//Attack a random nearby enemy
						} else if (action < 30) {
							Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,10,rc.getTeam().opponent());
							if (nearbyEnemies.length > 0) {
								RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[0]);
								rc.attackSquare(robotInfo.location);
							}
						//Move in a random direction
						} else if (action < 80) {
							Direction moveDirection = directions[rand.nextInt(8)];
							if (rc.canMove(moveDirection)) {
								rc.move(moveDirection);
							}
						//Sneak towards the enemy
						} else {
							Direction toEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
							if (rc.canMove(toEnemy)) {
								rc.sneak(toEnemy);
							}
						}
					}
				} catch (Exception e) {
					System.out.println("Soldier Exception");
				}
			}
			
			rc.yield();
		}
	}
}
```


Notice the while(true) loop, which prevents the run method from returning. While the robot is alive, it will be continually cycling through this loop. The try/catch block inside the loop prevents the robot from throwing an uncaught exception and dying. 




Execution Order
------------------

The game is comprised of a number of rounds. During each round, all robots get a turn in order of their IDs. Robots that were created earlier have lower IDs. Newly spawned robots will have a turn on the same round they were created.

The following is a detailed list of a robot's execution order within a single turn. If it dies halfway through, the remainder of the list does not get executed. In particular, note that changes to a robot's state do not happen while player code is being executed. All actions instead get sent to an action queue, and they are executed after the player code is run. For example, if a SOLDIER calls move() and then getLocation(), it will not reflect the location of the robot yet.

1. Robot executes up to `GameConstants.BYTECODE_LIMIT` of player code. Power costs for action calls in the player code are checked based on the available power at this point.
2. Channels are updated with new broadcasts
3. Actions are performed

    a. The robot moves

    b. Attacks happen


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
* **1.0.0** (1/7/2014) - Initial specs released
* **1.0.1** (1/7/2014) - Basic improvements and fixes
-   * Improve game finish message so that it does not always say the game ends on tiebreaks.
-   * Removing references to mining and capturing in RobotController documentation.
-   * Changing bytecode penalty to 0.00005.
* **1.0.2** (1/?/2014) - API CHANGES (minor)
-   * Removing references to old things.
-   * Fix non-milk tiebreaker code so that tiebreaks are functional.

Appendices
------------

### Appendix A: Javadocs and Game Constants

Javadocs can be found here, and they are also included in the software distribution.

The javadocs include the values of the game constants and robot attributes.
