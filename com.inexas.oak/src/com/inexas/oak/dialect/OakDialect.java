// Created by Oak: 15AUG15 12:41:03
package com.inexas.oak.dialect;

import static com.inexas.oak.dialect.CollectionType.*;
import com.inexas.oak.DataType;
import com.inexas.util.Cardinality;

public class OakDialect {
	public static Rulebase rulebase;

	private static Relationship relate(Rule key, Cardinality cardinality, CollectionType collection) {
		return new Relationship(key, cardinality, collection);
	}

	private static void setRules(ObjectRule... rules) {
		OakDialect.rulebase = new Rulebase("Dialect", rules);
	}

	static {
		try {
			final ObjectRule _Constraint = new ObjectRule(
					"Constraint",
					com.inexas.oak.template.Constraint.class,
					false);

			final ObjectRule _Dialect = new ObjectRule(
					"Dialect",
					com.inexas.oak.template.Dialect.class,
					true);

			final ObjectRule _Member = new ObjectRule(
					"Member",
					com.inexas.oak.template.Member.class,
					false);

			final ObjectRule _Object = new ObjectRule(
					"Object",
					com.inexas.oak.template.Objet.class,
					false);

			final ObjectRule _Property = new ObjectRule(
					"Property",
					com.inexas.oak.template.Property.class,
					false);

			final PropertyRule _Constraint_type = new PropertyRule(
					"type",
					DataType.identifier,
					new ChoiceConstraint("choice", "regexp"));

			final PropertyRule _Constraint_value = new PropertyRule(
					"value",
					DataType.any);

			final PropertyRule _Dialect_key = new PropertyRule(
					"key",
					DataType.identifier);

			final PropertyRule _Member_cardinality = new PropertyRule(
					"cardinality",
					DataType.cardinality);

			final PropertyRule _Member_collection = new PropertyRule(
					"collection",
					DataType.identifier,
					new ChoiceConstraint("list", "map", "set"));

			final PropertyRule _Member_key = new PropertyRule(
					"key",
					DataType.identifier);

			final PropertyRule _Object_class = new PropertyRule(
					"class",
					DataType.text);

			final PropertyRule _Object_key = new PropertyRule(
					"key",
					DataType.identifier);

			final PropertyRule _Object_root = new PropertyRule(
					"root",
					DataType.bool);

			final PropertyRule _Property_key = new PropertyRule(
					"key",
					DataType.identifier);

			final PropertyRule _Property_type = new PropertyRule(
					"type",
					DataType.identifier,
					new ChoiceConstraint("text", "identifier", "path", "boolean", "integer", "INTEGER",
							"decimal", "DECIMAL", "cardinality", "date", "time", "datetime", "any"));

			_Constraint.setRelationships(
					relate(_Constraint_type, Cardinality.newInstance("1..1"), singleton),
					relate(_Constraint_value, Cardinality.newInstance("0..*"), list));

			_Dialect.setRelationships(
					relate(_Dialect_key, Cardinality.newInstance("1..1"), singleton),
					relate(_Object, Cardinality.newInstance("1..*"), map));

			_Member.setRelationships(
					relate(_Member_key, Cardinality.newInstance("0..1"), singleton),
					relate(_Property, Cardinality.newInstance("0..1"), singleton),
					relate(_Member_cardinality, Cardinality.newInstance("0..1"), singleton),
					relate(_Member_collection, Cardinality.newInstance("0..1"), singleton));

			_Object.setRelationships(
					relate(_Object_key, Cardinality.newInstance("1..1"), singleton),
					relate(_Object_class, Cardinality.newInstance("1..1"), singleton),
					relate(_Object_root, Cardinality.newInstance("0..1"), singleton),
					relate(_Member, Cardinality.newInstance("1..*"), list));

			_Property.setRelationships(
					relate(_Property_key, Cardinality.newInstance("1..1"), singleton),
					relate(_Property_type, Cardinality.newInstance("0..1"), singleton),
					relate(_Constraint, Cardinality.newInstance("0..*"), list));

			setRules(_Constraint, _Dialect, _Member, _Object, _Property);
		} catch(final Exception e) {
			throw new RuntimeException("Error building OakDialect", e);
		}
	}
}
