package com.inexas.oak.ast;

import java.math.*;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.*;
import com.inexas.oak.DataType.NullValueException;
import com.inexas.oak.DataType.OverflowException;
import com.inexas.oak.DataType.TypeMismatchException;

public class BinaryNode extends ExpressionNode {
	private class Converter {
		private String lhs_text, rhs_text;
		private long lhs_z, rhs_z;
		private BigInteger lhs_Z, rhs_Z;
		private double lhs_f, rhs_f;
		private BigDecimal lhs_F, rhs_F;
		private boolean lhsBoolean, rhsBoolean;
		private final Object lhsValue, rhsValue;

		public Converter() {
			lhsValue = lhsNode.evaluate().getValue();
			if(commonType == DataType.bool && lhsNode.getType() == DataType.bool) {
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
				lhs_z = ((Number)lhsValue).longValue();
				rhs_z = ((Number)rhsValue).longValue();
				break;

			case Z:
				lhs_Z = toBigInteger(lhsValue);
				rhs_Z = toBigInteger(rhsValue);
				break;

			case f:
				lhs_f = ((Number)lhsValue).doubleValue();
				rhs_f = ((Number)rhsValue).doubleValue();
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
			default:
				throw new UnexpectedException("Converter: " + commonType);
			}
		}

		private BigInteger toBigInteger(Object value) {
			final BigInteger result;
			if(value.getClass() == BigInteger.class) {
				result = (BigInteger)value;
			} else {
				result = BigInteger.valueOf(((Long)value).longValue());
			}
			return result;
		}

		private BigDecimal toBigDecimal(Object value) {
			final BigDecimal result;
			final Class<?> clazz = value.getClass();
			if(clazz == Long.class) {
				result = BigDecimal.valueOf(((Long)value).longValue());
			} else if(clazz == BigInteger.class) {
				result = new BigDecimal((BigInteger)value);
			} else if(clazz == Double.class) {
				result = BigDecimal.valueOf(((Double)value).doubleValue());
			} else {
				assert clazz == BigDecimal.class;
				result = (BigDecimal)value;
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
	 * The common type depends on the operation and operands. For something like
	 * 5z + 6Z then the operands' common type is Z (the broader of the two) and
	 * the returnType will be Z because of the operation.
	 */
	private final DataType commonType;
	/** See commonType's Javadoc */
	private final DataType returnType;

	public BinaryNode(
			ParserRuleContext context,
			int operator,
			ExpressionNode rhsNode,
			ExpressionNode lhsNode) {
		super(context);

		this.operator = operator;
		this.lhsNode = lhsNode;
		this.rhsNode = rhsNode;

		DataType tmp = null;
		try {
			tmp = lhsNode.getType().getCommnType(rhsNode.getType());
		} catch(final TypeMismatchException e) {
			error(e.getMessage());
		}
		commonType = tmp;

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
			tmp = DataType.bool;
			break;

		case OakLexer.Lt:
		case OakLexer.Lte:
		case OakLexer.Gte:
		case OakLexer.Gt:
			ok = commonType != DataType.cardinality;
			tmp = DataType.bool;
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

		returnType = tmp;

		if(!ok) {
			throwInvalidTypes();
		}
	}

	@Override
	public ConstantNode evaluate() {
		final ConstantNode result;

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

			case any:
			case bool:
			case cardinality:
			case date:
			case datetime:
			case identifier:
			case path:
			case time:
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
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Divide:
			switch(commonType) {
			case z:
				result = new ConstantNode(lhsContext, converter.lhs_z / converter.rhs_z);
				break;
			case Z:
				result = new ConstantNode(lhsContext, converter.lhs_Z.divide(converter.rhs_Z));
				break;
			case f:
				result = new ConstantNode(lhsContext, converter.lhs_f / converter.rhs_f);
				break;
			case F:
				result = new ConstantNode(lhsContext, converter.lhs_F.divide(converter.rhs_F));
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
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Mod:
			if(commonType == DataType.z) {
				result = new ConstantNode(lhsContext, converter.lhs_z % converter.rhs_z);
			} else {
				result = new ConstantNode(lhsContext, converter.lhs_Z.remainder(converter.rhs_Z));
			}
			break;

		case OakLexer.And:
			if(commonType == DataType.z) {
				result = new ConstantNode(lhsContext, converter.lhs_z & converter.rhs_z);
			} else {
				assert commonType == DataType.Z : commonType.name();
				result = new ConstantNode(lhsContext, converter.lhs_Z.and(converter.rhs_Z));
			}
			break;

		case OakLexer.Or:
			if(commonType == DataType.z) {
				result = new ConstantNode(lhsContext, converter.lhs_z | converter.rhs_z);
			} else {
				assert commonType == DataType.Z : commonType.name();
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
			case any:
			case cardinality:
			case date:
			case datetime:
			case f:
			case identifier:
			case path:
			case text:
			case time:
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Shl: {
			ConstantNode tmp = null;
			try {
				if(commonType == DataType.z) {
					final int n = DataType.getInt(new Long(converter.rhs_z));
					tmp = new ConstantNode(lhsContext, converter.lhs_z << n);
				} else {
					assert commonType == DataType.Z : commonType.name();
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
				if(commonType == DataType.z) {
					final int n = DataType.getInt(new Long(converter.rhs_z));
					tmp = new ConstantNode(lhsContext, converter.lhs_z >> n);
				} else {
					assert commonType == DataType.Z : commonType.name();
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
			assert commonType == DataType.z : commonType.name();
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

			case any:
			case bool:
			case cardinality:
			case date:
			case datetime:
			case time:
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

			case any:
			case bool:
			case cardinality:
			case date:
			case datetime:
			case time:
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

			case any:
			case bool:
			case cardinality:
			case date:
			case datetime:
			case time:
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

			case any:
			case bool:
			case cardinality:
			case date:
			case datetime:
			case time:
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
				compare = converter.lhs_F.equals(converter.rhs_F);
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
				compare = !converter.lhs_F.equals(converter.rhs_F);
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
	public DataType getType() {
		return returnType;
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

}
