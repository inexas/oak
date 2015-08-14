package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.DataType;
import com.inexas.util.Cardinality;

public class CardinalityNode extends Node {
	public final Cardinality cardinality;

	public CardinalityNode(ParserRuleContext context, Cardinality cardinality) {
		super(context);

		this.cardinality = cardinality;
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
		return DataType.cardinality;
	}

}
