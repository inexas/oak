/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.examples;

import java.io.File;
import com.inexas.oak.Oak;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.ast.AstToStringVisitor;
import com.inexas.oak.dialect.Rulebase;

public class Documentation {

	public static void main(String[] args) {
		processString();
		toStringExample();
		toObjectTreeExample();
	}

	public static void processString() {
		try {
			final Oak oak = new Oak("meaningOfLife:42;");
			final AstToStringVisitor toStringVisitor = new AstToStringVisitor(true);
			oak.accept(toStringVisitor);
			System.out.println(toStringVisitor.toString());
		} catch(final OakException e) {
			System.err.println(e.getAdvisory().toString());
			e.printStackTrace();
		}
	}

	public static void toStringExample() {
		try {
			final Oak oak = new Oak("meaningOfLife:42;");
			final AstToStringVisitor toStringVisitor = new AstToStringVisitor(true);
			oak.accept(toStringVisitor);
			System.out.println(toStringVisitor.toString());
		} catch(final OakException e) {
			System.err.println(e.getAdvisory().toString());
			e.printStackTrace();
		}
	}

	public static void toObjectTreeExample() {
		try {
			// Create a Person dialect...
			final Oak personDialectOak = new Oak(new File("datatest/oak/Person.dialect"));
			final Rulebase personDialect = personDialectOak.toDialect();

			// Parse a person file...
			final Oak personOak = new Oak(new File("datatest/oak/John.person"));
			final Person person = personOak.toObjectTree(personDialect);
			System.out.println(person.toString());

			// Write and use the Java dialect files...
			personDialect.write("srctest", "com.inexas.oak.examples");
			// final Person john = personOak.toObjectTree(PersonDialect.class);
			// System.out.println(john.toString());
		} catch(final OakException e) {
			System.out.println(e.getAdvisory());
		}
	}

}
