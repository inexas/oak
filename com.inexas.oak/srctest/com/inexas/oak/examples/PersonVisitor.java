// Created by Oak: 16JUL15 07:32:54
package com.inexas.oak.examples;

public interface PersonVisitor {

	public static class Base implements PersonVisitor {
		@Override public void visit(com.inexas.oak.examples.Email email) { /**/ }
		@Override public void enter(com.inexas.oak.examples.Person person) { /**/ }
		@Override public void exit(com.inexas.oak.examples.Person person) { /**/ }
	}

	void visit(com.inexas.oak.examples.Email email);
	void enter(com.inexas.oak.examples.Person person);
	void exit(com.inexas.oak.examples.Person person);
}
