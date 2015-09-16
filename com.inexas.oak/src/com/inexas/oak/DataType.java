package com.inexas.oak;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.advisory.Advisory;
import com.inexas.oak.path.*;
import com.inexas.tad.Context;
import com.inexas.util.*;

public enum DataType {
	// @formatter:off
	// Keep z first and the order: z, Z, f, F for getCommonType()
	z(				true,	false,	Long.class),
	Z(				true,	false,	BigInteger.class),
	f(				true,	false,	Double.class),
	F(				true,	false,	BigDecimal.class),
	text(			false,	false,	String.class),
	identifier(		false,	false,	Identifier.class),
	path(			false,	false,	Path.class),
	bool(			false,	false,	Boolean.class),
	cardinality(	false,	false,	Cardinality.class),
	datetime(		false,	true,	LocalDateTime.class),
	date(			false,	true,	LocalDate.class),
	time(			false,	true,	LocalTime.class),
	any(			false,	false,	Object.class);

	// @formatter:on

	private final static BigDecimal BigDecimal_MIN_FLOAT = new BigDecimal(String.valueOf(-Float.MAX_VALUE));
	private final static BigDecimal BigDecimal_MAX_FLOAT = new BigDecimal(String.valueOf(Float.MAX_VALUE));

	private final static BigDecimal BigDecimal_MIN_DOUBLE = new BigDecimal(String.valueOf(-Double.MAX_VALUE));
	private final static BigDecimal BigDecimal_MAX_DOUBLE = new BigDecimal(String.valueOf(Double.MAX_VALUE));

	public final boolean numeric;
	public final boolean temporal;
	public final Class<?> javaClass;
	private final static Map<Class<?>, DataType> javaToDataType = new HashMap<>();

	/**
	 * This class is thrown if an attempt is made to convert a null to a
	 * primitive type.
	 */
	public static class NullValueException extends RuntimeException {
		private static final long serialVersionUID = -7248210736043642035L;

		public NullValueException() {
			super("Null cannot be converted to a Java primitive type");
		}
	}

	/**
	 * This is thrown if an attempt is made to convert a number to a data type
	 * that cannot properly represent it. For example if a minimum or maximum
	 * value is exceeded, or trying to convert 1.1 to an integer (the decimal
	 * part would be lost).
	 */
	public static class OverflowException extends RuntimeException {
		private static final long serialVersionUID = -4962691443844037348L;

		public OverflowException(Number n, Class<?> expectedClass) {
			super("Overflow exception, "
					+ n.toString() + " cannot be converted to: " + expectedClass.getSimpleName());
		}
	}

	/**
	 * The actual data type cannot be converted to the expected type. For
	 * example a datetime cannot be created to a boolean.
	 */
	public static class TypeMismatchException extends RuntimeException {
		private static final long serialVersionUID = 5182463621882188013L;

		TypeMismatchException(String message) {
			super(message);
		}

		TypeMismatchException(Class<? extends Object> got, Class<?> expected) {
			super("Cannot convert " + got.getSimpleName()
					+ " to  " + expected.getSimpleName());
		}
	}

	private DataType(boolean numeric, boolean temporal, Class<?> javaClass) {
		this.numeric = numeric;
		this.temporal = temporal;
		this.javaClass = javaClass;
	}

	/**
	 * Can't override valueOf() so uUse this method instead as it treats boolean
	 * properly. If an error is detected the Advisory is updated.
	 *
	 * @param key
	 *            The key to look up.
	 * @return The DataType associated with the given key.
	 */
	public static DataType valueOfCorrected(String key) {
		DataType result;

		try {
			if("boolean".equals(key)) {
				result = bool;
			} else {
				result = DataType.valueOf(key);
			}
		} catch(final IllegalArgumentException e) {
			// todo Perhaps throw the catch when the Locus is known?
			final Advisory advisory = Context.get(Advisory.class);
			advisory.error("Invalid data type: " + key);
			result = any;
		}

		return result;
	}

	/**
	 * Return the DataType corresponding to a given Java type. For example
	 * 'text' is returned given String.class.
	 *
	 * @param clazz
	 *            The class to look up.
	 * @return The associated class or null if not found.
	 */
	@Nullable
	public static DataType getDataType(Class<?> clazz) {
		/*
		 * This has to be done just-in-time as the javaToDataType is static and
		 * can't be loaded as the enum is created.
		 */
		if(javaToDataType.isEmpty()) {
			for(final DataType dataType : DataType.values()) {
				javaToDataType.put(dataType.javaClass, dataType);
			}
		}
		return javaToDataType.get(clazz);
	}

	/**
	 * @return Return the Java type corresponding to this DataType.
	 */
	public Class<?> getJavaClass() {
		return javaClass;
	}

	/**
	 * Given this type and another, find the 'broadest' type
	 *
	 * @param rhs
	 *            The other type.
	 * @return The broadest type
	 * @throws TypeMismatchException
	 *             Data does not match expected data type.
	 */
	public DataType getCommnType(DataType rhs) throws TypeMismatchException {
		final DataType result;

		result = ordinal() > rhs.ordinal() ? this : rhs;
		if(numeric || rhs.numeric) {
			// One numeric then both must be..
			if(result.ordinal() > F.ordinal()) {
				throw new TypeMismatchException("Either both must be numeric or neither");
			}
		} else {
			// Neither numeric then both must be the same
			if(ordinal() != rhs.ordinal()) {
				throw new TypeMismatchException("Both types must be the same for non-numerics");
			}
		}

		return result;
	}

	public static int getInt(Object value)
			throws NullValueException, TypeMismatchException, OverflowException {
		final Integer result;
		if(value == null) {
			throw new NullValueException();
		}
		result = convert(value, Integer.class);
		return result.intValue();
	}

	public static long getLong(Object value)
			throws NullValueException, TypeMismatchException, OverflowException {
		final Long result;
		if(value == null) {
			throw new NullValueException();
		}
		result = convert(value, Long.class);
		return result.longValue();
	}

	public static float getFloat(Object value)
			throws NullValueException, TypeMismatchException, OverflowException {
		final Float result;
		if(value == null) {
			throw new NullValueException();
		}
		result = convert(value, Float.class);
		return result.floatValue();
	}

	public static double getDouble(Object value)
			throws NullValueException, TypeMismatchException, OverflowException {
		final Double result;
		if(value == null) {
			throw new NullValueException();
		}
		result = convert(value, Double.class);
		return result.doubleValue();
	}

	/**
	 * Type converts values to other classes. For example an Integer can be
	 * converted to a Long.
	 *
	 * @param <T>
	 *            Data Type.
	 * @param value
	 *            The value to be converted.
	 * @param to
	 *            The class to be converted to. Must be one of the Java types
	 *            corresponding to the DataTypes.
	 * @return The converted value.
	 * @throws TypeMismatchException
	 *             Thrown desired class is incompatible with the source class.
	 * @throws OverflowException
	 *             Thrown only on narrowing value conversions, e.g from a
	 *             BigInteger to an Integer.
	 */
	public static <T> T convert(Object value, Class<?> to) throws TypeMismatchException, OverflowException {
		final Object result;

		if(value == null || value.getClass() == to) {
			result = value;
		} else {
			final Class<?> from = value.getClass();
			try {
				if(from == Integer.class) { // Integer -> ...
					if(to == Long.class) {
						result = new Long(((Number)value).longValue());
					} else if(to == BigInteger.class) {
						result = BigInteger.valueOf(((Number)value).intValue());
					} else if(to == Float.class) {
						result = new Float(((Number)value).floatValue());
					} else if(to == Double.class) {
						result = new Double(((Number)value).doubleValue());
					} else if(to == BigDecimal.class) {
						// Use intValue() to avoid precision errors
						result = new BigDecimal(((Number)value).intValue());
					} else {
						throw new TypeMismatchException(value.getClass(), to);
					}

				} else if(from == Long.class) { // Long -> ...
					if(to == Integer.class) {
						final long l = ((Long)value).longValue();
						if(l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
							throw new OverflowException((Number)value, to);
						}
						result = new Integer((int)l);
					} else if(to == BigInteger.class) {
						result = BigInteger.valueOf(((Number)value).longValue());
					} else if(to == Float.class) {
						result = new Float(((Number)value).floatValue());
					} else if(to == Double.class) {
						result = new Double(((Number)value).doubleValue());
					} else if(to == BigDecimal.class) {
						// Use longValue() to avoid precision errors
						result = new BigDecimal(((Number)value).longValue());
					} else {
						throw new TypeMismatchException(value.getClass(), to);
					}

				} else if(from == BigInteger.class) { // BigInteger -> ...
					final BigInteger bi = (BigInteger)value;
					if(to == Integer.class) {
						result = new Integer(bi.intValueExact());
					} else if(to == Long.class) {
						result = new Long(bi.longValueExact());
					} else if(to == Float.class) {
						final float f1 = bi.floatValue();
						if(f1 == Float.NEGATIVE_INFINITY || f1 == Float.POSITIVE_INFINITY) {
							throw new OverflowException(bi, to);
						}
						result = new Float(f1);
					} else if(to == Double.class) {
						final double d = bi.doubleValue();
						if(d == Double.NEGATIVE_INFINITY || d == Double.POSITIVE_INFINITY) {
							throw new OverflowException(bi, to);
						}
						result = new Double(d);
					} else if(to == BigDecimal.class) {
						result = new BigDecimal(bi);
					} else {
						throw new TypeMismatchException(value.getClass(), to);
					}

				} else if(from == Float.class) { // Float -> ...
					final float fl = ((Float)value).floatValue();
					if(to == Integer.class) {
						if(fl < Integer.MIN_VALUE || fl > Integer.MAX_VALUE | fl % 1 > 0) {
							throw new OverflowException((Number)value, to);
						}
						result = new Integer((int)fl);
					} else if(to == Long.class) {
						if(fl < Long.MIN_VALUE || fl > Long.MAX_VALUE | fl % 1 > 0) {
							throw new OverflowException((Number)value, to);
						}
						result = new Long((long)fl);
					} else if(to == BigInteger.class) {
						if(fl % 1 > 0) {
							throw new OverflowException((Number)value, to);
						}
						final BigDecimal bd = BigDecimal.valueOf(fl);
						result = bd.toBigInteger();
					} else if(to == Double.class) {
						result = new Double(((Number)value).doubleValue());
					} else if(to == BigDecimal.class) {
						result = BigDecimal.valueOf(fl);
					} else {
						throw new TypeMismatchException(value.getClass(), to);
					}

				} else if(from == Double.class) { // Double -> ...
					final double d = ((Double)value).doubleValue();
					if(to == Integer.class) {
						if(d < Integer.MIN_VALUE || d > Integer.MAX_VALUE || d % 1 > 0) {
							throw new OverflowException((Number)value, to);
						}
						result = new Integer((int)d);
					} else if(to == Long.class) { // OK
						if(d < Long.MIN_VALUE || d > Long.MAX_VALUE || d % 1 > 0) {
							throw new OverflowException((Number)value, to);
						}
						result = new Long((int)d);
					} else if(to == BigInteger.class) { // OK
						if(d % 1 > 0) {
							throw new OverflowException((Number)value, to);
						}
						final BigDecimal bd = BigDecimal.valueOf(d);
						result = bd.toBigInteger();
					} else if(to == Float.class) { // OK
						if(d < -Float.MAX_VALUE || d > Float.MAX_VALUE) {
							throw new OverflowException((Number)value, to);
						}
						result = new Float((float)d);
					} else if(to == BigDecimal.class) { // OK
						result = BigDecimal.valueOf(d);
					} else {
						throw new TypeMismatchException(value.getClass(), to);
					}

				} else if(from == BigDecimal.class) { // BigDecimal -> ...
					final BigDecimal bd = (BigDecimal)value;
					if(to == Integer.class) { // OK
						result = new Integer(bd.intValueExact());
					} else if(to == Long.class) { // OK
						result = new Long(bd.longValueExact());
					} else if(to == BigInteger.class) { // OK
						// BigDecimal modulus
						final BigDecimal remainder = bd.remainder(BigDecimal.ONE);
						if(!remainder.equals(BigDecimal.ZERO)) {
							throw new OverflowException(bd, to);
						}
						result = bd.toBigInteger();
					} else if(to == Float.class) { // OK
						if(bd.compareTo(BigDecimal_MIN_FLOAT) < 0 || bd.compareTo(BigDecimal_MAX_FLOAT) > 0) {
							throw new OverflowException(bd, to);
						}
						result = new Float(bd.floatValue());
					} else if(to == Double.class) { // OK
						if(bd.compareTo(BigDecimal_MIN_DOUBLE) < 0 || bd.compareTo(BigDecimal_MAX_DOUBLE) > 0) {
							throw new OverflowException(bd, to);
						}
						result = new Double(bd.doubleValue());
					} else {
						throw new TypeMismatchException(value.getClass(), to);
					}

				} else {
					throw new UnexpectedException("convert: " + from.getName());
				}
			} catch(final ArithmeticException e) {
				// Thrown by intValueExact() etc.
				throw new OverflowException((Number)value, to);
			}
		}

		@SuppressWarnings("unchecked")
		final T t = (T)result;
		return t;
	}

	/**
	 * Escape a string for Oak. If the string is null then "null" is returned
	 * otherwise the string is returned with [tn\"] escaped \r discarded and the
	 * whole thing surrounded in quotes if requested.
	 *
	 * @param string
	 *            The String to process.
	 * @param quote
	 *            Double quotes are added if true.
	 * @param result
	 *            Where to place the processed String.
	 */
	public static void escapeForOak(String string, boolean quote, TextBuilder result) {
		if(string == null) {
			result.append("null");
		} else {
			if(quote) {
				result.append('"');
			}
			final char[] ca = string.toCharArray();
			for(final char c : ca) {
				switch(c) {
				case '\t':
					result.append("\\t");
					break;
				case '\n':
					result.append("\\n");
					break;
				case '\\':
					result.append("\\\\");
					break;
				case '\"':
					result.append("\\\"");
					break;
				case '\r':
					// Discard
					break;
				default:
					result.append(c);
				}
			}
			if(quote) {
				result.append('"');
			}
		}
	}

	/**
	 * This returns the same as name() except for booleans where "boolean" is
	 * returned.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this == bool ? "boolean" : name();
	}

	public static void toString(Object value, TextBuilder tb) {
		if(value == null) {
			tb.append("null");
		} else {
			final Class<? extends Object> clazz = value.getClass();
			final DataType type = getDataType(clazz);
			if(type == null) {
				throw new RuntimeException("Invalid type: " + clazz.getName());
			}
			type.toMarkup(value, tb);
		}
	}

	/**
	 * Given a value it to Oak markup for this type.
	 *
	 * @param value
	 *            The value to convert.
	 * @param result
	 *            Where the markup is inserted.
	 */
	public void toMarkup(Object value, TextBuilder result) {
		if(value == null) {
			result.append("null");
		} else if(this == text) {
			escapeForOak((String)value, true, result);
		} else if(this == identifier || this == path) {
			result.append(value.toString());
		} else {
			result.append(value.toString());
		}
	}

	/**
	 * Parse a string and set the value accordingly.
	 *
	 * @param string
	 *            The string to parse. May be null. The string should be in Oak
	 *            markup so "a string", `/Some/Path`, etc.
	 * @return The parsed value.
	 * @see asString(T)
	 * @see asString(List<T)
	 * @throws ParsingException
	 *             Thrown if the string cannot be parsed correctly.
	 */
	@SuppressWarnings("unchecked")
	public <T> T parse(@Nullable String string) throws ParsingException {
		final T result;

		assert string != null : "Null string";

		if(string.equals("null")) {
			result = null;
		} else {
			switch(this) {
			case text: // "a string"
				try {
					result = (T)StringU.removeQuotes(string);
				} catch(final RuntimeException e) {
					throw new ParsingException("Missing quotes");
				}
				break;

			case identifier:
				// Verify
				Identifier.parse(string);
				result = (T)string;
				break;

			case path:
				result = (T)Path.parse(string);
				if(result == null) {
					throw new ParsingException("Invalid path: " + string);
				}
				break;

			case bool:
				// Boolean.parse() is not strict enough
				if(string.equals("true")) {
					result = (T)Boolean.TRUE;
				} else if(string.equals("false")) {
					result = (T)Boolean.FALSE;
				} else {
					throw new ParsingException("Invalid boolean, should be (true/false): " + string);
				}
				break;

			case z:
				try {
					// Remove optional trailing 'z'
					final int length = string.length();
					if(length > 1 && string.charAt(length - 1) == 'z') {
						result = (T)new Long(string.substring(0, length - 1));
					} else {
						result = (T)new Long(string);
					}
				} catch(final NumberFormatException e) {
					throw new ParsingException("Invalid integer (Long): " + string, e);
				}
				break;

			case Z:
				try {
					// Remove trailing 'Z'
					final int end = string.length() - 1;
					if(end < 0 || string.charAt(end) != 'Z') {
						throw new ParsingException("Invalid number");
					}
					result = (T)new Long(string.substring(0, end));
				} catch(final NumberFormatException e) {
					throw new ParsingException("Invalid INTEGER (BigInteger): " + string, e);
				}
				break;

			case f:
				try {
					// Remove optional trailing 'f'
					final int length = string.length();
					if(length > 1 && string.charAt(length - 1) == 'f') {
						result = (T)new Double(string.substring(0, length - 1));
					} else {
						result = (T)new Double(string);
					}
				} catch(final NumberFormatException e) {
					throw new ParsingException("Invalid float (Double): " + string, e);
				}
				break;

			case F:
				try {
					// Remove trailing 'F'
					final int end = string.length() - 1;
					if(end < 0 || string.charAt(end) != 'F') {
						throw new ParsingException("Invalid number");
					}
					result = (T)new Long(string.substring(0, end));
				} catch(final NumberFormatException e) {
					throw new ParsingException("Invalid FLOAT (BigDecimal): " + string, e);
				}
				break;

			case cardinality:
				try {
					result = (T)Cardinality.newInstance(string);
				} catch(final Cardinality.Exception e) {
					throw new ParsingException("Invalid cardinality: " + string, e);
				}
				break;

			case date:
				if(string.length() == 0 || string.charAt(0) != '@') {
					throw new ParsingException("Invalid date, missing leading '@': " + string);
				}
				try {
					result = (T)DateU.parseStandardDate(string.substring(1));
				} catch(final DateTimeParseException e) {
					throw new ParsingException("Invalid date: " + string);
				}
				break;

			case time:
				if(string.length() == 0 || string.charAt(0) != '@') {
					throw new ParsingException("Invalid time, missing leading '@': " + string);
				}
				try {
					result = (T)DateU.parseStandardDate(string.substring(1));
				} catch(final DateTimeParseException e) {
					throw new ParsingException("Invalid time: " + string);
				}
				break;

			case datetime:
				if(string.length() == 0 || string.charAt(0) != '@') {
					throw new ParsingException("Invalid datetime, missing leading '@': " + string);
				}
				try {
					result = (T)DateU.parseStandardDate(string.substring(1));
				} catch(final DateTimeParseException e) {
					throw new ParsingException("Invalid datetime: " + string);
				}
				break;

			case any:
			default:
				throw new UnexpectedException(string);
			}
		}

		return result;
	}

	/**
	 * Convert a list of values to an Oak markup String, e.g.
	 * "[ true, null, false ]".
	 *
	 * @param values
	 *            The list of values
	 * @return An Oak markup representation of the values.
	 * @see parse(String)
	 */
	public String asString(List<Object> values) {
		assert values != null : "Arrays can't be null, only empty";

		final TextBuilder tb = new TextBuilder(true);

		tb.append('[');
		if(values.size() > 0) {
			tb.space();
			for(final Object value : values) {
				tb.delimit();
				asString(value, tb);
			}
			tb.space();
		}
		tb.append(']');

		return tb.toString();

	}

	/**
	 * Convert a value to an Oak markup representation.
	 *
	 * @param value
	 *            The value to represent.
	 * @return E.g. `/Some/Path`
	 * @see parse(String)
	 */
	public String asString(Object value) {
		final TextBuilder tb = new TextBuilder();
		asString(value, tb);
		return tb.toString();
	}

	/**
	 * Convert a value to an Oak markup representation.
	 *
	 * @param value
	 *            The value to represent.
	 * @param tb
	 *            The buffer in which to write the markup.
	 * @see parse(String)
	 */
	public void asString(Object value, TextBuilder tb) {
		if(value == null) {
			tb.append("null");
		} else {
			switch(this) {
			case z:
			case Z:
			case f:
			case F:
			case bool:
				tb.append(value.toString());
				break;

			case cardinality:
				((Cardinality)value).toString(tb);
				break;

			case text:
				escapeForOak((String)value, true, tb);
				break;

			case identifier:
				tb.append((String)value);
				break;

			case path:
				((Path)value).toString(tb);
				break;

			case datetime:
				tb.append('@');
				tb.append(DateU.formatStandardDateTime((LocalDateTime)value));
				break;

			case date:
				tb.append('@');
				tb.append(DateU.formatStandardDate((LocalDate)value));
				break;

			case time:
				tb.append('@');
				tb.append(DateU.formatStandardTime((LocalTime)value));
				break;

			case any:
			default:
				throw new UnexpectedException("asString: " + this);
			}
		}
	}

	/**
	 * Parse a string into a list of data values. White space is supported only
	 * in between the [square brackets].
	 *
	 * @param string
	 *            A string representation of an Oak property array, e.g. [ true,
	 *            null, false ]
	 * @return A potentially empty but not null list of values.
	 */
	public static List<?> parseArray(String string) {
		final List<?> result;

		final TextBuilder tb = new TextBuilder();
		tb.append(string);
		if(tb.consume('[')
				&& (result = elementList(tb)) != null
				&& tb.consume(']')
				&& tb.isEof()) {
			// OK
		} else {
			throw new ParsingException("Invalid array: " + string);
		}

		return result;
	}

	private static List<Object> elementList(TextBuilder tb) {
		final List<Object> result = new ArrayList<>();

		// elementList: ( WS? value ( WS? ',' WS? value )* WS? )?
		if(tb.ws() && value(tb, result)) {
			while(true) {
				final int save = tb.cursor();
				if(tb.ws() && tb.consume(',') && tb.ws() && value(tb, result)) {
					continue;
				}
				tb.setCursor(save);
				break;
			}
		}

		return result;
	}

	/**
	 * Parse a value.
	 *
	 * @param tb
	 *            Source.
	 * @param valueList
	 *            Result added here.
	 * @return Return true if a value has been added to the valueList and the
	 *         cursor advanced or false and the cursor is as it was.
	 */
	private static boolean value(TextBuilder tb, List<Object> valueList) {
		boolean result;

		final int save = tb.cursor();

		// value
		// : Text
		// | Path
		// | Date | Time | DateTime
		// | 'null'
		// | 'true' | 'false'
		// | z | Z | f | F
		// | Identifier
		// | Cardinality
		// ;

		if(tb.isEof()) {
			result = false;
		} else {
			result = true;
			final char c = tb.peek();
			if(c == '"') { // Text
				if(tb.consumeString()) {
					final String textValue = tb.getString(save);
					valueList.add(textValue);
				} else {
					throw new ParsingException("Unterminated string");
				}
			} else if(c == '`') { // Path
				final Path pathValue = Path.parse(tb);
				if(pathValue != null) {
					valueList.add(pathValue);
				} else {
					result = false;
				}
			} else if(c == '@') { // Temporal
				final Temporal temporal = temporal(tb);
				if(temporal != null) {
					valueList.add(temporal);
				} else {
					result = false;
				}
			} else if(c >= '0' && c <= '9' || c == '.' || c == '-') {
				final Object number = number(tb);
				if(number != null) {
					valueList.add(number);
				} else {
					result = false;
				}
			} else {
				final int start = tb.cursor();
				if(Identifier.consume(tb)) {
					final String token = tb.getString(start);
					if(token.equals("null")) {
						valueList.add(null);
					} else if(token.equals("true")) {
						valueList.add(Boolean.TRUE);
					} else if(token.equals("false")) {
						valueList.add(Boolean.FALSE);
					} else {
						valueList.add(token); // Identifier
					}
				} else {
					result = false;
				}
			}

			if(result == false) {
				tb.setCursor(save);
			}
		}

		return result;
	}

	/**
	 * There's a bug in this as it requires seconds in the string whereas the
	 * time() function does not. It seems to be in the Java parser.
	 *
	 * @param tb
	 *            Source to parse.
	 * @return A Temporal or null if none found.
	 */
	@Nullable
	private static Temporal temporal(TextBuilder tb) {
		Temporal result;

		final int save = tb.cursor();
		tb.consume('@');
		final int start = save + 1;

		// yyyy/MM/dd HH:mm:ss.S

		try {
			if(date(tb)) {
				final int save2 = tb.cursor();
				tb.ws();
				if(time(tb)) {
					final String string = tb.getString(start);
					result = DateU.parseStandardDateTime(string);
				} else {
					tb.setCursor(save2);
					final String string = tb.getString(start);
					result = DateU.parseStandardDate(string);
				}
			} else if(time(tb)) {
				final String string = tb.getString(start);
				result = DateU.parseStandardTime(string);
			} else {
				result = null;
				tb.setCursor(save);
			}
		} catch(final DateTimeParseException e) {
			result = null;
			tb.setCursor(save);
		}

		return result;
	}

	private static boolean date(TextBuilder tb) {
		final boolean result;

		// yyyy '/' MM '/' dd

		final int save = tb.cursor();
		if(tb.consumeAscii(TextBuilder.ASCII_0_9)
				&& tb.consume('/')
				&& tb.consumeAscii(TextBuilder.ASCII_0_9)
				&& tb.consume('/')
				&& tb.consumeAscii(TextBuilder.ASCII_0_9)) {
			result = true;
		} else {
			result = false;
			tb.setCursor(save);
		}

		return result;
	}

	private static boolean time(TextBuilder tb) {
		final boolean result;

		// HH ':' mm ( : ss )?

		// Try for HH:mm...
		int reset = tb.cursor();
		if(tb.consumeAscii(TextBuilder.ASCII_0_9)
				&& tb.consume(':')
				&& tb.consumeAscii(TextBuilder.ASCII_0_9)) {
			result = true;
			reset = tb.cursor();

			// Try for :ss...
			if(tb.consume(':') && tb.consumeAscii(TextBuilder.ASCII_0_9)) {
				reset = tb.cursor();
			}
		} else {
			result = false;
		}

		tb.setCursor(reset);

		return result;
	}

	private static Object number(TextBuilder tb) {
		final Object result;

		// posNumber
		// : cardinality
		// | '0b' BinaryDigits+ [zZ]?
		// | '0x' HexDigit+ [zZ]?
		// | '-'? Pint? ( '.' Digit+ ) ('e' Pint) [fF]
		// | '-'? Pint [zZfF]?
		// ;

		int start = tb.cursor();
		if(cardinality(tb)) {
			result = Cardinality.newInstance(tb.getString(start));
		} else {
			final boolean negative = tb.consume('-');
			boolean preferFloat = false;
			int radix;
			if(tb.consume("0b") && tb.consumeAscii(TextBuilder.ASCII_0_1)) {
				if(negative) {
					throw new ParsingException("Binary numbers cannot be negative (-0b111)");
				}
				radix = 2;
				start += 2;
			} else if(tb.consume("0x") && tb.consumeAscii(TextBuilder.ASCII_0_F)) {
				if(negative) {
					throw new ParsingException("Hex numbers cannot be negative (-0xF5)");
				}
				radix = 16;
				start += 2;
			} else {
				radix = 10;
				tb.consumePint();

				// Optional decimal part
				int save = tb.cursor();
				if(tb.consume('.') && tb.consumeAscii(TextBuilder.ASCII_0_9)) {
					preferFloat = true;
				} else {
					tb.setCursor(save);
				}

				// Optional exponent
				save = tb.cursor();
				if(tb.consume('e') && tb.consumeInt()) {
					preferFloat = true;
				} else {
					tb.setCursor(save);
				}
			}
			// Optional data type
			final int end = tb.cursor();

			if(start == end) {
				// Nothing parsed...
				result = null;
			} else {
				final String string = tb.getString(start);
				final char numberType = numberType(tb, preferFloat);
				if(numberType == 'z') {
					result = new Long(Long.parseLong(string, radix));
				} else if(numberType == 'Z') {
					result = new BigInteger(string, radix);
				} else if(numberType == 'f') {
					result = new Double(string);
				} else {
					assert numberType == 'F';
					result = new BigDecimal(string);
				}
			}
		}

		return result;
	}

	private static char numberType(TextBuilder tb, boolean preferFloat) {
		final char result;

		if(tb.isEof()) {
			result = preferFloat ? 'f' : 'z';
		} else {
			final char c = tb.peek();
			if(c == 'z' || c == 'Z' || c == 'f' || c == 'F') {
				result = c;
				tb.consume(c);
			} else {
				result = preferFloat ? 'f' : 'z';
			}
		}

		return result;
	}

	private static boolean cardinality(TextBuilder tb) {
		final boolean result;

		// cardinality
		// : '*'
		// | Pint '..' ( '*' | Pint )
		// ;

		final int save = tb.cursor();
		if(tb.consume('*') ||
				tb.consumePint() && tb.consume("..") && (tb.consume('*') || tb.consumePint())) {
			result = true;
		} else {
			tb.setCursor(save);
			result = false;
		}

		return result;
	}

}