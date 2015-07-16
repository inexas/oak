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
		 */
		@Override
		public void visit(Constraint constraint) throws OakException {
			// Nothing to do
		}

	}

	/**
	 * @param dialect
	 */
	void enter(DialectNode dialect);

	/**
	 * @param dialect
	 */
	void exit(DialectNode dialect);

	/**
	 * @param object
	 */
	void enter(Objet object);

	/**
	 * @param object
	 */
	void exit(Objet object);

	/**
	 * @param member
	 */
	void visit(Member member);

	/**
	 * @param property
	 */
	void enter(Property property);

	/**
	 * @param property
	 */
	void exit(Property property);

	/**
	 * @param constraint
	 * @throws OakException
	 */
	void visit(Constraint constraint) throws OakException;

}
