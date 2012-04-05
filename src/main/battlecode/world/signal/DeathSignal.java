package battlecode.world.signal;

import battlecode.engine.signal.Signal;
import battlecode.world.InternalObject;

/**
 * Signifies that an object has died somewhere.
 *
 * @author Matt
 */
public class DeathSignal extends Signal {

    private static final long serialVersionUID = 8518453257317948520L;

    /**
     * The ID of the object that died.
     */
    private final int objectID;

    /**
     * Creates a signal representing the death of
     * the specified object.
     *
     * @param object the object that has died
     */
    public DeathSignal(InternalObject object) {
        this.objectID = object.getID();
    }

    /**
     * Creates a signal representing the death of
     * the specified object.
     *
     * @param objectID the ID of the object that died
     */
    public DeathSignal(int objectID) {
        this.objectID = objectID;
    }

    /**
     * Returns the ID of the object that just died.
     *
     * @return the dying object's ID
     */
    public int getObjectID() {
        return objectID;
    }
}
