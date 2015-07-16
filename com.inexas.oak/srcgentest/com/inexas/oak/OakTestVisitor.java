// Created by Oak: 16JUL15 14:54:07
package com.inexas.oak;

public interface OakTestVisitor {

	public static class Base implements OakTestVisitor {
		@Override public void visit(com.inexas.oak.Test1 test1) { /**/ }
	}

	void visit(com.inexas.oak.Test1 test1);
}
