/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.dialect;

import static org.junit.Assert.*;
import org.junit.*;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.Advisory;
import com.inexas.oak.template.Constraint;
import com.inexas.tad.TadContext;

public class TestConstraints {
	private Advisory advisory;

	private String getFirstMessage() {
		final Advisory a = TadContext.get(Advisory.class);
		return a.getFirstError();
	}

	@Before
	public void before() {
		advisory = new Advisory("Unit test");
		TadContext.attach(advisory);
	}

	@After
	public void after() {
		TadContext.detach(advisory);
		advisory = null;
	}

	@Test
	public void testConstraintToString() {
		final Constraint constraint = new ChoiceConstraint("\"a\"", "\"b\"",
				"null");
		final String expected = "Constraint {\n"
				+ "\ttype: choice;\n"
				+ "\tvalue [\"a\", \"b\", null ]\n"
				+ "}\n";
		assertEquals(expected, constraint.toString());

		constraint.setDataType(DataType.text);
		assertEquals(expected, constraint.toString());
	}

	@Test
	public void testRegexpNoValues() {
		final Constraint constraint = new RegexpConstraint();
		assertNotNull(constraint);
	}

	@Test
	public void testRegexpNonStringValues() {
		final Constraint constraint = new RegexpConstraint("\"a\"", "2",
				"\"c\"");
		assertNotNull(constraint);
		assertTrue(getFirstMessage().indexOf("Invalid type") >= 0);
	}

	@Test
	public void testRegexp1() {
		final Constraint constraint = new RegexpConstraint("\"b\"", "\"a+\"");
		constraint.validate("b");
		constraint.validate("aaaa");
		assertNull(getFirstMessage());
	}

	@Test
	public void testRegexp2() {
		final Constraint constraint = new RegexpConstraint("\"b\"", "\"a+\"");
		constraint.validate("bb");
		assertTrue(getFirstMessage().indexOf("Invalid value") >= 0);
	}

	@Test
	public void testRegexpSingleValueToString() {
		final Constraint constraint = new RegexpConstraint("\"a\"");
		assertEquals(
				"Constraint {\n\ttype: regexp;\n\tvalue: \"a\"\n}\n",
				constraint.toString());
	}

	@Test
	public void testRegexpMultiValueToString() {
		final Constraint constraint = new RegexpConstraint("\"a\"", "\"b\"");
		assertEquals(
				"Constraint {\n\ttype: regexp;\n\tvalue [\"a\", \"b\" ]\n}\n",
				constraint.toString());
	}

}
