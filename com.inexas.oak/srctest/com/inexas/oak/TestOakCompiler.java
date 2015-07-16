package com.inexas.oak;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.advisory.OakException;
import com.inexas.util.FileU;

/**
 * This runs a test but also rebuilds the Oak dialect files.
 */
public class TestOakCompiler {

	private void doStringBasedTest(String string) {
		final Object root = doTest(new Oak(string), false, "com.inexas.oak");
		assertTrue(root instanceof Dialect);
		// final String got = rule.toString(false);
		// final String exp = string.replaceAll(" ", "");
		// assertEquals(exp, got);
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
	public void testOak() {
		final File file = new File(FileU.DATATEST + "oak/OakTest.dialect");
		if(!file.exists()) {
			throw new InexasRuntimeException("No such file: " + file.getAbsolutePath());
		}

		doTest(new Oak(file), true, "com.inexas.oak");
	}

	@Test
	public void testObject() {
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
				+ "      key:choice;\n"
				+ "      value[\"a\",\"b\"]\n"
				+ "      value:2*3;\n"
				+ "    }\n"
				+ "  }, {\n"
				+ "    key:q;\n"
				+ "    type:identifier;\n"
				+ "  }]\n"
				+ "}\n");
	}
}
