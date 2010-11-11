package battlecode.common;

/**
 * An interface for controlling a component of a robot.
 */
public interface ComponentController
{
	/**
	 * Returns true if this component is currently being used.
	 */
	public boolean isActive();

	/**
	 * Returns the number of rounds until this component becomes idle.
	 */
	public int roundsUntilIdle();

	/**
	 * Returns the type of this component.
	 */
	public ComponentType type();

	/**
	 * Equivalent to <code>type().componentClass</code>.
	 */
	public ComponentClass componentClass();

	/**
	 * Returns true if loc is within this component's range.
	 */
	public boolean withinRange(MapLocation loc);

}
