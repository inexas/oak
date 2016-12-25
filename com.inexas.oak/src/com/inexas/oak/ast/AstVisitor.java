package com.inexas.oak.ast;

import com.inexas.oak.*;

public interface AstVisitor {
	public class Base implements AstVisitor {
		private boolean tracing = false;
		private String indent = "";

		/**
		 * Enable/disable tracing. Note that asserts must be enabled too.
		 *
		 * @param tracing
		 *            True: tracing will be enabled.
		 */
		public void setTracing(boolean tracing) {
			this.tracing = tracing;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean enterEveryNode(Node node) {
			if(tracing) {
				System.out.println(indent + '>'
						+ node.getClass().getSimpleName()
						+ ':' + (node.getLine() + 1)
						+ ((node instanceof PairNode) ? " " + ((PairNode)node).name : ""));
				indent += ". ";
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean exitEveryNode(Node node) {
			if(tracing) {
				indent = indent.substring(2);
				System.out.println(indent + "<" + node.getClass().getSimpleName());
			}
			return true;
		}

		@Override
		public void enter(Oak oak) {
			// Do nothing
		}

		@Override
		public void exit(Oak oak) {
			// Do nothing
		}

		@Override
		public void enter(ObjectPairNode node) {
			// Do nothing
		}

		@Override
		public void exit(ObjectPairNode node) {
			// Do nothing
		}

		@Override
		public void enter(ObjectNode node) {
			// Do nothing
		}

		@Override
		public void exit(ObjectNode node) {
			// Do nothing
		}

		@Override
		public void enter(ValueArrayPairNode node) {
			// Do nothing
		}

		@Override
		public void exit(ValueArrayPairNode node) {
			// Do nothing
		}

		@Override
		public void enter(ValuePairNode node) {
			// Do nothing
		}

		@Override
		public void exit(ValuePairNode node) {
			// Do nothing
		}

		@Override
		public void delimit() {
			// Do nothing

		}

		@Override
		public void enter(BinaryNode node) {
			// Do nothing
		}

		@Override
		public void operator(int operator) {
			// Do nothing
		}

		@Override
		public void exit(BinaryNode node) {
			// Do nothing
		}

		@Override
		public void enter(ConditionalNode node) {
			// Do nothing
		}

		@Override
		public void exit(ConditionalNode node) {
			// Do nothing
		}

		@Override
		public void visit(ConstantNode node) {
			// Do nothing
		}

		@Override
		public void enter(FunctionNode node) {
			// Do nothing
		}

		@Override
		public void exit(FunctionNode node) {
			// Do nothing
		}

		@Override
		public void enter(UnaryNode node) {
			// Do nothing
		}

		@Override
		public void exit(UnaryNode node) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enter(ObjectArrayPairNode node) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(ObjectArrayPairNode node) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visit(IdentifierNode identifierNode) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visit(SymbolNode symbolNode) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visit(PathNode node) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void visit(CardinalityNode node) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void enter(Expression expression) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Expression expression) {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void startConditionalTrue() {
			// Do nothing
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void startConditionalFalse() {
			// Do nothing
		}

	}

	/**
	 * Called before each Node is visited but only if assertions are enabled
	 *
	 * @param node
	 *            The node to be visited.
	 * @return Always return true so that this can be used with an assert to
	 *         conditionally compile out the call, e.g. "assert
	 *         enterEveryNode(this);". If you don't then it an assertion error
	 *         will be thrown.
	 */
	boolean enterEveryNode(Node node);

	/**
	 * Called after each Node has been visited but only if assertions are
	 * enabled
	 *
	 * @param node
	 *            The visited node.
	 * @return Always returns true so that this can be used with an assert to
	 *         conditionally compile out the call, e.g. "assert
	 *         exitEveryNode(this);". If you don't then it an assertion error
	 *         will be thrown.
	 */
	boolean exitEveryNode(Node node);

	void enter(Oak oak);

	void exit(Oak oak);

	void enter(Expression expression);

	void exit(Expression expression);

	void enter(ObjectPairNode node);

	void exit(ObjectPairNode node);

	void enter(ObjectNode node);

	void exit(ObjectNode node);

	void enter(ValueArrayPairNode node);

	void exit(ValueArrayPairNode node);

	/**
	 * Objects members and array elements are delimited.
	 */
	void delimit();

	// Expression...

	void enter(BinaryNode node);

	void operator(int operator);

	void exit(BinaryNode node);

	void enter(ConditionalNode node);

	void exit(ConditionalNode node);

	void enter(FunctionNode node);

	void exit(FunctionNode node);

	void enter(UnaryNode node);

	void exit(UnaryNode node);

	void enter(ValuePairNode node);

	void exit(ValuePairNode node);

	/**
	 * Encountered: "name [ obj1 { ...}, obj1 {...}, ...]". It will be followed
	 * by one more more ObjectNodes
	 *
	 * @param node
	 *            The Node being entered.
	 */
	void enter(ObjectArrayPairNode node);

	void exit(ObjectArrayPairNode node);

	void visit(PathNode node);

	void visit(IdentifierNode identifierNode);

	void visit(SymbolNode symbolNode);

	void visit(ConstantNode node);

	void visit(CardinalityNode node);

	/**
	 * This is called in a conditional ( boolCondition ? trueValue : falseValue
	 * ) after the condition has been visited but before the true value.
	 */
	void startConditionalTrue();

	/**
	 * This is called in a conditional ( boolCondition ? trueValue : falseValue
	 * ) after the true value has been visited but before the false value.
	 */
	void startConditionalFalse();

}
