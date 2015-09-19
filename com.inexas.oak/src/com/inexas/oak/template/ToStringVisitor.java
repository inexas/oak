package com.inexas.oak.template;

import com.inexas.oak.DataType;
import com.inexas.util.Text;

/**
 * Visits a Dialect AST and produces a String version of it that will transform
 * back into the original source.
 *
 * todo Print out arrays of objects as arrays
 *
 * todo Insert a line break between type changes
 */
public class ToStringVisitor implements DialectVisitor {
	private final Text t;

	public ToStringVisitor(boolean pretty) {
		t = new Text(pretty);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return t.toString();
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

		t.indent();
		t.append("class:");
		t.space();
		t.append('"');
		t.append(object.templateClass.getName());
		t.append('"');
		t.append(';');
		t.newline();

		if(object.isRoot) {
			t.indent();
			t.append("root;");
			t.newline();
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
			t.indent();
			t.append("type:");
			t.space();
			t.append(property.type.name());
			t.append(';');
			t.newline();
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
		constraint.toMarkup(t);
	}

	private void startObject(String name, String key) {
		t.indent();
		t.append(name);
		t.space();
		t.append('{');
		t.newline();
		t.indentMore();

		if(key != null) {
			t.indent();
			t.append("key:");
			t.space();
			t.append(key);
			t.append(';');
			t.newline();
		}
	}

	private void endObject() {
		t.indentLess();
		t.indent();
		t.append('}');
		t.newline();
	}
}
