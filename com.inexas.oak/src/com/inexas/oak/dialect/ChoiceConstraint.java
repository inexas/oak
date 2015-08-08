package com.inexas.oak.dialect;

import java.math.BigDecimal;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.OakException;
import com.inexas.util.*;

/**
 * A choice constraint is given a list of choices in the values array. The value
 * must be one of those choices.
 */
public class ChoiceConstraint extends Constraint {
	public final static String KEY = "choice";

	public ChoiceConstraint(Object... options) throws OakException {
		super(options);
		if(options == null || options.length <= 0) {
			throw new OakException("Choice constraint must have at least two options");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(Object value) throws OakException {
		boolean found = false;

		for(final Object option : values) {
			if(option.equals(value)) {
				found = true;
				break;
			}
		}

		if(!found) {
			final TextBuilder tb = new TextBuilder(true);
			tb.append("Invalid value for choice constraint: '");
			tb.append(value == null ? "<null>" : value.toString());
			tb.append(", should have be one of: ");
			valuesToTextArray(tb);
			throw new OakException(tb.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		// todo Do I need keys?
		return KEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toMarkup(TextBuilder tb) {
		tb.indent();
		tb.append("Constraint");
		tb.space();
		tb.append('{');
		tb.newline();
		tb.indentMore();

		tb.indent();
		tb.append("type:");
		tb.space();
		tb.append(KEY);
		tb.append(';');
		tb.newline();

		// value [ "set", "map", "list" ]
		tb.indent();
		tb.append("value");
		tb.space();
		valuesToTextArray(tb);
		tb.newline();

		tb.indentLess();
		tb.indent();
		tb.append('}');
		tb.newline();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final TextBuilder result = new TextBuilder(true);
		toMarkup(result);
		return result.toString();
	}

	@Override
	void setDataType(DataType dataType) throws OakException {
		this.dataType = dataType;

		final Class<?> expectedClass;
		final String expected;
		switch(dataType) {
		case text:
		case identifier:
		case path:
			expected = DataType.text.toString();
			expectedClass = String.class;
			break;

		case integer:
			expected = DataType.integer.toString();
			expectedClass = Long.class;
			break;

		case decimal:
			expected = DataType.decimal.toString();
			expectedClass = Double.class;
			break;

		case precision:
			// todo Might want to accept integer and decimals
			expected = DataType.precision.toString();
			expectedClass = BigDecimal.class;
			break;

		case cardinality:
			expected = DataType.cardinality.toString();
			expectedClass = Cardinality.class;
			break;

		case ANY:
		case NULL:
		case bool:
		case date:
		case time:
		case datetime:
			throw new OakException("Choice with " + dataType.name() + " does not make sense");

		default:
			throw new UnexpectedException("Type: " + dataType.name());
		}

		for(final Object value : values) {
			if(value != null) {
				if(value.getClass() != expectedClass) {
					throw new OakException("Invalid data type for value: " + value + ", expected " + expected);
				}
			}
		}

		this.dataType = dataType;
	}

}
