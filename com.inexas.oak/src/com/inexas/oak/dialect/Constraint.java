package com.inexas.oak.dialect;

import java.time.*;
import java.util.*;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.*;
import com.inexas.util.*;

public abstract class Constraint extends Locus.Base implements Keyed {
	protected final Object[] values;
	protected DataType dataType;

	protected Constraint(Object[] values) {
		this.values = values;
	}

	public static Constraint newConstraint(String type, List<Object> values) throws OakException {
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
			throw new OakException("Unrecognised Constraint type: '" + type + '\'');
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
	 * @throws OakException
	 *             Thrown on invalid value.
	 */
	void validate(Map<String, Object> map) throws OakException {
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
	 * @param collection
	 *            A non-empty Collection of values to test.
	 * @throws OakException
	 *             Thrown on invalid value.
	 */
	void validate(Collection<Object> collection) throws OakException {
		for(final Object value : collection) {
			validate(value);
		}
	}

	abstract void validate(Object value) throws OakException;

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
				case decimal:
				case integer:
				case precision:
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
					throw new InexasRuntimeException("Type: " + dataType);
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
	 * @throws OakException
	 *             Thrown if there dataType is incompatible with the Constraint
	 *             or with one or more of the values.
	 */
	abstract void setDataType(DataType dataType) throws OakException;

}
