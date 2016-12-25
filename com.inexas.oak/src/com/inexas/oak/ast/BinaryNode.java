package com.inexas.oak.ast;

import java.math.*;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.DataType;
import com.inexas.oak.DataType.*;

public class BinaryNode extends ExpressionNode {
	/**
	 * Used when evaluating to promote the data types++
	 */
	private class Converter {
		private String lhs_text, rhs_text;
		private int lhs_z, rhs_z;
		private BigInteger lhs_Z, rhs_Z;
		private float lhs_f, rhs_f;
		private BigDecimal lhs_F, rhs_F;
		private boolean lhsBoolean, rhsBoolean;
		private final Object lhsValue, rhsValue;

		public Converter() {
			lhsValue = lhsNode.evaluate().getValue();
			if(type == DataType.bool && lhsNode.getType() == DataType.bool) {
				final boolean lhsResult = ((Boolean)lhsValue).booleanValue();

				// Check for chance to short circuit if it's OR or AND...
				if(operator == OakLexer.Lor && lhsResult) {
					// true OR <don't care>
					rhsValue = Boolean.TRUE;
				} else if(operator == OakLexer.Land && !lhsResult) {
					// false AND <don't care>
					rhsValue = Boolean.FALSE;
				} else {
					rhsValue = rhsNode.evaluate().getValue();
				}
			} else {
				rhsValue = rhsNode.evaluate().getValue();
			}
			switch(commonType) {
			case z:
				lhs_z = ((Number)lhsValue).intValue();
				rhs_z = ((Number)rhsValue).intValue();
				break;

			case Z:
				lhs_Z = toBigInteger(lhsValue);
				rhs_Z = toBigInteger(rhsValue);
				break;

			case f:
				lhs_f = ((Number)lhsValue).floatValue();
				rhs_f = ((Number)rhsValue).floatValue();
				break;

			case F:
				lhs_F = toBigDecimal(lhsValue);
				rhs_F = toBigDecimal(rhsValue);
				break;

			case bool:
				lhsBoolean = ((Boolean)lhsValue).booleanValue();
				rhsBoolean = ((Boolean)rhsValue).booleanValue();
				break;

			case text:
			case identifier:
			case path:
				lhs_text = (String)lhsValue;
				rhs_text = (String)rhsValue;
				break;

			case datetime:
			case date:
			case time:
			case cardinality:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("Converter: " + type);
			}
		}

		private BigInteger toBigInteger(Object theValue) {
			final BigInteger result;
			if(theValue.getClass() == BigInteger.class) {
				result = (BigInteger)theValue;
			} else {
				result = BigInteger.valueOf(((Integer)theValue).intValue());
			}
			return result;
		}

		private BigDecimal toBigDecimal(Object theValue) {
			final BigDecimal result;
			final Class<?> clazz = theValue.getClass();
			if(clazz == Integer.class) {
				result = BigDecimal.valueOf(((Integer)theValue).intValue());
			} else if(clazz == BigInteger.class) {
				result = new BigDecimal((BigInteger)theValue);
			} else if(clazz == Float.class) {
				result = BigDecimal.valueOf(((Float)theValue).floatValue());
			} else {
				assert clazz == BigDecimal.class;
				result = (BigDecimal)theValue;
			}
			return result;
		}

		/**
		 * Return the result of comparing the two objects using Java's equals().
		 * If either of the objects are zero then a NullPointerException will
		 * probably be thrown depending on the type of objects being tested.
		 */
		boolean areEqual() {
			return lhsValue.equals(rhsValue);
		}
	}

	private final int operator;
	private final ExpressionNode lhsNode, rhsNode;
	/**
	 * The commonType is derived from the types of the left and right operands.
	 * It is not necessarily the same as the 'type'. For example in the binary
	 * node for "3 > 4" the common type is z but the type is boolean.
	 */
	private DataType commonType;

	public BinaryNode(
			ParserRuleContext context,
			int operator,
			ExpressionNode rhsNode,
			ExpressionNode lhsNode) {
		super(context);

		this.operator = operator;
		this.lhsNode = lhsNode;
		this.rhsNode = rhsNode;

		setTypeAndStatic(false);
	}

	@Override
	public ConstantNode evaluate() {
		final ConstantNode result;

		if(type == null) {
			setTypeAndStatic(true);
		}

		final ParserRuleContext lhsContext = lhsNode.context;

		final Converter converter = new Converter();

		switch(operator) {
		case OakLexer.Plus:
			switch(commonType) {
			case z:
				result = new ConstantNode(lhsContext, converter.lhs_z + converter.rhs_z);
				break;
			case Z:
				result = new ConstantNode(lhsContext, converter.lhs_Z.add(converter.rhs_Z));
				break;
			case f:
				result = new ConstantNode(lhsContext, converter.lhs_f + converter.rhs_f);
				break;
			case F:
				result = new ConstantNode(lhsContext, converter.lhs_F.add(converter.rhs_F));
				break;
			case text:
				result = new ConstantNode(lhsContext, converter.lhs_text + converter.rhs_text);
				break;

			case bool:
			case cardinality:
			case date:
			case datetime:
			case identifier:
			case path:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Minus:
			switch(commonType) {
			case z:
				result = new ConstantNode(lhsContext, converter.lhs_z - converter.rhs_z);
				break;
			case Z:
				result = new ConstantNode(lhsContext, converter.lhs_Z.subtract(converter.rhs_Z));
				break;
			case f:
				result = new ConstantNode(lhsContext, converter.lhs_f - converter.rhs_f);
				break;
			case F:
				result = new ConstantNode(lhsContext, converter.lhs_F.subtract(converter.rhs_F));
				break;

			case bool:
			case cardinality:
			case date:
			case datetime:
			case identifier:
			case path:
			case text:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Multiply:
			switch(commonType) {
			case z:
				result = new ConstantNode(lhsContext, converter.lhs_z * converter.rhs_z);
				break;
			case Z:
				result = new ConstantNode(lhsContext, converter.lhs_Z.multiply(converter.rhs_Z));
				break;
			case f:
				result = new ConstantNode(lhsContext, converter.lhs_f * converter.rhs_f);
				break;
			case F:
				result = new ConstantNode(lhsContext, converter.lhs_F.multiply(converter.rhs_F));
				break;

			case bool:
			case cardinality:
			case date:
			case datetime:
			case identifier:
			case path:
			case text:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Divide:
			switch(commonType) {
			case z:
				result = new ConstantNode(lhsContext, converter.lhs_z /
						converter.rhs_z);
				break;
			case Z:
				result = new ConstantNode(lhsContext,
						converter.lhs_Z.divide(converter.rhs_Z));
				break;
			case f:
				result = new ConstantNode(lhsContext, converter.lhs_f /
						converter.rhs_f);
				break;
			case F:
				// !todo There's a bug here if we divide 1 by 3, we need
				// rounding
				result = new ConstantNode(lhsContext,
						converter.lhs_F.divide(converter.rhs_F));
				break;

			case bool:
			case cardinality:
			case date:
			case datetime:
			case identifier:
			case path:
			case text:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Mod:
			if(type == DataType.z) {
				result = new ConstantNode(lhsContext, converter.lhs_z % converter.rhs_z);
			} else {
				result = new ConstantNode(lhsContext, converter.lhs_Z.remainder(converter.rhs_Z));
			}
			break;

		case OakLexer.And:
			if(type == DataType.z) {
				result = new ConstantNode(lhsContext, converter.lhs_z & converter.rhs_z);
			} else {
				assert type == DataType.Z : type.name();
				result = new ConstantNode(lhsContext, converter.lhs_Z.and(converter.rhs_Z));
			}
			break;

		case OakLexer.Or:
			if(type == DataType.z) {
				result = new ConstantNode(lhsContext, converter.lhs_z | converter.rhs_z);
			} else {
				assert type == DataType.Z : type.name();
				result = new ConstantNode(lhsContext, converter.lhs_Z.or(converter.rhs_Z));
			}
			break;

		case OakLexer.Xor:
			switch(commonType) {
			case z:
				result = new ConstantNode(lhsContext, converter.lhs_z ^ converter.rhs_z);
				break;
			case Z:
				result = new ConstantNode(lhsContext, converter.lhs_Z.xor(converter.rhs_Z));
				break;
			case bool:
				result = new ConstantNode(lhsContext, converter.lhsBoolean ^ converter.rhsBoolean);
				break;

			case F:
			case cardinality:
			case date:
			case datetime:
			case f:
			case identifier:
			case path:
			case text:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Shl: {
			ConstantNode tmp = null;
			try {
				if(type == DataType.z) {
					final int n = DataType.getInt(new Integer(converter.rhs_z));
					tmp = new ConstantNode(lhsContext, converter.lhs_z << n);
				} else {
					assert type == DataType.Z : type.name();
					final int n = DataType.getInt(converter.rhs_Z);
					tmp = new ConstantNode(lhsContext, converter.lhs_Z.shiftLeft(n));
				}
			} catch(final OverflowException e) {
				error("Shift to big");
			} catch(NullValueException | TypeMismatchException e) {
				throw new UnexpectedException("evaluate");
			}
			result = tmp;
			break;
		}

		case OakLexer.Shr: {
			ConstantNode tmp = null;
			try {
				if(type == DataType.z) {
					final int n = DataType.getInt(new Integer(converter.rhs_z));
					tmp = new ConstantNode(lhsContext, converter.lhs_z >> n);
				} else {
					assert type == DataType.Z : type.name();
					final int n = DataType.getInt(converter.rhs_Z);
					tmp = new ConstantNode(lhsContext, converter.lhs_Z.shiftRight(n));
				}
			} catch(final OverflowException e) {
				error("Shift to big");
			} catch(NullValueException | TypeMismatchException e) {
				throw new UnexpectedException("evaluate");
			}
			result = tmp;
			break;
		}

		case OakLexer.Usr:
			assert type == DataType.z : type.name();
			result = new ConstantNode(lhsContext, converter.lhs_z >>> converter.rhs_z);
			break;

		case OakLexer.Lt: {
			boolean compare;

			switch(commonType) {
			case z:
				compare = converter.lhs_z < converter.rhs_z;
				break;
			case Z:
				compare = converter.lhs_Z.compareTo(converter.rhs_Z) < 0;
				break;
			case f:
				compare = converter.lhs_f < converter.rhs_f;
				break;
			case F:
				compare = converter.lhs_F.compareTo(converter.rhs_F) < 0;
				break;
			case identifier:
			case path:
			case text:
				compare = converter.lhs_text.compareTo(converter.lhs_text) < 0;
				break;

			case bool:
			case cardinality:
			case date:
			case datetime:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			result = new ConstantNode(lhsContext, compare);
			break;
		}

		case OakLexer.Lte: {
			boolean compare;
			switch(commonType) {
			case z:
				compare = converter.lhs_z <= converter.rhs_z;
				break;
			case Z:
				compare = converter.lhs_Z.compareTo(converter.rhs_Z) <= 0;
				break;
			case f:
				compare = converter.lhs_f <= converter.rhs_f;
				break;
			case F:
				compare = converter.lhs_F.compareTo(converter.rhs_F) <= 0;
				break;
			case identifier:
			case path:
			case text:
				compare = converter.lhs_text.compareTo(converter.lhs_text) <= 0;
				break;

			case bool:
			case cardinality:
			case date:
			case datetime:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			result = new ConstantNode(lhsContext, compare);
			break;
		}

		case OakLexer.Gte: {
			boolean compare;

			switch(commonType) {
			case z:
				compare = converter.lhs_z >= converter.rhs_z;
				break;
			case Z:
				compare = converter.lhs_Z.compareTo(converter.rhs_Z) >= 0;
				break;
			case f:
				compare = converter.lhs_f >= converter.rhs_f;
				break;
			case F:
				compare = converter.lhs_F.compareTo(converter.rhs_F) >= 0;
				break;
			case identifier:
			case path:
			case text:
				compare = converter.lhs_text.compareTo(converter.lhs_text) >= 0;
				break;

			case bool:
			case cardinality:
			case date:
			case datetime:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			result = new ConstantNode(lhsContext, compare);
			break;
		}

		case OakLexer.Gt: {
			boolean compare;
			switch(commonType) {
			case z:
				compare = converter.lhs_z > converter.rhs_z;
				break;
			case Z:
				compare = converter.lhs_Z.compareTo(converter.rhs_Z) > 0;
				break;
			case f:
				compare = converter.lhs_f > converter.rhs_f;
				break;
			case F:
				compare = converter.lhs_F.compareTo(converter.rhs_F) > 0;
				break;
			case identifier:
			case path:
			case text:
				compare = converter.lhs_text.compareTo(converter.lhs_text) > 0;
				break;

			case bool:
			case cardinality:
			case date:
			case datetime:
			case time:
			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			result = new ConstantNode(lhsContext, compare);
			break;
		}

		case OakLexer.Eq: {
			boolean compare;
			switch(commonType) {
			case z:
				compare = converter.lhs_z == converter.rhs_z;
				break;
			case f:
				compare = converter.lhs_f == converter.rhs_f;
				break;
			case Z:
				compare = converter.lhs_Z.equals(converter.rhs_Z);
				break;
			case F:
				// Beware, don't use equals()
				compare = converter.lhs_F.compareTo(converter.rhs_F) == 0;
				break;
			case identifier:
			case path:
			case text:
			case cardinality:
			case date:
			case datetime:
			case time:
			case bool:
				compare = converter.areEqual();
				break;

			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			result = new ConstantNode(lhsContext, compare);
			break;
		}

		case OakLexer.Ne: {
			boolean compare;
			switch(commonType) {
			case z:
				compare = converter.lhs_z != converter.rhs_z;
				break;
			case f:
				compare = converter.lhs_f != converter.rhs_f;
				break;
			case Z:
				compare = !converter.lhs_Z.equals(converter.rhs_Z);
				break;
			case F:
				// Beware, don't use equals()
				compare = converter.lhs_F.compareTo(converter.rhs_F) != 0;
				break;
			case identifier:
			case path:
			case text:
			case cardinality:
			case date:
			case datetime:
			case time:
			case bool:
				compare = !converter.areEqual();
				break;

			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			result = new ConstantNode(lhsContext, compare);
			break;
		}

		case OakLexer.Land:
			result = new ConstantNode(lhsContext, converter.lhsBoolean && converter.rhsBoolean);
			break;

		case OakLexer.Lor:
			result = new ConstantNode(lhsContext, converter.lhsBoolean || converter.rhsBoolean);
			break;

		default:
			throw new RuntimeException("Operator not handled: "
					+ AstToStringVisitor.operatorToString[operator]);
		}

		return result;
	}

	@Override
	public boolean isStatic() {
		return lhsNode.isStatic() && rhsNode.isStatic();
	}

	@Override
	public void accept(AstVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.enter(this);
		lhsNode.accept(visitor);
		visitor.operator(operator);
		rhsNode.accept(visitor);
		visitor.exit(this);
		assert visitor.exitEveryNode(this);
	}

	private void setTypeAndStatic(boolean force) {
		if(force) {
			if(lhsNode.getClass() == SymbolNode.class) {
				lhsNode.evaluate();
			}
			if(rhsNode.getClass() == SymbolNode.class) {
				rhsNode.evaluate();
			}
		}
		final DataType lhsType = lhsNode.type;
		final DataType rhsType = rhsNode.type;

		DataType tmp;
		if(lhsType == null || rhsType == null) {
			tmp = null;
		} else {
			try {
				tmp = lhsNode.getType().getCommnType(rhsNode.getType());
			} catch(final TypeMismatchException e) {
				error(e.getMessage());
				tmp = null;
			}
		}
		type = commonType = tmp;

		if(commonType != null) {
			final boolean ok;
			switch(operator) {
			case OakLexer.Plus:
				ok = commonType.numeric || commonType == DataType.text;
				break;

			case OakLexer.Minus:
			case OakLexer.Multiply:
			case OakLexer.Divide:
				ok = commonType.numeric;
				break;

			case OakLexer.Eq:
			case OakLexer.Ne:
				ok = true;
				type = DataType.bool;
				break;

			case OakLexer.Lt:
			case OakLexer.Lte:
			case OakLexer.Gte:
			case OakLexer.Gt:
				ok = commonType != DataType.cardinality;
				type = DataType.bool;
				break;

			case OakLexer.And:
			case OakLexer.Or:
			case OakLexer.Mod:
			case OakLexer.Shl:
			case OakLexer.Shr:
				ok = commonType == DataType.z || commonType == DataType.Z;
				break;

			case OakLexer.Usr:
				ok = commonType == DataType.z;
				break;

			case OakLexer.Xor:
				ok = commonType == DataType.z || commonType == DataType.Z || commonType == DataType.bool;
				break;

			case OakLexer.Lor:
			case OakLexer.Land:
				ok = commonType == DataType.bool;
				break;

			default:
				throw new RuntimeException("Operator not handled: " + operator);
			}

			if(!ok) {
				throwInvalidTypes();
			}
		}
	}
}
