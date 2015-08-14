package com.inexas.oak.advisory;

import java.io.File;
import java.util.*;
import com.inexas.tad.Tad;
import com.inexas.util.*;

public class Advisory implements Tad {
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
			return (isError ? "ERROR> " : "WARNING> ")
					+ sourceName + ':' + (line == 0 ? "" : Integer.toString(line))
					+ ' ' + message;
		}
	}

	private final String sourceName;
	private final List<Advice> items = new ArrayList<>();

	private int errorCount, warningCount;
	private final List<Pair<Object, Locus>> register = new ArrayList<>();
	private final String string;

	public Advisory(String string) {
		this.sourceName = "(String input)";
		this.string = string;
	}

	public Advisory(File file) {
		this.sourceName = file.getName();
		string = null;
	}

	/**
	 * Report an error.
	 *
	 * @param message
	 *            The error message.
	 */
	public void error(String message) {
		report(null, message, true);
	}

	/**
	 * Report an error.
	 *
	 * @param object
	 *            The object in which this error is being reported.
	 * @param message
	 *            The error message.
	 */
	public void error(Object object, String message) {
		report(object, message, true);
	}

	public void error(int line, int column, String message) {
		add(line, column, true, message);
	}

	/**
	 * Report an warning.
	 *
	 * @param object
	 *            The object in which this error is being reported.
	 * @param message
	 *            The error message.
	 */
	public void warning(Object object, String message) {
		report(object, message, false);
	}

	/**
	 * @return the errorCount
	 */
	public int getErrorCount() {
		return errorCount;
	}

	/**
	 * @return the warningCount
	 */
	public int getWarningCount() {
		return warningCount;
	}

	/**
	 * @return The first error message or null if the Advistory contains no
	 *         errors.
	 */
	public String getFirstError() {
		String result = null;
		for(final Advice advice : items) {
			if(advice.isError) {
				result = advice.message;
				break;
			}
		}
		return result;
	}

	public void associate(Locus locus, Object object) {
		register.add(new Pair<>(object, locus));
	}

	/**
	 * @return True if at least one error has been reported.
	 */
	public boolean hasErrors() {
		return errorCount > 0;
	}

	/**
	 * @return True if there are no errors or warnings.
	 */
	public boolean isEmpty() {
		return errorCount == 0 && warningCount == 0;
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
			result = "<No errors>";
		} else {
			sort();
			final TextBuilder tb = new TextBuilder();
			if(string != null) {
				tb.append(string);
				tb.newline();
			}
			for(final Advice item : items) {
				tb.append(item.toString());
				tb.newline();
			}
			result = tb.toString();
		}
		return result;
	}

	private void report(Object object, String message, boolean isError) {
		final Locus locus = lookup(object);
		if(locus == null) {
			add(0, 0, true, message);
		} else {
			add(locus.getLine(), locus.getColumn(), isError, message);
		}
	}

	private void add(int line, int column, boolean isError, String message) {
		final Advice item = new Advice(line, column, isError, message);
		items.add(item);
		if(isError) {
			this.errorCount++;
		} else {
			this.warningCount++;
		}
	}

	private Locus lookup(Object object) {
		Locus result;

		if(object == null) {
			result = null;
		} else if(object instanceof Locus) {
			result = (Locus)object;
		} else {
			result = null;
			for(final Pair<Object, Locus> pair : register) {
				if(pair.object1 == object) {
					result = pair.object2;
					break;
				}
			}
		}

		return result;
	}

}
