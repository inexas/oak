package com.inexas.oak.template;


public interface DialectVisitor {
	public class Base implements DialectVisitor {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enter(Dialect dialect) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Dialect dialect) {
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
		public void enter(Member member) {
			// Nothing to do
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Member member) {
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
		 */
		@Override
		public void visit(Constraint constraint) {
			// Nothing to do
		}

	}

	void enter(Dialect dialect);

	void exit(Dialect dialect);

	void enter(Objet object);

	void exit(Objet object);

	void enter(Member member);

	void exit(Member member);

	void enter(Property property);

	void exit(Property property);

	void visit(Constraint constraint);

}
