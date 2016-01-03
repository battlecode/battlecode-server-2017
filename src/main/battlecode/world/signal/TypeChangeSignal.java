package battlecode.world.signal;

import battlecode.common.RobotType;

/**
 * Signifies that a robot had its type changed.
 */
public class TypeChangeSignal implements InternalSignal {

    private static final long serialVersionUID = -6853620837777044985L;

    /**
     * The ID of the robot that changed type.
     */
    private final int robotID;

    /**
     * The new type.
     */
    private final RobotType type;

    /**
     * Constructs a TypeChangeSignal.
     *
     * @param robotID the robot ID whose type changed
     * @param type new type
     */
    public TypeChangeSignal(int robotID, RobotType type) {
        this.robotID = robotID;
        this.type = type;
    }

    public int getRobotID() {
        return robotID;
    }

    public RobotType getType() {
        return type;
    }

    /**
     * For use by serializers.
     */
    @SuppressWarnings("unused")
    private TypeChangeSignal() {
        this(0, null);
    }
}
