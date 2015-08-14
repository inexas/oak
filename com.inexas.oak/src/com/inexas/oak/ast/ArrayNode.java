package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.DataType;

public class ArrayNode extends Node {
	private final Node[] values;

	public ArrayNode(ParserRuleContext context, Node[] values) {
		super(context);

		this.values = values;
		for(final Node node : values) {
			node.parent = this;
		}
	}

	@Override
	public void accept(AstVisitor visitor) {
		throw new UnexpectedException("accept: ");
		// visitor.enter(this);
		// boolean delimit = false;
		// for(final Node value : values) {
		// if(delimit) {
		// visitor.delimit();
		// } else {
		// delimit = true;
		// }
		// value.accept(visitor);
		// }
		// visitor.exit(this);
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
