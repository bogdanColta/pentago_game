package exceptions;

public class ConnectionFailed extends Exception {
    /**
     * Creates an exception with the message when the exception is thrown.
     *
     * @param message the massage that is used to print in case the exception is caught
     */
    public ConnectionFailed(String message) {
        super(message);
    }
}
