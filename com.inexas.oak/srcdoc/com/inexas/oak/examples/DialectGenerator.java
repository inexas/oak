package com.inexas.oak.examples;

import java.io.File;
import com.inexas.oak.Oak;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.dialect.Rulebase;

public class DialectGenerator {

	public static void main(String[] args) {
		try {
			// Read Person.dialect
			final Oak personDialectOak = new Oak(new File("resources/Person.dialect"));
			// Convert the Oak file to a Rulebase
			final Rulebase personDialect = personDialectOak.toDialect();
			// Generate the runtime support
			personDialect.write("srcgen", "com.inexas.oak.examples");
			System.out.println("Runtime files created");
		} catch(final OakException e) {
			System.out.println(e.getAdvisory());
		}
	}
}
