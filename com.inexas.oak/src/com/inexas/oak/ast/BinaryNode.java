package com.inexas.oak.ast;

import java.math.*;
import java.util.Date;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.DataType;

public class BinaryNode extends ExpressionNode {
	private class Converter {
		private String lhsString, rhsString;
		private int lhsInteger, rhsInteger;
		private BigInteger lhsBigInteger, rhsBigInteger;
		private double lhsDecimal, rhsDecimal;
		// !todo Handle BigDecimal throughout file
		private BigDecimal lhsBigDecimal, rhsBigDecimal;
		private boolean lhsBoolean, rhsBoolean;
		private Date lhsDate, rhsDate;

		public Converter() {
			final Object lhsValue = lhsNode.evaluate().getValue();
			final Object rhsValue;
			if(resultType == DataType.bool && lhsNode.getType() == DataType.bool) {
				// Short circuiting for OR and AND...
				final boolean lhsResult = ((Boolean)lhsValue).booleanValue();
				if(operator == OakLexer.Or && lhsResult) {
					// true OR <don't care>
					rhsValue = Boolean.TRUE;
				} else if(operator == OakLexer.And && !lhsResult) {
					// false AND <don't care>
					rhsValue = Boolean.FALSE;
				} else {
					rhsValue = rhsNode.evaluate().getValue();
				}
			} else {
				rhsValue = rhsNode.evaluate().getValue();
			}
			switch(commonType) {
			case bool:
				lhsBoolean = ((Boolean)lhsValue).booleanValue();
				rhsBoolean = ((Boolean)rhsValue).booleanValue();
				break;

			case date:
				lhsDate = (Date)lhsValue;
				rhsDate = (Date)rhsValue;
				break;

			case integer:
				lhsInteger = ((Long)lhsValue).intValue();
				rhsInteger = ((Long)rhsValue).intValue();
				break;

			case INTEGER:
				lhsBigInteger = toBigInteger(lhsValue);
				rhsBigInteger = toBigInteger(lhsValue);
				break;

			case decimal:
				lhsDecimal = ((Number)lhsValue).doubleValue();
				rhsDecimal = ((Number)rhsValue).doubleValue();
				break;

			case DECIMAL:
				lhsBigDecimal = toBigDecimal(lhsValue);
				rhsBigDecimal = toBigDecimal(lhsValue);
				break;

			case text:
				lhsString = lhsValue == null ? null : lhsValue.toString();
				rhsString = rhsValue == null ? null : rhsValue.toString();
				break;

			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("Converter: " + resultType);
			}
		}

		private BigInteger toBigInteger(Object value) {
			final BigInteger result;

			final Class<?> clazz = value.getClass();
			if(clazz == Integer.class) {
				result = BigInteger.valueOf(((Integer)value).intValue());
			} else if(clazz == Double.class) {
				// ?todo Warn on rounding
				result = BigInteger.valueOf(((Double)value).intValue());
			} else {
				assert clazz == BigInteger.class;
				result = (BigInteger)value;
			}

			return result;
		}

		private BigDecimal toBigDecimal(Object value) {
			final BigDecimal result;

			final Class<?> clazz = value.getClass();
			if(clazz == Integer.class) {
				result = BigDecimal.valueOf(((Integer)value).intValue());
			} else if(clazz == Double.class) {
				result = BigDecimal.valueOf(((Double)value).intValue());
			} else if(clazz == BigInteger.class) {
				result = new BigDecimal((BigInteger)value);
			} else {
				assert clazz == BigDecimal.class;
				result = (BigDecimal)value;
			}

			return result;
		}
	}

	private final int operator;
	private final ExpressionNode lhsNode, rhsNode;
	private final DataType resultType, commonType;

	public BinaryNode(
			ParserRuleContext context,
			int operator,
			ExpressionNode rhsNode,
			ExpressionNode lhsNode) {
		super(context);

		assert DataType.integer.bit == 0x0001 && DataType.INTEGER.bit == 0x0002
				&& DataType.decimal.bit == 0x0004 && DataType.DECIMAL.bit == 0x0008;

		this.operator = operator;
		this.lhsNode = lhsNode;
		this.rhsNode = rhsNode;
		final DataType lhsType = lhsNode.getType();
		final DataType rhsType = rhsNode.getType();
		final int lhsTypeBit = lhsType.bit;
		final int rhsTypeBit = rhsType.bit;
		final int combinedType = lhsTypeBit | rhsTypeBit;

		if(combinedType <= DataType.DECIMAL.bit) {
			// Both numeric
			commonType = lhsTypeBit > rhsTypeBit ? lhsType : rhsType;
		} else if(lhsTypeBit == rhsTypeBit) {
			// Both the same type
			commonType = lhsType;
		} else {
			throwInvalidTypes();
			commonType = null;
		}

		// Figure out what data type this node is...
		switch(operator) {
		case OakLexer.Plus:
			if(combinedType == DataType.text.bit) {
				// Concatenate strings
				resultType = DataType.text;
			} else if(combinedType <= DataType.DECIMAL.bit) {
				// Need number (x) number,..
				resultType = commonType;
			} else {
				throwInvalidTypes();
				resultType = null;
			}
			break;

		case OakLexer.Minus:
		case OakLexer.Multiply:
		case OakLexer.Divide:
			if(combinedType <= DataType.DECIMAL.bit) {
				// Need number (x) number,..
				resultType = commonType;
			} else {
				throwInvalidTypes();
				resultType = null;
			}
			break;

		case OakLexer.Eq:
		case OakLexer.Ne:
			if((combinedType <= DataType.DECIMAL.bit)
					|| combinedType == DataType.text.bit
					|| combinedType == DataType.bool.bit) {
				resultType = DataType.bool;
			} else {
				throwInvalidTypes();
				resultType = null;
			}
			break;

		case OakLexer.Lt:
		case OakLexer.Lte:
		case OakLexer.Gte:
		case OakLexer.Gt:
			if((combinedType == DataType.text.bit) || (combinedType <= DataType.DECIMAL.bit)) {
				// Both strings or both numbers
				resultType = DataType.bool;
			} else {
				throwInvalidTypes();
				resultType = null;
			}
			break;

		case OakLexer.And:
		case OakLexer.Or:
		case OakLexer.Mod:
		case OakLexer.Shl:
		case OakLexer.Shr:
		case OakLexer.Usr:
			// Need integer (x) integer,..
			if(combinedType != 1) {
				throwInvalidTypes();
			}
			resultType = commonType;
			break;

		case OakLexer.Xor:
			if(combinedType == 1) {
				// integer (x) integer,..
				resultType = DataType.integer;
			} else if(combinedType == DataType.bool.bit) {
				// boolean (x) boolean,..
				resultType = DataType.bool;
			} else {
				throwInvalidTypes();
				resultType = null;
			}
			break;

		case OakLexer.Lor:
		case OakLexer.Land:
			// Need boolean (x) boolean,..
			if(combinedType == DataType.bool.bit) {
				resultType = DataType.bool;
			} else {
				throwInvalidTypes();
				resultType = null;
			}
			break;

		default:
			throw new RuntimeException("Operator not handled: " + operator);
		}
	}

	@Override
	public ConstantNode evaluate() {
		final ConstantNode result;

		final Converter converter = new Converter();
		final ParserRuleContext lhsContext = lhsNode.context;

		switch(operator) {
		case OakLexer.Plus:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger + converter.rhsInteger);
				break;
			case INTEGER:
				result = new ConstantNode(lhsContext, converter.lhsBigInteger.add(converter.rhsBigInteger));
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal + converter.rhsDecimal);
				break;
			case DECIMAL:
				result = new ConstantNode(lhsContext, converter.lhsBigDecimal.add(converter.rhsBigDecimal));
				break;
			case text:
				result = new ConstantNode(lhsContext, converter.lhsString + converter.rhsString);
				break;
			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Minus:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger - converter.rhsInteger);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext,
						converter.lhsBigInteger.subtract(converter.rhsBigInteger));
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal - converter.rhsDecimal);
				break;
			case DECIMAL:
				result = new ConstantNode(
						lhsContext,
						converter.lhsBigDecimal.subtract(converter.rhsBigDecimal));
				break;
			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Multiply:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger * converter.rhsInteger);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext,
						converter.lhsBigInteger.multiply(converter.rhsBigInteger));
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal * converter.rhsDecimal);
				break;
			case DECIMAL:
				result = new ConstantNode(
						lhsContext,
						converter.lhsBigDecimal.multiply(converter.rhsBigDecimal));
				break;
			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Divide:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger / converter.rhsInteger);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext, converter.lhsBigInteger.divide(converter.rhsBigInteger));
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal / converter.rhsDecimal);
				break;
			case DECIMAL:
				result = new ConstantNode(
						lhsContext, converter.lhsBigDecimal.divide(converter.rhsBigDecimal));
				break;
			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Mod:
			result = new ConstantNode(lhsContext, converter.lhsInteger % converter.rhsInteger);
			break;

		case OakLexer.And:
			result = new ConstantNode(lhsContext, converter.lhsInteger & converter.rhsInteger);
			break;

		case OakLexer.Or:
			result = new ConstantNode(lhsContext, converter.lhsInteger | converter.rhsInteger);
			break;

		case OakLexer.Xor:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger ^ converter.rhsInteger);
				break;
			case bool:
				result = new ConstantNode(lhsContext, converter.lhsBoolean ^ converter.rhsBoolean);
				break;
			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Shl:
			result = new ConstantNode(lhsContext, converter.lhsInteger << converter.rhsInteger);
			break;

		case OakLexer.Shr:
			result = new ConstantNode(lhsContext, converter.lhsInteger >> converter.rhsInteger);
			break;

		case OakLexer.Usr:
			result = new ConstantNode(lhsContext, converter.lhsInteger >>> converter.rhsInteger);
			break;

		case OakLexer.Lt:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger < converter.rhsInteger);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext,
						converter.lhsBigInteger.compareTo(converter.rhsBigInteger) < 0);
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal < converter.rhsDecimal);
				break;
			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Lte:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger <= converter.rhsInteger);
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal <= converter.rhsDecimal);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext,
						converter.lhsBigInteger.compareTo(converter.rhsBigInteger) <= 0);
				break;
				// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Gte:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger >= converter.rhsInteger);
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal >= converter.rhsDecimal);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext,
						converter.lhsBigInteger.compareTo(converter.rhsBigInteger) >= 0);
				break;
				// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Gt:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger > converter.rhsInteger);
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal > converter.rhsDecimal);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext,
						converter.lhsBigInteger.compareTo(converter.rhsBigInteger) > 0);
				break;
				// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Eq:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger == converter.rhsInteger);
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal == converter.rhsDecimal);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext, converter.lhsBigInteger.equals(converter.rhsBigInteger));
				break;
			case bool:
				result = new ConstantNode(lhsContext, converter.lhsBoolean == converter.rhsBoolean);
				break;
			case date:
				result = new ConstantNode(lhsContext, converter.lhsDate.equals(converter.rhsDate));
				break;
			case text:
				result = new ConstantNode(lhsContext, converter.lhsString.equals(converter.rhsString));
				break;
				// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

		case OakLexer.Ne:
			switch(commonType) {
			case integer:
				result = new ConstantNode(lhsContext, converter.lhsInteger != converter.rhsInteger);
				break;
			case decimal:
				result = new ConstantNode(lhsContext, converter.lhsDecimal != converter.rhsDecimal);
				break;
			case INTEGER:
				result = new ConstantNode(
						lhsContext,
						!converter.lhsBigInteger.equals(converter.rhsBigInteger));
				break;
			case bool:
				result = new ConstantNode(lhsContext, converter.lhsBoolean != converter.rhsBoolean);
				break;
			case date:
				result = new ConstantNode(lhsContext, !converter.lhsDate.equals(converter.rhsDate));
				break;
			case text:
				result = new ConstantNode(lhsContext, !converter.lhsString.equals(converter.rhsString));
				break;
				// $CASES-OMITTED$
			default:
				throw new UnexpectedException("evaluate: " + operator);
			}
			break;

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

		result.coerce(resultType);

		return result;
	}

	@Override
	public DataType getType() {
		return resultType;
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
