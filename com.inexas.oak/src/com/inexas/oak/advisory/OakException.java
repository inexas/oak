package com.inexas.oak.advisory;

public class OakException extends Exception {
	private static final long serialVersionUID = -799956828761675924L;

	public OakException(String message) {
		super(message);
	}

	public OakException(String message, Exception cause) {
		super(message, cause);
	}

}
