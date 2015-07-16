// Created by Oak: 16JUL15 07:32:48
package com.inexas.oak.examples;

import static com.inexas.oak.CollectionType.*;
import com.inexas.oak.*;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.util.Cardinality;

public class PersonDialect {
	public static Dialect dialect;

	private static Relation relate(PropertyRule rule, Cardinality cardinality) {
		return new Relation(rule, cardinality);
	}

	private static Relation relate(ObjectRule key, Cardinality cardinality, CollectionType collection) {
		return new Relation(key, cardinality, collection);
	}

	private static void setRules(Rule... rules) {
		PersonDialect.dialect = new Dialect("Dialect", rules);
	}

	public static String[] visitorList = {
	};

	static {
		try {
			final ObjectRule Email_ = new ObjectRule(
					"Email",
					com.inexas.oak.examples.Email.class,
					false);

			final PropertyRule isHome_ = new PropertyRule(
					"isHome",
					DataType.bool);

			final PropertyRule text_ = new PropertyRule(
					"text",
					DataType.text);

			final ObjectRule Person_ = new ObjectRule(
					"Person",
					com.inexas.oak.examples.Person.class,
					true);

			final PropertyRule userId_ = new PropertyRule(
					"userId",
					DataType.identifier);

			Email_.setRelations(
					relate(text_, Cardinality.newInstance("1..1")),
					relate(isHome_, Cardinality.newInstance("1..1")));

			Person_.setRelations(
					relate(userId_, Cardinality.newInstance("1..1")),
					relate(Email_, Cardinality.newInstance("0..*"), list));

			setRules(Email_,isHome_,text_,Person_,userId_);
		} catch(final Exception e) {
			throw new InexasRuntimeException("Error building PersonDialect", e);
		}
	}
}
