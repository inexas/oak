package com.inexas.oak.dialect;


public abstract class Rule {
	final String name;
	final Constraint[] constraints;

	protected Rule(String name, Constraint[] constraints) {
		assert name != null;

		this.name = name;
		this.constraints = constraints == null || constraints.length == 0 ? null : constraints;
	}
}