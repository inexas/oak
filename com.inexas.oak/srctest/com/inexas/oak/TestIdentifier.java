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

import static org.junit.Assert.*;
import org.junit.Test;
import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.Context;

public class TestIdentifier {

	@Test
	public void test() {
		assertTrue(Identifier.isValid("A"));
		assertTrue(Identifier.isValid("P"));
		assertTrue(Identifier.isValid("Z"));
		assertTrue(Identifier.isValid("a"));
		assertTrue(Identifier.isValid("p"));
		assertTrue(Identifier.isValid("z"));
		assertTrue(Identifier.isValid("_"));

		assertTrue(Identifier.isValid("A0a_"));
		assertTrue(Identifier.isValid("A5b"));
		assertTrue(Identifier.isValid("A9z"));

		assertTrue(Identifier.isValid("ab123456789012345678901234567890"));
	}

	@Test(expected = ParsingException.class)
	public void testTooLong() {
		assertTrue(Identifier.isValid("ab123456789012345678901234567890x"));
	}

	@Test
	public void testTooLongAdvisory() {
		final String string = "ab123456789012345678901234567890x";
		final Advisory advisory = new Advisory(string);
		Context.attach(advisory);

		assertFalse(Identifier.isValid("ab123456789012345678901234567890x"));
		assertFalse(advisory.isEmpty());

		Context.detach(advisory);
	}
}