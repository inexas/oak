package com.inexas.oak.dialect;

import java.math.*;
import com.inexas.oak.DataType;
import com.inexas.oak.template.Constraint;
import com.inexas.util.*;

/**
 * A choice constraint is given a list of choices in the values array. The value
 * must be one of those choices.
 */
public class ChoiceConstraint extends Constraint {
	public final static String KEY = "choice";

	public ChoiceConstraint(Object... options) {
		super(options);
		if(options == null || options.length <= 0) {
			error("Choice constraint must have at least two options");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(Object value) {
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
			tb.append(", should have been one of: ");
			valuesToTextArray(tb);
			error(tb.toString());
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
	public void setDataType(DataType dataType) {
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

		case z:
			expected = DataType.z.toString();
			expectedClass = Long.class;
			break;

		case Z:
			expected = DataType.Z.toString();
			expectedClass = BigInteger.class;
			break;

		case f:
			expected = DataType.f.toString();
			expectedClass = Double.class;
			break;

		case F:
			expected = DataType.Z.toString();
			expectedClass = BigDecimal.class;
			break;

		case cardinality:
			expected = DataType.cardinality.toString();
			expectedClass = Cardinality.class;
			break;

			// $CASES-OMITTED$
		default:
			error("Choice with " + dataType.name() + " does not make sense");
			expected = null;
			expectedClass = null;
			break;
		}

		if(expectedClass != null) {
			for(final Object value : values) {
				if(value != null) {
					if(value.getClass() != expectedClass) {
						error("Invalid data type for value: " + value + ", expected " + expected);
					}
				}
			}
		}

		this.dataType = dataType;
	}

}
