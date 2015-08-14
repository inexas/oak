package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.DataType;
import com.inexas.tad.Context;

public class FunctionNode extends ExpressionNode {
	private final ExpressionNode argumentNodes[];
	private final Function function;
	private final int argumentCount;
	private final boolean isStatic;
	private final DataType type;

	public FunctionNode(ParserRuleContext context, String name, ExpressionNode argumentNodes[]) {
		super(context);

		this.argumentNodes = argumentNodes;
		argumentCount = argumentNodes.length;

		final FunctionRegister functionRegister = Context.get(FunctionRegister.class);
		if(functionRegister == null) {
			throw new InexasRuntimeException("No function register loaded");
		}
		function = functionRegister.getFunction(name, argumentNodes);

		type = function.returnType;

		boolean soFarItsStatic = function.isStatic;
		if(soFarItsStatic) {
			for(int i = 0; i < argumentCount; i++) {
				final ExpressionNode argument = argumentNodes[i];
				if(!argument.isStatic()) {
					soFarItsStatic = false;
				}
			}
		}
		isStatic = soFarItsStatic;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public ConstantNode evaluate() {
		/*
		 * The arguments come in reverse order because they come off a stack so
		 * we need to reverse them.
		 */
		final Object arguments[] = new Object[argumentCount];
		for(int i = 0, j = argumentCount - 1; i < argumentCount; i++, j--) {
			arguments[i] = argumentNodes[j].evaluate().getValue();
		}
		return function.invoke(null, arguments);
	}

	@Override
	public DataType getType() {
		return type;
	}

	public String getName() {
		return function.name;
	}

	@Override
	public void accept(AstVisitor visitor) {
		assert visitor.enterEveryNode(this);
		visitor.enter(this);
		boolean delimit = false;
		for(final Node argument : argumentNodes) {
			if(delimit) {
				// todo This is a bit of a hack
				visitor.delimit();
			} else {
				delimit = true;
			}
			argument.accept(visitor);
		}
		visitor.exit(this);
		assert visitor.exitEveryNode(this);
	}

}
