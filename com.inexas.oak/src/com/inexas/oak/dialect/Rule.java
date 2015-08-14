package com.inexas.oak.dialect;


public abstract class Rule {
	public final String key;

	protected Rule(String key) {
		assert key != null;

		this.key = key;
	}
}