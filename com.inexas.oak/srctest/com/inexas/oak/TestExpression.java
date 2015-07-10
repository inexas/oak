package com.inexas.oak;

import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.*;
import com.inexas.exception.*;
import com.inexas.oak.advisory.Advisory;
import com.inexas.tad.Context;

public class TestExpression {
	private final static FunctionRegister functionRegister = new FunctionRegister();

	@SuppressWarnings("unused")
	private static class TestMethods {
		public static String notStatic() {
			return "x";
		}

		public static String isStatic() {
			return "x";
		}

		public static Long echo(Long x) {
			return x;
		}

		public static Double echo(Double x) {
			return x;
		}

		public static BigDecimal echo(BigDecimal x) {
			return x;
		}

		public static Boolean echo(Boolean x) {
			return x;
		}

		public static String echo(String x) {
			return x;
		}

		public static long primitive(long x) {
			return x;
		}

		public static double primitive(double x) {
			return x;
		}

		public static boolean primitive(boolean x) {
			return x;
		}

		public static long plus(long x, long y) {
			return x + y;
		}

		public static double minus(double x, double y) {
			return x - y;
		}

	}

	static {
		try {
			register(true, DataType.integer, "primitive", DataType.integer);
			register(true, DataType.integer, "echo", DataType.integer);
			register(true, DataType.decimal, "primitive", DataType.decimal);
			register(true, DataType.decimal, "echo", DataType.decimal);
			register(true, DataType.precision, "echo", DataType.precision);
			register(true, DataType.bool, "primitive", DataType.bool);
			register(true, DataType.bool, "echo", DataType.bool);
			register(true, DataType.text, "echo", DataType.text);
			register(true, DataType.date, "echo", DataType.date);

			register(false, DataType.text, "notStatic");
			register(true, DataType.text, "isStatic");

			register(true, DataType.integer, "plus", DataType.integer, DataType.integer);

			register(true, DataType.decimal, "minus", DataType.decimal, DataType.decimal);
		} catch(final Exception e) {
			throw new InexasRuntimeException("Error loading test functions", e);
		}
	}

	private static void register(boolean isStatic, DataType type, String name, DataType... argumentTypes) {
		for(final Method method : TestMethods.class.getMethods()) {
			if(method.getName().equals(name) && method.getParameterCount() == argumentTypes.length) {
				boolean soFarSoGood = true;
				for(int i = 0; i < argumentTypes.length && soFarSoGood; i++) {
					final DataType argumentType = argumentTypes[i];
					final Class<?> parameterType = method.getParameterTypes()[i];
					switch(argumentType) {
					case bool:
						soFarSoGood = parameterType == Boolean.class || parameterType == boolean.class;
						break;

					case text:
						soFarSoGood = parameterType == String.class;
						break;

					case date:
						soFarSoGood = parameterType == Date.class;
						break;

					case precision:
						soFarSoGood = parameterType == BigDecimal.class;
						break;

					case decimal:
						soFarSoGood = parameterType == Double.class || parameterType == double.class;
						break;

					case integer:
						soFarSoGood = parameterType == Long.class || parameterType == long.class;
						break;

						// $CASES-OMITTED$
					default:
						throw new UnexpectedException("register: ");
					}
				}

				if(soFarSoGood) {
					functionRegister.register(name, method, type, isStatic, argumentTypes);
					break;
				}
			}
		}
	}

	@Before
	public void setUp() {
		Context.attach(functionRegister);
	}

	@After
	public void tearDown() {
		Context.detach(functionRegister);
	}

	private void doTest(String expected, String toTest) {
		final Expression expression = new Expression(toTest);
		checkParsingErrors(expression);

		final String got = expression.toString();
		if(!expected.equals(got)) {
			System.err.println("exp: " + expected);
			System.err.println("got: " + got);
		}

		assertEquals(expected, got);
	}

	private void doTest(String toTest, boolean isStatic, String optimised, String evaluated) {
		final Expression expression = new Expression(toTest);
		checkParsingErrors(expression);

		String got = expression.toString();
		if(!optimised.equals(got)) {
			System.err.println("exp: " + optimised);
			System.err.println("got: " + got);
		}
		assertEquals(optimised, got);
		assertTrue(isStatic == expression.isStatic());

		final ConstantNode constant = expression.evaluate();
		got = constant.toString();
		if(!evaluated.equals(got)) {
			System.err.println("exp: " + evaluated);
			System.err.println("got: " + got);
		}
		assertEquals(evaluated, got);
	}

	private void checkParsingErrors(Expression expression) {
		final Advisory advisory = expression.getAdvisory();
		if(advisory.hasErrors()) {
			System.err.println(advisory);
			fail();
		}
	}

	@Test
	public void testInteger() {
		// Unary..
		doTest("" + 2, "+2");
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
		doTest("" + (-1 >>> 1), "-1>>>1");

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
	public void testDecimal() {
		// Test type conversion
		doTest("6.0", "4.0+2");
		doTest("6.0", "4+2.0");

		// Unary..
		doTest("" + 2.0, "+2.0");
		doTest("" + -2.0, "-2.0");
		doTest("" + -(2.0), "-(2.0)");

		// Binary..

		// Arithmetic...
		doTest("2.0", "1*2.0");
		doTest("2.0", "4/2.0");
		doTest("3.0", "1+2.0");
		doTest("-1.0", "1-2.0");

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
		doTest("201.0", "1+2E2");
		doTest("1.02", "1+2E-2");
		// todo Binary exponents 0b1p3
		// todo Hex floating point literals
	}

	@Test
	public void testPrecision() {
		/*
		 * !todo There are three dimensions to BigDecimals: the scale: how many
		 * numbers after the decimal point, the rounding: how the number is
		 * rounded and the precision: how many decimal digits in all are used to
		 * specify the number
		 * 
		 * 0s21.34 the 's' specifies a BigDecimal
		 * 
		 * 0s<2,3,HALF_UP>23.45 could be used to specify scale, precision,
		 * rounding
		 * 
		 * Number(2, 3, "HALF_UP", 23.45) or like that
		 */
		doTest("1.234567890123456789", "0s1.234567890123456789");
		// todo doTest("6.0", "0s<2,HALF_UP>21", true);
	}

	@Test
	public void testBoolean() {
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
	public void testString() {
		// Literal...
		doTest("\"abc\"", "\"abc\"");

		// Concatenation...
		doTest("\"abcdef\"", "\"abc\"+\"def\"");

		// Unicode...
		doTest("\"ABC\"", "\"A\\u0042C\"");

		// Escape codes...
		doTest("\"a\\bc\bde\tfg\nhi\fjk\rlm\"no\'p\"", "\"a\\\\bc\\bde\\tfg\\nhi\\fjk\\rlm\\\"no\\'p\"");
	}

	@Test
	public void testNull() {
		doTest("null", "null");
	}

	@Test
	public void testFunctions() {
		doTest("1", "primitive(1)");
		doTest("isStatic()", true, "\"x\"", "\"x\"");
		doTest("notStatic()", false, "notStatic()", "\"x\"");

		doTest("1", "echo(1)");
		doTest("1.0", "echo(1.0)");
		doTest("12.34", "echo(0s12.34)");
		doTest("true", "echo(true)");
		doTest("\"abc\"", "echo(\"abc\")");

		doTest("1", "primitive(1)");
		doTest("1.0", "primitive(1.0)");
		doTest("false", "primitive(false)");

		doTest("14", "plus(plus(2, 3), plus(4, 5))");

		doTest("1.0", "minus(2, 1)");
		doTest("-2.0", "minus(2, 1)+plus(-1, -2)");
	}

	@Test
	public void testDates() {
		doTest("12:15:00", "'12:15:00'");
		doTest("2012/4/30", "'2012/4/30'");
		doTest("2012/4/30 02:05:06", "'2012/4/30 02:05:06'");
	}

	@Test
	public void testPrecedence() {
		doTest("17", "2+3*5");
		doTest("11", "2*3+5");
		doTest("25", "(2+3)*5");
	}

	@Test
	public void test() {
		doTest("1.0", "minus(2, 1)");
	}

}
