package com.inexas.oak;

import java.util.regex.*;
import com.inexas.oak.advisory.OakException;
import com.inexas.util.TextBuilder;

/**
 * A choice constraint is given a list of choices in the values array. The value
 * must be one of those choices.
 */
public class RegexpConstraint extends Constraint {
	public final static String KEY = "regexp";

	public RegexpConstraint(Object... regexps) throws OakException {
		super(regexps);
		if(regexps.length == 0) {
			throw new OakException(
					"Regular expression (regexp) Constraints need at least one regular expression");
		}

		for(final Object regexp : regexps) {
			if(regexp == null) {
				throw new OakException("Cannot use <null> as a regular expression");
			}
			if(regexp instanceof String) {
				final String string = (String)regexp;
				try {
					Pattern.compile(string);
				} catch(final Exception e) {
					throw new OakException("Error compiling regular expression: " + string);
				}
			} else {
				throw new OakException("Constraint value not a string: " + regexp.toString());
			}
		}
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

		tb.indent();
		if(values.length == 1) {
			// value: "a*b";
			tb.append("value: ");
			valueToText(tb, values[0]);
			tb.newline();
		} else {
			// value [ "a*b", "b*", "aba" ]
			tb.append("value ");
			valuesToTextArray(tb);
			tb.newline();
		}

		tb.indentLess();
		tb.indent();
		tb.append('}');
		tb.newline();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		return KEY;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validate(Object value) throws OakException {
		boolean matched = false;

		final String valueAsString = (String)value;
		for(final Object object : values) {
			final Pattern pattern = Pattern.compile((String)object);
			final Matcher matcher = pattern.matcher(valueAsString);
			if(matcher.matches()) {
				matched = true;
				break;
			}
		}

		if(!matched) {
			final TextBuilder tb = new TextBuilder(true);
			tb.append("Invalid value for regexp constraint: '");
			tb.append(value == null ? "<null>" : value.toString());
			tb.append(", should have be one of: ");
			valuesToTextArray(tb);
			throw new OakException(tb.toString());
		}
	}

	@Override
	void setDataType(DataType dataType) throws OakException {
		this.dataType = dataType;

		final Class<?> expectedClass;
		final String expected;
		if(dataType == DataType.text) {
			expected = DataType.text.toString();
			expectedClass = String.class;
		} else {
			throw new OakException(
					"Regular expressions cannot be applied to " + dataType.name() + " properties");
		}

		for(final Object value : values) {
			if(value.getClass() != expectedClass) {
				throw new OakException("Invalid data type for value: " + value + ", expected " + expected);
			}
		}

		this.dataType = dataType;
	}

}
