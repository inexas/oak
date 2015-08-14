package com.inexas.oak.ast;

import java.util.Stack;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.advisory.Advisory;
import com.inexas.oak.ast.OakParser.ArrayContext;
import com.inexas.oak.ast.OakParser.CardinalityContext;
import com.inexas.oak.ast.OakParser.ExpressionContext;
import com.inexas.oak.ast.OakParser.LiteralContext;
import com.inexas.oak.ast.OakParser.ObjectContext;
import com.inexas.oak.ast.OakParser.PairContext;
import com.inexas.oak.ast.OakParser.PathContext;
import com.inexas.tad.Context;
import com.inexas.util.Cardinality;

/**
 * Visits the ANTLR AST and outputs an Oak AST.
 *
 * @author kwhittingham
 */
public class AntlrToAstVisitor extends OakBaseListener {
	private final Stack<Node> stack = new Stack<>();
	/** Set trace to true to send a rule by rule log to stdout */
	private final boolean trace = false;
	private String indent = "";

	public Node getRoot() {
		assert stack.size() == 1;
		return stack.peek();
	}

	@Override
	public void exitLiteral(LiteralContext ctx) {
		final int type = ctx.start.getType();
		final String text = ctx.getText();
		final Node constant;
		switch(ctx.start.getType()) {
		case OakLexer.IntegerLiteral: {
			constant = ConstantNode.toIntegerConstant(ctx, text);
			break;
		}

		case OakLexer.FloatingPointLiteral: {
			constant = ConstantNode.toDecimalConstant(ctx, text);
			break;
		}

		case OakLexer.BigDecimalLiteral:
			constant = ConstantNode.toPrecisionConstant(ctx, text);
			break;

		case OakLexer.StringLiteral:
			constant = new ConstantNode(ctx, text.substring(1, text.length() - 1));
			break;

		case OakLexer.DateTimeLiteral:
			constant = ConstantNode.toDate(ctx, text.substring(1, text.length()));
			break;

		case OakLexer.True:
			constant = new ConstantNode(ctx, true);
			break;

		case OakLexer.False:
			constant = new ConstantNode(ctx, false);
			break;

		case OakLexer.Null:
			constant = new ConstantNode(ctx);
			break;

		default:
			throw new UnexpectedException("enterLiteral: " + type);
		}
		stack.add(constant);
	}

	@Override
	public void exitExpression(ExpressionContext ctx) {
		final int count = ctx.getChildCount();
		final ExpressionNode node;
		if(count >= 3 && ctx.getChild(1).getText().charAt(0) == '(') {
			// Functions in the form name(<parameter list>)...
			final int parameterCount = (count - 2) / 2;
			final String functionName = ctx.getChild(0).getText();
			final ExpressionNode[] arguments = new ExpressionNode[parameterCount];
			for(int i = 0; i < parameterCount; i++) {
				arguments[i] = (ExpressionNode)stack.pop();
			}
			node = new FunctionNode(ctx, functionName, arguments);
		} else {
			// Everything exception functions...
			switch(count) {
			case 1:
				// Either a constant or a non-reducible expression
				node = null;
				break;

			case 2: {
				final int operator = getOperand(ctx, 0);
				switch(operator) {
				case OakLexer.Plus:
				case OakLexer.Minus:
				case OakLexer.Not:
				case OakLexer.Comp:
					node = new UnaryNode(ctx, operator, (ExpressionNode)stack.pop());
					break;

				default:
					throw new UnexpectedException("exitExpression: ");
				}
				break;
			}

			case 3:
				final int operator = getOperand(ctx, 1);
				switch(operator) {
				case OakLexer.Multiply:
				case OakLexer.Divide:
				case OakLexer.Mod:
				case OakLexer.Plus:
				case OakLexer.Minus:
				case OakLexer.Shl:
				case OakLexer.Shr:
				case OakLexer.Usr:
				case OakLexer.Lt:
				case OakLexer.Lte:
				case OakLexer.Gte:
				case OakLexer.Gt:
				case OakLexer.Eq:
				case OakLexer.Ne:
				case OakLexer.Or:
				case OakLexer.And:
				case OakLexer.Xor:
				case OakLexer.Lor:
				case OakLexer.Land:
					node = new BinaryNode(
							ctx,
							operator,
							(ExpressionNode)stack.pop(),
							(ExpressionNode)stack.pop());
					break;

				default:
					error(ctx, "Syntax error");
					node = null;
				}
				break;

			case 5: // condition ? t : f
				node = new ConditionalNode(
						ctx,
						(ExpressionNode)stack.pop(),
						(ExpressionNode)stack.pop(),
						(ExpressionNode)stack.pop());
				break;

			default:
				error(ctx, "Syntax error");
				node = null;
			}
		}

		if(node != null) {
			if(node.isStatic()) {
				stack.add(node.evaluate());
			} else {
				stack.add(node);
			}
		}
	}

	@Override
	public void exitPair(PairContext ctx) {
		// : Key Colon value Semi
		// | Key object
		// | Key array
		// | Key Semi
		final Node node;
		final int childCount = ctx.getChildCount();
		if(childCount == 4) {
			// Either an expression, literal or path...
			final String name = ctx.getChild(0).getText();
			node = new ValuePairNode(ctx, name, stack.pop());

		} else if(childCount == 2) {
			// ObjectName Body
			// ArrayName ElementList
			// BooleanName TrueOrFalse
			final String name = ctx.getChild(0).getText();
			final ParseTree child = ctx.getChild(1);
			if(child instanceof ObjectContext) {
				node = new ObjectPairNode(ctx, name, (ObjectNode)stack.pop());
			} else if(child instanceof ArrayContext) {
				final Node[] members = ((ArrayNode)stack.pop()).getValues();
				if(members[0].getClass() == ObjectNode.class) {
					node = new ObjectArrayPairNode(ctx, name, members);
				} else {
					node = new ValueArrayPairNode(ctx, name, members);
				}
			} else if(child instanceof TerminalNode) {
				// It's a boolean short-cut like "export;" which is the same
				// as "export: true"
				node = new ValuePairNode(ctx, name, new ConstantNode(ctx, true));
			} else {
				throw new UnexpectedException("exitPair: " + child.getText());
			}

		} else {
			node = new ErrorNode(ctx);
			error(ctx, "Syntax error");
		}
		stack.push(node);
	}

	@Override
	public void exitObject(ObjectContext ctx) {
		// '{' pair+ '}'
		final int memberCount = ctx.getChildCount() - 2;
		final PairNode[] members = new PairNode[memberCount];
		// Reverse order as it's a stack
		for(int i = memberCount - 1; i >= 0; i--) {
			members[i] = (PairNode)stack.pop();
		}
		final ObjectNode node = new ObjectNode(ctx, members);
		stack.push(node);
	}

	@Override
	public void exitArray(ArrayContext ctx) {
		// '[' value+ ']'
		final int memberCount = (ctx.getChildCount() - 1) / 2;
		final Node[] values = new Node[memberCount];
		// Reverse order as it's a stack
		for(int i = memberCount - 1; i >= 0; i--) {
			values[i] = stack.pop();
		}
		final ArrayNode node = new ArrayNode(ctx, values);
		stack.push(node);
	}

	@Override
	public void exitPath(PathContext ctx) {
		final int type = ctx.start.getType();
		final String text = ctx.getText();
		switch(ctx.start.getType()) {
		case OakLexer.Path: {
			stack.add(new PathNode(ctx, text));
			break;
		}

		case OakLexer.Key: {
			stack.add(new PathNode(ctx, text));
			break;
		}

		case OakLexer.Divide: {
			stack.add(new PathNode(ctx, "/"));
			break;
		}

		default:
			throw new UnexpectedException("enterLiteral: " + type);
		}
	}

	@Override
	public void exitCardinality(CardinalityContext ctx) {
		final String text = ctx.getText();
		Cardinality cardinality;
		try {
			cardinality = Cardinality.newInstance(text);
			stack.add(new CardinalityNode(ctx, cardinality));
		} catch(final Cardinality.Exception e) {
			error(ctx, e.getMessage());
		}
	}

	/**
	 * Tracing only
	 */
	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		if(trace) {
			System.out.println(indent + ">"
					+ ctx.getClass().getSimpleName() + " "
					+ ctx.getText());
			indent += ". ";
		}
	}

	/**
	 * Tracing only
	 */
	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		if(trace) {
			indent = indent.substring(2);
			System.out.println(indent + "<" + ctx.getClass().getSimpleName());
		}
	}

	private int getOperand(ExpressionContext ctx, int index) {
		return ((TerminalNode)ctx.getChild(index)).getSymbol().getType();
	}

	private void error(ParserRuleContext context, String message) {
		final Token token = context.getStart();
		final Advisory advisory = Context.get(Advisory.class);
		advisory.error(token.getLine(), token.getCharPositionInLine() + 1, message);
	}

}
