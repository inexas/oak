/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.path;

import static org.junit.Assert.*;
import org.junit.Test;
import com.inexas.oak.ParsingException;
import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.Context;

public class TestIdentifier {

	@Test
	public void test() {
		assertTrue(Identifier.parse("A"));
		assertTrue(Identifier.parse("P"));
		assertTrue(Identifier.parse("Z"));
		assertTrue(Identifier.parse("a"));
		assertTrue(Identifier.parse("p"));
		assertTrue(Identifier.parse("z"));
		assertTrue(Identifier.parse("_"));

		assertTrue(Identifier.parse("A0a_"));
		assertTrue(Identifier.parse("A5b"));
		assertTrue(Identifier.parse("A9z"));

		assertTrue(Identifier.parse("ab123456789012345678901234567890"));
	}

	@Test(expected = ParsingException.class)
	public void testTooLong() {
		assertTrue(Identifier.parse("ab123456789012345678901234567890x"));
	}

	@Test
	public void testTooLongAdvisory() {
		final String string = "ab123456789012345678901234567890x";
		final Advisory advisory = new Advisory(string);
		Context.attach(advisory);

		assertFalse(Identifier.parse("ab123456789012345678901234567890x"));
		assertFalse(advisory.isEmpty());

		Context.detach(advisory);
	}
}