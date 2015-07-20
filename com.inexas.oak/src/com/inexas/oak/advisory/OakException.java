package com.inexas.oak.advisory;

public class OakException extends Exception {
	private static final long serialVersionUID = -799956828761675924L;
	public final Advisory advisory;

	public OakException(Advisory advisory) {
		super("Oak processing error, call OakException.getAdvisory()");
		this.advisory = advisory;
	}

	/**
	 * Construct an exception with an error message (as opposed to a warning).
	 * Once the locus of the error message is known the message will be added to
	 * the Advisory and the exception re-thrown.
	 *
	 * @param errorMessage
	 *            The error message.
	 */
	public OakException(String errorMessage) {
		super(errorMessage);
		advisory = null;
	}

	/**
	 * Return the error/warning message Advisory.
	 *
	 * @return The Advisory.
	 */
	public Advisory getAdvisory() {
		return advisory;
	}
}
