package com.inexas.oak;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.advisory.*;
import com.inexas.util.FileU;

/**
 * This runs a test but also rebuilds the Oak dialect files.
 */
public class TestOakCompiler {

	private void doStringBasedTest(String string) throws OakException {
		final Object root = doTest(new Oak(string), false, "com.inexas.oak");
		assertTrue(root instanceof Dialect);
	}

	private Dialect doTest(Oak oak, boolean write, String packageName) {
		try {
			final Dialect result = oak.toDialect();
			if(write) {
				result.write("srcgentest", packageName);
			}
			return result;
		} catch(final OakException e) {
			System.out.println(e.getAdvisory());
			fail("Compiler errors");
			return null;
		}
	}

	@Test
	public void testOak() throws OakException {
		final File file = new File(FileU.DATATEST + "oak/OakTest.dialect");
		if(!file.exists()) {
			throw new InexasRuntimeException("No such file: " + file.getAbsolutePath());
		}

		doTest(new Oak(file), true, "com.inexas.oak");
	}

	@Test
	public void testObject() throws OakException {
		// doStringBasedTest(""
		// + "Dialect {"
		// + "  key:G;"
		// + "  Object [{"
		// + "    key:O;"
		// + "  },{"
		// + "    key:B;"
		// + "  }]"
		// + "}");
		doStringBasedTest(""
				+ "Dialect {\n"
				+ "  key:G;\n"
				+ "  Object {\n"
				+ "    key:O;\n"
				+ "    class:\"com.inexas.oak.TestObject\";\n"
				+ "    root;\n"
				+ "    Member {\n"
				+ "      key:p;\n"
				+ "    }\n"
				+ "    Member {\n"
				+ "      key:q;\n"
				+ "    }\n"
				+ "  }\n"
				+ "  Property [{\n"
				+ "    key:p;\n"
				+ "    type:identifier;\n"
				+ "    Constraint{\n"
				+ "      type:choice;\n"
				+ "      value[\"a\",\"b\"]\n"
				+ "    }\n"
				+ "  }, {\n"
				+ "    key:q;\n"
				+ "    type:identifier;\n"
				+ "  }]\n"
				+ "}\n");
	}

	// !todo Test Members when the referenced Property is missing
	// !todo Test Throwing OakCtorExcep catching it and turning it a normal
	// exception

	@Test
	public void testAntlrRecognisedError() {
		try {
			final Oak oak = new Oak("meaningOfLife:42;\n§");
			oak.toAst();
			final ToStringVisitor toStringVisitor = new ToStringVisitor(true);
			oak.accept(toStringVisitor);
			System.out.println(toStringVisitor.toString());
		} catch(final OakException e) {
			final Advisory advisory = e.getAdvisory();
			assertEquals(1, advisory.getErrorCount());
			assertEquals(0, advisory.getWarningCount());
			assertTrue(advisory.hasErrors());
		}
	}

}
