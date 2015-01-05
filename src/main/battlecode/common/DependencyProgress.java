package battlecode.common;

/**
 * An enumeration that represents the status of dependencies. For example, a tank factory requires a barracks
 * in order to be built. Calling DependencyProgress(RobotType.TANK_FACTORY) will return:
 * NONE if no barracks exists and none are currently being constructed.
 * INPROGRESS if a barracks is currently being constructed but is not complete.
 * DONE if there is a barracks that has been fully constructed.
 * Note that a tank factory cannot be built unless the dependency progress is DONE.
 */
public enum DependencyProgress {
    NONE, INPROGRESS, DONE
}
