package battlecode.server.controller;

import battlecode.server.Config;
import battlecode.server.proxy.Proxy;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class LocalController extends Controller implements Observer {

    private final Proxy proxy;

    public LocalController(Config options, Proxy proxy) {
        this.proxy = proxy;
    }

    public void start() throws IOException {
        //proxy.addObserver(this);
    }

    public void finish() throws IOException {
    }

    public void update(Observable o, Object arg) {
        // Dispatch updates from LocalProxy up the chain. It's a little
        // weird, but it's better than polling.
        this.setChanged();
        this.notifyObservers(arg);
        this.clearChanged();
    }
}
