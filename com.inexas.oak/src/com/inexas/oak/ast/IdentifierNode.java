package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.DataType;

public class IdentifierNode extends Node {
	public final String identifier;

	public IdentifierNode(ParserRuleContext context, String identifier) {
		super(context);

		this.identifier = identifier;
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
