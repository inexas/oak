package com.inexas.oak;

import java.math.*;
import java.time.*;
import java.util.*;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.advisory.Advisory;
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
	identifier(		false,	false,	String.class),
	path(			false,	false,	String.class),
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
	private final Map<Class<?>, DataType> javaToDataType = new HashMap<>();

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

	DataType(boolean numeric, boolean temporal, Class<?> javaClass) {
		this.numeric = numeric;
		this.temporal = temporal;
		this.javaClass = javaClass;
		javaToDataType.put(javaClass, this);
	}

	public Object parse(String value) {
		final Object result;
		switch(this) {
		case text:
		case identifier:
		case path:
			result = value;
			break;

		case bool:
			result = Boolean.valueOf(value);
			break;

		case z:
			result = new Long(value);
			break;

		case Z:
			result = new BigInteger(value);
			break;

		case f:
			result = new Double(value);
			break;

		case F:
			result = new BigDecimal(value);
			break;

		case cardinality:
			result = Cardinality.newInstance(value);
			break;

		case date:
		case time:
		case datetime:
			result = DateU.parseDateTimeStandard(value);
			break;

		case any:
		default:
			throw new UnexpectedException(value);
		}

		return result;
	}

	public void toString(TextBuilder result) {
		result.append(toString());
	}

	/**
	 * Use this instead of valueOf as it treats boolean properly. If an error is
	 * detected the Advisory is updated.
	 *
	 * @param key
	 *            The key to look up.
	 * @return The DataType associated with the given key.
	 */
	public static DataType get(String key) {
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
	 * This returns the same as name() except for booleans where "boolean" is
	 * returned.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this == bool ? "boolean" : name();
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
				throw new TypeMismatchException("Both type must be the same");
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

	public String getJavaType() {
		return javaClass.getSimpleName();
	}

	/**
	 * Given a value it to Oak markup for this type.
	 *
	 * @param value
	 *            The value to convert.
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
	 * Escape a string for Java. If the string is null then "null" is returned
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
}