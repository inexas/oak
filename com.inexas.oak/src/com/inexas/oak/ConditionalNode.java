package com.inexas.oak;

import org.antlr.v4.runtime.ParserRuleContext;

public class ConditionalNode extends ExpressionNode {
	private final boolean isStatic;
	private final DataType type;
	private final ExpressionNode condition, trueValue, falseValue;

	/**
	 * condition ? trueValue : falseValue statement.
	 *
	 * The first expression must be of type boolean, or a compile-time error
	 * occurs.
	 *
	 * The conditional operator may be used to choose between second and third
	 * operands of numeric type, or second and third operands of type boolean,
	 * or second and third operands that are each of either reference type or
	 * the null type. All other cases result in a compile-time error.
	 */
	public ConditionalNode(
			ParserRuleContext context,
			ExpressionNode falseValue,
			ExpressionNode trueValue,
			ExpressionNode condition) {
		super(context);

		this.condition = condition;
		this.trueValue = trueValue;
		this.falseValue = falseValue;

		final DataType trueType = trueValue.getType();
		final DataType falseType = falseValue.getType();
		if(trueType == falseType) {
			type = trueType;
		} else {
			switch(trueType) {
			case integer:
				if(falseType.isNumeric) {
					type = falseType;
				} else {
					throwInvalidTypes();
					type = null;
				}
				break;

			case decimal:
				if(falseType.isNumeric) {
					type = falseType == DataType.integer ? trueType : falseType;
				} else {
					throwInvalidTypes();
					type = null;
				}
				break;

			case precision:
				if(falseType.isNumeric) {
					type = trueType;
				} else {
					throwInvalidTypes();
					type = null;
				}
				break;

			// $CASES-OMITTED$
			default:
				throwInvalidTypes();
				type = null;
				break;
			}
		}

		if(condition.getType() != DataType.bool) {
			throwInvalidTypes();
		}

		isStatic = trueValue.isStatic() && falseValue.isStatic();
	}

	@Override
	public ConstantNode evaluate() {
		final ConstantNode result;

		final boolean isTrue = ((Boolean)condition.evaluate().getValue()).booleanValue();
		result = isTrue ? trueValue.evaluate() : falseValue.evaluate();
		result.coerce(type);

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
	void accept(OakVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.enter(this);
		condition.accept(visitor);
		trueValue.accept(visitor);
		falseValue.accept(visitor);
		visitor.exit(this);
		assert visitor.exitEveryNode(this);
	}
}
