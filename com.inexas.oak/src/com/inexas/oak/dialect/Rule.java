package com.inexas.oak.dialect;


public abstract class Rule implements Comparable<Rule> {
	public final String key;

	protected Rule(String key) {
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