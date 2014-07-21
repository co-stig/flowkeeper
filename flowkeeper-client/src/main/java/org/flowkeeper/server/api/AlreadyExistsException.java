package org.flowkeeper.server.api;

/**
 * Thrown by the {@link Server} when we try to create a Plan, which already
 * exists.
 */
public class AlreadyExistsException extends Exception {

	private static final long serialVersionUID = -6827872353680546660L;

	public AlreadyExistsException() {
	}

	public AlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public AlreadyExistsException(String message) {
		super(message);
	}

	public AlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
