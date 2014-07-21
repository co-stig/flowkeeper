package org.flowkeeper.server.api;

/**
 *
 * 
 */
public class NotFoundException extends Exception {

	private static final long serialVersionUID = -3853824204594872062L;

	public NotFoundException() {
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
