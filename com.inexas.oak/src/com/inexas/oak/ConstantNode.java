package com.inexas.oak;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.StringEscapeUtils;
import com.inexas.exception.*;

public class ConstantNode extends ExpressionNode {
	public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("y/M/d HH:mm:ss");
	public final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("y/M/d");
	public final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	public final DataType type;
	private Object value;

	public static ConstantNode toIntegerConstant(ParserRuleContext context, String text) {

		final ConstantNode result;

		final String noUnderlines = text.replace("_", "");
		if(noUnderlines.length() > 2 && noUnderlines.charAt(0) == '0') {
			switch(noUnderlines.charAt(1)) {
			case 'b':
			case 'B':
				result = new ConstantNode(context, Long.parseLong(noUnderlines.substring(2), 2));
				break;
			case 'x':
			case 'X':
				result = new ConstantNode(context, Long.parseLong(noUnderlines.substring(2), 16));
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

	public static ConstantNode toDate(ParserRuleContext context, String text) {

		final boolean containsDate = text.indexOf('/') > 0;
		final boolean containsTime = text.indexOf(':') > 0;

		final Temporal temporal;
		final DataType dataType;
		if(containsDate) {
			if(containsTime) {
				temporal = LocalDateTime.parse(text, dateTimeFormatter);
				dataType = DataType.datetime;
			} else {
				temporal = LocalDate.parse(text, dateFormatter);
				dataType = DataType.date;
			}
		} else {
			temporal = LocalTime.parse(text, timeFormatter);
			dataType = DataType.time;
		}

		return new ConstantNode(context, temporal, dataType);
	}

	/**
	 * For null only
	 */
	public ConstantNode(ParserRuleContext context) {
		super(context);

		value = null;
		type = DataType.NULL;
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

	public ConstantNode(ParserRuleContext context, BigDecimal value) {
		super(context);

		this.value = value;
		type = DataType.precision;
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
			} else if(expectedType == DataType.precision) {
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
	boolean isStatic() {
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
	void accept(OakVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.visit(this);
		assert visitor.exitEveryNode(this);
	}

	public String getString() {
		if(type != DataType.text) {
			throw new InexasRuntimeException("Wrong data type. Expected string but is: " + type.name());
		}
		return (String)value;
	}

	public Boolean getBoolean() {
		if(type != DataType.bool) {
			throw new InexasRuntimeException("Wrong data type. Expected boolean but is: " + type.name());
		}
		return (Boolean)value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Object getValue() {
		return value;
	}

	/**
	 * @return
	 */
	public Object getInteger() {
		// !todo Implement me
		throw new ImplementMeException();
	}
}
