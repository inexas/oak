package com.inexas.oak.dialect;

import java.util.regex.*;
import com.inexas.oak.Identifier;
import com.inexas.oak.template.Constraint;
import com.inexas.util.Text;

/**
 * A choice constraint is given a list of choices in the values array. The value
 * must be one of those choices.
 */
public class RegexConstraint extends Constraint {
	public final static String KEY = "regex";

	public RegexConstraint(String... regexs) {
		super(regexs);
		if(values.length == 0) {
			error("Regular expression (regex) Constraints need at least one regular expression");
		} else {
			for(final Object value : values) {
				try {
					final String string = (String)value;
					Pattern.compile(string);
				} catch(final PatternSyntaxException e) {
					error("Error compiling regular expression: " + value);
				} catch(final ClassCastException e) {
					error("Invalid type for regular expression: " + value);
				}
			}
		}
	}

	/**
	 * @param values
	 *            A list of zero or more objects.
	 */
	public RegexConstraint(Object... values) {
		super(values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toMarkup(Text t) {
		t.indent();
		t.append("Constraint");
		t.space();
		t.append('{');
		t.newline();
		t.indentMore();

		t.indent();
		t.append("type:");
		t.space();
		t.append(KEY);
		t.append(';');
		t.newline();

		t.indent();
		if(values.length == 1) {
			// value: "a*b";
			t.append("value: ");
			valueToText(t, values[0]);
			t.newline();
		} else {
			// value [ "a*b", "b*", "aba" ]
			t.append("value ");
			valuesToTextArray(t);
			t.newline();
		}

		t.indentLess();
		t.indent();
		t.append('}');
		t.newline();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identifier getKey() {
		return new Identifier(KEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final Text result = new Text(true);
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
			final Text t = new Text(true);
			t.append("Invalid value for regex constraint: '");
			t.append(value == null ? "<null>" : value.toString());
			t.append(", should have be one of: ");
			valuesToTextArray(t);
			error(t.toString());
		}
	}

}
