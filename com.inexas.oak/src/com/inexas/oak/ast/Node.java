package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.NotImplementedException;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.Locus;
import com.inexas.util.TextBuilder;

public abstract class Node implements Locus {
	protected Node parent;
	protected ParserRuleContext context;

	protected Node(ParserRuleContext context) {
		this.context = context;
	}

	@Override
	public String toString() {
		final ToStringVisitor visitor = new ToStringVisitor(true);
		accept(visitor);
		return visitor.toString();
	}

	public abstract void accept(OakAstVisitor visitor);

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

	public String getPath() {
		final TextBuilder result = new TextBuilder();
		getPath(result);
		return result.toString();
	}

	private void getPath(TextBuilder result) {
		if(parent == null) {
			result.append('/');
		} else {
			if(result.length() > 1) {
				// Not directly beneath root
				result.append('/');
			}
		}
	}
}
