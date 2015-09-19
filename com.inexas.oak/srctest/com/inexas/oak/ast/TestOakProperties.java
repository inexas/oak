package com.inexas.oak.ast;

import static org.junit.Assert.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;
import com.inexas.oak.*;
import com.inexas.oak.advisory.*;
import com.inexas.tad.TadRuntimeException;

public class TestOakProperties {

	private void doTest(String toTest) throws OakException {
		doTest(toTest, toTest);
	}

	private void doPrettyTest(String expected, String toTest) throws OakException {
		final Oak oak = new Oak(toTest);
		checkParsingErrors(oak);
		final AstToStringVisitor toStringVisitor = new AstToStringVisitor(true);
		oak.accept(toStringVisitor);
		final String got = toStringVisitor.toString();

		if(!expected.equals(got)) {
			System.out.println("Got as is:");
			System.out.println(got);
			System.err.println("exp: " + StringEscapeUtils.escapeJava(expected));
			System.err.println("got: " + StringEscapeUtils.escapeJava(got));
		}

		assertEquals(expected, got);
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

	private void doTest(String toTest, DataType expectedType) throws OakException {
		final Oak oak = new Oak(toTest);
		final PairNode root = oak.getRoot();
		assertEquals(expectedType, root.getType());
	}

	@Test
	public void testPairs() throws OakException {
		doTest("a:1;");
		doTest("a:true;", "a;");
		doTest("a:true;");
		doTest("a:false;");
		doTest("a:null;");
		doTest("a:\"b\";");
		doTest("a:3;", "a:1+2;");

		// Test cardinality...
		doTest("a:0..1;", "a: 0..1;");
		doTest("a:0..*;", "a:0..*;");
	}

	@Test
	public void testPairPaths() throws OakException {
		doTest("a:`/`;", "a:`/`;");
		doTest("a:`.`;", "a:`.`;");
		doTest("a:`..`;", "a:`..`;");

		doTest("a:`/b`;", "a:`/b`;");
		doTest("a:`./b`;", "a:`./b`;");
		doTest("a:`../b`;", "a:`../b`;");

		doTest("a:`/b/c`;", "a:`/b/c`;");

		doTest("a:`/b[0]`;", "a:`/b[0]`;");
	}

	@Test
	public void testArrays() throws OakException {
		doTest("a[abc]");
		doTest("a[1]");
		doTest("a[1,2]");
		doTest("a[1,3,\"b\",4]");

		doTest("a[abc]");
		doTest("a[true,ghi]");
	}

	@Test
	public void testPretty() throws OakException {
		doPrettyTest("a [\n\t1, 2, 3\n]\n", "a[1,2,3]");
		doPrettyTest("a [\n"
				+ "\t1, 2, 3, 4, 5, 6, 7, 8, 9, 0,\n"
				+ "\t1, 2, 3, 4, 5, 6, 7, 8, 9, 0,\n"
				+ "\t1, 2\n]\n",
				"a[1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2]");
		doPrettyTest("a: 1;\n", "a:1;");
		doPrettyTest("a: true;\n", "a:true;");
		doPrettyTest("a: \"b\";\n", "a:\"b\";");
		doPrettyTest("a: 3;\n", "a:1+2;");

		doPrettyTest("a [\n\t1\n]\n", "a[1]");
		doPrettyTest("a [\n\t1, 2\n]\n", "a[1,2]");
	}

	@Test
	public void testPrettyObjectArray() throws OakException {
		doPrettyTest("A [\n\t{\n\t\ta: 1;\n\t}, {\n\t\tb: 2;\n\t\tc: 3;\n\t}\n]\n",
				"A[{a:1;},{b:2;c:3;}]");
	}

	@Test
	public void testExpressions() throws OakException {
		// todo Collapsing (changing 1+1->2) should be optional
		// Plus
		doTest("a:2;", "a:1+1;");
		doTest("a:7;", "a:1+1+5;");

		// Minus
		doTest("a:0;", "a:1-1;");
		doTest("a:-2;", "a:1-1-2;");

		// Multiply
		doTest("a:35;", "a:7*5;");
		doTest("a:30;", "a:2*3*5;");

		// Divide
		doTest("a:1;", "a:2/2;");
		doTest("a:3;", "a:2+2/2;");

		// Complement
		doTest("a:-3;", "a:~2;");

		// Not
		doTest("a:false;", "a:!true;");

		// Comparisons
		doTest("a:false;", "a:2>2;");
		doTest("a:true;", "a:2>=2;");
		doTest("a:false;", "a:1=2;");
		doTest("a:true;", "a:1!=2;");
		doTest("a:true;", "a:2<=2;");
		doTest("a:false;", "a:2<2;");

		// Logical OR
		doTest("a:false;", "a:false||false;");
		doTest("a:true;", "a:true||false;");
		doTest("a:true;", "a:false||true;");
		doTest("a:true;", "a:true||true;");

		// Logical AND
		doTest("a:false;", "a:false&&false;");
		doTest("a:false;", "a:true&&false;");
		doTest("a:false;", "a:false&&true;");
		doTest("a:true;", "a:true&&true;");

		// Logical XOR
		doTest("a:false;", "a:false^false;");
		doTest("a:true;", "a:true^false;");
		doTest("a:true;", "a:false^true;");
		doTest("a:false;", "a:true^true;");

		// ? :
		doTest("a:2;", "a:true?2:5;");
		doTest("a:5;", "a:false?2:5;");
	}

	@SuppressWarnings("unused")
	@Test(expected = TadRuntimeException.class)
	public void testFunctionsNotLoaded() throws OakException {
		new Oak("a:abs(-3);");
	}

	@Test
	public void testDate() throws OakException {
		doTest("a:@2012/12/12;", "a: @2012/12/12;");
	}

	@Test
	public void testTime() throws OakException {
		doTest("a:@01:02;", "a: @01:02;");
	}

	@Test
	public void testDateTime() throws OakException {
		doTest("a:@2012/12/12 10:20:39;", "a: @2012/12/12 10:20:39;");
	}

	@Test
	public void testText() throws OakException {
		doTest("a:\"a\\nc\";", "a: \"a\nc\";");
	}

	@Test
	public void testNumbers() throws OakException {
		doTest("a:-1;", DataType.z);
		doTest("a:1;", DataType.z);
	}
}