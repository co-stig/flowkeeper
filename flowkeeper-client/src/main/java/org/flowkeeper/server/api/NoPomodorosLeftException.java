package org.flowkeeper.server.api;

/**
 *
 * 
 */
public class NoPomodorosLeftException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2930276386378008465L;

	public NoPomodorosLeftException() {
    }

    public NoPomodorosLeftException(Throwable cause) {
        super(cause);
    }

    public NoPomodorosLeftException(String message) {
        super(message);
    }

    public NoPomodorosLeftException(String message, Throwable cause) {
        super(message, cause);
    }
}
