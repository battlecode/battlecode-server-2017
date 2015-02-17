Battlecode 2015 Gameplay Specs
==============================

Table of Contents [bcd00]
---------------

1. Plot [bcd01]
2. Objective [bcd02]
3. Major Mechanics [bcd03]
4. Robot Overview [bcd04]
5. Structures Overview [bcd05]
6. Units Overview [bcd06]
7. Ore [bcd07]
8. Supply [bcd08]
9. Messaging [bcd09]
10. Maps and Terrain [bcd10]
11. Sensing [bcd11]
12. Victory and Tiebreaks [bcd12]
13. Actions and Delays [bcd13]
14. Structures In-depth [bcd14]
15. Units In-depth [bcd15]
16. Getting Help [bcd16]
17. Disclaimers [bcd17]
18. Appendix A: Javadocs and Game Constants [bcd18]
19. Appendix B: More In-depth Mechanics [bcd19]
20. Changelog [bcd20]

Plot [bcd01]
--------

Last year, the Interplanetary Development Committee succeeded in harnessing cows and the nutritional value of milk to rejuvenate the galaxy that was ailing after years of terrible, destructive war. Economy flourished as vast scalar fields of cows produced virtually endless quantities of milk. Thus, it was generally agreed upon that the time was right to transition back to terrible, destructive war. By leveraging the abundant resources, military developers designed and mass-produced hordes of unique robots specialized in either obliterating one another or assisting said obliteration in some way. Powerful supportive structures called TOWERs were developed to fortify and strengthen the conventional central HQ. Modern warfare necessitates destroying these structures in order to weaken the enemy's HQ before crushing it underfoot.

Objective [bcd02]
---------------

Destroy your opponent's HQ. However, each team starts the game with several TOWERs, powerful structures that strengthen the HQ as long as they stand. Produce units and structures for econ and combat, take down TOWERs, and then destroy the HQ.

Good luck!

Major Mechanics for 2015 [bcd03]
--------------

- Both teams start with a HQ and up to six TOWERs, all immobile structures that can attack. The HQ becomes less powerful each time a friendly TOWER is destroyed.
- The main resource of the game is 'ore'. Each square of the map starts with some amount of ore on them, and this ore can be mined by certain units.
- Using ore, the HQ can spawn mobile BEAVER units, which can build various structures that in turn spawn other types of units.
- Each unit type is unique and has its own attributes like attack power, HP, and range.
- Each robot also has an individual 'supply' count. A robot's supply decreases as it performs actions or runs code. Robots without supply will still be able to perform actions and/or run code, but with penalties to action frequency and/or bytecode limit.
- Robots have two delay timers, a weapon delay and a core delay (for all non-attack actions). However, attacking and moving are not completely separate, as performing an attack may also increase core delay, and vice versa.
- To communicate between robots, you can post and read integers to and from a team-shared array.
- The layout and details of the map are not initially revealed. All MapLocations are offset by the same random amount. However, robots know the location of allied and enemy HQs and TOWERs.

Robot Overview [bcd04]
--------------

Robots are the central part of the Battlecode world.

Each robot in the Battlecode game will run the `run()` function located in its team's RobotPlayer.java file, regardless of the type of robot. Each robot runs on a separate JVM, which means that static variables in RobotPlayer.java are not actually shared.

The Battlecode game is turn-based and divided into 'rounds' or 'turns', in which each robot gets a chance to run code and perform actions. Code that a robot runs costs bytecodes, either a predetermined amount for certain game functions, or otherwise the amount of Java bytecode that the code generates. A robot only has a certain amount of bytecodes per turn, after which the robot's turn is immediately ended and the computation is continued on the next turn. Using `yield()` can end a turn early, saving bytecodes and ending computation.

All robots have a certain amount of HP (also known by hitpoints, health, life, or such). When a robot's HP reaches zero, the robot is immediately removed from the game.

Each robot also has a unique ID, uniformly generated in the range [1, 32000].

This year's game contains a wide variety of different robots. There are two major classes of robots: structures and units. The term 'robots' refers to both classes.

The following is a visual summary of the units, the structures, and the dependencies.

![Image of Tech Tree]
(https://github.com/battlecode/battlecode-server/blob/master/techtree.png)

Structures Overview [bcd05]
----------

Note that this is an overview section. Detailed structure statistics can be found in a later section.

Structures refer to all robots that cannot move. There are two types of special structures, TOWERs and HQs, that each team starts the game with and which cannot be built.

#### HQ
The HQ is your main base and your most important robot. Losing the HQ is equivalent to losing the game.
- Each team starts with exactly one HQ pre-placed on the map
- Can attack at long range
- Can spawn BEAVER units
- Offensive and defensive abilities are increased for each friendly TOWER alive
- Per-turn bytecode limit of 10000

#### TOWER
TOWERs are powerful defensive structures in their own right, but TOWERs also increase the strength of the allied HQ by merely existing.
- Each team starts with up to six TOWERs pre-placed on the map
- Can attack at long range
- Cannot spawn units
- Per-turn bytecode limit of 2000

Non-special structures (buildable structures) can be built by the BEAVER unit. Certain structures can only be built while you have a prerequisite structure standing. Each of these structures has 100 max HP and cannot attack or move. In addition, buildable structures have a per-turn bytecode limit of only 2000. The structures are:

#### Supply Depot
- Increases the supply generated by the friendly HQ (see the section on supply)

#### Miner Factory
- Can produce MINER units

#### Technology Institute
- Can produce COMPUTER units

#### Barracks
- Can produce SOLDIER and BASHER units

#### Helipad
- Can produce DRONE units

#### Training Field
- Can produce the COMMANDER unit
- Can only be built if you have a Technology Institute

#### Tank Factory
- Can produce TANK units
- Can only be built if you have a Barracks

#### Aerospace Lab
- Can produce LAUNCHER units
- Can only be built if you have a Helipad

#### Handwash Station
- Sanitary
- All robots must wash hands before returning to war
- May or may not be entirely useless

Units Overview [bcd06]
----------

Note that this is an overview section. Detailed unit statistics can be found in a later section.

Units refer to all robots that can move. Each unit must be spawned at an appropriate building (or unit, in the case of MISSILEs). Each unit has a per-turn bytecode limit of 10000 (computers have 20000), with the exception of MISSILEs, which have per-turn bytecode limits of only 500.

#### BEAVER
The BEAVER is a versatile starting unit that can do it all.
- Spawned at HQ
- Only unit that can build structures
- One of two units that can mine ore
- Has a weak attack at short range
- Frequently hosed

#### MINER
The MINER is a unit that can mine ore.
- Spawned at Miner Factory
- Has a weak attack at short range
- One of two units that can mine ore

#### COMPUTER
The COMPUTER is a unit that can be used for extra computing power.
- Spawned at Technology Institute
- Extremely low hp
- No attack
- Moves very slowly
- Higher bytecode limit

#### SOLDIER
The SOLDIER is a basic, generic combat unit.
- Spawned at Barracks
- Has a mediocre attack at short range
- Can attack often
- Boring

#### BASHER
The BASHER is a unit that attacks all enemies in melee range.
- Spawned at Barracks
- Slightly beefier and more expensive than the SOLDIER
- Has a decent attack that hits all enemies adjacent to it (so up to 8)
- Throwback to Battlecode 2013

#### DRONE
The DRONE is a fast and mobile unit.
- Spawned at Helipad
- Has a mediocre attack at short range
- Moves quickly
- Can move over VOID terrain (but at half its normal speed)

#### COMMANDER
The COMMANDER is a strong unit with special abilities.
- Spawned at Training Field
- Each team can only control one COMMANDER at a time
- Ore cost doubles for each friendly COMMANDER that has been produced
- Very high HP
- Has a mediocre attack at decent range
- Has special abilities
- Gains experience from nearby defeated enemy units
- More details below
- This list is too long

#### TANK
The TANK is a powerful and expensive combat unit.
- Spawned at Tank Factory
- High HP
- Has a strong attack at long range

#### LAUNCHER
The LAUNCHER is a unit that can generate and launch MISSILEs.
- Spawned at Aerospace Lab
- Very high HP
- No attack
- Moves slowly
- Generates a MISSILE every 8 turns and can store up to 5
- Super cool

#### MISSILE
The MISSILE is a special unit generated at LAUNCHERs.
- Can be launched from its parent LAUNCHER
- Dies after 3 attacks from any source, 5 turns after being launched, or when it calls the `explode()` function
- Damages all adjacent units upon death, with friendly fire
- Moves fast
- Can move over VOID terrain
- Cannot broadcast messages
- Painfully low bytecode limit

Ore [bcd07]
-----------

'Ore' is the main resource of the game. Each team has a certain stockpile of ore, and spawning units and building structure deducts ore from the team stockpile. Unit and structure costs depend on the unit or structure type.

Each location of each map has some non-negative (possibly zero) amount of ore on it, which can be mined by BEAVERs and MINERs standing on that location by calling the `mine()` method. If the amount of ore on a location is n, BEAVERs mine max(min(2,n/20),0.2) per `mine()` call while MINERs mine max(min(2.5,n/4),0.2) per `mine()` call (ore on a square cannot be mined below 0). This may be easier to understand explained as 'n/20, but upper-bounded by 2 and lower-bounded by 0.2' for BEAVERs and 'n/4, but upper-bounded by 2.5 and lower-bounded by 0.2' for MINERs. Mining reduces the amount of ore on the robot's location and adds that amount to the player's stockpile.

Players start the game with 500 ore, and each team automatically receives 5 ore per turn before any mining income.

Unit/structure costs can be found further down, in the in-depth unit/structure sections.

Supply [bcd08]
-------------

Each robot carries some non-negative amount of 'supply'. The HQ automatically generates a certain amount of supply each turn, which is increased for each friendly supply depot standing. The exact formula for the amount of supply generated by the HQ per turn is 100*(2+supply_depots^0.6).

Robots can carry arbitrary amounts of supply and can transfer integer amounts of supply to other robots within a square radius** of 15. When a robot is destroyed, its supply is destroyed along with it.

Each unit type has a certain 'supply upkeep' associated with it. Roughly put, a unit that is fully active (performing actions as often as possible) will lose that amount of supply per round. The exact mechanics behind this are complicated and not so important to know, but they are in the 'Actions and Delays' section further down.

In addition, using more than 2000 bytecodes in a single turn will cost the unit 0.001 supply per extra bytecode.

Units that have no supply faces two penalties. First, it will only be able to act (move, mine, and/or attack) roughly half as fast as it would with supply. Second, its bytecode limit is halved to 5000 (10000 for computers). Structures have no supply upkeep and never face penalties from lacking supply. A unit with no supply is marked with a white square on the client.

** Square radius refers to Euclidean distance squared. For example, a square radius of 15 refers to all locations within sqrt(15) of that location. All distances in this game will be represented using this way because it's more computationally efficient and avoids floating point numbers.

Messaging [bcd09]
---------------

Each team has a size-65536 array of ints that can be broadcasted to and read from. These arrays are private, so message jamming and interference is impossible between teams. All robots (except for MISSILEs) can broadcast to the message array, and all robots can read from the array. The initial value of every element in the array is 0.

Maps and Terrain [bcd10]
--------------

Maps for this year's competition will be rectangular grids whose dimensions may be from 30x30 to 120x120. Each square of the grid will either be VOID or non-VOID (most units cannot move onto VOID squares, and structures cannot be built there). Coordinates on the map are represented by `MapLocation` objects, which hold the x and y coordinates of the location. All `MapLocation`s are offset by the same random amount. The top left corner has coordinates in the range [-16000, 16000].

One HQ per team and up to six TOWERs per team will be pre-placed on non-VOID squares of each map. None of these starting structures will be in attack range of any enemy starting structure.

Each non-VOID square on the map will be connected to all other non-VOID squares (in essence, a single unit placed on the map should be able to navigate to any square to another). There will be at least one non-VOID square adjacent to the HQ.

Each non-VOID square of the map will start with some non-negative amount of ore. Teh Devs make no guarantees about the patterns of ore, but we will attempt to make the distribution of ore across squares somewhat 'continuous'.

Naturally, each map will be symmetric by either a reflection or a rotation.

Sensing [bcd11]
---------------

Info on robots in sight range of ANY allied unit can be sensed (so vision is shared between robots). The locations of allied and enemy HQs are known along with the location of all allied and enemy TOWERs. You are always able to sense all of your allied robots.

HQ and TOWERs can see all locations within square radius 35, while all other robots have sight ranges of 24 (square radius).

The terrain type and ore amount of a map square cannot be sensed until that map square has been within sight range of an allied unit. The ore amount sensed on a map square will return the ore amount on the square during the last time that map square was within allied sight range - meaning that enemy mining activity cannot be sensed from far away.

Sensing info on a robot includes the robot's location, delays, its supply levels, its type, its health, whether it is building something, its missile count, and more. See the `RobotInfo` Java documentation for more details, as well as the `senseNearbyRobots()` method. There are a number of other useful sensing methods which are mentioned in the documentation.

Victory and Tiebreaks [bcd12]
---------------

A team whose HQ is destroyed immediately loses (and conversely, a team who destroys the opponent HQ immediately wins!).

However, games must end in finite amounts of time, and hence each game only lasts for a number of turns in the range 2000 to 3000, inclusive, depending on the map (which you can query using `getRoundLimit()`). If neither HQ is destroyed by the end of the last round, the winner will be determined by a series of tiebreaks. These tiebreaks, in order of their application, are:

1. Number of towers remaining
2. HQ HP remaining
3. Total HP of towers
4. Number of handwash stations
5. Sum of ore stockpile plus ore costs of all surviving robots
6. Team HQ ID

Actions and Delays [bcd13]
--------------

Certain robot actions cannot be performed multiple times in a single turn or short period of time. In general, all actions are queued and happen at the end of the turn, but changes to robot delays happen immediately when the methods are called. The exceptions are exploding and disintegrating, which happen immediately.

The actions are:

#### Attacking
Attacking can only be done when the robot's weapon delay is <1 (which can be checked using the `isWeaponReady()` method), and can be performed by calling the `attackLocation()` method. This deals damage to the unit on the targeted square. Bashers automatically attack all adjacent enemies after movement.
This increases core delay up to the robot type's COOLDOWN_DELAY and increases weapon delay up to the robot's ATTACK_DELAY.

#### Moving
Moving can only be done when the robot's core delay is <1 (which can be checked using the `isCoreReady()` method), and can be performed by calling the `move()` method. This moves the unit in the specified direction. If you wish to check whether a move is valid, you can use the `canMove()` method.
This increases core delay up to the robot type's MOVEMENT_DELAY and increases weapon delay up to the robot's LOADING_DELAY.

#### Mining
Only BEAVERs and MINERs can mine, and only when the robot's core delay is <1 (`isCoreReady()`). It can be performed by calling the `mine()` method. This reduces the amount of ore on the robot's square by a certain value (see the section on Ore) and increases the player's stockpile by that value.
This increases core delay up to the robot type's MOVEMENT_DELAY and increases weapon delay up to the robot's LOADING_DELAY (the same delay as movement).

#### Spawning
Certain structures can spawn certain units, but only when the structure's coreDelay is <1 (`isCoreReady()`). Spawning can be performed by calling the `spawn()` method. This deducts the ore cost of the unit from the player's stockpile, then creates one unit of the specified type in the specified direction. The structure's core delay is increased by the turn cost of the spawned unit type. The `hasSpawnRequirements()` and `canSpawn()` methods can be used to check whether a spawn action is legal.
This increases core delay by the turn cost of the unit spawned.

#### Building
Only BEAVERs can build, and only when the robot's core delay is <1 (`isCoreReady()`). When `build()` is called, several things happen. The ore cost of the structure is deducted from the player's stockpile. The BEAVER is put in a 'constructing' state for a certain number of turns, during which it cannot perform any above action but can compute. An incomplete structure is also created in the specified direction, which starts with 50 HP (half of building max HP). This incomplete structure cannot compute or perform any actions. The number of turns for construction depends on the type of structure being built.

After the required number of turns, the structure will become complete, and its HP will double. The BEAVER will be also be able to perform other actions again. The `hasBuildRequirements()`, `canBuild()`, and `checkDependencyProgress()` methods can be used to check whether a build action is legal.

If the BEAVER is destroyed while it is building a structure, the structure will also be destroyed. If the structure is destroyed during construction, then the BEAVER will be free to do other things.

#### Exploding
Only MISSILEs can explode. When `explode()` is called, the missile is immediately destroyed, and 20 damage is dealt to all adjacent units (regardless of team).

#### Disintegrating
Any robot can call the `disintegrate()` method. This immediately destroys the robot that calls it. Using this method is generally not advised.

### Explanation of Delays
#### TL;DR version
Each type of unit has an associated fixed movement delay and fixed attack delay. Roughly put, units can only move once every (MOVE_DELAY) turns and can only attack once every (ATTACK_DELAY) turns. For units without supply, these values are approximately doubled. HQs and TOWERs also have an attack delay and can only attack once every (ATTACK_DELAY) turns, but they have no supply restrictions. Also, `mine()` counts as movement.

#### Details version
The rest of this section details the exact implementation of delays. It may be complicated and is not fully necessary for writing a bot.

Each individual robot has two changing delay counters: a core delay and a weapon delay. Delays are doubles that represent some amount of time that must pass before an action can be taken. Weapon delay pertains to the action of attacking, while core delay pertains to all other actions (mining, spawning, building, and moving). Specifically, actions cannot be performed unless the associated delay is <1. Both delay counters decrease by a base of 0.5 per turn, but can be further reduced by an additional 0.5 per turn if the robot has enough supply to pay its supply upkeep (this happens automatically). Therefore, a supplied robot can act about twice as often as an unsupplied robot. The way LAUNCHERs generate missiles works the same way - each time a LAUNCHER generates a missile, it gains 8 weapon delay, and the weapon delay must be <1 in order for a LAUNCHER to generate a missile. Note that actually launching a missile is independent of weapon delay.

Delays increase when actions are performed. Naturally, attacking increases weapon delay and moving/mining/spawning increases core delay. Attacking increases the weapon delay of the robot by the ATTACK_DELAY of the unit type. Similarly, moving or mining increases the core delay of the robot by the MOVEMENT_DELAY of the unit type (1.4*MOVEMENT_DELAY if the movement was in a diagonal direction). However, attacking can also increase core delay up to the COOLDOWN_DELAY of the unit type, and moving or mining can increase the attack delay of the robot up to the LOADING_DELAY of the unit type. This means that attacking and movement are not completely independent of each other, and that units must wait some turns after attacking in order to move, or vice versa.

In other words:
- Attacking means that a robot must wait ATTACK_DELAY turns before attacking again, and at least COOLDOWN_DELAY turns before moving.
- Moving means that a robot must wait MOVE_DELAY turns before moving again, and at least LOADING_DELAY turns before attacking.

We can also look at the delays from another perspective:
- If a robot wants to attack, it must have not attacked for ATTACK_DELAY turns and must have not moved for LOADING_DELAY turns.
- If a robot wants to move, it must have not moved for MOVE_DELAY turns and must have not attacked for COOLDOWN_DELAY turns.

Structures In-depth [bcd14]
-------------------

#### HQ
- The HQ has 2000 hp, 24 attack, and 2 attack delay. Base attack range is 24.
- The HQ has a bytecode limit of 10000 and a sight range of 35.
- The HQ produces 100*(2+supply_depots^0.6) supply per turn, which is added to its supply amount.
- The HQ can spawn BEAVERs.
- Based on the number of friendly towers, the HQ gains certain buffs:
 - 1 tower: HQ takes 80% damage.
 - 2 towers: HQ range increases to 35.
 - 3 towers: HQ does 150% damage.
 - 4 towers: HQ takes 50% damage (overrides the 1-tower buff)
 - 5 towers: HQ attack delay is halved (to 1) and gains 50% splash damage within a range of 2, which only damages enemies.
 - 6 towers: HQ takes 30% damage and does 1000% damage.

#### TOWER
- Towers have 1000 hp, 8 attack, and 1 attack delay. Attack range is 24 units^2.
- Towers have bytecode limits of 2000 and sight ranges of 35.

#### Other structures
- Buildable structures all have 100 max hp.
- Bytecode limits of 2000 and sight range of 24.
- They have no supply upkeep, face no penalties for having no supply, and can hold infinite supply.
- They cannot attack or move.
- They can send messages.

| Name                 | Req             | Costs              | Use                            |
|----------------------|-----------------|--------------------|--------------------------------|
| Supply Depot         | None            | 100 ore, 40 turns  | Increases HQ supply generation |
| Miner Factory        | None            | 500 ore, 100 turns | Spawns Miners                  |
| Technology Institute | None            | 200 ore, 50 turns  | Spawns Computers               |
| Barracks             | None            | 300 ore, 50 turns  | Spawns Soldiers and Bashers    |
| Helipad              | None            | 300 ore, 100 turns | Spawns Drones                  |
| Training Field       | Tech. Institute | 200 ore, 200 turns | Spawns Commander               |
| Tank Factory         | Barracks        | 500 ore, 100 turns | Spawns Tanks                   |
| Aerospace Lab        | Helipad         | 500 ore, 100 turns | Spawns Launchers               |
| Handwash Station     | None            | 200 ore, 100 turns | Sanitation                     |


Units In-Depth [bcd15]
----------------------

#### All units
- Units all have bytecode limits of 10000 and 24 sight range (units^2), except for COMPUTER, which has a 20000 bytecode limit.
- All units except the COMPUTER and LAUNCHER can attack. The BASHER attacks automatically (regardless of attack delay), however, which is different from other units.
- All units except for the MISSILE can send messages.

| Name      | Spawned From    | Cost               | Supply Upkeep | HP  | Attack | Range | MD | AD  | LD  | CD  |
|-----------|-----------------|--------------------|---------------|-----|--------|-------|----|-----|-----|-----|
| Beaver    | HQ              | 100 ore, 20 turns  | 10            | 30  | 4      | 5     | 2  | 2   | 1   | 1   |
| Miner     | Miner Factory   | 60 ore, 20 turns   | 8             | 50  | 3      | 5     | 2  | 2   | 2   | 1   |
| Computer  | Tech. Institute | 10 ore, 25 turns   | 2             | 1   | n/a    | n/a   | 8  | n/a | n/a | n/a |
| Soldier   | Barracks        | 60 ore, 20 turns   | 5             | 40  | 4      | 8     | 2  | 1   | 1   | 1   |
| Basher    | Barracks        | 80 ore, 20 turns   | 6             | 64  | 4      | 2*    | 2  | 1   | 0   | 1   |
| Drone     | Helipad         | 125 ore, 30 turns  | 10            | 70  | 8      | 5     | 1  | 3   | 1   | 1   |
| Tank      | Tank Factory    | 250 ore, 50 turns  | 15            | 144 | 20     | 15    | 2  | 3   | 2   | 2   |
| Commander | Training Field  | 100* ore, 200 turns| 15            | 200 | 6      | 10    | 2  | 1   | 0   | 0   |
| Launcher  | Aerospace Lab   | 400 ore, 100 turns | 25            | 200 | n/a    | n/a   | 4  | 8*  | n/a | n/a |
| Missile   | Launcher        | 8 turns            | 0             | 3*  | 18     | 2*    | 1  | 0   | 0   | 0   |

All ranges are expressed as squared radius. A square radius of X includes all locations within a Euclidean distance of sqrt(X).

### Unique unit properties:
#### BEAVER:
- Can mine at a rate of max(min(n/20,2),0.2) ore per turn, where n is the amount of ore on their location.
- Can build structures.

#### COMPUTER:
- Cannot attack.

#### BASHER:
- Attacks hit all enemy units within range 2. Each BASHER attacks automatically at the end of every turn, after any movement (if the BASHER moves, the attack hits enemies around the location the BASHER moves to, rather than the location it started in).
- Attacks every turn regardless of attack delay.

#### MINER:
- Can mine at a rate of max(min(n/4,2.5),0.2) ore per turn, where n is the amount of ore on their location.

#### DRONE:
- Can move onto VOID terrain, but incurs twice the loading and movement delay.

#### COMMANDER:
- A team can only possess one COMMANDER at any time. The ore cost of building a commander starts at 100, and doubles each time a COMMANDER is built by the team.
- Each enemy unit (not structures) that dies within 24 range of him rewards him experience equal to the ore cost of the enemy unit.
- The commander also has skills.
 - Regenerate (passive) - The commander automatically gains 1 health per turn.
 - Flash - (20 turn cooldown) - Teleports to any valid location within 10 range. Can only be used when you have no core delay, and adds a movement delay of 1.
 - Leadership (passive, gained at 1000 xp) - All allied units within range 24 deal 1 additional damage. If the COMMANDER has 2000 xp, the additional damage is 2 instead of 1. MISSILEs are not boosted.
 - Heavy Hands (gained at 1500 xp) - COMMANDER attacks set the target's weapon and core delays to 3, if the delays are below 3. Does not work on enemy TOWER, COMMANDER, or HQ. It will prevent LAUNCHERs from generating new MISSILEs but does not stop them from launching existing MISSILEs. BASHER attacks will not be affected, nor will MISSILE explosions (but both will be disabled from moving).
 
#### LAUNCHER:
- Cannot attack directly. A LAUNCHER's weapon delay is associated with the amount of time it needs to generate a new MISSILE.
- Automatically generates MISSILEs that can be launched. A LAUNCHER gains one MISSILE every 8 turns if supplied or one MISSILE every 16 turns if unsupplied, and can store up to 5. 
- Launching a missile in a direction subtracts one from the LAUNCHER's missile count and creates a MISSILE unit in the square in that direction. Missiles can be launched regardless of weapon or core delay.
- The `canLaunch()` method is there to help check if a launch is valid.
- LAUNCHERs can move and launch missiles in the same turn, but `launchMissile()` must be called before `move()`.

#### MISSILE:
- Special unit spawned only by LAUNCHERs.
- MISSILEs only have 3 hp, but only take 1 damage from any attack.
- MISSILEs are automatically destroyed at the end of their fifth turn after spawning.
- MISSILEs can destroy themselves with `explode()`.
- When a MISSILE is destroyed, it hits all adjacent robots for 18 damage (with friendly fire). If a MISSILE is destroyed by attacks, then the damage dealt is halved.
- MISSILEs have a bytecode limit of 500.
- MISSILEs cannot write messages (but they can read them).

Getting Help [bcd16]
-------------

We have both a forum (https://www.battlecode.org/contestants/forum/) and an IRC Channel (#battlecode on irc.freenode.net). Hang out and chat with us -- we're friendly!

Disclaimers [bcd17]
-------------

We have done our best to test and balance the properties of the Battlecode world. Inevitably, however, we will need to make adjustments in the interest of having a fair competition that allows a variety of creative strategies. We will endeavor to keep these changes to a minimum, and release them as early as possible. All changes will be carefully documented in the Changelog.

Despite our best efforts, there may be bugs or exploits that allow players to operate outside the spirit of the competition. Using such exploits for the tournament or scrimmage will result in immediate disqualification, at the discretion of the directors. Such exploits might include, but are not limited to, robot communication without broadcasts, bypassing the bytecode limit, or terminating the game engine. If you are not sure what qualifies as "in the spirit of the competition", ask the devs before submitting your code.

Appendix A: Javadocs and Other References [bcd18]
------------

Javadocs can be found here (https://www.battlecode.org/contestants/releases/), included in the software distribution. Here, you'll find everything you need, as well as some helpful methods that might not be mentioned above.

The javadocs include the values of the game constants and robot attributes.

### Sample Player

Also included in the release is an example player, the `examplefuncsplayer`. It defines `RobotPlayer`, which is a class with a public static `run` method that takes one argument of type `battlecode.common.RobotController`. Whenever a new robot is created, the game calls the run method with the robots RobotController as its argument. If this method ever finishes, either because it returned or because of an uncaught exception, the robot dies and is removed from the game. You are encouraged to wrap your code in loops and exception handlers so that this does not happen.

The RobotController argument to the RobotPlayer constructor is very important -- this is how you will control your robot. RobotController has methods for sensing (e.g. `senseNearbyObjects`, `senseOre`, etc) and performing actions (e.g., `move()`, `attackLocation()`, etc). If you're not sure how to get your robot to do something, the Javadocs for RobotController are a good place to start.

Notice the while(true) loop, which prevents the run method from returning. While the robot is alive, it will be continually cycling through this loop. The try/catch block inside the loop prevents the robot from throwing an uncaught exception and dying from it.

Appendix B: More In-depth Mechanics [bcd19]
-------------------

### Team Memory

Official matches will usually be sets of multiple games. Each team can save a small amount of information (`GameConstants.TEAM_MEMORY_LENGTH` longs) for the next game using the function `setTeamMemory()`. This information may be retrieved using `getTeamMemory()`. If there was no previous game in the match, or no information was saved, then the memory will be filled with zeros.

### Execution Order

The game is comprised of a number of rounds. During each round, all robots get a turn in the order they were spawned, starting with the two HQs. Newly spawned robots will have a turn on the same round they were created.

The following is a detailed list of a robot's execution order within a single turn. If it dies halfway through, the remainder of the list does not get executed. In particular, note that changes to a robot's state (via actions) do not happen while player code is being executed. All actions instead get sent to an action queue, and they are executed after the player code is run. For example, if a SOLDIER calls move() and then getLocation(), it will not reflect the location of the robot yet. However, changes to weapon and core delay happen immediately after action methods are called, so isCoreReady() and isWeaponReady() will always reflect whether certain actions can still be performed in a turn.

1. Robot's core and weapon delays are decremented, and the robot's supply upkeep is paid.
2. Robot executes player code until the total number of bytecodes executed exceeds the robot's allotted amount, with supply restrictions taken into account. The following things happen immediately: changes in weapon delay and core delay, broadcasts, explosions, and disintegrating.
3. Supply is subtracted based on how many bytecodes were used.
4. Actions (queued) are performed in this order:
    - Supply is transferred between units.
    - Attacks happen, except for BASHER attacks.
    - Missiles are launched (LAUNCHER only).
    - Core actions (moving, mining, spawning, or building) happen. Commander FLASH happens here.
    - Basher attacks (BASHER only).
5. End-of-turn events: missile production, supply generation, commander health regeneration, and missile deaths.

### Timing

Each robot is allowed a certain amount of computation each round. Computation is measured in terms of Java bytecodes, the atomic instructions of compiled Java code. Individual bytecodes are simple instructions such as "subtract" or "get field", and a single line of code generally contains several bytecodes. (For details see http://en.wikipedia.org/wiki/Java_bytecode) Each round, every player runs a number of bytecodes determined by the robot's individual properties. When a robot hits the bytecode limit, its computation is paused while other robots get to do their computation for the same round or the next round. On the next round, the robot's computation is resumed exactly where it left off. Thus, to the robot's code, the round change is invisible. Nothing will jump out and shout at the robot when a round ends.

### Monitoring

The Clock class provides a way to identify the current round ( `Clock.getRoundNum()` ), and how many bytecodes have been executed during the current round ( `Clock.getBytecodeNum()` ).

### GameActionExceptions

GameActionExceptions are thrown when something cannot be done. It is often the result of uncertainty about the game world, or an unexpected round change in your code. Thus, you must write your player defensively and handle GameActionExceptions judiciously. You should also be prepared for any ability to fail and make sure that this has as little effect as possible on the control flow of your program.

Exceptions cause a bytecode penalty of 500 bytecodes.

### Java Language Usage

The next few sections deal with some of the mechanics of how your players are run in the game engine, including bytecode-counting, library restrictions, etc.

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

The function `System.arraycopy` costs one bytecode for each element copied. All other functions have a fixed bytecode cost. These costs are listed in the `MethodCosts.txt` file. Methods not listed are free. The bytecode costs of battlecode.common functions are also listed in the javadoc.

### Memory Usage

Robots must keep their memory usage reasonable. If a robot uses more than 8 Mb of heap space during a tournament or scrimmage match, the robot may be killed.

### Debugging

This section describes some of the features of the game engine intended to make debugging somewhat less painful. Debug mode reveals valuable information about robots at development time but will be turned off for scrimmages and real tournaments.

#### System.out

Any output that your robots print to System.out is directed to the output stream of the Battlecode engine, prefixed with information about the robot.

#### Indicator Items and Control Bits

You'll find that your primary source of debugging is setting one of 3 indicator strings that are viewable in the client.  Unlike System.out which is not synchronized to match execution (as the engine precomputes the game faster than the client views it), Indicator strings are synchronized to the round number and can be used for debugging complex robot behaviors. 

Use `setIndicatorString(int,String)` to change a robot's indicator string. The are viewable in the top right corner of the client when the robot is selected. Indicator strings maintain value until they are changed.

In addition, there exist `setIndicatorDot()` and `setIndicatorLine()`, which draw visuals on the map when the robot is selected. Second, the user can manually set a long for each robot, which the robot can query using `getControlBits()`.

#### Debug Methods

The game engine has a feature that allows you to separate out debugging code that is unimportant to your player's performance in the tournament. Methods that have names beginning with debug_ and that have a void return type are given special status. By default, these methods are skipped during execution of the player. When the System property debugMethodsEnabled is set to true, however, the methods are executed normally except that they do not count against your robot's bytecode limit. Code that prepares the arguments to such a method may consume bytecodes, but the body of the method and any methods that it invokes are not counted.

#### System Properties

Your robot can read system properties whose names begin with "bc.testing.". You can set a property by adding a line to bc.conf like this:

```
bc.testing.team-a-strategy=experimental
```

You can check the value of the property like this:

```java
String strategy = System.getProperty("bc.testing.team-a-strategy");
```

#### Breakpoints

Breakpoints allow you to pause the game engine's calculations. If breakpoints are enabled (see the software page), and a robot calls RobotController.breakpoint(), the game engine will stop computing at the end of the round. This gives you a chance to see exactly what's going on in the game when your robot hits a certain point in its code. You can resume the game engine's computation in the client, by hitting the "resume" button. If the match is being dumped straight to a file (i.e., there is no client to resume the game), breakpoints are ignored.

Note that when a robot calls breakpoint(), computation will be stopped at the end of the round, not immediately when breakpoint() is called. Depending on the circumstances, you might want to use breakpoint(); yield(); instead.

Changelog [bcd20]
-------------------
* 1.0.0 (1/5/2015) - Initial specs released
* 1.0.1 (1/6/2015) - Client/engine bug fixes and changes. Fixed specs typos and added small clarifications. BACKWARDS INCOMPATIBLE.
    * `RobotInfo` now tells you information about whether a robot is building something or being built.
    * `senseOre()`, `senseTerrainTile()`, and `disintegrate()` work properly on round 0.
    * Fix bug with sensor radius checking, and fixed sensing off the map.
    * More meaningful game over messages.
    * Changes to some of the maps (notably, onetower only has one tower now).
    * You can no longer steal supply from other units.
* 1.0.2 (1/7/2015) - Engine bug fixes, specs clarifications, and minor gameplay changes.
    * `HashSet`, `TreeSet`, and other `java.util` classes work properly now.
    * A few additional methods now have a fixed Bytecode cost (in `Math`, `StrictMath`, `String`, `StringBuffer`, and `StringBuilder`).
    * The amount of ore mined per turn used to be lower-bounded by the MINIMUM_MINING_AMOUNT game constant, which was not in the specs. We have decided to keep this functionality, but the MINIMUM_MINING_AMOUNT has been reduced from 1 to 0.2. The specs have been changed to reflect this.
    * LAUNCHERs now gain new missiles at approximately half the original rate (once every 12 turns rather than once every 6 turns) when they have no supply. In addition, the LAUNCHER's weapon delay now tracks the amount of time it will need to generate the next MISSILE. Note that launching a missile is independent of this weapon delay, and that building a MISSILE happens automatically. This is explained in the specs.
* 1.0.3 (1/10/2015) - Engine bug fixes and small changes. Client improvements. Minor specs clarifications.
    * `HashSet`, `TreeSet`, and other `java.util` classes work properly now on Java 7.
    * Disable missile broadcasts to be consistent with specs.
    * Added methods `canSenseRobot` and `senseRobot` to help get information about a robot given just its ID.
    * Robot IDs range from 1 to 32000.
    * In any round, HQs always execute before other units.
    * Clarify that broadcasts happen immediately, unlike in past years.
* 1.1.0 (1/15/2015) - Post-Sprint release. Engine bug fixes. Gameplay changes. Client improvements. Slightly backwards incompatible (some GameConstants changed from being ints to doubles).
    * MapLocation.getAllMapLocationsWithinRadiusSq can no longer be called with a radiusSquared argument greater than 100. The engine is open source so you are free to implement the method yourself.
    * Slightly change how core and weapon delay count down when a structure is built.
    * Missiles correctly explode after their 5th turn alive (instead of 6th).
    * Added `isBuildingSomething()` to RobotController.
    * Commander FLASH spell is fixed.
    * Balance changes (see the website changelog for rationale):
        * Drone: increase cooldown delay and loading delay to 1; decrease attack range to 5; drones incur twice movement and loading delays when moving onto void; supply upkeep is 10.
        * Basher: increase health to 64.
        * Miner: increase ore cost to 60; decrease max mine amount to 2.5.
        * Soldier: decrease attack delay to 1; decrease attack power to 4.
        * Launcher: decrease max missile storage to 5; decrease health to 300.
        * Computer: decrease supply upkeep to 2; increase bytecode limit to 20000.
        * Commander: increase health to 200; increase flash range to 10; increase leadership range to 24; make flash innate; make leadership improve at 2000xp to grant 2 extra damage; add heavy hands ability to add up to 3 core and weapon delay upon attacks; regen is 2.
        * Missile: a missile that is destroyed by attacks will only do half damage.
        * Structures: increase helipad turn cost to 100; increase supply depot turn cost to 40; decrease supply generation formula to 100*(2+supply_depots^0.6).
* 1.1.1 (1/15/2015) - Clarify in specs that Commander Heavy Hands does not affect TOWER, HQ, and COMMANDER. Fix exploit with `isPathable()`. COMMANDER regen rate back down to 1. Correctly not reward XP for enemy structure deaths.
* 1.1.2 (1/16/2015) - Bug fixes.
    * Clarify that changes in core and weapon delay happen immediately even though the corresponding actions are queued for the end of a turn.
    * Clarifications for Heavy Hands.
    * Bug fix: COMMANDERs can Flash a distance of 10 (units^2) now.
    * Bug fix: MISSILEs destroyed by other MISSILEs (either team) will properly explode now with half damage.
* 1.1.3 (1/17/2015) - Bug fix: no more "zombie" MISSILEs (exploded MISSILEs that do not disappear from the client).
* 1.2.0 (1/22/2015) - Post-Seeding release. New maps. Additional client toggle options. Gameplay changes.
    * Important: not all games will have a 2000 turn round limit. This round limit will now vary depending on the map but will always be in the range [2000, 3000]. For a given map, you can query the round limit by using `getRoundLimit()`. Generally, only larger maps will have larger round limits. We have released a few maps with changed round limits (marked by "extended" in the map name).
    * Balance changes:
        * Launcher: decrease health to 200; increase turns to generate a missile to 8 (16 if unsupplied).
        * Missile: decrease attack power to 18 (9 if destroyed by attack).
        * Commander: decrease attack power to 6; increase supply upkeep to 15; increase turn cost to 200; increase Flash cooldown to 20.
        * Tank: decrease health to 144.
        * Soldier: increase attack range to 8; increase turn cost to 16.
* 1.2.1 (1/23/2015) - Balance changes.
    * Training field turn cost increased from 100 to 200.
    * Soldier turn cost increased from 16 to 20.
* 1.2.2 (2/1/2015) - Post-finals release. Release new maps.
* 1.2.3 (2/17/2015) - Add a forgotten map. Release compiled version of referenceplayer.
