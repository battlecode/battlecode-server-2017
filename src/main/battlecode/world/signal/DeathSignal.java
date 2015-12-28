package battlecode.world.signal;

/**
 * Signifies that an object has died somewhere.
 *
 * @author Matt
 */
public class DeathSignal implements Signal {

    private static final long serialVersionUID = 8518453257317948520L;

    /**
     * The ID of the object that died.
     */
    private final int objectID;

    /**
     * Whether the death of the robot is due to it being activated.
     */
    private final boolean deathByActivation;

    /**
     * Creates a signal representing the death of
     * the specified object.
     *
     * @param objectID the ID of the object that died
     */
    public DeathSignal(int objectID) {
        this.objectID = objectID;
        this.deathByActivation = false;
    }

    /**
     * Creates a signal representing the death of the specified object and
     * contains information on whether the death was due to the bot being
     * activated.
     *
     * @param objectID the ID of the object that died
     * @param deathByActivation whether the death was due to activation
     */
    public DeathSignal(int objectID, boolean deathByActivation) {
        this.objectID = objectID;
        this.deathByActivation = deathByActivation;
    }

    /**
     * Returns the ID of the object that just died.
     *
     * @return the dying object's ID
     */
    public int getObjectID() {
        return objectID;
    }

    /**
     * Returns whether the death is because a robot was activated.
     *
     * @return whether the death is because a robot was activated.
     */
    public boolean isDeathByActivation() {
        return deathByActivation;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private DeathSignal() {
        this(0);
    }
}
