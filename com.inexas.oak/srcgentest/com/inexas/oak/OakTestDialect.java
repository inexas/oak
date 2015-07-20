// Created by Oak: 20JUL15 11:05:44
package com.inexas.oak;

import static com.inexas.oak.CollectionType.*;
import com.inexas.oak.*;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.util.Cardinality;

public class OakTestDialect {
	public static Dialect dialect;

	private static Relation relate(PropertyRule rule, Cardinality cardinality) {
		return new Relation(rule, cardinality);
	}

	private static Relation relate(ObjectRule key, Cardinality cardinality, CollectionType collection) {
		return new Relation(key, cardinality, collection);
	}

	private static void setRules(Rule... rules) {
		OakTestDialect.dialect = new Dialect("Dialect", rules);
	}

	public static String[] visitorList = {
	};

	static {
		try {
			final ObjectRule Test1_ = new ObjectRule(
					"Test1",
					com.inexas.oak.Test1.class,
					false);

			final PropertyRule key1_ = new PropertyRule(
					"key1",
					DataType.identifier);

			final PropertyRule key2_ = new PropertyRule(
					"key2",
					DataType.bool);

			Test1_.setRelations(
					relate(key1_, Cardinality.newInstance("1..1")),
					relate(key2_, Cardinality.newInstance("0..1")));

			setRules(Test1_,key1_,key2_);
		} catch(final Exception e) {
			throw new InexasRuntimeException("Error building OakTestDialect", e);
		}
	}
}
