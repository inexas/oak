package com.inexas.oak.advisory;

import java.util.*;
import com.inexas.util.*;

public class Advisory {
	public class Advice implements Comparable<Advice> {
		public final String message;
		public final int line, column;
		public final boolean isError;

		public Advice(int line, int column, boolean isError, String message) {
			this.isError = isError;
			this.message = message;
			this.line = line;
			this.column = column;
		}

		/**
		 * @return Line number in source, first line is 1. If the line number is
		 *         not known then 0 is returned.
		 */
		public int getLine() {
			return line;
		}

		/**
		 * @return Column number in source, first column is 1. If the column
		 *         number is not known then 0 is returned.
		 */
		public int getColumn() {
			return column;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(Advice rhs) {
			int result;

			result = line - rhs.line;
			if(result == 0) {
				result = column - rhs.column;
			}

			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return sourceName + ':' + (line == 0 ? "" : Integer.toString(line)) + ' ' + message;
		}
	}

	private final String sourceName;
	private final List<Advice> items = new ArrayList<>();
	private boolean hasErrors;
	private final List<Pair<Object, Locus>> register = new ArrayList<>();

	public Advisory(String sourceName) {
		this.sourceName = sourceName;
	}

	public void reportError(Object object, String message) {
		final Locus locus = lookup(object);
		if(locus == null) {
			reportError(message);
		} else {
			add(locus.getLine(), locus.getColumn(), true, message);
		}
	}

	public void reportError(Locus locus, String message) {
		add(locus.getLine(), locus.getColumn(), true, message);
	}

	public void reportError(String message) {
		add(0, 0, true, message);
	}

	public void reportError(int line, int column, String message) {
		add(line, column, true, message);
	}

	public void reportWarning(Locus locus, String message) {
		add(locus.getLine(), locus.getColumn(), false, message);
	}

	public void associate(Locus locus, Object object) {
		register.add(new Pair<>(object, locus));
	}

	/**
	 * @return True if at least one error has been reported.
	 */
	public boolean hasErrors() {
		return hasErrors;
	}

	/**
	 * @return True if there are no errors or warnings.
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}

	public void sort() {
		Collections.sort(items);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final String result;
		if(items.isEmpty()) {
			result = "";
		} else {
			sort();
			final TextBuilder tb = new TextBuilder();
			for(final Advice item : items) {
				tb.append(item.toString());
				tb.newline();
			}
			result = tb.toString();
		}
		return result;
	}

	private void add(int line, int column, boolean isError, String message) {
		final Advice item = new Advice(line, column, false, message);
		items.add(item);
		if(isError) {
			this.hasErrors = true;
		}
		System.err.println("ERROR> " + item.toString());
	}

	private Locus lookup(Object object) {
		Locus result = null;
		for(final Pair<Object, Locus> pair : register) {
			if(pair.object1 == object) {
				result = pair.object2;
			}
		}
		return result;
	}

}
