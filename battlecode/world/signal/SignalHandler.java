package battlecode.world.signal;

public interface SignalHandler<T> {

	public T visitSignal(Signal s);

}
