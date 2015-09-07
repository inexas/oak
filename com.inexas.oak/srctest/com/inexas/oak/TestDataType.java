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
import java.math.*;
import org.junit.Test;

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

	private static final BigDecimal doubleMaxAsBigDecimal = BigDecimal.valueOf(Double.MAX_VALUE);
	private static final BigDecimal doubleMaxPlus1AsBigDecimal =
			BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.valueOf(1e100));
	private static final BigDecimal doubleMinAsBigDecimal = BigDecimal.valueOf(-Double.MAX_VALUE);
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

		compareDoubles(Double.MAX_VALUE, DataType.getDouble(doubleMaxAsBigDecimal));
		compareDoubles(-Double.MAX_VALUE, DataType.getDouble(doubleMinAsBigDecimal));

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

	@Test(expected = DataType.OverflowException.class)
	public void testOverflow5() throws Exception {
		DataType.getFloat(floatMaxPlus1AsDouble);
	}

	@Test(expected = DataType.OverflowException.class)
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

}