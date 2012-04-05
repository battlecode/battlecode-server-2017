package battlecode.server.proxy;

import battlecode.server.Config;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is a factory for Proxy objects. It returns
 * implementation-independent Proxies of different types based on the needs of
 * the server.
 */
public class ProxyFactory {

    public static Proxy createProxy(final OutputStream stream)
            throws IOException {
        if (Boolean.parseBoolean(Config.getGlobalConfig().get("bc.server.output-xml")))
            return new XStreamProxy(stream);
        else
            return new Proxy() {
                protected OutputStream getOutputStream() throws IOException {
                    return stream;
                }
            };
    }

    /**
     * This method creates a Proxy for writing match data to a file.
     *
     * @param fileName the name of the file to use for saving match data
     * @return a new Proxy for writing match data to binary file
     * @throws IOException if the Proxy could not be created
     */
    public static Proxy createProxyFromFile(String fileName) throws IOException {
        if (Boolean.parseBoolean(Config.getGlobalConfig().get("bc.server.output-xml")))
            return createXStreamProxyFromFile(fileName);
        else
            return new FileProxy(fileName);
    }

    /**
     * This method creates a Proxy for writing match data in a text format.
     *
     * @param fileName the name of the file to use for saving match data
     * @return a new Proxy for writing match data to binary file
     * @throws IOException if the Proxy could not be created
     */
    public static Proxy createXStreamProxyFromFile(String fileName)
            throws IOException {
        return new FileProxy(fileName) {

            public OutputStream getOutputStream() throws IOException {
                return XStreamProxy.getXStream().createObjectOutputStream(stream);
            }

            public void writeObject(Object o) throws IOException {
                // XStream object output streams do not support reset
                output.writeObject(o);
            }

        };
    }
}
