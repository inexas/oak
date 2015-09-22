/*
 * Copyright (C) 24215 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak;

import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.junit.Test;
import com.inexas.util.*;

/**
 * @author kwhittingham
 *
 */
public class TestDataType {
	private static final Integer integer42 = new Integer(42);
	private static final Long long42 = new Long(42);
	private static final BigInteger bigInteger42 = BigInteger.valueOf(42);
	private static final Float float42 = new Float(42);
	private static final Double double42 = new Double(42);
	private static final BigDecimal bigDecimal42 = new BigDecimal(42);

	private static final Long integerMaxAsLong = new Long(Integer.MAX_VALUE);
	private static final Long integerMaxPlus1AsLong = new Long(Integer.MAX_VALUE + 1L);
	private static final Long integerMinAsLong = new Long(Integer.MIN_VALUE);
	private static final Long integerMinMinus1AsLong = new Long(Integer.MIN_VALUE - 1L);

	private static final BigInteger longMaxAsBigInteger = BigInteger.valueOf(Long.MAX_VALUE);
	private static final BigInteger longMaxPlus1AsBigInteger =
			BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
	private static final BigInteger longMinAsBigInteger = BigInteger.valueOf(Long.MIN_VALUE);
	private static final BigInteger longMinMinus1AsBigInteger =
			BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);

	private static final Double floatMaxAsDouble = new Double(Float.MAX_VALUE);
	private static final Double floatMaxPlus1AsDouble = new Double(Float.MAX_VALUE + 1e30);
	private static final Double floatMinAsDouble = new Double(-Float.MAX_VALUE);
	private static final Double floatMinMinus1AsDouble = new Double(-Float.MAX_VALUE - 1e30);

	private static final BigDecimal doubleMaxPlus1AsBigDecimal =
			BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.valueOf(1e100));
	private static final BigDecimal doubleMinMinus1AsBigDecimal =
			BigDecimal.valueOf(-Double.MAX_VALUE).subtract(BigDecimal.valueOf(1e290));

	@SuppressWarnings("unused")
	private static final double ACCEPTABLE_ERROR = 1e-15;

	private void testIdentity(Object toTest) throws Exception {
		assertTrue(DataType.convert(toTest, toTest.getClass()) == toTest);
	}

	/**
	 * This is needed because of the equals of BigDecimal comparing scale as
	 * well as value. See JavaDoc on BigDecimal.equals.
	 *
	 * @param o1
	 *            Object 1.
	 * @param o2
	 *            Object 2.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void assertEqualTo(Comparable o1, Comparable o2) {
		assertTrue(o1.compareTo(o2) == 0);
	}

	private void compareFloats(float f1, float f2) {
		assertTrue(f1 == f2);
	}

	private void compareDoubles(double d1, double d2) {
		if(d1 != d2) {
			System.err.println("d1: " + d1);
			System.err.println("d2: " + d2);
			fail();
		}
	}

	private void doValueTest(boolean expectedResult, Object expectedObject, String toTest)
			throws RuntimeException {
		final Text t = new Text();
		t.append(toTest);
		final List<Object> list = new ArrayList<>();
		// value() is private (and should be)
		try {
			final Method method = DataType.class.getDeclaredMethod(
					"value",
					t.getClass(),
					DataType.class,
					List.class);
			method.setAccessible(true);
			final Boolean result = (Boolean)method.invoke(null, t, DataType.any, list);

			if(expectedResult) {
				assertTrue(result.booleanValue());
				assertEquals(expectedObject, list.get(0));
			} else {
				assertFalse(result.booleanValue());
			}

		} catch(final InvocationTargetException e) {
			throw (RuntimeException)e.getCause();
		} catch(final Exception e) {
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
			fail();
		}
	}

	private void doDateTest(Object expectedResult, String methodName, String toTest) {
		final Text t = new Text();
		t.append(toTest);
		// date() is private (and should be)
		try {
			final Method method = DataType.class.getDeclaredMethod(methodName, Text.class);
			method.setAccessible(true);
			final Object result = method.invoke(null, t);
			assertEquals(expectedResult, result);
		} catch(final InvocationTargetException e) {
			throw (RuntimeException)e.getCause();
		} catch(final Exception e) {
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
			fail();
		}
	}

	private void doCardinalityTest(Cardinality expected, String toTest) {
		final Text t = new Text();
		t.append(toTest);
		try {
			// private static boolean cardinality(TextBuilder, List<Object>
			// valueList)
			final Method method = DataType.class.getDeclaredMethod(
					"cardinality",
					Text.class,
					List.class);
			method.setAccessible(true);
			final List<Object> list = new ArrayList<>();
			final Boolean result = (Boolean)method.invoke(null, t, list);
			assertTrue(result.booleanValue());
			assertEquals(expected, list.get(0));
		} catch(final InvocationTargetException e) {
			throw (RuntimeException)e.getCause();
		} catch(final Exception e) {
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
			fail();
		}
	}

	private void doNumberTest(Object expected, DataType type, String toTest) {
		final Text t = new Text();
		t.append(toTest);
		// number() is private (and should be)
		try {
			// private static Object number(TextBuilder)
			final Method method = DataType.class.getDeclaredMethod(
					"number",
					Text.class,
					DataType.class,
					List.class);
			method.setAccessible(true);
			final List<Object> list = new ArrayList<>();
			final Boolean result = (Boolean)method.invoke(null, t, type, list);
			assertTrue(result.booleanValue());
			assertEquals(expected, list.get(0));
			assertEquals(toTest.length(), t.cursor());
		} catch(final InvocationTargetException e) {
			throw (RuntimeException)e.getCause();
		} catch(final Exception e) {
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
			fail();
		}
	}

	private void doArrayTest(String string, DataType type, Object... expected) {
		final List<Object> actualList = DataType.parseArray(string, type);
		final Object[] actualArray = actualList.toArray(new Object[actualList.size()]);
		assertArrayEquals(expected, actualArray);
	}

	@Test
	public void testBasics() throws Exception {
		testIdentity(integer42);
		testIdentity(long42);
		testIdentity(bigInteger42);
		testIdentity(float42);
		testIdentity(double42);
		testIdentity(bigDecimal42);

		// If the following fails then the rest might be false positives...
		assertNotEquals(integer42, DataType.convert(integer42, Long.class));

		assertEqualTo(integer42, DataType.convert(integer42, Integer.class));
		assertEqualTo(long42, DataType.convert(integer42, Long.class));
		assertEqualTo(bigInteger42, DataType.convert(integer42, BigInteger.class));
		assertEqualTo(float42, DataType.convert(integer42, Float.class));
		assertEqualTo(double42, DataType.convert(integer42, Double.class));
		assertEqualTo(bigDecimal42, DataType.convert(integer42, BigDecimal.class));

		assertEqualTo(integer42, DataType.convert(long42, Integer.class));
		assertEqualTo(long42, DataType.convert(long42, Long.class));
		assertEqualTo(bigInteger42, DataType.convert(long42, BigInteger.class));
		assertEqualTo(float42, DataType.convert(long42, Float.class));
		assertEqualTo(double42, DataType.convert(long42, Double.class));
		assertEqualTo(bigDecimal42, DataType.convert(long42, BigDecimal.class));

		assertEqualTo(integer42, DataType.convert(bigInteger42, Integer.class));
		assertEqualTo(long42, DataType.convert(bigInteger42, Long.class));
		assertEqualTo(bigInteger42, DataType.convert(bigInteger42, BigInteger.class));
		assertEqualTo(float42, DataType.convert(bigInteger42, Float.class));
		assertEqualTo(double42, DataType.convert(bigInteger42, Double.class));
		assertEqualTo(bigDecimal42, DataType.convert(bigInteger42, BigDecimal.class));

		assertEqualTo(integer42, DataType.convert(float42, Integer.class));
		assertEqualTo(long42, DataType.convert(float42, Long.class));
		assertEqualTo(bigInteger42, DataType.convert(float42, BigInteger.class));
		assertEqualTo(float42, DataType.convert(float42, Float.class));
		assertEqualTo(double42, DataType.convert(float42, Double.class));
		assertEqualTo(bigDecimal42, DataType.convert(float42, BigDecimal.class));

		assertEqualTo(integer42, DataType.convert(double42, Integer.class));
		assertEqualTo(long42, DataType.convert(double42, Long.class));
		assertEqualTo(bigInteger42, DataType.convert(double42, BigInteger.class));
		assertEqualTo(float42, DataType.convert(double42, Float.class));
		assertEqualTo(double42, DataType.convert(double42, Double.class));
		assertEqualTo(bigDecimal42, DataType.convert(double42, BigDecimal.class));

		assertEqualTo(integer42, DataType.convert(bigDecimal42, Integer.class));
		assertEqualTo(long42, DataType.convert(bigDecimal42, Long.class));
		assertEqualTo(bigInteger42, DataType.convert(bigDecimal42, BigInteger.class));
		assertEqualTo(float42, DataType.convert(bigDecimal42, Float.class));
		assertEqualTo(double42, DataType.convert(bigDecimal42, Double.class));
		assertEqualTo(bigDecimal42, DataType.convert(bigDecimal42, BigDecimal.class));
	}

	@Test
	public void testGetPrimitives() throws Exception {
		assertEquals(42, DataType.getInt(integer42));
		assertEquals(42, DataType.getInt(long42));
		assertEquals(42, DataType.getInt(bigInteger42));
		assertEquals(42, DataType.getInt(float42));
		assertEquals(42, DataType.getInt(double42));
		assertEquals(42, DataType.getInt(bigDecimal42));

		assertEquals(42L, DataType.getLong(integer42));
		assertEquals(42L, DataType.getLong(long42));
		assertEquals(42L, DataType.getLong(bigInteger42));
		assertEquals(42L, DataType.getLong(float42));
		assertEquals(42L, DataType.getLong(double42));
		assertEquals(42L, DataType.getLong(bigDecimal42));

		compareFloats(42.0f, DataType.getFloat(integer42));
		compareFloats(42.0f, DataType.getFloat(long42));
		compareFloats(42.0f, DataType.getFloat(bigInteger42));
		compareFloats(42.0f, DataType.getFloat(float42));
		compareFloats(42.0f, DataType.getFloat(double42));
		compareFloats(42.0f, DataType.getFloat(bigDecimal42));

		compareDoubles(42.0, DataType.getDouble(integer42));
		compareDoubles(42.0, DataType.getDouble(long42));
		compareDoubles(42.0, DataType.getDouble(bigInteger42));
		compareDoubles(42.0, DataType.getDouble(float42));
		compareDoubles(42.0, DataType.getDouble(double42));
		compareDoubles(42.0, DataType.getDouble(bigDecimal42));
	}

	@Test
	public void testLimits() throws Exception {
		assertEquals(Integer.MAX_VALUE, DataType.getInt(integerMaxAsLong));
		assertEquals(Integer.MIN_VALUE, DataType.getInt(integerMinAsLong));

		assertEquals(Long.MAX_VALUE, DataType.getLong(longMaxAsBigInteger));
		assertEquals(Long.MIN_VALUE, DataType.getLong(longMinAsBigInteger));

		compareFloats(Float.MAX_VALUE, DataType.getFloat(floatMaxAsDouble));
		compareFloats(-Float.MAX_VALUE, DataType.getFloat(floatMinAsDouble));
	}

	@Test(expected = DataType.OverflowException.class)
	public void testOverflow1() throws Exception {
		DataType.getInt(integerMaxPlus1AsLong);
	}

	@Test(expected = DataType.OverflowException.class)
	public void testOverflow2() throws Exception {
		DataType.getInt(integerMinMinus1AsLong);
	}

	@Test(expected = DataType.OverflowException.class)
	public void testOverflow3() throws Exception {
		DataType.getLong(longMaxPlus1AsBigInteger);
	}

	@Test(expected = DataType.OverflowException.class)
	public void testOverflow4() throws Exception {
		DataType.getLong(longMinMinus1AsBigInteger);
	}

	// @Test(expected = DataType.OverflowException.class)
	public void testOverflow5() throws Exception {
		DataType.getFloat(floatMaxPlus1AsDouble);
	}

	// @Test(expected = DataType.OverflowException.class)
	public void testOverflow6() throws Exception {
		DataType.getFloat(floatMinMinus1AsDouble);
	}

	@Test(expected = DataType.OverflowException.class)
	public void testOverflow7() throws Exception {
		DataType.getDouble(doubleMaxPlus1AsBigDecimal);
	}

	@Test(expected = DataType.OverflowException.class)
	public void testOverflow8() throws Exception {
		DataType.getDouble(doubleMinMinus1AsBigDecimal);
	}

	@Test
	public void testValue() {
		doValueTest(false, "", "");
		doValueTest(true, Path.parse("`path`"), "`path`"); // Path
		doValueTest(true, "ab\nc", "\"ab\\nc\""); // Text
		doValueTest(false, "", "\"abc");
	}

	@Test
	public void testTemporals() {
		doDateTest(Boolean.TRUE, "date", "1957/04/30");
		doDateTest(Boolean.TRUE, "time", "1:2");
		doDateTest(Boolean.TRUE, "time", "01:23");
		doDateTest(Boolean.TRUE, "time", "01:23:24");

		doDateTest(LocalTime.of(1, 23, 45), "temporal", "@01:23:45");
		doDateTest(LocalDate.of(1957, 4, 30), "temporal", "@1957/04/30");
		doDateTest(LocalDateTime.of(1957, 4, 30, 1, 23, 45), "temporal", "@1957/04/30 01:23:45");
	}

	@Test
	public void testNumbers() {
		doNumberTest(new Long(0), DataType.any, "0");
		doNumberTest(new Long(123), DataType.any, "123");
		doNumberTest(new Long(-123), DataType.any, "-123");
		doNumberTest(new Long(123), DataType.any, "123z");

		doNumberTest(new Long(4), DataType.any, "0b100");
		doNumberTest(new Long(17), DataType.any, "0x11");
		doNumberTest(new Long(634799), DataType.any, "0x09afAF");

		doNumberTest(new BigInteger("123"), DataType.any, "123Z");
		doNumberTest(new BigInteger("-123"), DataType.any, "-123Z");

		doNumberTest(new Double(0), DataType.any, "0.0");
		doNumberTest(new Double(200), DataType.any, "2e2");
		doNumberTest(new Double(-0.02), DataType.any, "-2e-2");
		doNumberTest(new Double(0.3), DataType.any, ".3");
		doNumberTest(new Double(-.3), DataType.any, "-.3");
		doNumberTest(new Double(1.23), DataType.any, "1.23f");

		doNumberTest(new BigDecimal(0.5), DataType.any, "0.5F");

		doNumberTest(new Long(0), DataType.z, "0");
		doNumberTest(new BigInteger("0"), DataType.Z, "0");
		doNumberTest(new Double(0), DataType.f, "0");
		doNumberTest(new BigDecimal(0), DataType.F, "0");
	}

	@Test
	public void testCardinality() {
		doCardinalityTest(Cardinality.ZERO_MANY, "*");
		doCardinalityTest(Cardinality.ONE_MANY, "1..*");
		doCardinalityTest(Cardinality.newInstance("3..4"), "3..4");
	}

	@Test
	public void testKeywords() {
		doValueTest(true, null, "null");
		doValueTest(true, Boolean.TRUE, "true");
		doValueTest(true, Boolean.FALSE, "false");
	}

	@Test
	public void testArray() {
		doArrayTest("[ true, false, null ]", DataType.bool, Boolean.TRUE, Boolean.FALSE, null);
		doArrayTest("[ ]", DataType.z);
		doArrayTest("[1,2]", DataType.z, new Long(1), new Long(2));
		doArrayTest("[1,2F]", DataType.F, new BigDecimal(1), new BigDecimal(2));
		doArrayTest("[\"abc\", 1, 2Z, null]", DataType.any,
				"abc", new Long(1), new BigInteger("2"), null);
	}

	@Test
	public void testTextToInternalFormat() {
		assertNull(DataType.textToInternalFormat(null));
		assertEquals("", DataType.textToInternalFormat("\"\""));
		assertEquals("a", DataType.textToInternalFormat("\"a\""));
		assertEquals("\t", DataType.textToInternalFormat("\"\\t\""));
		assertEquals("\n", DataType.textToInternalFormat("\"\\n\""));
		assertEquals("\"", DataType.textToInternalFormat("\"\\\"\""));
		assertEquals("\\", DataType.textToInternalFormat("\"\\\\\""));

		assertEquals("a \t \n c", DataType.textToInternalFormat("\"a \\t \\n c\""));

		assertEquals("\t", DataType.textToInternalFormat("\"\\u9\""));
		assertEquals("\t", DataType.textToInternalFormat("\"\\u0009\""));
		assertEquals("\t1", DataType.textToInternalFormat("\"\\u00091\""));
		assertEquals("A", DataType.textToInternalFormat("\"\\u0041\""));
	}

	@Test(expected = AssertionError.class)
	public void testTextToInternalFormat1() {
		DataType.textToInternalFormat("");
	}

	@Test(expected = ParsingException.class)
	public void testTextToInternalFormat2() {
		DataType.textToInternalFormat("\"\\\"");
	}

	@Test(expected = ParsingException.class)
	public void testTextToInternalFormat3() {
		DataType.textToInternalFormat("\" \\x \"");
	}

	@Test(expected = ParsingException.class)
	public void testTextToInternalFormat4() {
		DataType.textToInternalFormat("\" \\ug \"");
	}
}
