package com.inexas.oak;

import static com.inexas.oak.CollectionType.*;
import com.inexas.util.Cardinality;

public class Relation {
	final Rule subject;
	/**
	 * The cardinality of Objects is defined simply in the Relation.
	 *
	 * An Property's cardinality depends on the subject. If the Property is a
	 * element rather than an array then it may be optional so the subject could
	 * be defined as a 0..1: optional element, 1..1 mandatory array, 0..*
	 * optional array or 1..*: mandatory array.
	 */
	final Cardinality cardinality;

	/**
	 * Collection types for cardinalities where to > 1 otherwise singleton
	 */
	final CollectionType collection;
	final String subjectName;
	final boolean subjectIsObject;

	/**
	 * Object --- m..n ---> Property
	 *
	 * Note that the collection is always a list for Properties.
	 *
	 * @param subject
	 *            The subject Property
	 * @param cardinality
	 *            The cardinality of the relation.
	 */
	public Relation(PropertyRule subject, Cardinality cardinality) {
		assert subject != null;

		this.subject = subject;
		this.cardinality = cardinality;
		// See comment on collection above
		this.collection = cardinality.to <= 1 ? singleton : list;
		subjectIsObject = false;
		subjectName = subject.name;
	}

	/**
	 * For root Object only
	 */
	public Relation(ObjectRule subject) {
		assert subject.isRoot();

		this.subject = subject;
		cardinality = Cardinality.ONE_MANY;
		collection = singleton;
		subjectIsObject = true;
		subjectName = subject.name;
	}

	public Relation(
			ObjectRule subject,
			Cardinality cardinality,
			CollectionType collection) {
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
		subjectName = subject.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "- " + cardinality.text + " -> " + subjectName;
	}
}