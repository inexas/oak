package com.inexas.oak.dialect;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;
import com.inexas.oak.Oak;
import com.inexas.oak.advisory.OakException;
import com.inexas.util.FileU;

public class TestDialectBuild {

	private Rulebase doTest(String expected, String toTest) {
		try {
			final Rulebase result;
			final Oak oak = new Oak(toTest);
			result = oak.toDialect();
			assertNotNull(result);
			return result;
		} catch(final OakException e) {
			if(expected == null) {
				fail();
			} else {
				final String message = e.toString();
				if(message.indexOf(expected) < 0) {
					System.err.println("Expected: " + expected);
					System.err.println(" But got: " + e.getMessage());
					fail();
				}
			}
			return null;
		}
	}

	@Test
	public void testNoInput() {
		doTest("Null or empty string", null);
		doTest("Null or empty string", "");
		doTest("Null or empty string", " ");
		doTest("Syntax error", " \t\n // asdf\n/*Crap*/");
	}

	// todo Need better error handling @Test
	public void testNoDialect() {
		doTest("No such root object", "A{b;}");
		doTest("Need 1..1 key", "Dialect{b;}");
		doTest("Expected identifier but is: boolean", "Dialect{key;}");
		doTest(": text", "Dialect{key:\"d\";}");
	}

	@Test
	public void testVariousErrors() {
		doTest("ass not found", ("Dialect{"
				+ "	key:i;"
				+ "	Object{"
				+ "		key:o;"
				+ "		class:\"x\";"
				+ "		Member{"
				+ "			Property {"
				+ "				key:p;"
				+ "			}"
				+ "		}"
				+ "	}"
				+ "}").replace("\t", ""));
		doTest("upper case", ("Dialect{"
				+ "	key:i;"
				+ "	Object{"
				+ "		key:o;"
				+ "		class:\"com.inexas.oak.dialect.Template\";"
				+ "		Member{"
				+ "			Property {"
				+ "				key:p;"
				+ "			}"
				+ "		}"
				+ "	}"
				+ "}").replace("\t", ""));
	}

	@Test
	public void testInlineProperties() {
		final Rulebase dialect = doTest("xxx", (""
				+ "Dialect{"
				+ "	key:Person;"
				+ "	Object[{"
				+ "		key:Test;"
				+ "		root;"
				+ "		class:\"com.inexas.oak.dialect.Person\";"
				+ "		Member{"
				+ "			Property{"
				+ "				key:gender;"
				+ "				Constraint{"
				+ "					type:choice;"
				+ "					value[m,f]"
				+ "				}"
				+ "			}"
				+ "		}"
				+ "	}]"
				+ "}").replace("\t", ""));
		dialect.write(FileU.ROOT + "srcgentest", "com.inexas.oak.dialect");
	}

	@Test
	public void testGenerateOak() throws OakException {
		final Oak oak = new Oak(new File(FileU.DATA + "oak/Oak.dialect"));
		final Rulebase dialect = oak.toDialect();
		dialect.write(FileU.ROOT + "srcgentest", "com.inexas.oak.testoak");
	}

}
