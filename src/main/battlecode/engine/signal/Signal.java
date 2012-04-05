package battlecode.engine.signal;

import java.io.Serializable;

// Ideally Signal would be a method-less interface; the accept method exists for legacy reasons

public abstract class Signal implements Serializable {

    public void accept(SignalHandler handler) {
        handler.visitSignal(this);
    }

}
