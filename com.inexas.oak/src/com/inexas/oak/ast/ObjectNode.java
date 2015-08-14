package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.DataType;

public class ObjectNode extends Node {
	private final PairNode[] members;

	public ObjectNode(ParserRuleContext context, PairNode[] members) {
		super(context);

		this.members = members;
		for(final PairNode member : members) {
			member.parent = this;
		}
	}

	@Override
	public void accept(AstVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.enter(this);
		for(final PairNode member : members) {
			member.accept(visitor);
		}
		visitor.exit(this);
		assert visitor.exitEveryNode(this);
	}

	public PairNode[] getMembers() {
		return members;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType getType() {
		return null;
	}

}
