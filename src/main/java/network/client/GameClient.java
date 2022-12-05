package network.client;

import exceptions.ConnectionFailed;
import exceptions.InvalidMessage;
import exceptions.InvalidUsername;

public interface GameClient {
    /**
     * Connects to the socket of the server.
     *
     * @param address the address of the server socket
     * @param port    the port of the server socket
     * @throws ConnectionFailed if the connection failed
     */
    void connect(String address, int port) throws ConnectionFailed;

    /**
     * Closes the socket and removes the TUI from the listeners.
     */
    void close();

    /**
     * Sends the username parameter to the server. Throws the InvalidUsername exception
     * in case the username is null, or it contains the character '~'.
     *
     * @param username the username sent to the server
     * @throws InvalidUsername when the username parameter is null, or has '~' in it
     */
    void sendUsername(String username) throws InvalidUsername;

    /**
     * Handles the message received.
     *
     * @param message message containing the command
     * @throws InvalidMessage if the message is null
     */
    void sendMessage(String message) throws InvalidMessage;

    /**
     * Add listener parameter to the gameListeners list.
     *
     * @param listener the listener added to the list
     */
    void addListener(Listener listener);

    /**
     * Remove listener parameter to the gameListeners list.
     *
     * @param listener the listener removed to the list
     */
    void removeListener(Listener listener);
}
