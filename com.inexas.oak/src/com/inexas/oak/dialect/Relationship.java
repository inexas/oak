package com.inexas.oak.dialect;

import com.inexas.util.Cardinality;

/**
 * A Relationship defines the relationship between an object and its members:
 * the name, the cardinality the type of collection
 */
public class Relationship {
	/**
	 * Either a Object or Property which is a member of the containing Object.
	 */

	public final Rule subject;

	/**
	 * The key of the Object or Property which is a member of the containing
	 * Object.
	 */
	public final String subjectKey;

	/**
	 * True if the subject of this Relationship is an Object, false if it's a
	 * Property-
	 */
	public final boolean subjectIsObject;

	/**
	 * The cardinality of Objects is defined simply in the Relationship.
	 *
	 * A Property's cardinality depends on the subject. If the Property is a
	 * element rather than an array then it may be optional so the subject could
	 * be defined as a 0..1: optional element, 1..1 mandatory or an array, 0..*,
	 * 1..*, etc.
	 */
	public final Cardinality cardinality;

	/**
	 * Collection types for cardinalities where to > 1 otherwise singleton.
	 */
	public final CollectionType collection;

	public Relationship(Rule subject, Cardinality cardinality, CollectionType collection) {
		assert subject != null;
		assert cardinality != null;
		assert collection != null;

		this.subject = subject;
		this.cardinality = cardinality;
		this.collection = collection;
		subjectIsObject = subject instanceof ObjectRule;
		subjectKey = subject.key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "- " + cardinality.text + " -> " + subjectKey;
	}
}