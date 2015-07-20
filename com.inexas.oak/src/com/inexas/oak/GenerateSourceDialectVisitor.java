package com.inexas.oak;

import java.util.*;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.advisory.*;

/**
 * Visit a Dialect tree and output a Dialect and a Visitor interface.
 */
class GenerateSourceDialectVisitor extends DialectVisitor.Base {
	/**
	 * In the first pass we just collect all the Object and Property Rules
	 */
	private class FirstPassVisitor extends DialectVisitor.Base {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Objet object) {

			try {
				final Class<?> templateClass = Class.forName(object.templateClass);
				final Rule rule = new ObjectRule(
						object.key,
						templateClass,
						object.isRoot);
				put(rule);
			} catch(final Exception e) {
				error(object, "Class not found: " + object.templateClass);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Property property) {
			try {
				final String key = property.key;
				final Rule rule = new PropertyRule(key, property.type, property.constraints);
				rules.put(key, rule);
			} catch(final OakException e) {
				advisory.reportError(property, e.getMessage());
			}
		}
	}

	public final Advisory advisory;
	private final Map<String, Rule> rules = new HashMap<>();
	private final List<ObjectRule> objects = new ArrayList<>();
	private final List<PropertyRule> properties = new ArrayList<>();
	private final List<Relation> relations = new ArrayList<>();
	private String dialectKey;
	// todo Figure out what to do with visitors
	@SuppressWarnings("unused")
	private List<String> visitorsList;

	public GenerateSourceDialectVisitor(Advisory advisory) {
		this.advisory = advisory;
	}

	public Dialect getDialect() {
		return new Dialect(dialectKey, rules.values().toArray(new Rule[rules.size()]));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(DialectNode dialect) {
		// First pass visitor collects all the elements
		dialect.accept(new FirstPassVisitor());
		dialectKey = dialect.key;
		visitorsList = dialect.visitorsList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(Member member) {
		final Rule subject = rules.get(member.key);
		if(subject != null) {
			final Relation relation;
			if(subject instanceof PropertyRule) {
				final PropertyRule property = (PropertyRule)subject;
				relation = new Relation(property, member.cardinality);
			} else {
				final ObjectRule object = (ObjectRule)subject;
				final CollectionType collection = member.collectionType == null
						? CollectionType.list : member.collectionType;
				relation = new Relation(object, member.cardinality, collection);
			}
			relations.add(relation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(Objet object) {
		final ObjectRule rule = (ObjectRule)rules.get(object.key);
		final Relation[] array = relations.toArray(new Relation[relations.size()]);
		relations.clear();
		try {
			rule.setRelations(array);
		} catch(final Exception e) {
			error(object, e.getMessage());
		}
	}

	private void put(Rule rule) {
		if(rules.put(rule.name, rule) != null) {
			throw new InexasRuntimeException("Duplicate name: " + rule.name);
		}
		if(rule instanceof ObjectRule) {
			objects.add((ObjectRule)rule);
		} else {
			properties.add((PropertyRule)rule);
		}
	}

	private void error(Locus node, String message) {
		advisory.reportError(node, message);
	}

}
