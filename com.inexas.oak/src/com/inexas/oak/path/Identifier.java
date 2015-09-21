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

import org.checkerframework.checker.nullness.qual.Nullable;
import com.inexas.oak.ParsingException;
import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.Context;
import com.inexas.util.Text;

/**
 * [A-Za-z_][A-Za-z0-9_]{1,32}
 */
public class Identifier implements Comparable<Identifier> {
	private final static int MAX_LENGTH = 32;
	private final static byte FIRST =
			Text.ASCII_A_Z | Text.ASCII_a_z | Text.ASCII_UNDERLINE;
	private final static byte SUBSEQUENT = FIRST | Text.ASCII_0_9;
	private final String string;

	/**
	 * Parse a string as an identifier. The whole of the string must be
	 * consumed. As such this works like a verifier.
	 *
	 * @param string
	 *            The string to parse; cannot be null. The string "null" is
	 *            accepted as an Identifier.
	 * @return The parsed identifier.
	 * @throws ParsingException
	 *             Thrown if string cannot be fully parsed Unless an Advisory is
	 *             present in which case it is updated and false is returned.
	 */
	public static boolean parse(String string) {
		final boolean result;

		final Text t = new Text();
		t.append(string);
		if(t.length() <= MAX_LENGTH && consume(t) && t.isEof()) {
			result = true;
		} else {
			result = false;
			error(1, t.cursor(), "Unrecognized input");
		}

		return result;
	}

	public static boolean consume(Text t) {
		final boolean result;

		final int start = t.cursor();
		if(t.consumeAscii(FIRST) && (t.consumeAscii(SUBSEQUENT) || true)) {
			result = true;
			final int end = t.cursor();
			if(end - start > MAX_LENGTH) {
				error(1, 1, "Identifier too long: " + t.subSequence(start, end));
			}
		} else {
			result = false;
		}

		return result;
	}

	public Identifier(String string) {
		parse(string);
		this.string = string;
	}

	@Override
	public String toString() {
		return string;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return string.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(@Nullable Object rhsObject) {
		boolean result;

		if(rhsObject == null) {
			result = false;
		} else if(this == rhsObject) {
			result = true;
		} else {
			try {
				final Identifier rhs = (Identifier)rhsObject;
				result = string.equals(rhs.string);
			} catch(final ClassCastException e) {
				result = false;
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Identifier rhs) {
		return string.compareTo(rhs.string);
	}

	private static void error(int line, int column, String message) throws ParsingException {
		final Advisory advisory = Context.getButDontThrow(Advisory.class);
		if(advisory != null) {
			advisory.error(line, column, message);
		} else {
			throw new ParsingException(message);
		}
	}

	public int length() {
		return string.length();
	}

}
