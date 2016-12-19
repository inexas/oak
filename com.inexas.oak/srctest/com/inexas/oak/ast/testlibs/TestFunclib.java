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

import java.math.*;
import java.time.*;
import com.inexas.oak.Library;
import com.inexas.oak.ast.Function;
import com.inexas.util.Cardinality;

public class TestFunclib implements Library {

	public int notFunction() {
		return 0;
	}

	@Function
	public int sf1() {
		return 0;
	}

	@Function(dynamic = false)
	public int sf2() {
		return 0;
	}

	@Function(dynamic = true)
	public int df() {
		return 0;
	}

	@SuppressWarnings("unused")
	@Function
	public static String allTypes(
			int i1,
			Integer i2,
			long l1,
			Long l2,
			BigInteger i,
			float f1,
			Float f2,
			double d1,
			Double d2,
			BigDecimal d,
			String s,
			boolean b1,
			Boolean b2,
			LocalDateTime datetime,
			LocalDate date,
			LocalTime time,
			Cardinality c) {
		return null;
	}

	@Function
	public String intInt(int i1, int i2) {
		return Integer.toString(i1 * i2);
	}

	@Override
	public Object resolve(String identifier) {
		return UNRESOLVED;
	}
}
