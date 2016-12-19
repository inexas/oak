package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.*;

public class IdentifierNode extends Node {
	public final Identifier identifier;

	public IdentifierNode(ParserRuleContext context, String identifier) {
		super(context);

		this.identifier = new Identifier(identifier);
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
		return DataType.identifier;
	}

}
