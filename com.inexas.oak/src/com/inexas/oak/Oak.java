package com.inexas.oak;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.advisory.Advisory;

public class Oak {
	private class MyErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(
				Recognizer<?, ?> recognizer,
				Object offendingSymbol,
				int line, int column,
				String message,
				RecognitionException e) {
			advisory.reportError(line, column, message);
		}
	}

	private final Advisory advisory;
	private Node rootNode;
	private OakParser parser;

	public Oak(File file) {
		final String name = file.getName();
		advisory = new Advisory(name);
		if(file.isFile()) {
			try(final Reader reader = new java.io.FileReader(file)) {
				final ANTLRInputStream inputStream = new ANTLRInputStream(reader);
				process(inputStream);
			} catch(final FileNotFoundException e) {
				advisory.reportError("File not found");
			} catch(final IOException e) {
				advisory.reportError("Error reading file: " + e.getMessage());
			}
		} else {
			advisory.reportError(name + " is not a file");
		}
	}

	public Oak(String string) {
		advisory = new Advisory("(Input was String)");
		final InputStream stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
		try {
			final ANTLRInputStream inputStream = new ANTLRInputStream(stream);
			process(inputStream);
		} catch(final IOException e) {
			advisory.reportError("IO error: " + e.getMessage());
		}
	}

	public Node processOak() {
		final Node result;

		if(advisory.isEmpty()) {
			final ParserRuleContext ruleContext = parser.oak();
			final ParseTreeWalker walker = new ParseTreeWalker();
			final OakToAstVisitor visitor = new OakToAstVisitor();
			walker.walk(visitor, ruleContext);
			result = advisory.hasErrors() ? null : visitor.getRoot();
			rootNode = result;
		} else {
			result = null;
		}

		return result;
	}

	public ExpressionNode processExpression() {
		final Node result;

		if(advisory.isEmpty()) {
			final ParserRuleContext ruleContext = parser.expression();
			final ParseTreeWalker walker = new ParseTreeWalker();
			final OakToAstVisitor visitor = new OakToAstVisitor();
			walker.walk(visitor, ruleContext);
			result = advisory.hasErrors() ? null : visitor.getRoot();
		} else {
			result = null;
		}

		return (ExpressionNode)result;
	}

	public Object processTransform(Rule[] rules) {
		processOak();
		final Transformer visitor = new Transformer(rules, null, advisory);
		accept(visitor);
		return visitor.getRoot();
	}

	/**
	 * @return
	 */
	public Advisory getAdvisory() {
		return advisory;
	}

	public void accept(OakVisitor visitor) {
		if(!advisory.isEmpty()) {
			System.err.println(advisory);
			throw new InexasRuntimeException("Source file had error messages");
		}

		if(rootNode == null) {
			throw new InexasRuntimeException("Call processOak() first");
		}

		visitor.enter(this);
		rootNode.accept(visitor);
		visitor.exit(this);
	}

	public Node getRoot() {
		return rootNode;
	}

	@Override
	public String toString() {
		final ToStringVisitor visitor = new ToStringVisitor(true);
		accept(visitor);
		return visitor.toString();
	}

	private void process(ANTLRInputStream inputStream) {
		try {
			// Create the lexer...
			final OakLexer lexer = new OakLexer(inputStream);

			// Create the parser...
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			parser = new OakParser(tokens);

			// Fix up error listener...
			final MyErrorListener errorListener = new MyErrorListener();
			lexer.removeErrorListeners();
			lexer.addErrorListener(errorListener);
			parser.removeErrorListeners(); // Remove ConsoleErrorListener
			parser.addErrorListener(errorListener);
		} catch(final Exception e) {
			advisory.reportError(e.getMessage());
		}
	}
}
