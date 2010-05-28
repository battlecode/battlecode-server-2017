package battlecode.world;

import battlecode.world.signal.SignalHandler;

/**
 * An interface that includes the part of the game world that isn't expected to change
 * from year to year.
 */
public interface GenericWorld extends GameWorldViewer, SignalHandler<Exception> {

	public void beginningOfExecution(int id);

	public void endOfExecution(int id);

	public void processBeginningOfRound();

	public void processEndOfRound();

	public long [][] getArchonMemory();

	public void clearAllSignals();

	public boolean wasBreakpointHit();

	public BaseObject getObjectByID(int id);

	public int nextID();

}