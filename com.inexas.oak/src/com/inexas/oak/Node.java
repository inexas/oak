package com.inexas.oak;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.NotImplementedException;
import com.inexas.oak.advisory.Locus;

public abstract class Node implements Locus {
	protected Node parent;
	protected ParserRuleContext context;

	/** Line number of token, zero-based */

	protected Node(ParserRuleContext context) {
		this.context = context;
	}

	@Override
	public String toString() {
		final ToStringVisitor visitor = new ToStringVisitor(true);
		accept(visitor);
		return visitor.toString();
	}

	abstract void accept(OakVisitor visitor);

	public Node getParent() {
		return parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLine() {
		return context == null ? -1 : context.start.getLine() - 1;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumn() {
		return context.start.getCharPositionInLine();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSourcePosition(int line, int column) throws NotImplementedException {
		throw new NotImplementedException();
	}

	/**
	 * @return Data type, null for objects
	 */
	public abstract DataType getType();

}
