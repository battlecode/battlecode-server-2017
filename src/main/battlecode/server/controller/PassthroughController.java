package battlecode.server.controller;

/**
 * A simple controller that passes any arguments given to {@link #update(Object o)} to its observers.
 *
 * Created by james on 7/23/15.
 */
// TODO stop using Java's Observable? It doesn't quite seem to do what we want.
public final class PassthroughController extends Controller {
    public void update(final Object o) {
        setChanged();
        notifyObservers(o); // calls clearChanged(), we don't need to.
    }
}
