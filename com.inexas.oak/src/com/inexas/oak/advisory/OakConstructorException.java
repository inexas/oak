package com.inexas.oak.advisory;

/**
 * Thrown by Constructors invoked by Oak when constructing object tree.
 */
public class OakConstructorException extends Exception {
	private static final long serialVersionUID = -4974707918231194628L;

	public OakConstructorException(String message, Exception cause) {
		super(message, cause);
	}

	public OakConstructorException(String message) {
		super(message);
	}

}
