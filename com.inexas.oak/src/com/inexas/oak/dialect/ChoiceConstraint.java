package com.inexas.oak.dialect;

import com.inexas.oak.template.Constraint;
import com.inexas.util.Text;

/**
 * A choice constraint is given a list of choices in the values array. The value
 * must be one of those choices.
 */
public class ChoiceConstraint extends Constraint {
	public final static String KEY = "choice";

	/**
	 * This constructor is called when a Dialect is loaded from a rulebase and
	 * the constraints are written as strings. The Property type is not known
	 * yet so we can covert them to their proper types but not check them.
	 *
	 * @param options
	 *            String versions of the options, e.g. "`/Some/Path`"
	 */
	public ChoiceConstraint(String... options) {
		super(options);
		if(options == null || options.length <= 0) {
			error("Choice constraint must have at least two options");
		}
	}

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
			final Text t = new Text(true);
			t.append("Invalid value for choice constraint: '");
			t.append(value == null ? "<null>" : value.toString());
			t.append("', should have been one of: ");
			valuesToTextArray(t);
			error(t.toString());
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

		// value [ "set", "map", "list" ]
		t.indent();
		t.append("value");
		t.space();
		valuesToTextArray(t);
		t.newline();

		t.indentLess();
		t.indent();
		t.append('}');
		t.newline();
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

}
