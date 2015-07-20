package com.inexas.oak;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.*;
import com.inexas.oak.advisory.Locus;
import com.inexas.oak.path.*;
import com.inexas.util.TextBuilder;

public abstract class Node implements Locus, Navigable {
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

	abstract void accept(OakVisitor visitor);

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		final TextBuilder result = new TextBuilder();
		getPath(result);
		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Navigable> T getParent() {
		return (T)parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Navigable> T getRoot() {
		return parent == null ? (T)this : parent.getParent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Navigable> T getChild(String name) throws UnsupportedException {
		throw new UnsupportedException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Navigable> T getChild(int index) throws UnsupportedException {
		throw new UnsupportedException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Navigable> T locate(String path) {
		final Path locator = new Path(path, this);
		@SuppressWarnings("unchecked")
		final T result = (T)locator.locate();
		return result;
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
