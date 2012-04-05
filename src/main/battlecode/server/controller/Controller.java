package battlecode.server.controller;

import java.io.IOException;
import java.util.Observable;

/**
 * This class represents a source for server and match control data, including
 * match parameters (team and map name), notifications (start, stop, pause),
 * and actions taken by the user while in debugging mode. It employs the
 * observer/observable pattern to interface with the match server.
 */
public abstract class Controller extends Observable {

    /**
     * Used to allow a controller to initialize any resources it may need just
     * before it is used.
     *
     * @throws IOException if the controller source cannot start
     */
    public abstract void start() throws IOException;

    /**
     * Used for a controller to clean up any resources it may have used.
     *
     * @throws IOException if the controller source cannot stop
     */
    public abstract void finish() throws IOException;

}
