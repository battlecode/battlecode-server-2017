package battlecode.server.proxy;

import battlecode.server.GameInfo;

import java.io.IOException;

/**
 * "All problems in computer science can be solved with a layer of indirection."
 *
 * Used to abstract the creation of proxies at the beginning of matches.
 *
 * @author james
 */
@FunctionalInterface
public interface ProxyFactory {
    /**
     * Create a proxy for a game
     *
     * @param info information about the game the
     * @return a new proxy, or null if the factory decides not to create a
     *         proxy.
     * @throws IOException if creating the proxy fails
     */
    Proxy createProxy(GameInfo info) throws IOException;
}
