package com.inexas.oak;

import com.inexas.oak.advisory.OakException;

public interface GrammarVisitor {
	public class Base implements GrammarVisitor {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enter(Grammar grammar) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Grammar grammar) {
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
	 * @param grammar
	 */
	void enter(Grammar grammar);

	/**
	 * @param grammar
	 */
	void exit(Grammar grammar);

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
