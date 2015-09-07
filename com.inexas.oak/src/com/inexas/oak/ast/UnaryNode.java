package com.inexas.oak.ast;

import java.math.*;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.DataType;

/**
 * Unary Minus '-', Logical Complement '~' or Not '!'.
 *
 * Bitwise Complement '~' The type of the operand expression of the unary ~
 * operator must be a primitive integral type, or a compile-time error occurs.
 * Unary numeric promotion is performed on the operand. The type of the unary
 * bitwise complement expression is the promoted type of the operand.
 */
public class UnaryNode extends ExpressionNode {
	private final int operator;
	private final ExpressionNode operand;
	private final DataType type;
	private final boolean isStatic;

	public UnaryNode(ParserRuleContext context, int operator, ExpressionNode operand) {
		super(context);

		this.operator = operator;
		this.operand = operand;
		type = operand.getType();

		switch(operator) {
		case OakLexer.Minus:
			if(!type.numeric) {
				error("Invalid data type, should be numeric: " + type);
			}
			break;

		case OakLexer.Comp:
			if(type != DataType.z) {
				error("Invalid data type for '~' (should be integer): " + type);
			}
			break;

		case OakLexer.Not:
			if(type != DataType.bool) {
				error("Invalid data type for '!' (should be boolean): " + type);
			}
			break;

		default:
			throw new UnexpectedException("UnaryNode: " + operator);
		}
		isStatic = operand.isStatic();
	}

	@Override
	public ConstantNode evaluate() {
		final ConstantNode result;

		final ConstantNode valueConstant = operand.evaluate();
		final Object value = valueConstant.getValue();
		switch(operator) {
		case OakLexer.Minus:
			switch(type) {
			case z:
				result = new ConstantNode(context, -((Long)value).intValue());
				break;

			case Z:
				result = new ConstantNode(context, ((BigInteger)value).negate());
				break;

			case f:
				result = new ConstantNode(context, -((Number)value).doubleValue());
				break;
			case F:
				result = new ConstantNode(context, ((BigDecimal)value).negate());
				break;

			case any:
			case bool:
			case cardinality:
			case date:
			case datetime:
			case identifier:
			case path:
			case text:
			case time:
			default:
				throw new UnexpectedException("evaluate: " + type);
			}
			break;

		case OakLexer.Comp:
			if(type == DataType.z) {
				result = new ConstantNode(context, ~((Long)value).longValue());
			} else {
				// There's already an error in the advisory from the ctor
				result = new ConstantNode(context, 0);
			}
			break;

		case OakLexer.Not:
			result = new ConstantNode(context, !((Boolean)value).booleanValue());
			break;

		default:
			throw new RuntimeException("Invalid operator: " + AstToStringVisitor.operatorToString[operator]);
		}

		return result;
	}

	@Override
	public DataType getType() {
		return type;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public void accept(AstVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.enter(this);
		operand.accept(visitor);
		visitor.exit(this);
		assert visitor.exitEveryNode(this);
	}

	public int getOperator() {
		return operator;
	}

}
