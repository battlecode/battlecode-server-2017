package battlecode.common;

/**
 * An exception caused by a robot's interaction with the game world.  For instance, trying to move
 * a robot into an occupied square will cause a <code>GameActionException</code> to be thrown.
 * <p>
 * Each <code>GameActionException</code> has a type that roughly identifies what
 * caused the exception.
 *
 * @author Teh Devs
 */
public class GameActionException extends Exception {
	
	static final long serialVersionUID = 0x5def11da;
	
	private final GameActionExceptionType type;
	
	/**
	 * Creates a GameActionException with the given type and message.
	 */
	public GameActionException(GameActionExceptionType type, String message) {
		super(message);
		this.type = type;
	}
	
	/**
	 * Gives the type of gameworld interaction that caused this GameActionException, which
	 * was specified when this instance was constructed.
	 *
	 * @return this GameActionException's type
	 */
	public GameActionExceptionType getType() {
		return type;
	}

	public int hashCode() {
		return type.ordinal();
	}
}