/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak;

public class ParsingException extends RuntimeException {
	private static final long serialVersionUID = -4598403451483280441L;

	public ParsingException(String message) {
		super(message);
	}

	public ParsingException(String message, Exception cause) {
		super(message, cause);
	}

}
