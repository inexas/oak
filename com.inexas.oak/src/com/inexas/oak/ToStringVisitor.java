package com.inexas.oak;

import java.time.temporal.Temporal;
import com.inexas.util.TextBuilder;

public class ToStringVisitor extends OakVisitor.Base {
	static String[] operatorToString = new String[OakLexer.Usr + 1];
	static {
		operatorToString[OakLexer.Plus] = "+";
		operatorToString[OakLexer.Minus] = "-";
		operatorToString[OakLexer.Multiply] = "*";
		operatorToString[OakLexer.Divide] = "/";
		operatorToString[OakLexer.Mod] = "%";
		operatorToString[OakLexer.Comp] = "~";
		operatorToString[OakLexer.Not] = "!";
		operatorToString[OakLexer.Lt] = "<";
		operatorToString[OakLexer.Lte] = "<=";
		operatorToString[OakLexer.Gt] = ">";
		operatorToString[OakLexer.Gte] = ">=";
		operatorToString[OakLexer.And] = "&";
		operatorToString[OakLexer.Or] = "|";
		operatorToString[OakLexer.Land] = "&&";
		operatorToString[OakLexer.Lor] = "||";
		operatorToString[OakLexer.Xor] = "^";
		operatorToString[OakLexer.Eq] = "=";
		operatorToString[OakLexer.Ne] = "!=";
		operatorToString[OakLexer.Shl] = "<<";
		operatorToString[OakLexer.Shr] = ">>";
		operatorToString[OakLexer.Usr] = ">>>";
	}

	private class State {
		final State previousState;
		int count;

		public State() {
			previousState = currentState;
		}
	}

	private final TextBuilder tb;
	private State currentState;

	public ToStringVisitor(boolean pretty) {
		tb = new TextBuilder(pretty);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ObjectNode node) {
		tb.append('{');
		tb.newline();
		tb.indentMore();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ObjectNode node) {
		tb.indentLess();
		tb.indent();
		tb.append('}');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ObjectPairNode node) {
		tb.indent();
		tb.append(node.getName());
		tb.space();
		tb.append('{');
		tb.newline();
		tb.indentMore();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ObjectPairNode node) {
		tb.indentLess();
		tb.append('}');
		tb.newline();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ObjectArrayPairNode node) {
		enterArrayPairNode(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ObjectArrayPairNode node) {
		exitArrayPairNode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ValueArrayPairNode node) {
		enterArrayPairNode(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ValueArrayPairNode node) {
		exitArrayPairNode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delimit() {
		if(tb.pretty) {
			if(currentState.count == 9) {
				currentState.count = 0;
				tb.append(',');
				tb.newline();
				tb.indent();
			} else {
				tb.append(", ");
				currentState.count++;
			}
		} else {
			tb.append(',');
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void operator(int operator) {
		tb.space();
		tb.append(operatorToString[operator]);
		tb.space();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(FunctionNode node) {
		tb.append(node.getName());
		tb.append('(');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(FunctionNode node) {
		tb.append(')');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(UnaryNode node) {
		final int operator = node.getOperator();
		if(operator != OakLexer.Plus) {
			tb.append(operatorToString[operator]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ValuePairNode node) {
		push();
		tb.indent();
		tb.append(node.getName());
		tb.append(':');
		tb.space();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ValuePairNode node) {
		tb.append(';');
		tb.newline();
		pop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(ConstantNode node) {
		final Object value = node.getValue();
		final DataType type = node.getType();
		final String string;
		if(value == null) {
			string = "null";
		} else {
			if(type.isTemporal) {
				tb.append('@');
				if(type == DataType.date) {
					string = ConstantNode.dateFormatter.format((Temporal)value);
				} else if(type == DataType.time) {
					string = ConstantNode.timeFormatterSecs.format((Temporal)value);
				} else {
					string = ConstantNode.dateTimeFormatterSecs.format((Temporal)value);
				}
			} else if(type == DataType.text) {
				final TextBuilder valueAsString = new TextBuilder();
				valueAsString.append('"');
				valueAsString.append((String)value);
				valueAsString.append('"');
				string = valueAsString.toString();
			} else {
				string = value.toString();
			}
		}

		tb.append(string);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(CardinalityNode node) {
		tb.append(node.cardinality.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(PathNode node) {
		tb.append(node.path);
	}

	/**
	 *
	 * @return A parseable representation of the visited Oak file.
	 */
	@Override
	public String toString() {
		return tb.toString();
	}

	private void enterArrayPairNode(PairNode node) {
		tb.indent();
		tb.append(node.getName());
		tb.space();
		tb.append('[');
		tb.newline();
		tb.indentMore();
		tb.indent();
		push();
	}

	private void exitArrayPairNode() {
		pop();
		tb.newline();
		tb.indentLess();
		tb.indent();
		tb.append(']');
		tb.newline();
	}

	private void push() {
		if(tb.pretty) {
			currentState = new State();
		}
	}

	private void pop() {
		if(tb.pretty) {
			currentState = currentState.previousState;
		}
	}

}
