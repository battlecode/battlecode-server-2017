package battlecode.engine.signal;

public interface SignalHandler<T> {

	public T visitSignal(Signal s);

}
