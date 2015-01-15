package battlecode.common;

/**
 * This enumeration represents the unique skills possessed by the Commander robot. <br>
 * There are four skills: Regeneration, Leadership, Flash, and Heavy Hands.
 * <p>
 * <b>Regeneration</b> - the Commander regenerates 2 hp per turn. This skill is passive and
 * acquired by default. <br>
 * <b>Leadership</b> - all robots within 15 range of the Commander deal 1 additional damage with
 * attacks. This skill is acquired at 500 xp. <br>
 * <b>Flash</b> - The Commander targets a location within range and instantly moves to that location.
 * This skill is acquired at 1000 xp and has a cooldown. <br>
 * <b>Heavy Hands</b> - The commander's attacks deal 4 points of attack and movement delay to the
 * target unit. This skill is acquired at 1500 xp. <br>
 */
public enum CommanderSkillType {
        /* passive: +2 hp/sec */
        REGENERATION,

        /* passive: AoE increase to damage dealt; gained at 500 XP */
        LEADERSHIP,

        /* short-range teleport; gained at 1000 XP */
        FLASH,

        /* attacks deal movement and attack delay */
        HEAVY_HANDS,

        /* ... and more? */
}
