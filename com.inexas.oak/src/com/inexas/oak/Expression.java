package com.inexas.oak;

import java.io.File;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.ast.*;
import com.inexas.tad.TadContext;

public class Expression extends AbstractOak {
	private ExpressionNode rootNode;

	public Expression(String string, Library... libraries) throws OakException {
		super(string, libraries);
		toExpression();
	}

	public Expression(File file) throws OakException {
		super(file);
		toExpression();
	}

	/**
	 * Return the root of the AST parsed from the input. You should call one of
	 * the process...() methods to process the file and check for errors before
	 * calling this method.
	 *
	 * @return The root of the AST parsed from the input
	 */
	public ExpressionNode getRoot() {
		return rootNode;
	}

	public DataType getType() {
		assert advisory.isEmpty() : advisory.getFirstError();

		return rootNode.getType();
	}

	/**
	 * Convenience method equivalent to calling getRoot().isStatic().
	 *
	 * @return Return true if the expression is static i.e. does not change
	 *         depending on time or environment.
	 */
	public boolean isStatic() {
		assert advisory.isEmpty() : advisory.getFirstError();

		return rootNode.isStatic();
	}

	/**
	 * Convenience method equivalent to calling getRoot().evaluate().
	 *
	 * @return The evaluated result.
	 */
	public ConstantNode evaluate() {
		assert advisory.isEmpty() : advisory.getFirstError();
		final ConstantNode result;

		TadContext.pushAttach(advisory);
		TadContext.pushAttach(registry);
		result = rootNode.evaluate();
		TadContext.detach(registry);
		TadContext.detach(advisory);

		return result;
	}

	/**
	 * Visit the AST.
	 *
	 * @param visitor
	 *            The visitor to send around the AST.
	 * @throws OakException
	 *             Thrown if a syntax error is reported.
	 */
	@Override
	public void accept(AstVisitor visitor) throws OakException {
		if(!advisory.isEmpty()) {
			throw new RuntimeException("Source file had error messages: " + advisory.getFirstError());
		}

		TadContext.pushAttach(advisory);
		visitor.enter(this);
		rootNode.accept(visitor);
		visitor.exit(this);

		TadContext.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}
	}

	private void toExpression() throws OakException {
		TadContext.pushAttach(advisory);
		TadContext.pushAttach(registry);

		final ParserRuleContext ruleContext = parser.expression();
		if(advisory.isEmpty()) {
			final ParseTreeWalker walker = new ParseTreeWalker();
			final AntlrToAstVisitor visitor = new AntlrToAstVisitor();
			walker.walk(visitor, ruleContext);
			rootNode = (ExpressionNode)visitor.getRoot();
		}

		TadContext.detach(registry);
		TadContext.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}

	}
}
