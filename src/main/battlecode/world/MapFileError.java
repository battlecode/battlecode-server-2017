package battlecode.world;

public class MapFileError extends RuntimeException {

    static final long serialVersionUID = 1436950589908374072L;

	public MapFileError() {
		super();
	}
	
	public MapFileError(String message) {
		super(message);
	}
}