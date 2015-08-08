package com.inexas.oak.dialect;

import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.Context;
import com.inexas.util.Cardinality;

public class Member implements Keyed {
	final String key;
	final Cardinality cardinality;
	final CollectionType collectionType;
	final Property property;

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

		// Default cardinality is 1..1
		this.cardinality = cardinality == null ? Cardinality.ONE_ONE : cardinality;
		if(this.cardinality == Cardinality.ZERO) {
			error("Cardinality of 0..0 is invalid");
		}

		if(collectionType == null) {
			if(this.cardinality.to > 1) {
				error("Cardinality of " + this.cardinality.toString() + " must have collection type");
			}
			this.collectionType = CollectionType.singleton;
		} else {
			if(this.cardinality.to <= 1) {
				warning("Cardinality of " + this.cardinality.toString() + " must have collection type");
			}
			// todo I should probably warn if 0..1 or 1..1 and collection is
			// something other than singleton
			this.collectionType = CollectionType.valueOf(collectionType);
			// !todo Exception on invalid type
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		return key;
	}

	void accept(DialectVisitor visitor) {
		visitor.visit(this);
	}

	private void error(String message) {
		final Advisory advisory = Context.get(Advisory.class);
		advisory.error(this, message);
	}

	private void warning(String message) {
		final Advisory advisory = Context.get(Advisory.class);
		advisory.warning(this, message);
	}

}
