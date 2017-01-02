package com.inexas.oak.ast;

import java.math.*;
import java.time.*;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.*;
import com.inexas.tad.TadContext;
import com.inexas.util.Cardinality;

/**
 * A SymbolNode is a representation of an Identifier but as used in an
 * expression.
 *
 * @author kwhittingham, @date 19 Dec 2016
 */
public class SymbolNode extends ExpressionNode {
	final String identifier;

	public SymbolNode(ParserRuleContext context, String identifier) {
		super(context);

		this.identifier = identifier;
	}

	@Override
	public void accept(AstVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.visit(this);
		assert visitor.exitEveryNode(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStatic() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConstantNode evaluate() {
		if(type == null) {
			doEvaluate();
		}

		return (ConstantNode)value;
	}

	public void doEvaluate() {
		final String symbol = identifier.toString();
		final LibraryRegistry register = TadContext.getButDontThrow(LibraryRegistry.class);
		if(register == null) {
			throw new RuntimeException("No libraries attached to resolve: " + symbol);
		}
		final Object resolved = register.resolve(symbol);
		type = DataType.getDataType(resolved.getClass());
		switch(type) {
		case f:
			value = new ConstantNode(context, (Float)resolved);
			break;
		case F:
			value = new ConstantNode(context, (BigDecimal)resolved);
			break;
		case z:
			value = new ConstantNode(context, (Integer)resolved);
			break;
		case Z:
			value = new ConstantNode(context, (BigInteger)resolved);
			break;
		case bool:
			value = new ConstantNode(context, (Boolean)resolved);
			break;
		case cardinality:
			value = new ConstantNode(context, (Cardinality)resolved);
			break;
		case date:
			value = new ConstantNode(context, (LocalDate)resolved);
			break;
		case datetime:
			value = new ConstantNode(context, (LocalTime)resolved);
			break;
		case text:
			value = new ConstantNode(context, (String)resolved);
			break;
		case time:
			value = new ConstantNode(context, (BigInteger)resolved);
			break;
		case path:
		case identifier:
		case any:
		case notEvaluated:
		default:
			if(resolved == Library.UNRESOLVED) {
				throw new RuntimeException("Cannot resolve identifier: " + symbol);
			}
			throw new RuntimeException("evaluate: " + type.name());
		}
	}

}
