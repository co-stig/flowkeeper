package org.flowkeeper.server.api;

/**
 *
 * 
 */
public class LoginException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7713036003470255465L;

	public LoginException() {
    }

    public LoginException(Throwable cause) {
        super(cause);
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }
}
