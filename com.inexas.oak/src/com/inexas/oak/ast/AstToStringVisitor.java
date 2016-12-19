package com.inexas.oak.ast;

import java.util.List;
import com.inexas.oak.*;
import com.inexas.util.Text;

public class AstToStringVisitor extends AstVisitor.Base {
	public static String[] operatorToString = new String[OakLexer.Usr + 1];
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

	private final Text t;
	private State currentState;
	private boolean spacer;

	public AstToStringVisitor(boolean pretty) {
		t = new Text(pretty);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(Oak oak) {
		// Are there any libraries?
		final List<Library> libraries = oak.getRegistry().getLlibraries();

		if(libraries.size() > 0) {
			for(final Library llibrary : libraries) {
				t.append("#load \"");
				t.append(llibrary.getClass().getName());
				t.append('"');
				t.newline();
			}
			t.newline();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ObjectNode node) {
		t.append('{');
		t.newline();
		t.indentMore();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ObjectNode node) {
		t.indentLess();
		t.indent();
		t.append('}');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ObjectPairNode node) {
		if(spacer) {
			t.newline();
		}
		t.indent();
		t.append(node.getName());
		t.space();
		t.append('{');
		t.newline();
		t.indentMore();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ObjectPairNode node) {
		spacer = true;
		t.indentLess();
		t.indent();
		t.append('}');
		t.newline();
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
		if(t.pretty) {
			if(currentState.count == 9) {
				currentState.count = 0;
				t.append(',');
				t.newline();
				t.indent();
			} else {
				t.append(", ");
				currentState.count++;
			}
		} else {
			t.append(',');
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void operator(int operator) {
		t.space();
		t.append(operatorToString[operator]);
		t.space();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(FunctionNode node) {
		t.append(node.getName());
		t.append('(');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(FunctionNode node) {
		t.append(')');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(UnaryNode node) {
		final int operator = node.getOperator();
		if(operator != OakLexer.Plus) {
			t.append(operatorToString[operator]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ValuePairNode node) {
		push();
		t.indent();
		t.append(node.getName());
		t.append(':');
		t.space();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ValuePairNode node) {
		t.append(';');
		t.newline();
		pop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(ConstantNode node) {
		final Object value = node.getValue();
		final DataType type = node.getType();
		type.toMarkup(value, t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(CardinalityNode node) {
		t.append(node.cardinality.text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(PathNode node) {
		t.append(node.path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(IdentifierNode identifierNode) {
		t.append(identifierNode.identifier);
	}

	/**
	 *
	 * @return A parseable representation of the visited Oak file.
	 */
	@Override
	public String toString() {
		return t.toString();
	}

	private void enterArrayPairNode(PairNode node) {
		if(spacer) {
			t.newline();
		} else {
			spacer = true;
		}
		t.indent();
		t.append(node.getName());
		t.space();
		t.append('[');
		t.newline();
		t.indentMore();
		t.indent();
		push();
	}

	private void exitArrayPairNode() {
		pop();
		t.newline();
		t.indentLess();
		t.indent();
		t.append(']');
		t.newline();
	}

	private void push() {
		if(t.pretty) {
			currentState = new State();
		}
	}

	private void pop() {
		if(t.pretty) {
			currentState = currentState.previousState;
		}
	}

}
