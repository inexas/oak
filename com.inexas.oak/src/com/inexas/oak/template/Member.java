package com.inexas.oak.template;

import com.inexas.oak.advisory.Advisory;
import com.inexas.oak.dialect.*;
import com.inexas.tad.Context;
import com.inexas.util.Cardinality;

public class Member implements Keyed {
	public final String key;
	public final Cardinality cardinality;
	public final CollectionType collectionType;
	public final Property property;
	public final boolean subjectIsObject;

	public Member(
			String key,
			Property property,
			Cardinality cardinality,
			String collectionType) {
		this.key = key;
		this.property = property;

		// Exactly one of key and property must be null
		if(!((key == null) ^ (property == null))) {
			error(key == null ?
					"Member must have either key or property" :
					"Member can't have both key and property");
		}

		if(key != null && key.charAt(0) <= 'Z') {
			subjectIsObject = true;
			// Default cardinality is 0..*
			this.cardinality = cardinality == null ? Cardinality.ZERO_MANY : cardinality;

		} else {
			subjectIsObject = false;
			this.cardinality = cardinality == null ? Cardinality.ONE_ONE : cardinality;
		}

		if(this.cardinality == Cardinality.ZERO) {
			error("Cardinality of 0..0 is invalid");
		}

		if(collectionType == null) {
			// Use default collection if possible...
			this.collectionType = this.cardinality.to == 1 ? CollectionType.singleton : CollectionType.list;
		} else {
			// String is checked by parser
			this.collectionType = CollectionType.valueOf(collectionType);
		}

		// Sanity check collection type against cardinality...
		if(this.cardinality.to == 1) {
			if(this.collectionType != CollectionType.singleton) {
				error("Cardinality of " + this.cardinality.toString() +
						" should have a collection type of singleton for: " + key);
			}
		} else {
			assert this.cardinality.to > 1;
			if(this.collectionType == CollectionType.singleton) {
				error("Cardinality of " + this.cardinality.toString() + " should be list, map or set");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final ToStringVisitor visitor = new ToStringVisitor(true);
		accept(visitor);
		return visitor.toString();
	}

	void accept(DialectVisitor visitor) {
		visitor.enter(this);
		if(property != null) {
			property.accept(visitor);
		}
		visitor.exit(this);
	}

	private void error(String message) {
		final Advisory advisory = Context.get(Advisory.class);
		advisory.error(this, message);
	}

}
