package com.inexas.oak.advisory;

import com.inexas.exception.NotImplementedException;

public interface Locus {

	public class Base implements Locus {
		private int line;
		private int column;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void setSourcePosition(int line, int column) {
			this.line = line;
			this.column = column;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getLine() {
			return line;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getColumn() {
			return column;
		}
	}

	/**
	 * Set the position in the source code of the start of this object
	 *
	 * @param line
	 *            The line number, one-based. Zero indicates that the line
	 *            number is unknown.
	 * @param column
	 *            The column number, one-based. Zero indicates that the column
	 *            number is unknown.
	 */
	void setSourcePosition(int line, int column) throws NotImplementedException;

	/**
	 * @return The first line (one-based) that this token occupied in the
	 *         source. Zero indicates that the column number is unknown.
	 */
	int getLine();

	/**
	 * @return The first column (one-based) that this token occupied in the
	 *         source. Zero indicates that the column number is unknown.
	 */
	int getColumn();

}
