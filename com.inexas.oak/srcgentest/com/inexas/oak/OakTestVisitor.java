// Created by Oak: 07AUG15 12:40:25
package com.inexas.oak;

public interface OakTestVisitor {

	public static class Base implements OakTestVisitor {
		@Override public void visit(com.inexas.oak.Test1 test1) { /**/ }
	}

	void visit(com.inexas.oak.Test1 test1);
}
