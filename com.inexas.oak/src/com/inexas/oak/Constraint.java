package com.inexas.oak;

import java.util.*;
import com.inexas.oak.advisory.*;
import com.inexas.util.TextBuilder;

public abstract class Constraint extends Locus.Base implements Keyed {
	protected final Object[] values;

	protected Constraint(Object[] values) {
		this.values = values;
	}

	public static Constraint newConstraint(String type, List<Object> values) throws OakConstructorException {
		final Constraint result;

		final Object[] array = values.toArray();
		switch(type) {
		case "choice":
			result = new ChoiceConstraint(array);
			break;

		default:
			throw new OakConstructorException("Unrecognised Constraint type: '" + type + '\'');
		}

		return result;
	}

	public abstract void toMarkup(TextBuilder tb);

	void accept(DialectVisitor visitor) throws OakException {
		visitor.visit(this);
	}

	/**
	 * Check that the values in a Map are valid. The default implementation
	 * (i.e. Constraint.isValid(Map)) calls the isValid(Object) method for each
	 * value in the Map.
	 *
	 * Override this method if you need do some inter-Object checking of values.
	 *
	 * @param map
	 *            A non-empty Map of values to test.
	 * @return False if any one, or a combination of the values in the Map are
	 *         invalid.
	 */
	boolean isValid(Map<String, Object> map) {
		boolean result = true;
		for(final Object value : map.values()) {
			if(!isValid(value)) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * Check that the values in a Collection are valid. The Collection will
	 * either be a List or Set, if the Collection is a List the Objects it
	 * contains will be in order The default implementation (i.e.
	 * Constraint.isValid(Collection)) calls the isValid(Object) method for each
	 * value in the Collection.
	 *
	 * Override this method if you need do some inter-Object checking of values.
	 *
	 * @param collection
	 *            A non-empty Collection of values to test.
	 * @return False if any one, or a combination of the values in the
	 *         Collection are invalid.
	 */
	boolean isValid(Collection<Object> collection) {
		boolean result = true;
		for(final Object value : collection) {
			if(!isValid(value)) {
				result = false;
				break;
			}
		}
		return result;
	}

	abstract boolean isValid(Object value);

}
