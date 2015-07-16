package com.inexas.oak.advisory;

public class OakException extends RuntimeException {
	private static final long serialVersionUID = -799956828761675924L;
	public final Advisory advisory;

	/**
	 * Return the error/warning message Advisory.
	 * 
	 * @return The Advisory.
	 */
	public Advisory getAdvisory() {
		return advisory;
	}

	public OakException(Advisory advisory) {
		super("Oak processing error, call OakException.getAdvisory()");
		this.advisory = advisory;
	}
}
