package com.inexas.oak.advisory;

public class OakRuntimeException extends Exception {
	private static final long serialVersionUID = -4974707918231194628L;

	public OakRuntimeException(String message, Exception cause) {
		super(message, cause);
	}

	public OakRuntimeException(String message) {
		super(message);
	}

}
