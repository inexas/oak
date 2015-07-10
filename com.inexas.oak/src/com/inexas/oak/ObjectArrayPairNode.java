package com.inexas.oak;

import org.antlr.v4.runtime.ParserRuleContext;

public class ObjectArrayPairNode extends PairNode {
	private final Node[] values;

	public ObjectArrayPairNode(ParserRuleContext context, String name, Node[] values) {
		super(context, name);

		this.values = values;
		for(final Node node : values) {
			node.parent = this;
		}
	}

	@Override
	void accept(OakVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.enter(this);
		boolean delimit = false;
		for(final Node value : values) {
			if(delimit) {
				visitor.delimit();
			} else {
				delimit = true;
			}
			value.accept(visitor);
		}
		visitor.exit(this);
		assert visitor.exitEveryNode(this);
	}

	public Node[] getValues() {
		return values;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType getType() {
		return null;
	}

}
