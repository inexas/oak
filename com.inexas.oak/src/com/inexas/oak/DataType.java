package com.inexas.oak;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.TadContext;
import com.inexas.util.*;

public enum DataType {
	// @formatter:off
	// Keep z first and the order: z, Z, f, F for getCommonType()
	//				number	time	Java class
	z(				true,	false,	Integer.class),
	Z(				true,	false,	BigInteger.class),
	f(				true,	false,	Float.class),
	F(				true,	false,	BigDecimal.class),
	text(			false,	false,	String.class),
	identifier(		false,	false,	Identifier.class),
	path(			false,	false,	Path.class),
	bool(			false,	false,	Boolean.class),
	cardinality(	false,	false,	Cardinality.class),
	datetime(		false,	true,	LocalDateTime.class),
	date(			false,	true,	LocalDate.class),
	time(			false,	true,	LocalTime.class),
	any(			false,	false,	Object.class),
	notEvaluated(	false,	false,	null);

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
	 * Can't override valueOf() so use this method instead as it treats boolean
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
			final Advisory advisory = TadContext.get(Advisory.class);
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
	 * The common type depends on the operation and operands. For something like
	 * 5z + 6Z then the operands' common type is Z (the broader of the two) and
	 * the returnType will be Z because of the operation.
	 *
	 * @param rhs
	 *            The other type.
	 * @return The common type
	 * @throws TypeMismatchException
	 *             Data does not match expected data type.
	 */
	public DataType getCommnType(DataType rhs) throws TypeMismatchException {
		final DataType result;

		if(this == notEvaluated || rhs == notEvaluated) {
			result = DataType.notEvaluated;
		} else {
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
	@Nullable
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
						result = new Double(((Number)value).floatValue());
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
						result = new Double(((Number)value).floatValue());
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
						final float d = bi.floatValue();
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
						result = new Double(((Number)value).floatValue());
					} else if(to == BigDecimal.class) {
						result = BigDecimal.valueOf(fl);
					} else {
						throw new TypeMismatchException(value.getClass(), to);
					}

				} else if(from == Double.class) { // Double -> ...
					final float d = ((Double)value).floatValue();
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
						if(d < -(double)Float.MAX_VALUE || d > (double)Float.MAX_VALUE) {
							throw new OverflowException((Number)value, to);
						}
						result = new Float(d);
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
						result = new Double(bd.floatValue());
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
	public static void escapeForOak(String string, boolean quote, Text result) {
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
				case '\"':
					result.append("\\\"");
					break;
				case '\\':
					result.append("\\");
					break;
				default:
					if(c < ' ' || c >= '~') {
						// Must be four digits
						result.append("\\u" + Integer.toHexString(c | 0x10000).substring(1));
					} else {
						result.append(c);
					}
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

	public static void toString(Object value, Text t) {
		if(value == null) {
			t.append("null");
		} else {
			final Class<? extends Object> clazz = value.getClass();
			final DataType type = getDataType(clazz);
			if(type == null) {
				throw new RuntimeException("Invalid type: " + clazz.getName());
			}
			type.toMarkup(value, t);
		}
	}

	/**
	 * Convert a list of values to an Oak markup String, e.g. "[ true, null,
	 * false ]".
	 *
	 * @param values
	 *            The list of values
	 * @return An Oak markup representation of the values.
	 * @see #parse(String)
	 */
	public String toString(List<Object> values) {
		assert values != null : "Arrays can't be null, only empty";

		final Text t = new Text(true);

		t.append('[');
		if(values.size() > 0) {
			t.space();
			for(final Object value : values) {
				t.delimit();
				toMarkup(value, t);
			}
			t.space();
		}
		t.append(']');

		return t.toString();

	}

	/**
	 * Convert a value to an Oak markup representation.
	 *
	 * @param value
	 *            The value to represent.
	 * @return E.g. `/Some/Path`
	 * @see #parse(String)
	 */
	public final String toString(Object value) {
		final Text t = new Text();
		toMarkup(value, t);
		return t.toString();
	}

	/**
	 * Convert a value to an Oak markup representation.
	 *
	 * @param value
	 *            The value to represent.
	 * @param t
	 *            The buffer in which to write the markup.
	 */
	public void toMarkup(Object value, Text t) {
		if(value == null) {
			t.append("null");
		} else {
			switch(this) {
			case z:
			case Z:
			case f:
			case F:
			case bool:
				t.append(value.toString());
				break;

			case cardinality:
				((Cardinality)value).toString(t);
				break;

			case text:
				escapeForOak((String)value, true, t);
				break;

			case identifier:
				t.append(value.toString());
				break;

			case path:
				((Path)value).toString(t);
				break;

			case datetime:
				t.append('@');
				t.append(DateU.formatStandardDatetime((LocalDateTime)value));
				break;

			case date:
				t.append('@');
				t.append(DateU.formatStandardDate((LocalDate)value));
				break;

			case time:
				t.append('@');
				t.append(DateU.formatStandardTime((LocalTime)value));
				break;

			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException("asString: " + this);
			}
		}
	}

	/**
	 * In Oak it is possible to embed control characters like tab character as
	 * an ASCII 8 or as a \t but internally we always store the ASCII character
	 * rather than an escape sequence. Here we convert external format to
	 * internal format.
	 *
	 * @param source
	 *            The text to process, may be null. E.g. "Hello\tworld" (with
	 *            the quotes)
	 * @return Return an internal format string.
	 */
	@Nullable
	public static String textToInternalFormat(@Nullable String source) throws ParsingException {
		final String result;

		assert source == null
				|| source.length() >= 2
						&& source.charAt(0) == '"'
						&& source.charAt(source.length() - 1) == '"';

		if(source == null) {
			result = null;
		} else {
			final Text t = new Text();
			final char[] ca = source.toCharArray();
			final int length = ca.length - 1; // 1 because remove quotes

			for(int i = 1; i < length; i++) { // 1 because remove quotes
				final char c = ca[i];
				if(c == '\\') {
					if(i == length - 1) {
						error("Invalid text " + source + ", escape at end of line");
					} else {
						i++;
						final char next = ca[i];
						if(next == 't') {
							t.append('\t');
						} else if(next == 'n') {
							t.append('\n');
						} else if(next == '"') {
							t.append('"');
						} else if(next == '\\') {
							t.append('\\');
						} else if(next == 'u') {
							// Unicode, 1-4 hex characters...
							i++;

							// Leave t as it is so create a copy: hex
							final Parser hex = new Parser(source);
							hex.setCursor(i);
							final int start = i;
							if(hex.consumeAscii(Parser.ASCII_0_F)) {
								final int end = start + Math.min(4, hex.cursor() - start);
								final String string = hex.getString(start, end);
								final char u = (char)Integer.parseInt(string, 16);
								t.append(u);
								i = end - 1;
							} else {
								error("Invalid text " + source + ", incorrect unicode");
							}
						} else {
							error("Invalid text: \\" + next);
						}
					}
				} else {
					t.append(c);
				}
			}
			result = t.toString();
		}

		return result;
	}

	/**
	 * Parse a string and set the value accordingly.
	 *
	 * @param <T>
	 *            Type of object to parse
	 * @param string
	 *            The string to parse. May be null. The string should be in Oak
	 *            markup so "a string", `/Some/Path`, etc.
	 * @return The parsed value.
	 * @see #toString(List)
	 *
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
				Identifier.isValid(string);
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
						result = (T)new Integer(string.substring(0, length - 1));
					} else {
						result = (T)new Integer(string);
					}
				} catch(final NumberFormatException e) {
					throw new ParsingException("Invalid integer (z): " + string, e);
				}
				break;

			case Z:
				try {
					// Remove trailing 'Z'
					final int end = string.length() - 1;
					if(end < 0 || string.charAt(end) != 'Z') {
						throw new ParsingException("Invalid number");
					}
					result = (T)new BigInteger(string.substring(0, end));
				} catch(final NumberFormatException e) {
					throw new ParsingException("Invalid integer (Z): " + string, e);
				}
				break;

			case f:
				try {
					// Remove optional trailing 'f'
					final int length = string.length();
					if(length > 1 && string.charAt(length - 1) == 'f') {
						result = (T)new Float(string.substring(0, length - 1));
					} else {
						result = (T)new Float(string);
					}
				} catch(final NumberFormatException e) {
					throw new ParsingException("Invalid float (f): " + string, e);
				}
				break;

			case F:
				try {
					// Remove trailing 'F'
					final int end = string.length() - 1;
					if(end < 0 || string.charAt(end) != 'F') {
						throw new ParsingException("Invalid number");
					}
					result = (T)new BigDecimal(string.substring(0, end));
				} catch(final NumberFormatException e) {
					throw new ParsingException("Invalid decimal (F): " + string, e);
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
					result = (T)DateU.parseStandardTime(string.substring(1));
				} catch(final DateTimeParseException e) {
					throw new ParsingException("Invalid time: " + string);
				}
				break;

			case datetime:
				if(string.length() == 0 || string.charAt(0) != '@') {
					throw new ParsingException("Invalid datetime, missing leading '@': " + string);
				}
				try {
					result = (T)DateU.parseStandardDatetime(string.substring(1));
				} catch(final DateTimeParseException e) {
					throw new ParsingException("Invalid datetime: " + string);
				}
				break;

			case any:
			case notEvaluated:
			default:
				throw new UnexpectedException(string);
			}
		}

		return result;
	}

	/**
	 * Parse a string value and covert it to its proper data type.
	 *
	 * @param <T>
	 *            The type of data to be returned.
	 * @param value
	 *            The value to parse.
	 * @return The parsed value.
	 * @throws ParsingException
	 *             Thrown if not Advisory is available and there was an error
	 *             parsing.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseValue(String value) throws ParsingException {
		final T result;

		assert value != null;

		/*
		 * todo Restructure the whole class so I've got parse and parseArray as
		 * static methods and the share code properly.
		 */
		if("null".equals(value)) {
			result = null;
		} else {
			final List<Object> list = new ArrayList<>();
			final Parser parser = new Parser(value);

			if(any(parser, list) && parser.isEof()) {
				result = (T)list.get(0);
			} else {
				error("Count not parse value: " + value);
				result = null;
			}
		}

		return result;
	}

	/**
	 * Parse a string into a list of data values. White space is supported only
	 * in between the [square brackets].
	 *
	 * @param <T>
	 *            The type of data to be returned.
	 * @param string
	 *            A string representation of an Oak property array, e.g. [ true,
	 *            null, false ]
	 * @param type
	 *            The type of the array, may be 'any'.
	 * @return A potentially empty but not null list of values.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> parseArray(String string, DataType type) {
		final List<?> result;

		final Parser parser = new Parser(string);
		if(parser.consume('[')
				&& (result = elementList(parser, type)) != null
				&& (parser.ws() && parser.consume(']'))
				&& parser.isEof()) {
			// OK
		} else {
			throw new ParsingException("Invalid array: " + string);
		}

		return (List<T>)result;
	}

	private static List<Object> elementList(Parser parser, DataType type) {
		final List<Object> result = new ArrayList<>();

		// elementList: ( WS? value ( WS? ',' WS? value )* WS? )?
		if(parser.ws() && value(parser, type, result)) {
			while(true) {
				final int save = parser.cursor();
				if(parser.ws() && parser.consume(',') && parser.ws() && value(parser, type, result)) {
					continue;
				}
				parser.setCursor(save);
				break;
			}
		}

		return result;
	}

	/**
	 * Parse a value.
	 *
	 * @param parser
	 *            Source.
	 * @param type
	 *            The type of the array, may be any..
	 * @param valueList
	 *            Result added here.
	 * @return Return true if a value has been added to the valueList and the
	 *         cursor advanced or false and the cursor is as it was.
	 */
	private static boolean value(Parser parser, DataType type, List<Object> valueList) {
		boolean result;

		if(parser.isEof()) {
			result = false;
		} else if(parser.consume("null")) {
			valueList.add(null);
			result = true;
		} else {
			switch(type) {
			case z:
			case Z:
			case f:
			case F:
				result = number(parser, type, valueList);
				break;

			case text:
				result = text(parser, valueList);
				break;

			case identifier:
				result = identifier(parser, valueList);
				break;

			case path:
				result = path(parser, valueList);
				break;

			case datetime:
				result = datetime(parser, valueList);
				break;

			case date:
				result = date(parser, valueList);
				break;

			case time:
				result = time(parser, valueList);
				break;

			case bool:
				result = bool(parser, valueList);
				break;

			case cardinality:
				result = cardinality(parser, valueList);
				break;

			case any:
				result = any(parser, valueList);
				break;

			case notEvaluated:
			default:
				throw new UnexpectedException("value: " + type);
			}
		}

		return result;
	}

	private static boolean any(Parser parser, List<Object> valueList) {
		final boolean result;

		// value
		// : Text
		// | Path
		// | Date | Time | Datetime
		// | 'null'
		// | 'true' | 'false'
		// | z | Z | f | F
		// | Identifier
		// | Cardinality
		// ;

		/*
		 * Try and parse a value, we don't know the type so use the first
		 * character as an indicator to jump to the right type.
		 *
		 * We know we're not EOF and "null" has been dealt with by the caller.
		 */
		final char c = parser.peek();
		if(c == '"') { // Text
			result = text(parser, valueList);
		} else if(c == '`') { // Path
			result = path(parser, valueList);
		} else if(c == '@') { // Temporal
			result = datetime(parser, valueList) // Must be first
					|| date(parser, valueList)
					|| time(parser, valueList);
		} else if(c >= '0' && c <= '9' || c == '.' || c == '-') {
			result = cardinality(parser, valueList) ||
					number(parser, DataType.any, valueList);
		} else {
			result = bool(parser, valueList) // Must be first
					|| identifier(parser, valueList);
		}

		return result;
	}

	private static boolean datetime(Parser parser, List<Object> valueList) {
		final boolean result;

		final int start = parser.cursor();
		if(parser.consume('@') && date(parser) && parser.ws() && time(parser)) {
			final String string = parser.getString(start + 1); // Jump the'@'
			final LocalDateTime value = DateU.parseStandardDatetime(string);
			valueList.add(value);
			result = true;
		} else {
			result = false;
		}

		return result;
	}

	private static boolean date(Parser parser, List<Object> valueList) {
		final boolean result;

		final int start = parser.cursor();
		if(parser.consume('@') && date(parser)) {
			final String string = parser.getString(start + 1); // Jump the'@'
			final LocalDate value = DateU.parseStandardDate(string);
			valueList.add(value);
			result = true;
		} else {
			result = false;
		}

		return result;
	}

	private static boolean date(Parser parser) {
		final boolean result;

		// yyyy '/' MM '/' dd

		final int save = parser.cursor();
		if(parser.consumeAscii(Parser.ASCII_0_9)
				&& parser.consume('/')
				&& parser.consumeAscii(Parser.ASCII_0_9)
				&& parser.consume('/')
				&& parser.consumeAscii(Parser.ASCII_0_9)) {
			result = true;
		} else {
			result = false;
			parser.setCursor(save);
		}

		return result;
	}

	private static boolean time(Parser parser, List<Object> valueList) {
		final boolean result;

		final int start = parser.cursor();
		if(parser.consume('@') && time(parser)) {
			final String string = parser.getString(start + 1); // Jump the'@'
			final LocalTime value = DateU.parseStandardTime(string);
			valueList.add(value);
			result = true;
		} else {
			result = false;
		}

		return result;
	}

	private static boolean time(Parser parser) {
		final boolean result;

		// HH ':' mm ( : ss )?

		// Try for HH:mm...
		int reset = parser.cursor();
		if(parser.consumeAscii(Parser.ASCII_0_9)
				&& parser.consume(':')
				&& parser.consumeAscii(Parser.ASCII_0_9)) {
			result = true;
			reset = parser.cursor();

			// Try for :ss...
			if(parser.consume(':') && parser.consumeAscii(Parser.ASCII_0_9)) {
				reset = parser.cursor();
			}
		} else {
			result = false;
		}

		parser.setCursor(reset);

		return result;
	}

	private static boolean cardinality(Parser parser, List<Object> valueList) {
		final boolean result;

		final Cardinality value = Cardinality.parse(parser);
		if(value == null) {
			result = false;
		} else {
			valueList.add(value);
			result = true;
		}

		return result;
	}

	private static boolean path(Parser parser, List<Object> valueList) {
		final boolean result;

		final Path value = Path.parse(parser);
		if(value == null) {
			result = false;
		} else {
			valueList.add(value);
			result = true;
		}

		return result;
	}

	private static boolean identifier(Parser parser, List<Object> valueList) {
		final boolean result;

		final int start = parser.cursor();
		if(Identifier.consume(parser)) {
			final String string = parser.getString(start);
			valueList.add(new Identifier(string));
			result = true;
		} else {
			result = false;
		}

		return result;
	}

	private static boolean text(Parser parser, List<Object> valueList) {
		final boolean result;

		final int start = parser.cursor();
		if(parser.consumeString()) {
			// +/- 1s: drop the quotes
			final String textValue = unescape(parser, start + 1, parser.cursor() - 1);
			valueList.add(textValue);
			result = true;
		} else {
			// ?todo Could easily detect unterminated string and throw here
			result = false;
		}

		return result;
	}

	/**
	 * Convert a potentially escaped string to text.
	 *
	 * @param parser
	 *            The source string.
	 * @param start
	 *            Starting offset.
	 * @param end
	 *            Ending offset.
	 * @return Return the un-escaped string which may be zero-length but not
	 *         null.
	 */
	private static String unescape(Parser parser, int start, int end) {
		final Text result = new Text();

		/*
		 * We know that there's a starting and ending quote that's been removed
		 * from the string but otherwise we can't trust the contents.
		 */
		for(int i = start; i < end; i++) {
			final char c = parser.charAt(i);
			final char escaped;
			if(c == '\\') {
				if(i < end - 1) {
					i++;
					final char next = parser.charAt(i);
					if(next == 't') {
						escaped = '\t';
					} else if(next == 'n') {
						escaped = '\n';
					} else if(next == '"') {
						escaped = '"';
					} else if(next == '\\') {
						escaped = '\\';
					} else {
						error("Invalid escape: '\\" + next + '\'');
						escaped = next;
					}
				} else {
					error("Unterminated string");
					escaped = c;
				}
			} else {
				escaped = c;
			}
			result.append(escaped);
		}

		return result.toString();
	}

	private static boolean bool(Parser parser, List<Object> valueList) {
		final boolean result;

		if(parser.consume("true")) {
			valueList.add(Boolean.TRUE);
			result = true;
		} else if(parser.consume("false")) {
			valueList.add(Boolean.FALSE);
			result = true;
		} else {
			result = false;
		}

		return result;
	}

	/**
	 * There's a bug in this as it requires seconds in the string whereas the
	 * time() function does not. It seems to be in the Java parser.
	 *
	 * @param parser
	 *            Source to parse.
	 * @return A Temporal or null if none found.
	 */
	@Nullable
	private static Temporal temporal(Parser parser) {
		Temporal result;

		final int save = parser.cursor();
		parser.consume('@');
		final int start = save + 1;

		// yyyy/MM/dd HH:mm:ss

		try {
			if(date(parser)) {
				final int save2 = parser.cursor();
				parser.ws();
				if(time(parser)) {
					final String string = parser.getString(start);
					result = DateU.parseStandardDatetime(string);
				} else {
					parser.setCursor(save2);
					final String string = parser.getString(start);
					result = DateU.parseStandardDate(string);
				}
			} else if(time(parser)) {
				final String string = parser.getString(start);
				result = DateU.parseStandardTime(string);
			} else {
				result = null;
				parser.setCursor(save);
			}
		} catch(final DateTimeParseException e) {
			result = null;
			parser.setCursor(save);
		}

		return result;
	}

	private static boolean number(
			Parser parser,
			DataType requiredType,
			List<Object> valueList) {
		// posNumber
		// : cardinality
		// | '0b' BinaryDigits+ [zZ]?
		// | '0x' HexDigit+ [zZ]?
		// | '-'? Pint? ( '.' Digit+ ) ('e' Pint) [fF]
		// | '-'? Pint [zZfF]?
		// ;
		final boolean result;

		assert !parser.isEof() : "Should have been checked by caller";

		if(binary(parser, valueList, requiredType) || hex(parser, valueList, requiredType)) {
			// ?todo I could allow negatives here
			// ( binary | hex | '0' )
			result = true;
		} else {
			// ( Int DecPart | Int | DecPart ) ( 'e' Int )? [zZfF]?

			final int start = parser.cursor();

			if((parser.consumeInt() && (decPart(parser) || true) && (exponent(parser) || true))
					|| (parser.consume('-') || true) && decPart(parser) && (exponent(parser) || true)) {
				final String string = parser.getString(start);
				final Object value = deriveType(string, requiredType, parser, 10);
				valueList.add(value);
				result = true;
			} else {
				result = false;
			}

		}
		return result;
	}

	private static boolean exponent(Parser parser) {
		final boolean result;

		// 'e' Int
		final int save = parser.cursor();
		if(parser.consume('e') && parser.consumeInt()) {
			result = true;
		} else {
			parser.setCursor(save);
			result = false;
		}

		return result;
	}

	private static boolean decPart(Parser parser) {
		final boolean result;

		// '.' Digits+
		final int save = parser.cursor();
		if(parser.consume('.') && parser.consumeAscii(Parser.ASCII_0_9)) {
			result = true;
		} else {
			parser.setCursor(save);
			result = false;
		}

		return result;
	}

	private static boolean hex(Parser parser, List<Object> valueList, DataType requiredType) {
		final boolean result;

		// '0x' HexDigit+ [zZfF]?
		final int save = parser.cursor();
		if(parser.consume("0x") && parser.consumeAscii(Parser.ASCII_0_F)) {
			final String string = parser.getString(save + 2);
			final Object value = deriveType(string, requiredType, parser, 16);
			valueList.add(value);
			result = true;
		} else {
			parser.setCursor(save);
			result = false;
		}

		return result;
	}

	private static boolean binary(Parser parser, List<Object> valueList, DataType requiredType) {
		final boolean result;

		// '0b' [01]+ [zZfF]?
		final int save = parser.cursor();
		if(parser.consume("0b") && parser.consumeAscii(Parser.ASCII_0_1)) {
			final String string = parser.getString(save + 2);
			final Object value = deriveType(string, requiredType, parser, 2);
			valueList.add(value);
			result = true;
		} else {
			parser.setCursor(save);
			result = false;
		}

		return result;
	}

	/**
	 * Given the parsing context and what numerical data type is expected
	 * convert a string to the correct type. Note no attempt is made to let the
	 * magnitude of the number influence our choice.
	 *
	 * @param string
	 *            The string to convert to a number. E.g. "123.3e2". If it
	 *            contains a '.' or an 'e' then the type must either be f or F.
	 * @param requiredType
	 *            Either z, Z, f, F or any.
	 * @param parser
	 *            The source. The cursor will be at the end of the number but
	 *            any type specifier will not have been consumed. If there is
	 *            one then we'll eat it.
	 * @return The derived type.
	 * @throws ParsingException
	 *             If there is a clash of types.
	 */
	private static Object deriveType(
			String string,
			DataType requiredType,
			Parser parser,
			int radix) {
		final Object result;

		// Figure out the correct type...
		final DataType derivedType;
		if(parser.isEof()) {
			if(requiredType == DataType.any) {
				if(string.indexOf('.') >= 0 || string.indexOf('e') >= 0) {
					derivedType = DataType.f;
				} else {
					derivedType = DataType.z;
				}
			} else {
				derivedType = requiredType;
			}
		} else {
			final char c = parser.peek();
			if(c == 'z' || c == 'Z' || c == 'f' || c == 'F') {
				parser.consume(c);
				derivedType = DataType.valueOf(String.valueOf(c));
				if(!(requiredType == DataType.any || requiredType == derivedType)) {
					throw new ParsingException("Incompatible type: " + string + c);
				}
			} else {
				if(requiredType == DataType.any) {
					if(string.indexOf('.') >= 0 || string.indexOf('e') >= 0) {
						derivedType = DataType.f;
					} else {
						derivedType = DataType.z;
					}
				} else {
					derivedType = requiredType;
				}
			}
		}

		switch(derivedType) {
		case z:
			result = new Integer(Integer.parseInt(string, radix));
			break;
		case Z:
			result = new BigInteger(string, radix);
			break;
		case f:
			result = new Float(string);
			break;
		case F:
			result = new BigDecimal(string);
			break;
		// $CASES-OMITTED$
		default:
			throw new UnexpectedException("toType: " + derivedType);
		}

		return result;
	}

	/**
	 * Add message to Advisory if there is one otherwise throw an exception.
	 *
	 * @param message
	 *            Error message
	 * @throws ParsingException
	 *             Thrown only if there is not Advisory.
	 */
	private static void error(String message) throws ParsingException {
		final Advisory advisory = TadContext.getButDontThrow(Advisory.class);
		if(advisory != null) {
			advisory.error(message);
		} else {
			throw new ParsingException(message);
		}
	}

}