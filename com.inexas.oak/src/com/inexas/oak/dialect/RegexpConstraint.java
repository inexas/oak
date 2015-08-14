package com.inexas.oak.dialect;

import java.util.regex.*;
import com.inexas.oak.DataType;
import com.inexas.oak.template.Constraint;
import com.inexas.util.TextBuilder;

/**
 * A choice constraint is given a list of choices in the values array. The value
 * must be one of those choices.
 */
public class RegexpConstraint extends Constraint {
	public final static String KEY = "regexp";

	public RegexpConstraint(Object... regexps) {
		super(regexps);
		if(regexps.length == 0) {
			error("Regular expression (regexp) Constraints need at least one regular expression");
		} else {
			for(final Object regexp : regexps) {
				if(regexp == null) {
					error("Cannot use <null> as a regular expression");
				} else {
					if(regexp instanceof String) {
						final String string = (String)regexp;
						try {
							Pattern.compile(string);
						} catch(final Exception e) {
							error("Error compiling regular expression: " + string);
						}
					} else {
						error("Constraint value not a string: " + regexp.toString());
					}
				}
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
	public void validate(Object value) {
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
			error(tb.toString());
		}
	}

	@Override
	public void setDataType(DataType dataType) {
		this.dataType = dataType;

		final Class<?> expectedClass;
		final String expected;
		if(dataType == DataType.text) {
			expected = DataType.text.toString();
			expectedClass = String.class;

			for(final Object value : values) {
				if(value.getClass() != expectedClass) {
					error("Invalid data type for value: " + value + ", expected " + expected);
				}
			}

			this.dataType = dataType;
		} else {
			error("Regular expressions cannot be applied to " + dataType.name() + " properties");
		}
	}
}
