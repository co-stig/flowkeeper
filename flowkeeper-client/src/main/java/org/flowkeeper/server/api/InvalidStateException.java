package org.flowkeeper.server.api;

/**
 * Thrown by the {@link Server} when we try to modify a Pomodoro, which is not
 * active (e.g. to complete a void Pomodoro).
 */
public class InvalidStateException extends Exception {

	private static final long serialVersionUID = 409836467505986831L;

	public InvalidStateException() {
	}

	public InvalidStateException(Throwable cause) {
		super(cause);
	}

	public InvalidStateException(String message) {
		super(message);
	}

	public InvalidStateException(String message, Throwable cause) {
		super(message, cause);
	}
}
