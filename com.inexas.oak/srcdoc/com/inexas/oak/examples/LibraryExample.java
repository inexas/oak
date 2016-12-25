/*
 * Copyright (C) 2016 Inexas. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */
package com.inexas.oak.examples;

import com.inexas.oak.Expression;
import com.inexas.oak.advisory.OakException;

/**
 * @author kwhittingham, @date 25 Dec 2016
 */
public class LibraryExample {

	public static void main(String[] args) throws OakException {
		final Expression expression = new Expression("5 + 4 * 3");
		System.out.println("Result is: " + expression.evaluate().toString());
		System.out.println("Data type: " + expression.getType());
	}
}
