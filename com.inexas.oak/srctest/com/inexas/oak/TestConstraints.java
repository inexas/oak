/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.inexas.oak.advisory.OakException;

/**
 * @author kwhittingham
 *
 */
public class TestConstraints {

	@Test(expected = OakException.class)
	public void testRegexpNoValues() throws OakException {
		@SuppressWarnings("unused")
		final Constraint constraint = new RegexpConstraint();
	}

	@Test(expected = OakException.class)
	public void testRegexpNonStringValues() throws OakException {
		@SuppressWarnings("unused")
		final Constraint constraint = new RegexpConstraint("a", new Integer(2), "c");
	}

	@Test
	public void testRegexp1() throws OakException {
		final Constraint constraint = new RegexpConstraint("b", "a+");
		constraint.validate("b");
		constraint.validate("aaaa");
	}

	@Test(expected = OakException.class)
	public void testRegexp2() throws OakException {
		final Constraint constraint = new RegexpConstraint("b", "a+");
		constraint.validate("bb");
	}

	@Test
	public void testRegexpSingleValueToString() throws OakException {
		final Constraint constraint = new RegexpConstraint("a");
		assertEquals("Constraint {\n\ttype: regexp;\n\tvalue: \"a\"\n}\n", constraint.toString());
	}

	@Test
	public void testRegexpMultiValueToString() throws OakException {
		final Constraint constraint = new RegexpConstraint("a", "b");
		assertEquals("Constraint {\n\ttype: regexp;\n\tvalue [\"a\", \"b\" ]\n}\n", constraint.toString());
	}

	@Test
	public void constraintToString() throws OakException {
		final Constraint constraint = new ChoiceConstraint("a\n\\\tb", "b", null);
		final String expected = "Constraint {\n"
				+ "\ttype: choice;\n"
				+ "\tvalue [\"a\\n\\\tb\", \"b\", null ]\n"
				+ "}\n";
		assertEquals(expected, constraint.toString());

		constraint.setDataType(DataType.text);
		assertEquals(expected, constraint.toString());
	}

}
