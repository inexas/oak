/*
 * Copyright (C) 2016 Inexas. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak;

/**
 * @author kwhittingham, @date 10 Dec 2016
 */
public interface Library {
	public final static Object UNRESOLVED = new Object();

	/**
	 * Try to resolve a given symbol.
	 *
	 * @param symbol
	 *            The symbol to resolve
	 * @return Either an object that the symbol resolves to or UNRESOLVED
	 */
	Object resolve(String symbol);
}
