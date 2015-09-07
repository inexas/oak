package com.inexas.oak;

import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.Test;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.ast.*;

public class TestExpression {

	public static class TestFunclib {
		@Function(dynamic = true)
		public static LocalDateTime isDynamic() {
			return LocalDateTime.now();
		}

		@Function
		public static String isStatic() {
			return "x";
		}

		@Function
		public static Long echo(Long x) {
			return x;
		}

		@Function
		public static Double echo(Double x) {
			return x;
		}

		@Function
		public static BigDecimal echo(BigDecimal x) {
			return x;
		}

		@Function
		public static Boolean echo(Boolean x) {
			return x;
		}

		@Function
		public static String echo(String x) {
			return x;
		}

		@Function
		public static long primitive(long x) {
			return x;
		}

		@Function
		public static double primitive(double x) {
			return x;
		}

		@Function
		public static boolean primitive(boolean x) {
			return x;
		}

		@Function
		public static long plus(long x, long y) {
			return x + y;
		}

		@Function
		public static long minus(long x, long y) {
			return x - y;
		}

		@Function
		public static double minus(double x, double y) {
			return x - y;
		}
	}

	private void doTest(String expected, String toTest) throws OakException {
		final Expression expression = new Expression(toTest, TestFunclib.class);

		final String got = expression.toString();
		if(!expected.equals(got)) {
			System.err.println("exp: " + expected);
			System.err.println("got: " + got);
		}

		assertEquals(expected, got);
	}

	private void doTest(String toTest, boolean isStatic, String optimised, String evaluated)
			throws OakException {
		final Expression expression = new Expression(toTest, TestFunclib.class);

		String got = expression.toString();
		if(optimised != null) {
			if(!optimised.equals(got)) {
				System.err.println("exp: " + optimised);
				System.err.println("got: " + got);
			}
			assertEquals(optimised, got);
		}
		assertTrue(isStatic == expression.isStatic());

		final ConstantNode constant = expression.evaluate();

		if(evaluated != null) {
			got = constant.toString();
			if(!evaluated.equals(got)) {
				System.err.println("exp: " + evaluated);
				System.err.println("got: " + got);
			}
			assertEquals(evaluated, got);
		}
	}

	@Test
	public void testz() throws OakException {
		// Unary..
		doTest("" + -2, "-2");
		doTest("" + -(2), "-(2)");
		doTest("" + ~1, "~1");

		// Binary..

		// Arithmetic...
		doTest("2", "1*2");
		doTest("2", "4/2");
		doTest("1", "5%2");
		doTest("3", "1+2");
		doTest("-1", "1-2");

		// Shifts...
		doTest("4", "2<<1");
		doTest("1", "2>>1");
		doTest("" + (-1L >>> 1), "-1>>>1");

		// Comparisons...
		doTest("true", "2<3");
		doTest("false", "3<3");
		doTest("false", "4<3");
		doTest("true", "2<=3");
		doTest("true", "3<=3");
		doTest("false", "4<=3");
		doTest("false", "2>=3");
		doTest("true", "3>=3");
		doTest("true", "4>=3");
		doTest("false", "2>3");
		doTest("false", "3>3");
		doTest("true", "4>3");

		// Equal, not equal...
		doTest("false", "4=3");
		doTest("true", "4=4");
		doTest("true", "4!=3");
		doTest("false", "4!=4");

		// Bitwise...
		doTest("1", "3&1");
		doTest("1", "0^1");
		doTest("5", "5|4");

		// Binary, hex
		doTest("3", "0b11");
		doTest("17", "0x11");

		// Underlines
		doTest("1000000", "1_000_000");
	}

	@Test
	public void testZ() throws OakException {
		// Unary..
		doTest("" + -2, "-2Z");
		doTest("" + -(2), "-(2Z)");

		// Binary..

		// Arithmetic...
		doTest("2", "1*2Z");
		doTest("2", "4/2Z");
		doTest("1", "5Z%2");
		doTest("3", "1Z+2");
		doTest("-1", "1Z-2");

		// Shifts...
		doTest("4", "2Z<<1");
		doTest("1", "2Z>>1");

		// Comparisons...
		doTest("true", "2Z<3");
		doTest("false", "3Z<3");
		doTest("false", "4Z<3");
		doTest("true", "2Z<=3");
		doTest("true", "3Z<=3");
		doTest("false", "4Z<=3");
		doTest("false", "2Z>=3");
		doTest("true", "3Z>=3");
		doTest("true", "4Z>=3");
		doTest("false", "2Z>3");
		doTest("false", "3Z>3");
		doTest("true", "4Z>3");

		// Equal, not equal...
		doTest("false", "4Z=3");
		doTest("true", "4Z=4");
		doTest("true", "4Z!=3");
		doTest("false", "4Z!=4");

		// Bitwise...
		doTest("1", "3Z&1");
		doTest("1", "0Z^1");
		doTest("5", "5Z|4");

		// Underlines
		doTest("1000000", "1_000_000Z");
	}

	@Test
	public void testBoolean() throws OakException {
		// Literals...
		doTest("true", "true");
		doTest("false", "false");

		// Unary..
		doTest("false", "!true");

		// Logical...
		doTest("true", "true&&true");
		doTest("false", "true&&false");
		doTest("true", "true||true");
		doTest("true", "true||false");
		doTest("true", "false||true");
		doTest("false", "false||false");

		// Conditional...
		doTest("1", "true?1:2");
		doTest("2", "false?1:2");
	}

	@Test
	public void testString() throws OakException {
		// Literal...
		doTest("\"abc\"", "\"abc\"");

		// Concatenation...
		doTest("\"abcdef\"", "\"abc\"+\"def\"");

		// Unicode...
		doTest("\"ABC\"", "\"A\\u0042C\"");

		// Escape codes...
		doTest("\"a\\bc\bde\tfg\nhi\fjk\rlm\"nop\"", "\"a\\\\bc\\bde\\tfg\\nhi\\fjk\\rlm\\\"nop\"");
	}

	@Test
	public void testNull() throws OakException {
		doTest("null", "null");
	}

	@Test
	public void testDates() throws OakException {
		doTest("@12:15:00", "@12:15:00");
		doTest("@2012/4/30 02:05:06", "@2012/4/30 02:05:06");
		doTest("@2012/4/30", "@2012/4/30");
	}

	@Test
	public void testPrecedence() throws OakException {
		doTest("17", "2+3*5");
		doTest("11", "2*3+5");
		doTest("25", "(2+3)*5");
	}

	@Test
	public void testFunctions() throws OakException {
		doTest("isDynamic()", false, "isDynamic()", null);
		doTest("1", "minus(2, 1)");
		doTest("1", "primitive(1)");
		doTest("isStatic()", true, "\"x\"", "\"x\"");

		doTest("1", "echo(1)");
		doTest("1.0", "echo(1.0)");
		// todo Test DECIMAL etc. doTest("12.34", "echo(0d12.34)");
		doTest("true", "echo(true)");
		doTest("\"abc\"", "echo(\"abc\")");

		doTest("1", "primitive(1)");
		doTest("1.0", "primitive(1.0)");
		doTest("false", "primitive(false)");

		doTest("14", "plus(plus(2, 3), plus(4, 5))");

		doTest("1", "minus(2, 1)");
		doTest("-2", "minus(2, 1)+plus(-1, -2)");
	}

	@Test
	public void testf() throws OakException {
		// Test type conversion
		doTest("6.0", "4.0+2");
		doTest("6.0", "4+2.0");
		doTest("6.0", "6f");

		// Unary..
		doTest("" + -2.0, "-2.0");
		doTest("" + -(2.0), "-(2.0)");

		// Binary..

		// Arithmetic...
		doTest("3.0", "1+2.0");
		doTest("2.0", "1*2.0");
		doTest("-1.0", "1-2.0");
		doTest("2.0", "4/2.0");

		// Comparisons...
		doTest("true", "2<3.0");
		doTest("false", "3<3.0");
		doTest("false", "4<3.0");
		doTest("true", "2<=3.0");
		doTest("true", "3<=3.0");
		doTest("false", "4<=3.0");
		doTest("false", "2>=3.0");
		doTest("true", "3>=3.0");
		doTest("true", "4>=3.0");
		doTest("false", "2>3.0");
		doTest("false", "3>3.0");
		doTest("true", "4>3.0");

		// Equal, not equal...
		doTest("false", "4=3.0");
		doTest("true", "4=4.0");
		doTest("true", "4!=3.0");
		doTest("false", "4!=4.0");

		// Exponentials...
		doTest("2000.0", "2e3");
		doTest("201.0", "1+2e2");
		doTest("1.02", "1+2e-2");
	}

	@Test
	public void testF() throws OakException {
		/*
		 * todo There are three dimensions to BigDecimals: the scale: how many
		 * numbers after the decimal point, the rounding: how the number is
		 * rounded and the precision: how many decimal digits in all are used to
		 * specify the number
		 *
		 * 21.34 the 'F' specifies a BigDecimal
		 *
		 * 23.45F<2,3,HALF_UP> could be used to specify scale, precision,
		 * rounding
		 *
		 * Number(2, 3, "HALF_UP", 23.45) or like that
		 */

		// Unary..
		doTest("" + -2.0, "-2.0F");
		doTest("" + -(2.0), "-(2.0F)");

		// Binary..

		// Arithmetic...
		doTest("3.0", "1+2F");
		doTest("2.0", "1*2F");
		doTest("-1.0", "1-2F");
		doTest("2.0", "4/2F");

		// Comparisons...
		doTest("true", "2<3F");
		doTest("false", "3<3F");
		doTest("false", "4<3F");
		doTest("true", "2<=3F");
		doTest("true", "3<=3F");
		doTest("false", "4<=3F");
		doTest("false", "2>=3F");
		doTest("true", "3>=3F");
		doTest("true", "4>=3F");
		doTest("false", "2>3F");
		doTest("false", "3>3F");
		doTest("true", "4>3F");

		// Equal, not equal...
		doTest("false", "4=3F");
		doTest("true", "4=4F");
		doTest("true", "4!=3F");
		doTest("false", "4!=4F");

		// Exponentials...
		doTest("2000.0", "2e3");
		doTest("201.0", "1+2e2");
		doTest("1.02", "1+2e-2");
	}

}
