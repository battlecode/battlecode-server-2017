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
16. Execution Order In-Depth [bcd16]

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
- Each robot also has an individual 'supply' count. A robot's supply decreases as it performs actions or runs code. Robots without supply will stil be able to perform actions and/or run code, but with penalties to delay reduction and/or bytecode limit.
- Robots have two delay timers, a weapon delay and a core delay (for all non-attack actions). However, attacking and moving are not completely separate, as performing an attack may also increase core delay, and vice versa.
- To communicate between robots, you can post and read integers to and from a team-shared array.
- The layout and details of the map are not initially revealed. However, robots know the location of allied and enemy HQs and TOWERs.

Robot Overview [bcd04]
--------------

Robots are the central part of the Battlecode world.

Each robot in the Battlecode game will run the `run()` function located in its team's RobotPlayer.java file, regardless of the type of robot. Each robot runs on a separate JVM, which means that static variables in RobotPlayer.java are not actually shared.

The Battlecode game is turn-based and divided into 'rounds' or 'turns', in which each robot gets a chance to run code and perform actions. Code that a robot runs costs bytecodes, either a predetermined amount for certain game functions, or otherwise the amount of Java bytecode that the code generates. A robot only has a certain amount of bytecodes per turn, after which the robot's turn is immediately ended and the computation is continued on the next turn. Using `yield()` can end a turn early, saving bytecodes and ending computation.

All robots have a certain amount of HP (also known by hitpoints, health, life, or such). When a robot's HP reaches zero, the robot is immediately removed from the game.

This year's game contains a wide variety of different robots. There are two major classes of robots: structures and units. The term 'robots' refers to both classes.

The following is a visual summary of the units, the buildings, and the dependencies.

![Image of Tech Tree]
(https://github.com/battlecode/battlecode-server/edit/master/techtree.png)

Structures Overview [bcd05]
----------

Note that this is an overview section. Detailed structure statistics can be found in a later section.

Structures refer to all robots that cannot move. There are two types of special structures, TOWERs and HQs, that each team starts the game with and which cannot be built.

#### HQ
The HQ is your main base and your most important robot. Losing the HQ is equivalent to losing the game.
- Each team starts with exactly one HQ pre-placed on the map
- Can attack at long range
- Can build BEAVER units
- Offensive and defensive abilities are increased for each friendly TOWER alive
- Per-turn bytecode limit of 10000

#### TOWER
TOWERs are powerful defensive structures in their own right, but TOWERs also increase the strength of the allied HQ by merely existing.
- Each team starts with up to six TOWERs pre-placed on the map
- Can attack at long range
- Cannot build units
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

Units refer to all robots that can move. Each unit must be spawned at an appropriate building (or unit, in the case of MISSILEs). Each unit has a per-turn bytecode limit of 10000, with the exception of MISSILEs, which have per-turn bytecode limits of only 500.

#### BEAVER
The BEAVER is a versatile starting unit that can do it all.
- Spawned at HQ
- Only unit that can built structures
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

#### SOLDIER
The SOLDIER is a basic, generic combat unit.
- Spawned at Barracks
- Has a mediocre attack at short range
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
- Has a decent attack at decent range
- Moves fast
- Can move over VOID terrain

#### COMMANDER
The COMMANDER is a strong unit with special abilities.
- Spawned at Training Field
- Each team can only control one COMMANDER at a time
- Ore cost doubles for each friendly COMMANDER that has been produced
- High HP
- Has a strong attack at decent range
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
- Generates a MISSILE every 6 turns and can store up to 6
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

Each location of each map has some nonnegative (possibly zero) amount of ore on it, which can be mined by BEAVERs and MINERs standing on that location by calling the `mine()` method. If the amount of ore on a location is n, BEAVERs mine min(2,n/20) per `mine()` call while MINERs mine min(3,n/4) per `mine()` call. Mining reduces the amount of ore on the robot's location and adds that amount to the player's stockpile.

Players start the game with 500 ore, and each team automatically receives 5 ore per turn before any mining income.

Unit/structure costs can be found further down, in the in-depth unit/structure sections.

Supply [bcd08]
-------------

Each robot carries some nonnegative amount of 'supply'. The HQ automatically generates a certain amount of supply each turn, which is increased for each friendly supply depot standing. The exact formula for the amount of supply generated by the HQ per turn is 100*(2+supply_depots^0.7).

Robots can carry arbitrary amounts of supply and can transfer integer amounts of supply to other robots within a square radius of 15. When a robot is destroyed, its supply is destroyed along with it.

Each unit type has a certain 'supply upkeep' associated with it. Roughly put, a unit that is fully active (performing actions as often as possible) will lose that amount of supply per round. The exact mechanics behind this are complicated and not so important to know, but they are in the 'Actions and Delays' section further down.

In addition, using more than 2000 bytecodes in a single turn will cost the unit 0.001 supply per extra bytecode.

Units that have no supply faces two penalties. First, it will only be able to act (move, mine, and/or attack) roughly half as fast as it would with supply. Second, its bytecode limit is halved to 5000. Structures have no supply upkeep and never face penalties from lacking supply.

Messaging [bcd09]
---------------

Each team has a size-65536 array of ints that can be broadcasted to and read from. These arrays are private, so message jamming and interference is impossible between teams. All robots (except for MISSILEs) can broadcast to the message array, and all robots can read from the array.

Maps and Terrain [bcd10]
--------------

Maps for this year's competition will be rectangular grids whose dimensions may be from 30x30 to 120x120. Each square of the grid will either be VOID or non-VOID (most units cannot move onto VOID squares, and structures cannot be built there).

One HQ per team and up to six TOWERs per team will be pre-placed on non-VOID squares of each map. None of these starting structures will be in attack range of any enemy starting structure.

Each non-VOID square on the map will be connected to all other non-VOID squares (in essence, a single unit placed on the map should be able to navigate to any square to another). There will be at least one non-VOID square adjacent to the HQ.

Each non-VOID square of the map will start with some nonnegative amount of ore. Teh Devs make no guarantees about the patterns of ore, but we will attempt to make the distribution of ore across squares somewhat 'continuous'.

Naturally, each map will be symmetric by either a reflection or a rotation.

Sensing [bcd11]
---------------

Info on robots in sight range of any allied unit can be sensed (so vision is shared between robots). The locations of allied and enemy HQs are known along with the location of all allied and enemy TOWERs.

HQ and TOWERs have can see all locations within square radius 35, while all other robots have sight ranges of 24.

The terrain type and ore amount of a map square cannot be sensed until that map square has been within sight range of an allied unit. The ore amount sensed on a map square will return the ore amount on the square during the last time that map square was within allied sight range - meaning that enemy mining activity cannot be sensed from far away.

Sensing info on a robot includes the robot's location, delays, its supply levels, its type, its health, and more. See the `RobotInfo` Java documentation for more details.

Victory and Tiebreaks [bcd12]
---------------

A team whose HQ is destroyed immediately loses (and conversely, a team who destroys the opponent HQ immediately wins!).

However, games must end in finite amounts of time, and hence each game only lasts 2000 turns. If neither HQ is destroyed by the end of turn 2000, the winner will be determined by a series of tiebreaks. These tiebreaks, in order of their application, are:

1. Number of towers remaining
2. HQ HP remaining
3. Total HP of towers
4. Number of handwash stations
5. Team HQ ID

Actions and Delays [bcd13]
--------------

Certain robot actions cannot be performed multiple times in a single turn or short period of time. These actions are:

#### Attacking
Attacking can only be done when the robot's weapon delay is <1, and can be performed by calling the `attackLocation()` method. This deals damage to the unit on the targeted square. Bashers automatically attack all adjacent enemies after movement.

#### Moving
Moving can only be done when the robot's core delay is <1, and can be performed by calling the `move()` method. This moves the unit in the specified direction. If you wish to check whether a move is valid, you can use the `canMove()` method.

#### Mining
Only BEAVERs and MINERs can mine, and only when the robot's core delay is <1. It can be performed by calling the `mine()` method. This reduces the amount of ore on the robot's square by a certain value (see the section on Ore) and increases the player's stockpile by that value.

#### Spawning
Certain structures can spawn certain units, but only when the structure's coreDelay is <1. Spawning can be performed by calling the `spawn()` method. This immediately deducts the ore cost of the unit from the player's stockpile, then creates one unit of the specified type in the specified direction. The structure's core delay is increased by the turn cost of the spawned unit type. The `hasSpawnRequirements()` and `canSpawn()` methods can be used to check whether a spawn action is legal.

#### Building
Only BEAVERs can build, and only when the robot's core delay is <1. When `build()` is called, several things happen. The ore cost of the structure is deducted from the player's stockpile. The BEAVER is put in a 'constructing' state for a certain number of turns, during which it cannot perform any above action but can compute. A incomplete structure is also created in the specified direction, which starts with 50 HP (half of building max HP). This incomplete structure cannot compute or perform any actions. The number of turns for construction depends on the type of structure being built. 
After the required number of turns, the structure will become complete, and its HP will double. The BEAVER will be also be able to perform other actions again.

The `hasBuildRequirements()` and `canBuild()` methods can be used to check whether a build action is legal.

#### Exploding
Only MISSILEs can explode. When `explode()` is called, the missile is immediately destroyed, and 20 damage is dealt to all adjacent units (regardless of team).

#### Disintegrating
Any robot can call the `disintegrate()` method. This is a suicide method, and immediately destroys the robot that calls it. Using this method is generally not advised.

### Explanation of Delays
#### TL;DR version
Each type of unit has an associated fixed movement delay and fixed attack delay. Roughly put, units can only move once every (MOVE_DELAY) turns and can only attack once every (ATTACK_DELAY) turns. For units without supply, these values are approximately doubled. HQs and TOWERs also have an attack delay and can only attack once every (ATTACK_DELAY) turns, but they have no supply restrictions. Also, `mine()` counts as movement.

The rest of this section details the exact implementation of delays. It may be complicated and is not fully necessary for writing a bot.

Each individual robot has two changing delay counters: a core delay and a weapon delay. Delays are doubles that represent some amount of time that must pass before an action can be taken. Weapon delay pertains to the action of attacking, while core delay pertains to all other actions (mining, spawning, building, and moving). Specifically, actions cannot be performed unless the associated delay is <1. Both delay counters decrease by a base of 0.5 per turn, but can be further reduced by an additional 0.5 per turn if the robot has enough supply to pay its supply upkeep (this happens automatically). Therefore, a supplied robot can act about twice as often as an unsupplied robot.

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
- The HQ produces 100*(2+supplyDepots^0.7) supply per turn, which is added to its supply amount.
- The HQ can spawn BEAVERs.
- Based on the number of friendly towers, the HQ gains certain buffs:
1 tower: HQ takes 80% damage.
2 towers: HQ range increases to 35.
3 towers: HQ does 150% damage.
4 towers: HQ takes 50% damage (overrides the 1-tower buff)
5 towers: HQ attack delay is halved (to 1) and gains 50% splash damage within a range of 2, which only damages enemies.
6 towers: HQ takes 30% damage and does 1000% damage.

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
| Supply Depot         | None            | 100 ore, 20 turns  | Increases HQ supply generation |
| Miner Factory        | None            | 500 ore, 100 turns | Spawns Miners                  |
| Technology Institute | None            | 200 ore, 50 turns  | Spawns Computers               |
| Barracks             | None            | 300 ore, 50 turns  | Spawns Soldiers and Bashers    |
| Helipad              | None            | 300 ore, 50 turns  | Spawns Drones                  |
| Training Field       | Tech. Institute | 200 ore, 100 turns | Spawns Commander               |
| Tank Factory         | Barracks        | 500 ore, 100 turns | Spawns Tanks                   |
| Aerospace Lab        | Helipad         | 500 ore, 100 turns | Spawns Launchers               |
| Handwash Station     | None            | 200 ore, 100 turns | Sanitation                       |


Units In-Depth [bcd15]
----------------------

#### All units
- Units all have bytecode limits of 10000 and 24 sight range.
- All units except the COMPUTER and LAUNCHER can attack. The BASHER attacks automatically, however, which is different from other units.
- All units except for the MISSILE can send messages.

| Name      | Spawned From    | Cost               | Supply Upkeep | HP  | Attack | Range | MD | AD  | LD  | CD  |
|-----------|-----------------|--------------------|---------------|-----|--------|-------|----|-----|-----|-----|
| Beaver    | HQ              | 100 ore, 20 turns  | 10            | 30  | 4      | 5     | 2  | 2   | 1   | 1   |
| Miner     | Miner Factory   | 50 ore, 20 turns   | 8             | 50  | 3      | 5     | 2  | 2   | 2   | 1   |
| Computer  | Tech. Institute | 10 ore, 25 turns   | 5             | 1   | n/a    | n/a   | 8  | n/a | n/a | n/a |
| Soldier   | Barracks        | 60 ore, 15 turns   | 5             | 40  | 8      | 5     | 2  | 2   | 1   | 1   |
| Basher    | Barracks        | 80 ore, 20 turns   | 6             | 64  | 5      | 2*    | 2  | 1   | 0   | 0   |
| Drone     | Helipad         | 125 ore, 30 turns  | 5             | 70  | 8      | 10    | 1  | 3   | 0   | 0   |
| Tank      | Tank Factory    | 250 ore, 50 turns  | 15            | 160 | 20     | 15    | 2  | 3   | 2   | 2   |
| Commander | Training Field  | 100* ore, 80 turns | 5             | 120 | 10     | 10    | 2  | 1   | 0   | 0   |
| Launcher  | Aerospace Lab   | 400 ore, 100 turns | 25            | 400 | n/a    | n/a   | 4  | 6*  | n/a | n/a |
| Missile   | Launcher        | 6 turns            | 0             | 3*  | 20     | 2*    | 1  | 0   | 0   | 0   |

### Unique unit properties:
#### BEAVER:
- Can mine at a rate of min(n/20,2) ore per turn
- Can build structures

#### COMPUTER:
- Cannot attack.

#### BASHER:
- Attacks hit all enemy units within range 2. Each BASHER attacks automatically at the end of every turn, after any movement (if the BASHER moves, the attack hits enemies around the location the BASHER moves to, rather than the location it started in).

#### MINER:
- Can mine at a rate of min(n/4,3) ore per turn.

#### DRONE:
- Can move onto VOID terrain.

#### COMMANDER:
- A team can only possess one COMMANDER at any time. The ore cost of building a commander starts at 100, and doubles each time a COMMANDER is built by the team.
- Each enemy unit (not structures) that dies within 24 range of him rewards him experience equal to the ore cost of the enemy unit.
- The commander also has skills.
 - Regenerate (passive) - The commander automatically gains 1 health per turn.
 - Leadership (passive, gained at 1000 xp) - All allied units within range 15 deal 1 additional damage.
 - Flash - (10 turn cooldown, gained at 2000 xp) - Teleports to any valid location within 5 range.
 
#### LAUNCHER:
- Cannot attack directly
- Automatically generates MISSILEs that can be launched. A LAUNCHER gains one MISSILE every 6 turns and can store up to 6. 
- Launching a missile in a direction subtracts one from the LAUNCHER's missile count and creates a MISSILE unit in the square in that direction.
- The `canLaunch()` method is there to help check if a launch is valid.

#### MISSILE:
- Special unit spawned only by LAUNCHERs.
- MISSILEs only have 3 hp, but only take 1 damage from any attack.
- MISSILEs are automatically destroyed at the end of their fifth turn after spawning.
- MISSILEs can destroy themselves with `explode()`.
- When a MISSILE is destroyed, it hits all adjacent robots for 20 damage (with friendly fire).
- MISSILEs have a bytecode limit of 500.
- MISSILEs cannot write messages (but they can read them)
