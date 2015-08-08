package com.inexas.oak.ast;

import static org.junit.Assert.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;
import com.inexas.oak.Oak;
import com.inexas.oak.advisory.*;

public class TestOakObjects {

	private void doTest(String toTest) throws OakException {
		doTest(toTest, toTest);
	}

	private void doPrettyTest(String expected, String toTest) {
		try {
			final Oak oak = new Oak(toTest);
			oak.toAst();
			checkParsingErrors(oak);
			final ToStringVisitor toStringVisitor = new ToStringVisitor(true);
			oak.accept(toStringVisitor);
			final String got = toStringVisitor.toString();

			if(!expected.equals(got)) {
				System.out.println("Got as is:");
				System.out.println(got);
				System.err.println("exp: " + StringEscapeUtils.escapeJava(expected));
				System.err.println("got: " + StringEscapeUtils.escapeJava(got));
			}

			assertEquals(expected, got);
		} catch(final OakException e) {
			System.err.println(e.getAdvisory());
			fail();
		}
	}

	private void doTest(String expected, String toTest) throws OakException {
		final Oak oak = new Oak(toTest);
		oak.toAst();
		checkParsingErrors(oak);
		final ToStringVisitor toStringVisitor = new ToStringVisitor(false);
		// toStringVisitor.setTracing(true);
		oak.accept(toStringVisitor);
		final String got = toStringVisitor.toString();

		if(!expected.equals(got)) {
			System.out.println(oak);
			System.err.println("exp: " + expected);
			System.err.println("got: " + got);
		}

		assertEquals(expected, got);
	}

	private void checkParsingErrors(Oak oak) {
		final Advisory advisory = oak.getAdvisory();
		if(advisory.hasErrors()) {
			System.err.println(advisory);
			fail();
		}
	}

	@Test(expected = OakException.class)
	public void testKeyCases1() throws OakException {
		doTest("a{b:1;}");
	}

	@Test(expected = OakException.class)
	public void testKeyCases2() throws OakException {
		doTest("A{B:1;}");
	}

	@Test
	public void testKeyCases3() throws OakException {
		doTest("A{b:1;}");
	}

	@Test
	public void testPretty() {
		doPrettyTest(
				"A [\n\t{\n\t\ta: 1;\n\t}, {\n\t\tb: 2;\n\t}\n]\n",
				"A[{a:1;},{b:2;}]");

		doPrettyTest(
				"A {\n\tb: 1;\n}\n",
				"A{b:1;}");

		doPrettyTest(
				"A {\n\tB {\n\t\tb: 1;\n\t}\n\n\tC {\n\t\tc: 2;\n\t}\n}\n",
				"A{B{b:1;}C{c:2;}}");

	}

	// @Test
	public void testObjects() throws OakException {
		doTest("a{b:1;}");
		doTest("a{b:1;c:2;d:3;}");
	}

}
