package battlecode.common;

/**
 * This enumeration distinguishes objects that are on the ground, or in
 * the encampment level at a given location.
 * <p/>
 * Since RobotLevel is a Java 1.5 enum, you can use it in <code>switch</code>
 * statements, it has all the standard enum methods (<code>valueOf</code>,
 * <code>values</code>, etc.), and you can safely use <code>==</code> for
 * equality tests.
 */
public enum RobotLevel {

    /**
     * The vertical location of ground robots.
     */
		ON_GROUND(1);
    /**
     * An integer representation of the height.
     */
    private int height;

    /**
     * Creates a new RobotLevel, based on the given height.
     *
     * @param height the integer representation of the MapHeight
     */
    private RobotLevel(int height) {
        this.height = height;
    }

    /**
     * Returns an integral height-wise ordering of the height type.
     */
    public int getHeight() {
        return height;
    }
}
