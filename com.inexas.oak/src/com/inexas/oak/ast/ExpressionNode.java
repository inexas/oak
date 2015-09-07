package com.inexas.oak.ast;

import java.math.BigDecimal;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.Context;

public abstract class ExpressionNode extends Node {

	public ExpressionNode(ParserRuleContext context) {
		super(context);
	}

	protected static class TypedNumber {
		final DataType type;
		final Number number;

		public TypedNumber(Object object) {
			number = (Number)object;
			if(object instanceof Integer) {
				type = DataType.z;
			} else {
				final String name = object == null ? "null" : object.getClass().getCanonicalName();
				throw new RuntimeException("Not a valid number: " + name);
			}
		}

		public long longValue() {
			return number.longValue();
		}
	}

	protected static class Operands {
		public final boolean isInteger;
		public Long integerLhs, integerRhs;
		public BigDecimal decimalLhs, decimalRhs;

		public Operands(Long lhs, Long rhs) {
			isInteger = true;
			this.integerLhs = lhs;
			this.integerRhs = rhs;
		}

		public Operands(BigDecimal lhs, BigDecimal rhs) {
			isInteger = false;
			this.decimalLhs = lhs;
			this.decimalRhs = rhs;
		}
	}

	@Override
	public abstract DataType getType();

	/**
	 * An expression is static if it always returns the same value. For example:
	 * "1 + 2" will always return the same value, but "today()" will not.
	 *
	 * @return True if this node will always return the same value
	 */
	public abstract boolean isStatic();

	/**
	 * Evaluate the the value of this expression.
	 *
	 * @return The evaluated value of the expression.
	 */
	public abstract ConstantNode evaluate();

	protected void throwInvalidTypes() {
		throw new RuntimeException("Invalid data types: " + toString());
	}

	Object getValue() {
		return evaluate().getValue();
	}

	protected void error(String message) {
		final Advisory advisory = Context.get(Advisory.class);
		advisory.error(this, message);
	}

}
