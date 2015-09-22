package com.inexas.oak.template;

import java.time.*;
import java.util.*;
import com.inexas.oak.*;
import com.inexas.oak.advisory.*;
import com.inexas.oak.dialect.*;
import com.inexas.tad.Context;
import com.inexas.util.*;

public abstract class Constraint extends Locus.Base implements Keyed {
	public final Object[] values;
	protected DataType dataType;

	protected Constraint(String[] values) {
		this.values = toType(values);
	}

	protected Constraint(Object[] values) {
		this.values = values;
	}

	/**
	 * Factory method to construct a new Constraint.
	 *
	 * @param type
	 *            The type of the Constraint: regexp, choice, ...
	 * @param values
	 *            A list of 0..* strings that will be parsed into objects
	 *            depending on the type of the Constraint.
	 * @return The newly created constraint. However if an error is encountered
	 *         the the Advisory is updated and null is returned.
	 */
	public static Constraint newConstraint(Identifier type, List<Object> values) {
		final Constraint result;

		final Object[] array = values.toArray(new Object[values.size()]);
		switch(type.toString()) {
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
	public static Constraint newConstraint(Identifier type, Object... values) {
		final Constraint result;

		switch(type.toString()) {
		case ChoiceConstraint.KEY:
			result = new ChoiceConstraint(values);
			break;

		case RegexpConstraint.KEY:
			result = new RegexpConstraint(values);
			break;

		default:
			final Advisory advisory = Context.get(Advisory.class);
			advisory.error("Unrecognised Constraint type: '" + type + '\'');
			result = null;
		}

		return result;
	}

	public abstract void toMarkup(Text t);

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
	 * This method is called when the Constraint is added to a Property. Now we
	 * know what data type the Constraint has to constrain then we need to check
	 * that the values we were given on construction make sense.
	 *
	 * The default implementation is to check for at least on value (which would
	 * only make sense if it were an expression) and of a matching data type.
	 * Override me if you need anything else.
	 *
	 * Errors are passed to an Advisory if one is in the Context otherwise a
	 * Parsing exception is thrown.
	 *
	 * @param dataType
	 *            The data type of the property.
	 */
	public final void setDataType(DataType dataType) throws ParsingException {
		if(dataType != DataType.any) {
			for(final Object value : values) {
				if(value == null) {
					// Null is good for all types
					continue;
				}
				final DataType valueType = DataType.getDataType(value.getClass());
				if(valueType != dataType) {
					error("Invalid data type: '" + value.toString() + "', expected: " + dataType.name());
				}
			}
		}
	}

	/**
	 * Convert the value(s) to a human readable array and add it to a
	 * TextBuilder.
	 *
	 * @param tb
	 *            Something like "["one", two, 3]" is added.
	 */
	protected void valuesToTextArray(Text t) {
		t.append('[');
		for(final Object option : values) {
			t.delimit();
			valueToText(t, option);
		}
		t.space();
		t.append(']');
	}

	protected void valueToText(Text t, Object value) {
		if(dataType == null) {
			/*
			 * The Constraint has yet to be added to a Property so do the best
			 * we can using the class of the value.
			 */
			if(value == null) {
				t.append("null");
			} else {
				final Class<?> clazz = value.getClass();
				if(clazz == String.class) {
					t.append('"');
					StringU.escapeNewlinesAndQuotes((String)value, t);
					t.append('"');
				} else if(clazz == LocalDate.class) {
					t.append('@');
					t.append(DateU.formatStandardDate((LocalDate)value));
				} else if(clazz == LocalTime.class) {
					t.append('@');
					t.append(DateU.formatStandardTime((LocalTime)value));
				} else if(clazz == LocalDateTime.class) {
					t.append('@');
					t.append(DateU.formatStandardDatetime((LocalDateTime)value));
				} else {
					t.append(value.toString());
				}
			}
		} else {
			if(value == null) {
				t.append("null");
			} else {
				switch(dataType) {
				case bool:
				case cardinality:
				case f:
				case z:
				case Z:
				case identifier:
				case path:
					t.append(value.toString());
					break;

				case date:
					t.append(DateU.formatStandardDate((LocalDate)value));
					break;

				case time:
					t.append(DateU.formatStandardTime((LocalTime)value));
					break;

				case datetime:
					t.append(DateU.formatStandardDatetime((LocalDateTime)value));
					break;

				case text:
					t.append('"');
					StringU.escapeNewlinesAndQuotes((String)value, t);
					t.append('"');
					break;

					// $CASES-OMITTED$
				default:
					throw new RuntimeException("Type: " + dataType);
				}
			}
		}
	}

	protected void error(String message) throws ParsingException {
		final Advisory advisory = Context.getButDontThrow(Advisory.class);
		if(advisory == null) {
			throw new ParsingException(message);
		}
		advisory.error(this, message);
	}

	/**
	 * Convert Oak value strings to their proper types
	 *
	 * @param values
	 *            Values to convert; e.g. "1", "\"two\"", "three"
	 * @return Converted values; e.g. 1, "two" three.
	 */
	private static Object[] toType(String[] values) {
		final Object[] result;

		final int length = values.length;
		result = new Object[length];
		for(int i = 0; i < length; i++) {
			result[i] = DataType.parseValue(values[i]);
		}

		return result;
	}

}
