package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.NotImplementedException;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.Locus;
import com.inexas.util.Text;

public abstract class Node implements Locus {
	protected Node parent;

	protected ParserRuleContext context;

	protected Node(ParserRuleContext context) {
		this.context = context;
	}

	/**
	 * @return The parent Node.
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * @return Data type, null for objects
	 */
	public abstract DataType getType();

	public String getPath() {
		final Text result = new Text();
		getPath(result);
		return result.toString();
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

	public abstract void accept(AstVisitor visitor);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSourcePosition(int line, int column) throws NotImplementedException {
		throw new NotImplementedException();
	}

	@Override
	public String toString() {
		final AstToStringVisitor visitor = new AstToStringVisitor(true);
		accept(visitor);
		return visitor.toString();
	}

	private void getPath(Text result) {
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
