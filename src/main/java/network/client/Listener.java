package network.client;

public interface Listener {
    /**
     * Display in the TUI the message received.
     *
     * @param message the message received
     */
    void messageReceived(String message);
}
