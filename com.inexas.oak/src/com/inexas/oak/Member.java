package com.inexas.oak;

import com.inexas.util.Cardinality;

public class Member implements Keyed {
	final String key;
	final Cardinality cardinality;
	final CollectionType collectionType;

	public Member(
			String key,
			Cardinality cardinality,
			String collectionType) {
		this.key = key;

		if(cardinality == null) {
			final boolean isObject = Character.isUpperCase(key.charAt(0));
			this.cardinality = isObject ? Cardinality.ZERO_MANY : Cardinality.ONE_ONE;
		} else {
			this.cardinality = cardinality;
		}

		if(collectionType == null) {
			if(this.cardinality == Cardinality.ONE_ONE || this.cardinality == Cardinality.ZERO_ONE) {
				this.collectionType = CollectionType.singleton;
			} else {
				this.collectionType = null;
			}
		} else {
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
}
