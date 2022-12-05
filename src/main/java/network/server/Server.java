package network.server;

import java.io.IOException;

public interface Server {
    /**
     * Creates a new thread of server and starts it.
     *
     * @throws IOException if there is an error connecting to the input stream of the socket.
     */
    void start() throws IOException;

    /**
     * Return the field port.
     *
     * @return returns the value of the port field
     */
    int getPort();

    /**
     * Closes the server socket.
     */
    void stop();
}
