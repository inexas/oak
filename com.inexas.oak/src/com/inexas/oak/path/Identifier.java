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
import com.inexas.util.TextBuilder;

/**
 * [A-Za-z_][A-Za-z0-9_]{1,32}
 */
public class Identifier implements Comparable<Identifier> {
	private final static int MAX_LENGTH = 32;
	private final static byte FIRST =
			TextBuilder.ASCII_A_Z | TextBuilder.ASCII_a_z | TextBuilder.ASCII_UNDERLINE;
	private final static byte SUBSEQUENT = FIRST | TextBuilder.ASCII_0_9;
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

		final TextBuilder tb = new TextBuilder();
		tb.append(string);
		if(consume(tb) && tb.isEof()) {
			result = true;
		} else {
			result = false;
			error(1, tb.cursor(), "Unrecognized input");
		}

		return result;
	}

	@Nullable
	public static boolean consume(TextBuilder tb) {
		final boolean result;

		final int start = tb.cursor();
		if(tb.consumeAscii(FIRST) && (tb.consumeAscii(SUBSEQUENT) || true)) {
			result = true;
			final int end = tb.cursor();
			if(end - start > MAX_LENGTH) {
				error(1, 1, "Identifier too long: " + tb.subSequence(start, end));
			}
		} else {
			result = false;
		}

		return result;
	}

	public Identifier(String string) {
		assert parse(string);
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
			result = true;
		} else if(this == rhsObject) {
			result = false;
		} else {
			try {
				final Identifier rhs = (Identifier)rhsObject;
				result = rhs.string.equals(rhs.string);
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

}
