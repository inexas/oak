package com.inexas.oak.ast;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.StringEscapeUtils;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.Context;

public class ConstantNode extends ExpressionNode {
	public final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("y/M/d");
	public final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	public final static DateTimeFormatter timeFormatterSecs = DateTimeFormatter.ofPattern("HH:mm:ss");
	public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("y/M/d HH:mm");
	public final static DateTimeFormatter dateTimeFormatterSecs =
			DateTimeFormatter.ofPattern("y/M/d HH:mm:ss");
	public final DataType type;
	private Object value;

	public static ConstantNode toIntegerConstant(ParserRuleContext context, String text) {

		final ConstantNode result;

		final String noUnderlines = text.replace("_", "");
		if(noUnderlines.length() > 2 && noUnderlines.charAt(0) == '0') {
			final String number = noUnderlines.substring(2);
			switch(noUnderlines.charAt(1)) {
			case 'b':
				result = new ConstantNode(context, Long.parseLong(number, 2));
				break;
			case 'B':
				result = new ConstantNode(context, new BigInteger(number, 2));
				break;
			case 'x':
				result = new ConstantNode(context, Long.parseLong(number, 16));
				break;
			case 'X':
				result = new ConstantNode(context, new BigInteger(number, 16));
				break;
			default:
				result = new ConstantNode(context, new Long(noUnderlines));
				break;
			}
		} else {
			result = new ConstantNode(context, new Long(noUnderlines));
		}

		return result;
	}

	public static ConstantNode toDecimalConstant(ParserRuleContext context, String text) {
		final ConstantNode result;

		final String noUnderlines = text.replace("_", "");
		result = new ConstantNode(context, new Double(noUnderlines));

		return result;
	}

	public static Node toDate(ParserRuleContext context, String text) {

		final boolean containsDate = text.indexOf('/') > 0;
		int colonCount = 0;
		final char[] ca = text.toCharArray();
		for(final char c : ca) {
			if(c == ':') {
				colonCount++;
			}
		}

		final Temporal temporal;
		final DataType dataType;
		if(containsDate) {
			if(colonCount == 0) {
				dataType = DataType.date;
				temporal = LocalDate.parse(text, dateFormatter);
			} else {
				dataType = DataType.datetime;
				if(colonCount == 1) {
					temporal = LocalDateTime.parse(text, dateTimeFormatter);
				} else if(colonCount == 2) {
					temporal = LocalDateTime.parse(text, dateTimeFormatterSecs);
				} else {
					error(context, "Can't parse date/time: " + text);
					temporal = null;
				}
			}
		} else {
			dataType = DataType.time;
			if(colonCount == 1) {
				temporal = LocalTime.parse(text, timeFormatter);
			} else if(colonCount == 2) {
				temporal = LocalTime.parse(text, timeFormatterSecs);
			} else {
				error(context, "Can't parse date/time: " + text);
				temporal = null;
			}
		}

		return temporal == null ? new ErrorNode(context) : new ConstantNode(context, temporal, dataType);
	}

	/**
	 * For null only
	 *
	 * @param context
	 *            The current context.
	 */
	public ConstantNode(ParserRuleContext context) {
		super(context);
		value = null;
		type = DataType.any;
	}

	public ConstantNode(ParserRuleContext context, boolean b) {
		super(context);
		value = b ? Boolean.TRUE : Boolean.FALSE;
		type = DataType.bool;
	}

	public ConstantNode(ParserRuleContext context, Boolean b) {
		super(context);
		value = b;
		type = DataType.bool;
	}

	public ConstantNode(ParserRuleContext context, long value) {
		super(context);
		this.value = new Long(value);
		type = DataType.integer;
	}

	public ConstantNode(ParserRuleContext context, Long value) {
		super(context);
		this.value = value;
		type = DataType.integer;
	}

	public ConstantNode(ParserRuleContext context, double value) {
		super(context);
		this.value = new Double(value);
		type = DataType.decimal;
	}

	public ConstantNode(ParserRuleContext context, Double value) {
		super(context);
		this.value = value;
		type = DataType.decimal;
	}

	public ConstantNode(ParserRuleContext context, BigInteger value) {
		super(context);
		this.value = value;
		type = DataType.INTEGER;
	}

	public ConstantNode(ParserRuleContext context, BigDecimal value) {
		super(context);
		this.value = value;
		type = DataType.DECIMAL;
	}

	public ConstantNode(ParserRuleContext context, String value) {
		super(context);
		this.value = StringEscapeUtils.unescapeJava(value);
		type = DataType.text;
	}

	public ConstantNode(ParserRuleContext context, Date value) {
		super(context);
		this.value = value;
		type = DataType.text;
	}

	public ConstantNode(ParserRuleContext context, Temporal value, DataType type) {
		super(context);
		this.value = value;
		this.type = type;
	}

	@Override
	public DataType getType() {
		return type;
	}

	public void coerce(DataType expectedType) {
		if(type.isNumeric && type != expectedType) {
			if(expectedType == DataType.decimal) {
				value = new Double(((Integer)value).intValue());
			} else if(expectedType == DataType.INTEGER) {
				if(type == DataType.integer) {
					value = new BigDecimal(((Integer)value).intValue());
				} else if(type == DataType.decimal) {
					value = new BigDecimal(((Double)value).doubleValue());
				} else {
					throw new UnexpectedException("Coerce  " + type + " to " + expectedType);
				}
			} else {
				throw new UnexpectedException("Coerce  " + type + " to " + expectedType);
			}
		}
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public ConstantNode evaluate() {
		return this;
	}

	public static ConstantNode toPrecisionConstant(ParserRuleContext context, String text) {
		final ConstantNode result;
		final String noUnderlines = text.replace("_", "");
		result = new ConstantNode(context, new BigDecimal(noUnderlines.substring(2)));
		return result;
	}

	@Override
	public void accept(AstVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.visit(this);
		assert visitor.exitEveryNode(this);
	}

	public String getTextValue() {
		final String result;
		if(type == DataType.text) {
			result = (String)value;
		} else {
			error("Wrong data type. Expected string but is: " + type);
			result = null;
		}
		return result;
	}

	public String getIdentifierValue() {
		final String result;
		if(type == DataType.identifier) {
			result = (String)value;
		} else {
			error("Wrong data type. Expected identifier but is: " + type);
			result = null;
		}
		return result;
	}

	public String getPathValue() {
		final String result;
		if(type == DataType.identifier || type == DataType.path) {
			result = (String)value;
		} else {
			error("Wrong data type. Expected path but is: " + type);
			result = null;
		}
		return result;
	}

	public Boolean getBooleanValue() {
		final Boolean result;
		if(type == DataType.bool) {
			result = (Boolean)value;
		} else {
			error("Wrong data type. Expected boolean but is: " + type);
			result = null;
		}
		return result;
	}

	public Integer getInteger() {
		final Integer result;
		if(type == DataType.integer) {
			result = (Integer)value;
		} else {
			error("Wrong data type. Expected integer but is: " + type);
			result = null;
		}
		return result;
	}

	public BigInteger getBigInteger() {
		final BigInteger result;
		if(type == DataType.INTEGER) {
			result = (BigInteger)value;
		} else {
			error("Wrong data type. Expected INTEGER but is: " + type);
			result = null;
		}
		return result;
	}

	public Double getDecimal() {
		final Double result;
		if(type == DataType.integer) {
			result = (Double)value;
		} else {
			error("Wrong data type. Expected decimal but is: " + type);
			result = null;
		}
		return result;
	}

	public BigInteger getBigDecimal() {
		final BigInteger result;
		if(type == DataType.DECIMAL) {
			result = (BigInteger)value;
		} else {
			error("Wrong data type. Expected INTEGER but is: " + type);
			result = null;
		}
		return result;
	}

	public Date getDatetime() {
		final Date result;
		if(type == DataType.datetime) {
			result = (Date)value;
		} else {
			error("Wrong data type. Expected datetime but is: " + type);
			result = null;
		}
		return result;
	}

	public Date getDate() {
		final Date result;
		if(type == DataType.date) {
			result = (Date)value;
		} else {
			error("Wrong data type. Expected date but is: " + type);
			result = null;
		}
		return result;
	}

	public Date getTime() {
		final Date result;
		if(type == DataType.time) {
			result = (Date)value;
		} else {
			error("Wrong data type. Expected time but is: " + type);
			result = null;
		}
		return result;
	}

	@Override
	public Object getValue() {
		return value;
	}

	private static void error(ParserRuleContext context, String message) {
		final Advisory advisory = Context.get(Advisory.class);
		final Token token = context.getStart();
		advisory.error(token.getStartIndex(), token.getCharPositionInLine(), message);
	}

	/**
	 * @param string
	 */
	private void error(String message) {
		final Advisory advisory = Context.get(Advisory.class);
		advisory.error(this, message);
	}
}
