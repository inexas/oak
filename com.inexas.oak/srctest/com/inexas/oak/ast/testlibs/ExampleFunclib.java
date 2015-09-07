/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.ast.testlibs;

import com.inexas.oak.ast.Function;

public class ExampleFunclib {

	public static int nonFunction() {
		return 0;
	}

	@Function
	public static int staticFunction() {
		return 0;
	}

	@Function(dynamic = true)
	public static String dynamicFunction(int x, long y) {
		return Long.toString(x * y);
	}
}
