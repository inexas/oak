package com.inexas.oak;

import org.antlr.v4.runtime.ParserRuleContext;

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
	void accept(OakVisitor visitor) {
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
