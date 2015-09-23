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

import java.io.Serializable;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.Context;
import com.inexas.util.Text;

/**
 * This is a lightweight and specialized version of String.
 *
 * Identifiers are immutable.
 *
 * [A-Za-z_][A-Za-z0-9_]{1,32}
 */
public class Identifier implements Comparable<Identifier>, Serializable {
	private static final long serialVersionUID = -7908114113413045720L;
	public final static int MAX_LENGTH = 32;
	private final static byte FIRST = Text.ASCII_A_Z | Text.ASCII_a_z | Text.ASCII_UNDERLINE;
	private final static byte SUBSEQUENT = FIRST | Text.ASCII_0_9;

	private final char ca[];
	private final int length;

	/**
	 * Parse a string as an identifier. The whole of the string must be
	 * consumed. As such this works like a verifier.
	 *
	 * @param string
	 *            The string to parse; cannot be null. The string "null" is
	 *            accepted as an Identifier.
	 * @return Return true if the string passed is a valid Identifier
	 * @throws ParsingException
	 *             Thrown if string cannot be fully parsed Unless an Advisory is
	 *             present in which case it is updated and false is returned.
	 */
	public static boolean isValid(String string) {
		final boolean result;

		final Text t = new Text(string);
		if(t.length() <= MAX_LENGTH && consume(t) && t.isEof()) {
			result = true;
		} else {
			result = false;
			error(1, t.cursor(), "Unrecognized input");
		}

		return result;
	}

	/**
	 * Consume an Identifier.
	 *
	 * @param t
	 *            The Text from which to consume; cannot be null.
	 * @return True if an Identifier has been consumed. The cursor will have
	 *         been advanced but is not necessarily at EOF.
	 * @throws ParsingException
	 *             Thrown if the Identifier is too long unless an Advisory is
	 *             present in which case it is updated and true is returned to
	 *             allow parsing to continue.
	 */
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
		if(string == null || string.length() == 0) {
			throw new ParsingException("new Identifier(null) called");
		}
		final Text t = new Text(string);
		if(consume(t)) {
			ca = t.toCharArray();
			length = ca.length;
		} else {
			throw new ParsingException("Invalid Identifier: " + string);
		}
	}

	public Identifier(Identifier toCopy) {
		try {
			this.ca = toCopy.ca;
			this.length = ca.length;
		} catch(final NullPointerException e) {
			throw new ParsingException("new Identifier(null) called");
		}
	}

	/**
	 * @param t
	 *            Text to construct from.
	 */
	public Identifier(Text t) {
		if(consume(t)) {
			ca = t.toCharArray();
			length = ca.length;
		} else {
			throw new ParsingException("Invalid Identifier: " + t.getString(t.cursor()));
		}
	}

	public int length() {
		return length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Identifier rhs) {
		int result = 0;

		final int rhsLength = rhs.length;
		final char[] rhsCa = rhs.ca;
		final int count = length < rhsLength ? length : rhsLength;

		for(int i = 0; i < count; i++) {
			result = ca[i] - rhsCa[i];
			if(result != 0) {
				break;
			}
		}

		if(result == 0) {
			result = length - rhsLength;
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = 0;

		for(final char c : ca) {
			result = result ^ c;
		}

		return result;
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
				if(length != rhs.length) {
					result = false;
				} else {
					final char[] rhsCa = rhs.ca;

					result = true;
					for(int i = 0; i < length; i++) {
						if(ca[i] != rhsCa[i]) {
							result = false;
							break;
						}
					}
				}
			} catch(final ClassCastException e) {
				result = false;
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return new String(ca);
	}

	private static void error(int line, int column, String message) throws ParsingException {
		final Advisory advisory = Context.getButDontThrow(Advisory.class);
		if(advisory != null) {
			advisory.error(line, column, message);
		} else {
			throw new ParsingException(message);
		}
	}

	/**
	 * @param index
	 *            The index of the desired character, zero-based.
	 * @return Return the character at the index. Or throw an exception.
	 */
	public char charAt(int index) {
		return ca[index];
	}

}
