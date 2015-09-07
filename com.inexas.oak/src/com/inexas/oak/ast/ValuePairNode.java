package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.ImplementMeException;
import com.inexas.oak.DataType;
import com.inexas.util.Cardinality;

public class ValuePairNode extends PairNode {
	private final Node node;

	public ValuePairNode(ParserRuleContext context, String name, Node value) {
		super(context, name);
		this.node = value;
		value.parent = this;
	}

	@Override
	public void accept(AstVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.enter(this);
		node.accept(visitor);
		visitor.exit(this);
		assert visitor.exitEveryNode(this);
	}

	public String asString() {
		final String result;

		final Class<?> clazz = node.getClass();
		if(clazz == ConstantNode.class) {
			final ConstantNode constant = (ConstantNode)node;
			result = constant.getTextValue();
		} else if(clazz == ExpressionNode.class) {
			final ConstantNode constant = ((ExpressionNode)node).evaluate();
			result = constant.getTextValue();
		} else if(clazz == PathNode.class) {
			result = ((PathNode)node).toString();
		} else {
			throw new RuntimeException("Invalid node type: " + clazz.getName()
					+ " for " + name);
		}

		return result;
	}

	public String asKey() {
		final String result;
		if(node.getClass() == PathNode.class) {
			result = ((PathNode)node).path;
		} else {
			throw new RuntimeException("Invalid node type: " + node.getClass().getName()
					+ " for " + name);
		}
		return result;
	}

	public Cardinality asCardinality() {
		final Cardinality result;
		if(node.getClass() == CardinalityNode.class) {
			result = ((CardinalityNode)node).cardinality;
		} else {
			throw new RuntimeException("Invalid node for cardinality: " + node.getClass().getName());
		}
		return result;
	}

	public Integer asInteger() {
		// !todo Implement me
		throw new ImplementMeException();
	}

	public Object asAny() {
		return ((ExpressionNode)node).getValue();
	}

	public Boolean asBoolean() {
		final Boolean result;

		final Class<?> clazz = node.getClass();
		if(clazz == ConstantNode.class) {
			final ConstantNode constant = (ConstantNode)node;
			result = constant.getBooleanValue();
		} else if(clazz == ExpressionNode.class) {
			final ConstantNode constant = ((ExpressionNode)node).evaluate();
			result = constant.getBooleanValue();
		} else {
			throw new RuntimeException("Invalid node type: " + clazz.getName());
		}

		return result;
	}

	public ExpressionNode asExpressionNode() {
		final ExpressionNode result;

		final Class<?> clazz = node.getClass();
		if(clazz == ExpressionNode.class) {
			result = (ExpressionNode)node;
		} else {
			throw new RuntimeException("Invalid node type: " + clazz.getName());
		}

		return result;
	}

	public Object asObject() {
		final Object result;

		final Class<?> clazz = node.getClass();
		if(clazz == ExpressionNode.class) {
			result = ((ExpressionNode)node).evaluate().getValue();
		} else if(clazz == ConstantNode.class) {
			result = ((ConstantNode)node).getValue();
		} else {
			// todo Implement me
			throw new ImplementMeException();
			// result = node.getValue();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType getType() {
		return node.getType();
	}

}
