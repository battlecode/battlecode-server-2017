package battlecode.common;

/**
 * This enumeration represents the unique skills possessed by the Commander robot.
 * There are three skills: Regeneration, Leadership, and Flash.
 * Regeneration - the Commander regenerates 1 hp per turn. This skill is passive and
 * acquired by default.
 * Leadership - all robots within 15 range of the Commander deal 1 additional damage with
 * attacks. This skill is acquired at 1000 xp.
 * Flash - The Commander targets a location within range and instantly moves to that location.
 * This skill is acquired at 2000 xp and has a cooldown.
 */
public enum CommanderSkillType {
        /* passive: +1 hp/sec */
        REGENERATION,

        /* passive: AoE increase to damage dealt; gained at 1000 XP */
        LEADERSHIP,

        /* short-range teleport; gained at 2000 XP */
        FLASH,

        /* ... and more? */
}
