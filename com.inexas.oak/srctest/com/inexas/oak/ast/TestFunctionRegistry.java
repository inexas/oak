/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.ast;

import static org.junit.Assert.*;
import org.junit.Test;
import com.inexas.oak.DataType;
import com.inexas.oak.ast.FunctionRegistry.Function;
import com.inexas.oak.ast.FunctionRegistry.FunctionException;
import com.inexas.oak.ast.FunctionRegistry.InvalidMethodException;
import com.inexas.oak.ast.testlibs.*;

public class TestFunctionRegistry {

	@Test
	public void testFunctionCreation() throws FunctionException, InvalidMethodException {
		final FunctionRegistry register = new FunctionRegistry();
		register.register(TestFunclib.class);
		assertEquals(5, register.map.size());

		final Function sf1 = register.map.get("sf1:0")[0];
		assertEquals("sf1", sf1.method.getName());
		assertEquals("sf1:0", sf1.key);
		assertEquals(DataType.z, sf1.returnType);
		assertTrue(sf1.isStatic);
		assertEquals(0, sf1.argumentCount);
		assertEquals(0, sf1.argumentTypes.length);
		assertEquals("sf1():z", sf1.signature);

		assertTrue(register.map.get("sf2:0")[0].isStatic);
		assertFalse(register.map.get("df:0")[0].isStatic);

		final Function allTypes = register.map.get("allTypes:17")[0];
		final DataType[] argumentTypes = allTypes.argumentTypes;
		assertEquals(allTypes.argumentCount, argumentTypes.length);

		assertEquals(DataType.z, argumentTypes[0]);
		assertEquals(DataType.z, argumentTypes[1]);
		assertEquals(DataType.z, argumentTypes[2]);
		assertEquals(DataType.z, argumentTypes[3]);
		assertEquals(DataType.Z, argumentTypes[4]);
		assertEquals(DataType.f, argumentTypes[5]);
		assertEquals(DataType.f, argumentTypes[6]);
		assertEquals(DataType.f, argumentTypes[7]);
		assertEquals(DataType.f, argumentTypes[8]);
		assertEquals(DataType.F, argumentTypes[9]);
		assertEquals(DataType.text, argumentTypes[10]);
		assertEquals(DataType.bool, argumentTypes[11]);
		assertEquals(DataType.bool, argumentTypes[12]);
		assertEquals(DataType.datetime, argumentTypes[13]);
		assertEquals(DataType.date, argumentTypes[14]);
		assertEquals(DataType.time, argumentTypes[15]);
		assertEquals(DataType.cardinality, argumentTypes[16]);

		assertEquals("allTypes("
				+ "Long, Long, Long, Long, BigInteger, "
				+ "Double, Double, Double, Double, BigDecimal, "
				+ "String, "
				+ "Boolean, Boolean, "
				+ "LocalDateTime, LocalDate, LocalTime, Cardinality):text", allTypes.signature);
	}

	@Test(expected = InvalidMethodException.class)
	public void testVoidMethod() throws FunctionException, InvalidMethodException {
		final FunctionRegistry register = new FunctionRegistry();
		register.register(TestFunclib1.class);
	}

	@Test(expected = FunctionException.class)
	public void testNonPublicMethod() throws FunctionException, InvalidMethodException {
		final FunctionRegistry register = new FunctionRegistry();
		register.register(TestFunclib2.class);
	}

	@Test(expected = InvalidMethodException.class)
	public void testNonStaticMethod() throws FunctionException, InvalidMethodException {
		final FunctionRegistry register = new FunctionRegistry();
		register.register(TestFunclib3.class);
	}

	@Test(expected = FunctionException.class)
	public void testNoMethod() throws FunctionException, InvalidMethodException {
		final FunctionRegistry register = new FunctionRegistry();
		register.register(TestFunclib4.class);
	}

	@Test(expected = InvalidMethodException.class)
	public void testDupe() throws FunctionException, InvalidMethodException {
		final FunctionRegistry register = new FunctionRegistry();
		register.register(TestFunclib.class);
		register.register(TestFunclib.class);
	}

	@Test
	public void testGet() throws FunctionException, InvalidMethodException {
		final FunctionRegistry register = new FunctionRegistry();
		register.register(TestFunclib.class);
		final ExpressionNode[] parameters = {
				new ConstantNode(null, 2),
				new ConstantNode(null, 3L)
		};
		final Function function = register.getFunction("intLong", parameters);
		assertEquals("intLong(Long, Long):text", function.signature);

		final ConstantNode node = function.invoke(null, new Object[] { new Long(2), new Long(3) });
		assertEquals(DataType.text, node.getType());
		assertEquals("6", node.getString());
	}
}
