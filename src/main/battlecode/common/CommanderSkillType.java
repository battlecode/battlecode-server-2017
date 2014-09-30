package battlecode.common;

public enum CommanderSkillType {
        /* passive: +1 hp/sec */
        REGENERATION,

        /* target square: do damage after a short number of rounds */
        DELAYED_BURST,

        /* passive: AoE increase to damage dealt */
        LEADERSHIP,

        /* passive: no longer costs supply */
        SELF_SUFFICIENCY, 

        /* target square: all non-self units on that square become invulnerable */
        INTERVENTION,

        /* short-range teleport */
        FLASH,

        /* can hold and fire 3 charges of delayed burst at once */
        ARCANE_RITE,

        /* ... and more? */
}
