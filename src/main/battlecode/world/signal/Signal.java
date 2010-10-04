package battlecode.world.signal;

import java.io.Serializable;

// Ideally Signal would be a method-less interface; the accept method exists for legacy reasons

public abstract class Signal implements Serializable {

	public <R> R accept(SignalHandler<R> handler) {
		return handler.visitSignal(this);
	}

}
