// Created by Oak: 22SEP15 16:23:21
package com.inexas.oak.examples;

import static com.inexas.oak.dialect.CollectionType.*;
import com.inexas.oak.DataType;
import com.inexas.oak.*;
import com.inexas.oak.dialect.*;
import com.inexas.util.Cardinality;

public class PersonDialect {
	public static Rulebase rulebase;

	private static Relationship relate(Rule key, Cardinality cardinality, CollectionType collection) {
		return new Relationship(key, cardinality, collection);
	}

	private static void setRules(ObjectRule... rules) {
		PersonDialect.rulebase = new Rulebase(new Identifier("Dialect"), rules);
	}

	static {
		try {
			final ObjectRule _Email = new ObjectRule(
					new Identifier("Email"),
					com.inexas.oak.examples.Email.class,
					false);

			final ObjectRule _Person = new ObjectRule(
					new Identifier("Person"),
					com.inexas.oak.examples.Person.class,
					true);

			final PropertyRule _Email_isHome = new PropertyRule(
					new Identifier("isHome"),
					DataType.bool);

			final PropertyRule _Email_text = new PropertyRule(
					new Identifier("text"),
					DataType.text);

			final PropertyRule _Person_userId = new PropertyRule(
					new Identifier("userId"),
					DataType.identifier);

			_Email.setRelationships(
					relate(_Email_text, Cardinality.newInstance("1..1"), singleton),
					relate(_Email_isHome, Cardinality.newInstance("1..1"), singleton));

			_Person.setRelationships(
					relate(_Person_userId, Cardinality.newInstance("1..1"), singleton),
					relate(_Email, Cardinality.newInstance("0..*"), list));

			setRules(_Email,_Person);
		} catch(final Exception e) {
			throw new RuntimeException("Error building PersonDialect", e);
		}
	}
}
