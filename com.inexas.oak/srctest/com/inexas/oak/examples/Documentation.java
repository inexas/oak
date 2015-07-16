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
import com.inexas.oak.*;
import com.inexas.oak.advisory.*;

public class Documentation {

	public static void main(String[] args) {
		example();
	}

	public static void processString() {
		final Oak oak = new Oak("meaningOfLife:42;");
		oak.toAst();
		final Advisory advisory = oak.getAdvisory();
		if(advisory.hasErrors()) {
			System.err.println(advisory);
		} else {
			final ToStringVisitor toStringVisitor = new ToStringVisitor(true);
			oak.accept(toStringVisitor);
			System.out.println(toStringVisitor.toString());
		}
	}

	public static void toStringExample() {
		final Oak oak = new Oak("meaningOfLife:42;");
		oak.toAst();
		// todo Check for errors
		final ToStringVisitor toStringVisitor = new ToStringVisitor(true);
		oak.accept(toStringVisitor);
		System.out.println(toStringVisitor.toString());
	}

	public static void example() {
		try {
			// Create a Person dialect...
			final Oak personDialectOak = new Oak(new File("datatest/oak/Person.dialect"));
			final Dialect personDialect = personDialectOak.toDialect();

			// Parse a person file...
			final Oak personOak = new Oak(new File("datatest/oak/John.person"));
			final Person person = personOak.toObjectTree(personDialect);
			System.out.println(person.toString());

			// Write and use the Java dialect files...
			personDialect.write("srctest", "com.inexas.oak.examples");
			final Person john = personOak.toObjectTree(PersonDialect.class);
			System.out.println(john.toString());
		} catch(final OakException e) {
			System.out.println(e.getAdvisory());
		}
	}

}
