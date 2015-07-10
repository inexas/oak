package com.inexas.oak;

import org.antlr.v4.runtime.ParserRuleContext;

public class ObjectPairNode extends PairNode {
	private final PairNode[] members;

	public ObjectPairNode(ParserRuleContext context, String name, ObjectNode objectNode) {
		super(context, name);
		this.members = objectNode.getMembers();
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
