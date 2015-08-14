package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.DataType;

public class PathNode extends Node {
	public final String path;

	public PathNode(ParserRuleContext context, String path) {
		super(context);

		this.path = path;
	}

	@Override
	public void accept(AstVisitor visitor) {
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
