package com.inexas.oak.template;

import com.inexas.oak.DataType;
import com.inexas.util.TextBuilder;

/**
 * Visits a Dialect AST and produces a String version of it that will transform
 * back into the original source.
 *
 * todo Print out arrays of objects as arrays
 *
 * todo Insert a line break between type changes
 */
public class ToStringVisitor implements DialectVisitor {
	private final TextBuilder tb;

	public ToStringVisitor(boolean pretty) {
		tb = new TextBuilder(pretty);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return tb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(Dialect dialect) {
		startObject("Dialect", dialect.key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(Dialect dialect) {
		endObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(Objet object) {
		startObject("Object", object.key);

		tb.indent();
		tb.append("class:");
		tb.space();
		tb.append('"');
		tb.append(object.templateClass.getName());
		tb.append('"');
		tb.append(';');
		tb.newline();

		if(object.isRoot) {
			tb.indent();
			tb.append("root;");
			tb.newline();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(Objet objet) {
		endObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(Member member) {
		startObject("Member", member.key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(Member member) {
		endObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(Property property) {
		startObject("Property", property.key);

		if(property.type != DataType.text) {
			tb.indent();
			tb.append("type:");
			tb.space();
			tb.append(property.type.name());
			tb.append(';');
			tb.newline();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(Property property) {
		endObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(Constraint constraint) {
		constraint.toMarkup(tb);
	}

	private void startObject(String name, String key) {
		tb.indent();
		tb.append(name);
		tb.space();
		tb.append('{');
		tb.newline();
		tb.indentMore();

		if(key != null) {
			tb.indent();
			tb.append("key:");
			tb.space();
			tb.append(key);
			tb.append(';');
			tb.newline();
		}
	}

	private void endObject() {
		tb.indentLess();
		tb.indent();
		tb.append('}');
		tb.newline();
	}
}
