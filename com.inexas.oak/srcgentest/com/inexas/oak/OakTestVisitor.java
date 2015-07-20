// Created by Oak: 20JUL15 11:05:44
package com.inexas.oak;

public interface OakTestVisitor {

	public static class Base implements OakTestVisitor {
		@Override public void visit(com.inexas.oak.Test1 test1) { /**/ }
	}

	void visit(com.inexas.oak.Test1 test1);
}
