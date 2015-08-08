package com.inexas.oak.ast;

import java.math.BigDecimal;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.*;
import com.inexas.oak.*;

/**
 * Unary Plus '+', Minus '-', Logical Complement '~' or Not '!'.
 *
 * Bitwise Complement '~' The type of the operand expression of the unary ~
 * operator must be a primitive integral type, or a compile-time error occurs.
 * Unary numeric promotion is performed on the operand. The type of the unary
 * bitwise complement expression is the promoted type of the operand.
 */
public class UnaryNode extends ExpressionNode {
	// !todo UnaryExpression or UnaryExpressionNode
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
		case OakLexer.Plus:
			if(!(type == DataType.integer || type == DataType.decimal || type == DataType.precision)) {
				throw new InexasRuntimeException("Invalid data type for +/- (should be numeric): "
						+ type);
			}
			break;

		case OakLexer.Comp:
			if(operand.getType() != DataType.integer) {
				throw new InexasRuntimeException("Invalid data type for '~' (should be integer): "
						+ type);
			}
			break;

		case OakLexer.Not:
			if(operand.getType() != DataType.bool) {
				throw new InexasRuntimeException("Invalid data type for '!' (should be boolean): "
						+ type);
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
		case OakLexer.Plus:
			result = valueConstant;
			break;

		case OakLexer.Minus:
			switch(type) {
			case integer:
				result = new ConstantNode(context, -((Long)value).intValue());
				break;

			case decimal:
				result = new ConstantNode(context, -((Number)value).doubleValue());
				break;

			case precision:
				result = new ConstantNode(context, ((BigDecimal)value).negate());
				break;

				// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + type);
			}
			break;

		case OakLexer.Comp:
			result = new ConstantNode(context, ~((Long)value).intValue());
			break;

		case OakLexer.Not:
			result = new ConstantNode(context, !((Boolean)value).booleanValue());
			break;

		default:
			throw new RuntimeException("Invalid operator: " + ToStringVisitor.operatorToString[operator]);
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
	public void accept(OakAstVisitor visitor) {
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
