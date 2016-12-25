package com.inexas.oak;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.ast.Function;

public class TestExpressions2 {

	public static class TestFunclib implements Library {

		@Function(dynamic = true)
		public int echo(int x) {
			return x;
		}

		@Function
		public int two() {
			return 2;
		}

		@Override
		public Object resolve(String identifier) {
			final Object result;

			switch(identifier) {
			case "five":
				result = new Integer(5);
				break;

			default:
				result = UNRESOLVED;
				break;
			}

			return result;
		}
	}

	void doTest(String expected, String toTest) throws OakException {
		final Expression expression = new Expression(toTest, new TestFunclib());

		final String got = expression.toString();
		if(!expected.equals(got)) {
			System.err.println("exp: " + expected);
			System.err.println("got: " + got);
		}

		assertEquals(expected, got);
	}

	@Test
	public void testJustInTimeEvaluation1() throws OakException {
		final Expression expression = new Expression("five", new TestFunclib());
		assertEquals(null, expression.getType());
		expression.evaluate();
		assertEquals(DataType.z, expression.getType());
	}

	@Test
	public void testJustInTimeEvaluation2() throws OakException {
		final Expression staticExpression = new Expression("1+2", new TestFunclib());
		assertEquals("1 + 2", staticExpression.toString());
		staticExpression.evaluate();
		assertEquals("3", staticExpression.toString());

		final Expression dynamicExpression = new Expression("1+echo(2)", new TestFunclib());
		assertEquals("1 + echo(2)", dynamicExpression.toString());
		dynamicExpression.evaluate();
		assertEquals("1 + echo(2)", dynamicExpression.toString());

		final Expression onePlusTwo = new Expression("1+two()", new TestFunclib());
		assertEquals("1 + two()", onePlusTwo.toString());
		onePlusTwo.evaluate();
		assertEquals("3", onePlusTwo.toString());
	}

}
