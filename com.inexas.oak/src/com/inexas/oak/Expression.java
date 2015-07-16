package com.inexas.oak;

import java.io.File;
import com.inexas.oak.advisory.Advisory;

public class Expression {
	private final Oak oak;
	private final ExpressionNode rootNode;

	public Expression(String string) {
		oak = new Oak(string);
		rootNode = oak.toExpression();
	}

	public Expression(File file) {
		oak = new Oak(file);
		rootNode = oak.toExpression();
	}

	public Expression(ExpressionNode node) {
		oak = null;
		this.rootNode = node;
	}

	/**
	 * @return
	 */
	public Advisory getAdvisory() {
		return oak.getAdvisory();
	}

	public ConstantNode evaluate() {
		return rootNode.evaluate();
	}

	public boolean isStatic() {
		return rootNode.isStatic();
	}

	@Override
	public String toString() {
		return rootNode == null ? "Unevaluated expression" : rootNode.toString();
	}

}
