package com.inexas.oak;

import com.inexas.util.*;

/**
 * Visits a Dialect AST and produces a String version of it that will transform
 * back into the original source.
 *
 * todo Print out arrays of objects as arrays todo Insert a line break between
 * type changes
 */
public class ToStringDialectVisitor implements DialectVisitor {
	private final TextBuilder tb;

	public ToStringDialectVisitor(boolean pretty) {
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
	public void enter(DialectNode dialect) {
		startObject("Dialect", dialect.key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(DialectNode dialect) {
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
		tb.append(object.templateClass);
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
	public void visit(Member member) {
		startObject("Member", member.key);

		if(Character.isUpperCase(member.key.charAt(0))) {
			// Subject is an Object
			// todo Find more robust way of doing this
			final Cardinality cardinality = member.cardinality;
			if(cardinality != Cardinality.ONE_MANY) {
				tb.indent();
				tb.append("cardinality:");
				tb.space();
				tb.append(member.cardinality.toString());
				tb.append(';');
				tb.newline();
			}

			final CollectionType collection = member.collectionType;
			if(collection != CollectionType.list) {
				tb.indent();
				tb.append("collection:");
				tb.space();
				tb.append(collection.name());
				tb.append(';');
				tb.newline();
			}
		}

		endObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(Property property) {
		startObject("Property", property.key);

		tb.indent();
		tb.append("type:");
		tb.space();
		tb.append(property.type.name());
		tb.append(';');
		tb.newline();
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
