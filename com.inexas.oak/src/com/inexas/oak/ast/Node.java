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

	public abstract void accept(AstVisitor visitor);

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

	@Override
	public String toString() {
		final AstToStringVisitor visitor = new AstToStringVisitor(true);
		accept(visitor);
		return visitor.toString();
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
