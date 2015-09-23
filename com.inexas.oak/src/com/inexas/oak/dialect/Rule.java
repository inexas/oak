package com.inexas.oak.dialect;

import com.inexas.oak.Identifier;

public abstract class Rule implements Comparable<Rule> {
	public final Identifier key;

	protected Rule(Identifier key) {
		assert key != null;

		this.key = key;
	}

	/**
	 * <p>
	 * The key is used to compare.
	 * </p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Rule rhs) {
		return key.compareTo(rhs.key);
	}
}