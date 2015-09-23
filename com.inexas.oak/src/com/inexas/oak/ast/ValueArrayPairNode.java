package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.*;

public class ValueArrayPairNode extends PairNode {
	private final Node[] values;

	public ValueArrayPairNode(ParserRuleContext context, Identifier name, Node[] values) {
		super(context, name);

		this.values = values;
		for(final Node node : values) {
			node.parent = this;
		}
	}

	@Override
	public void accept(AstVisitor visitor) {
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
		return values[0].getType();
	}

}
