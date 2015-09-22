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
import com.inexas.util.Text;

public class TestIdentifier {
	private static final Identifier a = new Identifier("a");
	private static final Identifier aa = new Identifier("aa");
	private static final Identifier aPrime = new Identifier("a");
	private static final Identifier b = new Identifier("b");
	private static final Text longest;
	private static final Text tooLong;
	static {
		longest = new Text();
		tooLong = new Text();
		for(int i = 0; i < Identifier.MAX_LENGTH; i++) {
			longest.append('A');
			tooLong.append('A');
		}
		tooLong.append('X');
	}

	private void doConsumeTest(
			boolean expectedResult,
			int exepctedConsumeLength,
			boolean expectedEof,
			String toTest) {
		final Text t = new Text(toTest);
		final boolean actualResult = Identifier.consume(t);
		if(expectedResult) {
			assertTrue(actualResult);
			assertEquals(exepctedConsumeLength, t.cursor());
			assertTrue(expectedEof == t.isEof());
		} else {
			assertFalse(actualResult);
			assertEquals(exepctedConsumeLength, t.cursor());
			assertTrue(expectedEof == t.isEof());
		}
	}

	@Test
	public void testConsume() {
		doConsumeTest(true, 1, true, "a");
		doConsumeTest(true, 1, true, "z");
		doConsumeTest(true, 1, true, "A");
		doConsumeTest(true, 1, true, "Z");
		doConsumeTest(true, 1, true, "_");
		doConsumeTest(false, 0, false, "0");
		doConsumeTest(false, 0, false, "9");
		doConsumeTest(false, 0, false, "@");

		doConsumeTest(true, 2, true, "aa");
		doConsumeTest(true, 2, true, "_1");

		doConsumeTest(true, Identifier.MAX_LENGTH, true, longest.toString());

		doConsumeTest(true, 1, false, "a@");
	}

	@Test
	public void testBasics() {
		assertEquals(a, a);
		assertEquals(a, aPrime);
		assertNotEquals(a, b);
	}

	@Test(expected = ParsingException.class)
	public void testTooLong() {
		@SuppressWarnings("unused")
		final Identifier i = new Identifier(tooLong);
	}

	@Test
	public void testCompare() {
		assertTrue(a.compareTo(a) == 0);
		assertTrue(a.compareTo(aPrime) == 0);
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);
		assertTrue(a.compareTo(aa) < 0);
	}

	@Test(expected = ParsingException.class)
	public void testNullString() {
		@SuppressWarnings("unused")
		final Identifier i = new Identifier((String)null);
	}

	@Test(expected = ParsingException.class)
	public void testMtString() {
		@SuppressWarnings("unused")
		final Identifier i = new Identifier("");
	}

	@Test(expected = ParsingException.class)
	public void testNullIdentifier() {
		@SuppressWarnings("unused")
		final Identifier i = new Identifier((Identifier)null);
	}

}
