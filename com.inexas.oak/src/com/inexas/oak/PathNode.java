package com.inexas.oak;

import org.antlr.v4.runtime.ParserRuleContext;

public class PathNode extends Node {
	public final String path;

	public PathNode(ParserRuleContext context, String path) {
		super(context);

		this.path = path;
	}

	@Override
	void accept(OakVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.visit(this);
		assert visitor.exitEveryNode(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType getType() {
		return DataType.path;
	}

}
