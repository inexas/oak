package com.inexas.oak.template;

import java.time.*;
import java.util.*;

import com.inexas.oak.DataType;
import com.inexas.oak.advisory.*;
import com.inexas.oak.dialect.*;
import com.inexas.tad.Context;
import com.inexas.util.*;

public abstract class Constraint extends Locus.Base implements Keyed {
	public final Object[] values;
	protected DataType dataType;

	protected Constraint(Object[] values) {
		this.values = values;
	}

	/**
	 * Factory method to construct a new Constraint.
	 *
	 * @param type
	 *            The type of the Constraint: regexp, choice, ...
	 * @param values
	 *            A list of 0..* values depending on the type of the Constraint.
	 * @return The newly created constraint. However if an error is encountered
	 *         the the Advisory is updated and null is returned.
	 */
	public static Constraint newConstraint(String type, List<Object> values) {
		final Constraint result;

		final Object[] array = values.toArray();
		switch(type) {
		case ChoiceConstraint.KEY:
			result = new ChoiceConstraint(array);
			break;

		case RegexpConstraint.KEY:
			result = new RegexpConstraint(array);
			break;

		default:
			final Advisory advisory = Context.get(Advisory.class);
			advisory.error("Unrecognised Constraint type: '" + type + '\'');
			result = null;
		}

		return result;
	}

	public abstract void toMarkup(TextBuilder tb);

	void accept(DialectVisitor visitor) {
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
	 */
	public void validate(Map<String, Object> map) {
		for(final Object value : map.values()) {
			validate(value);
		}
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
	 */
	public void validate(Collection<Object> collection) {
		for(final Object value : collection) {
			validate(value);
		}
	}

	/**
	 * Test a value to see if it satisfies the constraint. If it doesn't then
	 * update the Advisory accordingly.
	 *
	 * @param value
	 *            The value to test.
	 */
	public abstract void validate(Object value);

	/**
	 * Convert the value(s) to a human readable array and add it to a
	 * TextBuilder.
	 *
	 * @param tb
	 *            Something like "["one", two, 3]" is added.
	 */
	protected void valuesToTextArray(TextBuilder tb) {
		tb.append('[');
		for(final Object option : values) {
			tb.delimit();
			valueToText(tb, option);
		}
		tb.space();
		tb.append(']');
	}

	protected void valueToText(TextBuilder tb, Object value) {
		if(dataType == null) {
			/*
			 * The Constraint has yet to be added to a Property so do the best
			 * we can using the class of the value.
			 */
			if(value == null) {
				tb.append("null");
			} else {
				final Class<?> clazz = value.getClass();
				if(clazz == String.class) {
					tb.append('"');
					StringU.escapeNewlinesAndQuotes((String)value, tb);
					tb.append('"');
				} else if(clazz == LocalDate.class) {
					tb.append('@');
					tb.append(DateU.format((LocalDate)value));
				} else if(clazz == LocalTime.class) {
					tb.append('@');
					tb.append(DateU.format((LocalTime)value));
				} else if(clazz == LocalDateTime.class) {
					tb.append('@');
					tb.append(DateU.format((LocalDateTime)value));
				} else {
					tb.append(value.toString());
				}
			}
		} else {
			if(value == null) {
				tb.append("null");
			} else {
				switch(dataType) {
				case bool:
				case cardinality:
				case f:
				case z:
				case Z:
				case identifier:
				case path:
					tb.append(value.toString());
					break;

				case date:
					tb.append(DateU.format((LocalDate)value));
					break;

				case time:
					tb.append(DateU.format((LocalTime)value));
					break;

				case datetime:
					tb.append(DateU.format((LocalDateTime)value));
					break;

				case text:
					tb.append('"');
					StringU.escapeNewlinesAndQuotes((String)value, tb);
					tb.append('"');
					break;

					// $CASES-OMITTED$
				default:
					throw new RuntimeException("Type: " + dataType);
				}
			}
		}
	}

	/**
	 * This method is called when the Constraint is added to a Property. The
	 * implementation should check that the data type makes sense for the
	 * Constraint and that the values of all the parameters make sense. It
	 * should also remember the data type: "this.dataType = dataType;"
	 *
	 * @param dataType
	 *            The data type of the property.
	 */
	public abstract void setDataType(DataType dataType);

	protected void error(String message) {
		final Advisory advisory = Context.get(Advisory.class);
		advisory.error(this, message);
	}
}
