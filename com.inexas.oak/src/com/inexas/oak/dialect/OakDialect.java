package com.inexas.oak.dialect;

import static com.inexas.oak.dialect.CollectionType.*;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.DataType;
import com.inexas.util.Cardinality;

/**
 * This file contains the hard-coded rules for transforming an Oak Dialect file
 * into an OP tree structure.
 */
public class OakDialect {
	public static Dialect dialect;

	private static Relation relate(PropertyRule rule, Cardinality cardinality) {
		return new Relation(rule, cardinality);
	}

	private static Relation relate(ObjectRule key, Cardinality cardinality) {
		return new Relation(key, cardinality, singleton);
	}

	private static Relation relate(ObjectRule key, Cardinality cardinality, CollectionType collection) {
		return new Relation(key, cardinality, collection);
	}

	private static void setRules(Rule... rules) {
		OakDialect.dialect = new Dialect("Dialect", rules);
	}

	public static String[] visitorList = {};

	static {
		try {
			/**
			 * KW: After trying visitors I don't like the idea too much because
			 * there are lots of little problems. For example ActionDefs don't
			 * get visited but the modifiers do. Similarly the visitor can't be
			 * initialized properly.
			 */

			// Properties...

			final PropertyRule key = new PropertyRule(
					"key",
					DataType.identifier);

			final PropertyRule clazz = new PropertyRule(
					"class",
					DataType.text);

			final PropertyRule root = new PropertyRule(
					"root",
					DataType.bool);

			final PropertyRule cardinality = new PropertyRule(
					"cardinality",
					DataType.cardinality);

			final PropertyRule collection = new PropertyRule(
					"collection",
					DataType.identifier,
					new com.inexas.oak.dialect.ChoiceConstraint("set", "map", "list"));

			final PropertyRule type = new PropertyRule(
					"type",
					DataType.identifier);

			final PropertyRule value = new PropertyRule(
					"value",
					DataType.ANY);

			final PropertyRule visitors = new PropertyRule(
					"visitors",
					DataType.text);

			// Objects...

			final ObjectRule dialectAst = new ObjectRule(
					"Dialect",
					DialectNode.class,
					true);

			final ObjectRule object = new ObjectRule(
					"Object",
					Objet.class,
					false);

			final ObjectRule member = new ObjectRule(
					"Member",
					Member.class,
					false);

			final ObjectRule property = new ObjectRule(
					"Property",
					Property.class,
					false);

			final ObjectRule constraint = new ObjectRule(
					"Constraint",
					Constraint.class,
					false);

			// Relations...

			dialectAst.setRelations(
					relate(key, Cardinality.ONE_ONE),
					relate(object, Cardinality.ONE_MANY, map),
					relate(property, Cardinality.ONE_MANY, map),
					relate(visitors, Cardinality.ZERO_MANY));

			object.setRelations(
					relate(key, Cardinality.ONE_ONE),
					relate(clazz, Cardinality.ONE_ONE),
					relate(root, Cardinality.ZERO_ONE),
					relate(member, Cardinality.ONE_MANY, list));

			member.setRelations(
					relate(key, Cardinality.ZERO_ONE),
					relate(property, Cardinality.ZERO_ONE),
					relate(cardinality, Cardinality.ZERO_ONE),
					relate(collection, Cardinality.ZERO_ONE));

			property.setRelations(
					relate(key, Cardinality.ONE_ONE),
					relate(type, Cardinality.ZERO_ONE),
					relate(constraint, Cardinality.ZERO_MANY, list));

			constraint.setRelations(
					relate(type, Cardinality.ONE_ONE),
					relate(value, Cardinality.ZERO_MANY));

			setRules(key, clazz, root, cardinality, collection, type, member, object, property, dialectAst);
		} catch(final Exception e) {
			throw new InexasRuntimeException("Error building OakDialect", e);
		}
	}

}