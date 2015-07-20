package com.inexas.oak;

import com.inexas.oak.advisory.OakException;

public interface DialectVisitor {
	public class Base implements DialectVisitor {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enter(DialectNode dialect) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(DialectNode dialect) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enter(Objet object) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Objet object) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visit(Member member) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enter(Property property) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Property property) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 *
		 * @throws OakException
		 *             Thrown if parsing error encountered.
		 */
		@Override
		public void visit(Constraint constraint) throws OakException {
			// Nothing to do
		}

	}

	void enter(DialectNode dialect);

	void exit(DialectNode dialect);

	void enter(Objet object);

	void exit(Objet object);

	void visit(Member member);

	void enter(Property property);

	void exit(Property property);

	/**
	 * @param constraint
	 *            The Constraint being visited.
	 * @throws OakException
	 *             Thrown if parsing error encountered.
	 */
	void visit(Constraint constraint) throws OakException;

}
