package battlecode.common;

/**
 * This enumeration represents the unique skills possessed by the Commander robot. <br>
 * There are four skills: Regeneration, Leadership, Flash, and Heavy Hands.
 * <p>
 * <b>Regeneration</b> - the Commander regenerates 2 hp per turn. This skill is passive and
 * acquired by default. <br>
 * <b>Flash</b> - The Commander targets a location within range and instantly moves to that location.
 * This skill is acquired by default and has a cooldown. <br>
 * <b>Leadership</b> - all robots within 24 range of the Commander deal 1 additional damage with
 * attacks. This skill is acquired at 1000 xp. The skill becomes twice as effective at 2000 xp. <br>
 * <b>Heavy Hands</b> - The commander's attacks deal up to 3 points of attack and movement delay
 * to the target unit. This skill is acquired at 1500 xp. <br>
 */
public enum CommanderSkillType {
        /* passive: +2 hp/sec */
        REGENERATION,

        /* passive: AoE increase to damage dealt; gained at 1000 XP, upgraded at 2000 XP */
        LEADERSHIP,

        /* short-range teleport */
        FLASH,

        /* attacks deal movement and attack delay; gained at 1500 XP */
        HEAVY_HANDS,

        /* ... and more? */
}
