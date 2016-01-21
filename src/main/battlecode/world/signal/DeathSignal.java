package battlecode.world.signal;

/**
 * Signifies that an object has died somewhere.
 *
 * @author Matt
 */
public class DeathSignal implements InternalSignal {

    /**
     * Stores information about why a robot died.
     */
    public enum RobotDeathCause {
        NORMAL, ACTIVATION, TURRET
    }

    private static final long serialVersionUID = 8518453257317948520L;

    /**
     * The ID of the object that died.
     */
    private final int objectID;

    /**
     * Not used. Only here to make things compile.
     */
    private final boolean deathByActivation;

    /**
     * Reason for a robot's death.
     */
    private final RobotDeathCause cause;

    /**
     * Creates a signal representing the death of
     * the specified object.
     *
     * @param objectID the ID of the object that died
     */
    public DeathSignal(int objectID) {
        this.objectID = objectID;
        this.deathByActivation = false;
        this.cause = RobotDeathCause.NORMAL;
    }

    /**
     * Creates a signal representing the death of the specified object and
     * contains information on why the robot died.
     *
     * @param objectID the ID of the object that died
     * @param cause the cause of the robot's death
     */
    public DeathSignal(int objectID, RobotDeathCause cause) {
        this.objectID = objectID;
        this.deathByActivation = cause == RobotDeathCause.ACTIVATION;
        this.cause = cause;
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
     * Returns the cause of the robot's death.
     *
     * @return the cause of the robot's death.
     */
    public RobotDeathCause getCause() {
        return cause;
    }

    /**
     * Return whether the robot died due to activation.
     *
     * @return whether the robot died due to activation.
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
