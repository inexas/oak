package com.inexas.oak.dialect;

import static com.inexas.oak.dialect.CollectionType.*;
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

	/**
	 * Object --- m..n ---&gt; Property
	 *
	 * Note that the collection is always a list for Properties.
	 *
	 * @param subject
	 *            The subject Property
	 * @param cardinality
	 *            The cardinality of the relation.
	 */
	public Relationship(PropertyRule subject, Cardinality cardinality) {
		assert subject != null;

		this.subject = subject;
		this.cardinality = cardinality;
		// See comment on collection above
		this.collection = cardinality.to <= 1 ? singleton : list;
		subjectIsObject = false;
		subjectKey = subject.key;
	}

	// !todo delete unused ctors

	/**
	 * For root Object only.
	 *
	 * @param subject
	 *            The root object.
	 */
	public Relationship(ObjectRule subject) {
		assert subject.isRoot();

		this.subject = subject;
		cardinality = Cardinality.ONE_MANY;
		collection = singleton;
		subjectIsObject = true;
		subjectKey = subject.key;
	}

	public Relationship(ObjectRule subject, Cardinality cardinality, CollectionType collection) {
		assert subject != null;
		assert cardinality != null;
		assert collection != null;

		this.subject = subject;
		this.cardinality = cardinality;

		if(collection == null) {
			if(this.cardinality == Cardinality.ONE_ONE || this.cardinality == Cardinality.ZERO_ONE) {
				this.collection = CollectionType.singleton;
			} else {
				this.collection = CollectionType.list;
			}
		} else {

			// todo I should probably warn if the cardinality is 0..1 or 1..1
			// and the collection is not singleton
			this.collection = collection;
		}

		subjectIsObject = true;
		subjectKey = subject.key;
	}

	/**
	 * @param rule
	 * @param cardinality2
	 * @param collectionType
	 */
	public Relationship(Rule subject, Cardinality cardinality, CollectionType collection) {
		this.subject = subject;
		this.subjectKey = subject.key;
		this.subjectIsObject = subject instanceof ObjectRule;
		this.cardinality = cardinality;
		this.collection = collection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "- " + cardinality.text + " -> " + subjectKey;
	}
}