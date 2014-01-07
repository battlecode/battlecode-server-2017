package battlecode.common;

/**
 * Not used in 2014 because all robots are at the same level.
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
