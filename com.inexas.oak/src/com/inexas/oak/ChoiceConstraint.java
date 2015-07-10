package com.inexas.oak;

import com.inexas.util.*;

public class ChoiceConstraint extends Constraint {
	public final static String KEY = "choice";

	public ChoiceConstraint(Object... options) {
		super(options);
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
		tb.append('[');
		for(final Object option : values) {
			tb.delimit();
			if(option instanceof String) {
				tb.append('"');
				StringU.escapeNewlinesAndQuotes((String)option, tb);
				tb.append('"');
			} else {
				tb.append(option.toString());
			}
		}
		tb.space();
		tb.append(']');
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
	public boolean isValid(Object value) {
		boolean result = false;

		for(final Object option : values) {
			if(option.equals(value)) {
				result = true;
				break;
			}
		}

		return result;
	}

}
