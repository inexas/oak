package com.inexas.oak.advisory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import com.inexas.tad.Tad;
import com.inexas.util.*;

/**
 * An Advisory is an object that collects Advices: error messages, warning
 * messages and the like. This is tasks where you want to collect the advice but
 * not terminate processing. It's a TAD object so you can attach it to the
 * thread and grab it when you need it.
 *
 * @author kwhittingham, @date 1 Jan 2017
 */
public class Advisory implements Tad {
	public static enum Type {
		ERR,
		WRN,
		INF;
	}

	// todo Add info
	public class Advice implements Comparable<Advice> {
		public final String message;
		public final int line, column;
		public final Type type;

		public Advice(int line, int column, Type type, String message) {
			this.type = type;
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
			return type.name() + "> "
					+ sourceName + ':' + (line == 0 ? "" : Integer.toString(line))
					+ ' ' + message;
		}
	}

	private final String path;
	private final String sourceName;
	private final List<Advice> items = new ArrayList<>();

	private int errorCount, warningCount;
	private final List<Pair<Object, Locus>> register = new ArrayList<>();

	public Advisory(String sourceName) {
		this.path = null;
		this.sourceName = sourceName;
	}

	public Advisory(File sourceFile) {
		this.path = sourceFile.getAbsolutePath();
		this.sourceName = sourceFile.getName();
	}

	/**
	 * Report an error.
	 *
	 * @param message
	 *            The error message.
	 */
	public void error(String message) {
		report(null, message, Type.ERR);
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
		report(object, message, Type.ERR);
	}

	public void error(int line, int column, String message) {
		add(line, column, Type.ERR, message);
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
		report(object, message, Type.WRN);
	}

	public void warning(int line, int column, String message) {
		add(line, column, Type.WRN, message);
	}

	/**
	 * Report information.
	 *
	 * @param object
	 *            The object in which this error is being reported.
	 * @param message
	 *            The error message.
	 */
	public void info(Object object, String message) {
		report(object, message, Type.INF);
	}

	public void info(int line, int column, String message) {
		add(line, column, Type.INF, message);
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
	 * @return The first error message or null if the Advisory contains no
	 *         errors.
	 */
	public String getFirstError() {
		String result = null;
		for(final Advice advice : items) {
			if(advice.type == Type.ERR) {
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
			final Text t = new Text();
			for(final Advice item : items) {
				t.append(item.toString());
				t.newline();
			}
			result = t.toString();
		}
		return result;
	}

	private void report(Object object, String message, Type type) {
		final Locus locus = lookup(object);
		if(locus == null) {
			add(0, 0, type, message);
		} else {
			add(locus.getLine(), locus.getColumn(), type, message);
		}
	}

	private void add(int line, int column, Type type, String message) {
		final Advice item = new Advice(line, column, type, message);
		items.add(item);
		if(type == Type.ERR) {
			this.errorCount++;
		} else if(type == Type.WRN) {
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

	/**
	 * Write the contents to the given path. The directory must exist but the
	 * file will be created if necessary.
	 *
	 * @param logPath
	 *            Path and file name to write; e.g. /my/file.log
	 */
	public void write(Path logPath) {
		try(BufferedWriter writer = Files.newBufferedWriter(logPath, Charset.defaultCharset())) {
			writer.write(DateU.formatDatetimeAirline(new Date()));
			if(path != null) {
				writer.write(" - ");
				writer.write(path);
			}
			writer.write('\n');

			for(final Advice advice : items) {
				writer.write(advice.toString());
				writer.write('\n');
			}
			writer.write(Integer.toString(errorCount));
			writer.write(" error(s), ");
			writer.write(Integer.toString(warningCount));
			writer.write(" warning(s)\n");
		} catch(final IOException x) {
			System.err.format("IOException: %s%n", x);
		}

	}
}
