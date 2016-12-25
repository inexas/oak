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
import com.inexas.oak.ast.LibraryRegistry.*;
import com.inexas.oak.ast.LibraryRegistry.Function;
import com.inexas.oak.ast.testlibs.*;

public class TestLibrary {

	@Test
	public void testFunctionCreation() throws InvalidMethodException {
		final LibraryRegistry register = new LibraryRegistry();
		register.register(new TestFunclib());
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
		assertEquals(DataType.Z, argumentTypes[2]);
		assertEquals(DataType.Z, argumentTypes[3]);
		assertEquals(DataType.Z, argumentTypes[4]);
		assertEquals(DataType.f, argumentTypes[5]);
		assertEquals(DataType.f, argumentTypes[6]);
		assertEquals(DataType.F, argumentTypes[7]);
		assertEquals(DataType.F, argumentTypes[8]);
		assertEquals(DataType.F, argumentTypes[9]);
		assertEquals(DataType.text, argumentTypes[10]);
		assertEquals(DataType.bool, argumentTypes[11]);
		assertEquals(DataType.bool, argumentTypes[12]);
		assertEquals(DataType.datetime, argumentTypes[13]);
		assertEquals(DataType.date, argumentTypes[14]);
		assertEquals(DataType.time, argumentTypes[15]);
		assertEquals(DataType.cardinality, argumentTypes[16]);

		assertEquals("allTypes("
				+ "Integer, Integer, BigInteger, BigInteger, BigInteger, "
				+ "Float, Float, BigDecimal, BigDecimal, BigDecimal, "
				+ "String, "
				+ "Boolean, Boolean, "
				+ "LocalDateTime, LocalDate, LocalTime, Cardinality):text", allTypes.signature);
	}

	@Test(expected = InvalidMethodException.class)
	public void testVoidMethod() throws InvalidMethodException {
		final LibraryRegistry register = new LibraryRegistry();
		register.register(new TestFunclib1());
	}

	@Test
	public void testNonPublicMethod() throws InvalidMethodException {
		final LibraryRegistry register = new LibraryRegistry();
		register.register(new TestFunclib2());
	}

	@Test
	public void testNoMethod() throws InvalidMethodException {
		final LibraryRegistry register = new LibraryRegistry();
		register.register(new TestFunclib4());
	}

	@Test(expected = InvalidMethodException.class)
	public void testDupe() throws InvalidMethodException {
		final LibraryRegistry register = new LibraryRegistry();
		register.register(new TestFunclib());
		register.register(new TestFunclib());
	}

	@Test
	public void testGet() throws InvalidMethodException, LibraryException {
		final LibraryRegistry register = new LibraryRegistry();
		register.register(new TestFunclib());
		final ExpressionNode[] parameters = {
				new ConstantNode(null, 2),
				new ConstantNode(null, 3)
		};
		final Function function = register.getFunction("intInt", parameters);
		assertEquals("intInt(Integer, Integer):text", function.signature);

		final ConstantNode node = function.invoke(null, new Object[] { new Integer(2), new Integer(3) });
		assertEquals(DataType.text, node.getType());
		assertEquals("6", node.getString());
	}
}
