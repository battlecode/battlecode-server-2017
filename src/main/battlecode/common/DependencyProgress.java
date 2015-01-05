package battlecode.common;

/**
 * An enumeration that represents the status of dependencies. For example, a tank factory requires a barracks
 * in order to be built. Calling DependencyProgress(RobotType.TANK_FACTORY) will return:
 * <p>
 * <code>NONE</code> if no barracks exists and none are currently being constructed. <br>
 * <code>INPROGRESS</code> if a barracks is currently being constructed but is not complete. <br>
 * <code>DONE</code> if there is a barracks that has been fully constructed. <br>
 * Note that a tank factory cannot be built unless the dependency progress is <code>DONE</code>. <br>
 */
public enum DependencyProgress {
    NONE, INPROGRESS, DONE
}
