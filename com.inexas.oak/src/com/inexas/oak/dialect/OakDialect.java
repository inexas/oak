package com.inexas.oak.dialect;

import static com.inexas.oak.dialect.CollectionType.*;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.DataType;
import com.inexas.oak.template.*;
import com.inexas.util.Cardinality;

/**
 * This file contains the hard-coded rules for transforming an Oak Dialect file
 * into a tree structure in the target dialect.
 */
public class OakDialect {
	public static Rulebase rulebase;

	private static Relationship relate(PropertyRule rule, Cardinality cardinality) {
		return new Relationship(rule, cardinality);
	}

	private static Relationship relate(ObjectRule key, Cardinality cardinality) {
		return new Relationship(key, cardinality, singleton);
	}

	private static Relationship relate(ObjectRule key, Cardinality cardinality, CollectionType collection) {
		return new Relationship(key, cardinality, collection);
	}

	private static void setRules(ObjectRule... rules) {
		OakDialect.rulebase = new Rulebase("Dialect", rules);
	}

	public static String[] visitorList = {};

	static {
		try {
			final ObjectRule _Dialect = new ObjectRule(
					"Dialect",
					Dialect.class,
					true);

			final PropertyRule _Dialect_visitor = new PropertyRule(
					"visitor",
					DataType.text);

			final PropertyRule _Dialect_key = new PropertyRule(
					"key",
					DataType.identifier);

			final ObjectRule _Object = new ObjectRule(
					"Object",
					Objet.class,
					false);

			final PropertyRule _Object_key = new PropertyRule(
					"key",
					DataType.identifier);

			final PropertyRule _Object_class = new PropertyRule(
					"class",
					DataType.text);

			final PropertyRule _Object_root = new PropertyRule(
					"root",
					DataType.bool);

			final ObjectRule _Member = new ObjectRule(
					"Member",
					Member.class,
					false);

			final PropertyRule _Member_key = new PropertyRule(
					"key",
					DataType.identifier);

			final PropertyRule _Member_cardinality = new PropertyRule(
					"cardinality",
					DataType.cardinality);

			final PropertyRule _Member_collection = new PropertyRule(
					"collection",
					DataType.identifier,
					new ChoiceConstraint("list", "map", "set"));

			final ObjectRule _Property = new ObjectRule(
					"Property",
					Property.class,
					false);

			final PropertyRule _Property_key = new PropertyRule(
					"key",
					DataType.identifier);

			// @formatter:off
			final PropertyRule _Property_type =new PropertyRule(
					"type",
					DataType.identifier,
					new ChoiceConstraint(
							"text",			"identifier",	"path",
							"boolean",
							"integer",		"INTEGER",		"decimal",		"DECIMAL",
							"cardinality",
							"date",			"time",			"datetime",
							"any"));
			// @formatter:on

			final ObjectRule _Constraint = new ObjectRule(
					"Constraint",
					Constraint.class,
					false);

			final PropertyRule _Constraint_type = new PropertyRule(
					"type",
					DataType.identifier,
					new ChoiceConstraint("choice", "regexp"));

			final PropertyRule _Constraint_value = new PropertyRule(
					"value",
					DataType.any);

			_Dialect.setRelationships(
					relate(_Dialect_key, Cardinality.ONE_ONE),
					relate(_Object, Cardinality.ONE_MANY, map),
					relate(_Dialect_visitor, Cardinality.ZERO_MANY));

			_Object.setRelationships(
					relate(_Object_key, Cardinality.ONE_ONE),
					relate(_Object_class, Cardinality.ONE_ONE),
					relate(_Object_root, Cardinality.ZERO_ONE),
					relate(_Member, Cardinality.ONE_MANY, list));

			_Member.setRelationships(
					relate(_Member_key, Cardinality.ZERO_ONE),
					relate(_Property, Cardinality.ZERO_ONE),
					relate(_Member_cardinality, Cardinality.ZERO_ONE),
					relate(_Member_collection, Cardinality.ZERO_ONE));

			_Property.setRelationships(
					relate(_Property_key, Cardinality.ONE_ONE),
					relate(_Property_type, Cardinality.ZERO_ONE),
					relate(_Constraint, Cardinality.ZERO_MANY, list));

			_Constraint.setRelationships(
					relate(_Constraint_type, Cardinality.ONE_ONE),
					relate(_Constraint_value, Cardinality.ZERO_MANY));

			setRules(
					// _Object_class,
					// _Object_root,
					// _Member_cardinality,
					// _Member_collection,
					// _Property_type,
					// _Constraint_type,
					// _Constraint_value,
					// _Dialect_visitor,
					_Dialect,
					_Object,
					_Member,
					_Property,
					_Constraint);
		} catch(final Exception e) {
			throw new InexasRuntimeException("Error building OakDialect", e);
		}
	}
}
