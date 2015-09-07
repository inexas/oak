package com.inexas.oak.ast;

import static org.junit.Assert.*;
import org.junit.Test;
import com.inexas.oak.Oak;
import com.inexas.oak.advisory.*;

public class TestAstBuild {

	private void expectException(String expected, String string) {
		try {
			doTest(string, string);
		} catch(final OakException e) {
			final String message = e.getMessage();
			if(message.indexOf(expected) < 0) {
				System.err.println("Expected: " + expected);
				System.err.println("But got: " + e.getMessage());
				fail();
			}
		}
	}

	private void doTest(String expected, String toTest) throws OakException {
		final Oak oak = new Oak(toTest);
		checkParsingErrors(oak);
		final AstToStringVisitor toStringVisitor = new AstToStringVisitor(false);
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

	@Test
	public void testNoInput() {
		expectException("Null or empty string", null);
		expectException("Null or empty string", "");
		expectException("Null or empty string", " ");
		expectException("Syntax error", " \t\n // asdf\n/*Crap*/");
	}

	@Test
	public void testNoDiactlect() throws OakException {
		doTest("A{b:true;}", "A{b;}");
	}

}
