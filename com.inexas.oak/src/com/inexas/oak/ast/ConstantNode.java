package com.inexas.oak.ast;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.StringEscapeUtils;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.Advisory;
import com.inexas.oak.ast.OakParser.LiteralContext;
import com.inexas.oak.path.Path;
import com.inexas.tad.Context;
import com.inexas.util.Cardinality;

public class ConstantNode extends ExpressionNode {
	public final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("y/M/d");
	public final static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	public final static DateTimeFormatter timeFormatterSecs = DateTimeFormatter.ofPattern("HH:mm:ss");
	public final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("y/M/d HH:mm");
	public final static DateTimeFormatter dateTimeFormatterSecs =
			DateTimeFormatter.ofPattern("y/M/d HH:mm:ss");
	public final DataType type;
	private final Object value;

	ConstantNode(ParserRuleContext context) {
		super(context);
		value = null;
		type = DataType.any;
	}

	ConstantNode(ParserRuleContext context, boolean b) {
		super(context);
		value = b ? Boolean.TRUE : Boolean.FALSE;
		type = DataType.bool;
	}

	ConstantNode(ParserRuleContext context, Boolean b) {
		super(context);
		value = b;
		type = DataType.bool;
	}

	ConstantNode(ParserRuleContext context, long value) {
		super(context);
		this.value = new Long(value);
		type = DataType.z;
	}

	ConstantNode(ParserRuleContext context, Long value) {
		super(context);
		this.value = value;
		type = DataType.z;
	}

	ConstantNode(ParserRuleContext context, double value) {
		super(context);
		this.value = new Double(value);
		type = DataType.f;
	}

	ConstantNode(ParserRuleContext context, Double value) {
		super(context);
		this.value = value;
		type = DataType.f;
	}

	ConstantNode(ParserRuleContext context, BigInteger value) {
		super(context);
		this.value = value;
		type = DataType.Z;
	}

	ConstantNode(ParserRuleContext context, BigDecimal value) {
		super(context);
		this.value = value;
		type = DataType.F;
	}

	ConstantNode(ParserRuleContext context, String value) {
		super(context);
		this.value = StringEscapeUtils.unescapeJava(value);
		type = DataType.text;
	}

	ConstantNode(ParserRuleContext context, LocalDateTime value) {
		super(context);
		this.value = value;
		type = DataType.datetime;
	}

	ConstantNode(ParserRuleContext context, LocalDate value) {
		super(context);
		this.value = value;
		type = DataType.date;
	}

	ConstantNode(ParserRuleContext context, LocalTime value) {
		super(context);
		this.value = value;
		type = DataType.time;
	}

	ConstantNode(ParserRuleContext context, Cardinality value) {
		super(context);
		this.value = value;
		type = DataType.text;
	}

	private ConstantNode(ParserRuleContext context, Temporal value, DataType type) {
		super(context);
		this.value = value;
		this.type = type;
	}

	public ConstantNode(LiteralContext context, Path value) {
		super(context);
		this.value = value;
		type = DataType.path;
	}

	@Override
	public DataType getType() {
		return type;
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public ConstantNode evaluate() {
		return this;
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

	public Cardinality getCardinality() {
		final Cardinality result;
		if(type == DataType.cardinality) {
			result = (Cardinality)value;
		} else {
			error("Wrong data type. Expected cardinality but is: " + type);
			result = null;
		}
		return result;
	}

	public Integer getInteger() {
		final Integer result;
		if(type == DataType.z) {
			result = (Integer)value;
		} else {
			error("Wrong data type. Expected integer but is: " + type);
			result = null;
		}
		return result;
	}

	public BigInteger getBigInteger() {
		final BigInteger result;
		if(type == DataType.Z) {
			result = (BigInteger)value;
		} else {
			error("Wrong data type. Expected INTEGER but is: " + type);
			result = null;
		}
		return result;
	}

	public Double getFloat() {
		final Double result;
		if(type == DataType.z) {
			result = (Double)value;
		} else {
			error("Wrong data type. Expected decimal but is: " + type);
			result = null;
		}
		return result;
	}

	public BigInteger getBigFloat() {
		final BigInteger result;
		if(type == DataType.F) {
			result = (BigInteger)value;
		} else {
			error("Wrong data type. Expected INTEGER but is: " + type);
			result = null;
		}
		return result;
	}

	public String getString() {
		final String result;
		if(type == DataType.text || type == DataType.identifier || type == DataType.path) {
			result = (String)value;
		} else {
			error("Wrong data type. Expected String but is: " + type);
			result = null;
		}
		return result;
	}

	public LocalDateTime getDatetime() {
		final LocalDateTime result;
		if(type == DataType.datetime) {
			result = (LocalDateTime)value;
		} else {
			error("Wrong data type. Expected datetime but is: " + type);
			result = null;
		}
		return result;
	}

	public LocalDate getDate() {
		final LocalDate result;
		if(type == DataType.date) {
			result = (LocalDate)value;
		} else {
			error("Wrong data type. Expected date but is: " + type);
			result = null;
		}
		return result;
	}

	public LocalTime getTime() {
		final LocalTime result;
		if(type == DataType.time) {
			result = (LocalTime)value;
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

	public static Node toIntegerConstant(LiteralContext context, String text) {
		// todo Check to see if we have to remove underlines
		final String noUnderlines = text.replace("_", "");
		final String noPostfix = removeOptionalPostFix(noUnderlines, 'z');
		// todo Check for negative, check for too big perhaps parse to BigInt?
		return new ConstantNode(context, new Long(noPostfix));
	}

	public static Node toBinaryIntegerConstant(LiteralContext context, String text) {
		final String noUnderlines = text.replace("_", "");
		final String noPrefix = noUnderlines.substring(2);
		// todo Check for negative, check for too big perhaps parse to BigInt?
		return new ConstantNode(context, Long.parseLong(noPrefix, 2));
	}

	public static Node toHexIntegerConstant(LiteralContext context, String text) {
		final String noUnderlines = text.replace("_", "");
		final String noPrefix = noUnderlines.substring(2);
		// todo Check for negative, check for too big perhaps parse to BigInt?
		return new ConstantNode(context, Long.parseLong(noPrefix, 16));
	}

	public static Node toBigIntegerConstant(LiteralContext context, String text) {
		final String noUnderlines = text.replace("_", "");
		final String noPostfix = removeOptionalPostFix(noUnderlines, 'Z');
		return new ConstantNode(context, new BigInteger(noPostfix));
	}

	public static Node toFloatingPointConstant(LiteralContext context, String text) {
		final String noUnderlines = text.replace("_", "");
		final String noPostfix = removeOptionalPostFix(noUnderlines, 'f');
		// todo Check for too big perhaps parse to BigDec?
		return new ConstantNode(context, Double.parseDouble(noPostfix));
	}

	public static Node toBigFloatingPointConstant(LiteralContext context, String text) {
		final String noUnderlines = text.replace("_", "");
		final String noPostfix = removeOptionalPostFix(noUnderlines, 'F');
		// todo Check for too big perhaps parse to BigDec?
		return new ConstantNode(context, Double.parseDouble(noPostfix));
	}

	public static Node toTextConstant(LiteralContext context, String text) {
		return new ConstantNode(context, text);
	}

	public static Node toPathConstant(LiteralContext context, String text) {
		return new ConstantNode(context, Path.parse(text));
	}

	public static Node toDate(LiteralContext context, String text) {
		final Temporal temporal = LocalDate.parse(text, dateFormatter);
		return temporal == null ? new ErrorNode(context) : new ConstantNode(context, temporal, DataType.date);
	}

	public static Node toTime(LiteralContext context, String text) {
		final Temporal temporal;

		int colonCount = 0;
		final char[] ca = text.toCharArray();
		for(final char c : ca) {
			if(c == ':') {
				colonCount++;
			}
		}
		if(colonCount == 1) {
			temporal = LocalTime.parse(text, timeFormatter);
		} else if(colonCount == 2) {
			temporal = LocalTime.parse(text, timeFormatterSecs);
		} else {
			error(context, "Can't parse date/time: " + text);
			temporal = null;
		}

		return temporal == null ? new ErrorNode(context) : new ConstantNode(context, temporal, DataType.time);
	}

	public static Node toDateTime(LiteralContext context, String text) {
		// todo Simplify this using toDate and toTime
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

	private static String removeOptionalPostFix(String string, char postFix) {
		final String result;
		final int index = string.length() - 1;
		if(string.charAt(index) == postFix) {
			result = string.substring(0, index);
		} else {
			result = string;
		}
		return result;
	}

}
