/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.dialect;

/**
 * Thrown if template object cannot be constructed for some reason.
 *
 */
public class OakConstructorException extends Exception {
	private static final long serialVersionUID = 3771975223910490978L;

	public OakConstructorException(String message) {
		super(message);
	}

	public OakConstructorException(String message, Exception exception) {
		super(message, exception);
	}
}
