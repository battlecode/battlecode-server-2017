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
    Proxy createProxy(GameInfo info) throws IOException;
}
