package com.inexas.oak;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import com.inexas.exception.*;
import com.inexas.oak.advisory.*;
import com.inexas.oak.ast.*;
import com.inexas.oak.dialect.*;
import com.inexas.oak.template.*;
import com.inexas.tad.Context;

public class Oak {
	private class MyErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(
				Recognizer<?, ?> recognizer,
				Object offendingSymbol,
				int line, int column,
				String message,
				RecognitionException e) {
			advisory.error(line, column, "Syntax error: " + message);
		}
	}

	private final Advisory advisory;
	private OakParser parser;
	private Node rootNode;
	private Dialect dialectAst;

	/**
	 * Construct an Oak process that will parse a file. The constructor parses
	 * the file and constructs an Abstract Syntax Tree representation. If errors
	 * are detected they are collected and, at the end of processing, an
	 * OakException is thrown.
	 *
	 * @param file
	 *            A text file to parse.
	 * @throws OakException
	 *             Thrown if an error is detected when processing the input
	 *             file.
	 */
	public Oak(File file) throws OakException {
		advisory = new Advisory(file);
		Context.pushAttach(advisory);

		if(file.isFile()) {
			try(final Reader reader = new java.io.FileReader(file)) {
				final ANTLRInputStream inputStream = new ANTLRInputStream(reader);
				process(inputStream);
			} catch(final FileNotFoundException e) {
				advisory.error("File not found");
			} catch(final IOException e) {
				advisory.error("Error reading file: " + e.getMessage());
			}
		} else {
			advisory.error(file.getName() + " is not a file");
		}

		Context.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}
	}

	/**
	 * Construct an Oak process that will parse a String. The constructor parses
	 * the String and constructs an Abstract Syntax Tree representation. If
	 * errors are detected they are collected and, at the end of processing, an
	 * OakException is thrown.
	 *
	 * @param string
	 *            The String to parse.
	 * @throws OakException
	 *             Thrown if an error is detected when processing the input
	 *             file.
	 */
	public Oak(String string) throws OakException {
		advisory = new Advisory(string);
		Context.pushAttach(advisory);

		if(string == null || string.trim().length() == 0) {
			advisory.error("Null or empty string");
		} else {
			try(final InputStream stream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8))) {
				final ANTLRInputStream inputStream = new ANTLRInputStream(stream);
				process(inputStream);
			} catch(final IOException e) {
				advisory.error("IO error: " + e.getMessage());
			}
		}

		Context.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}
	}

	/**
	 * Return the parsed input as an Abstract Syntax Tree.
	 *
	 * @return The root Node of the parsed Abstract Syntax Tree.
	 * @throws OakException
	 *             Thrown on parsing errors.
	 */
	public Node toAst() throws OakException {
		Context.pushAttach(advisory);

		if(rootNode == null && !advisory.hasErrors()) {
			final ParserRuleContext ruleContext = parser.oak();
			if(!advisory.hasErrors()) {
				final ParseTreeWalker walker = new ParseTreeWalker();
				final AntlrToAstVisitor visitor = new AntlrToAstVisitor();
				walker.walk(visitor, ruleContext);
				rootNode = visitor.getRoot();
			}
		}

		Context.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}

		return rootNode;
	}

	/**
	 * Parse the input as an expression. The expression parse is a useful side
	 * product of the Oak syntax.
	 *
	 * @return The root Node of the parsed Abstract Syntax Tree or null if
	 *         errors have been encountered.
	 * @throws OakException
	 *             Thrown on parsing errors.
	 */
	public ExpressionNode toExpression() throws OakException {
		Context.pushAttach(advisory);

		final ParserRuleContext ruleContext = parser.expression();
		final ParseTreeWalker walker = new ParseTreeWalker();
		final AntlrToAstVisitor visitor = new AntlrToAstVisitor();
		walker.walk(visitor, ruleContext);
		rootNode = visitor.getRoot();

		Context.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}

		return (ExpressionNode)rootNode;
	}

	/**
	 * Parse the input and transform it into implementation objects according to
	 * a set of rules defined in a Dialect.
	 *
	 * @param dialect
	 *            The dialect to apply to the transformation.
	 * @param <T>
	 *            The type of the root of the object tree.
	 * @return The root of the output object tree.
	 * @throws OakException
	 *             Thrown on parsing errors.
	 */
	public <T> T toObjectTree(Rulebase dialect) throws OakException {
		return toObjectTree(dialect.rules);
	}

	/**
	 * Parse the input and transform it into implementation objects according to
	 * a set of rules defined in a pre-defined Dialect class.
	 *
	 * @param dialect
	 *            The dialect to apply to the transformation.
	 * @param <T>
	 *            The type of the root of the object tree.
	 * @return The root of the output object tree.
	 * @throws OakException
	 *             Thrown if parsing error encountered.
	 */
	public <T> T toObjectTree(Class<?> dialect) throws OakException {
		T result;

		Context.pushAttach(advisory);

		try {
			final Field field = dialect.getDeclaredField("dialect");
			final Rule[] rules = ((Rulebase)field.get(null)).rules;
			try {
				result = toObjectTree(rules);
			} catch(final OakException e) {
				result = null;
			} catch(final Exception e) {
				// !todo Implement me
				throw new ImplementMeException();
			}
		} catch(final NoSuchFieldException
				| SecurityException
				| IllegalArgumentException
				| IllegalAccessException e) {
			advisory.error(
					"Cannot access field 'public static Dialect dialect;' in: "
							+ dialect.getName()
							+ " because: " + e.getMessage());
			result = null;
		}

		Context.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}

		return result;
	}

	/**
	 * Parse the input and extract an Oak Dialect form it.
	 *
	 * @return The root of the output object tree.
	 * @throws OakException
	 *             Thrown on parsing errors.
	 */
	public Rulebase toDialect() throws OakException {
		final Rulebase result;

		Context.pushAttach(advisory);

		dialectAst = toObjectTree(OakDialect.rulebase.rules);
		if(dialectAst != null) {
			result = getDialect();
			// final AstToRulesVisitor visitor = new AstToRulesVisitor();
			// dialectAst.accept(visitor);
			// result = visitor.getDialect();
		} else {
			result = null;
		}

		Context.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}

		return result;
	}

	/**
	 * Once the input has been parsed using one of the process...() methods then
	 * you can call this method with a visitor and it will be sent around the
	 * parsed AST.
	 *
	 * @param visitor
	 *            The visitor to send around the AST.
	 * @throws OakException
	 *             Thrown if a syntax error is reported.
	 */
	public void accept(AstVisitor visitor) throws OakException {

		if(!advisory.isEmpty()) {
			System.err.println(advisory);
			throw new InexasRuntimeException("Source file had error messages: " + advisory.getFirstError());
		}

		if(rootNode == null) {
			throw new InexasRuntimeException("Call processOak() first");
		}

		Context.pushAttach(advisory);
		visitor.enter(this);
		rootNode.accept(visitor);
		visitor.exit(this);

		Context.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}
	}

	/**
	 * Retrieve the error message Advisory.
	 *
	 * @return The error message Advisory.
	 */
	public Advisory getAdvisory() {
		return advisory;
	}

	/**
	 * Return the root of the AST parsed from the input. You should call one of
	 * the process...() methods to process the file and check for errors before
	 * calling this method.
	 *
	 * @return The root of the AST parsed from the input
	 */
	public Node getRoot() {
		return rootNode;
	}

	@Override
	public String toString() {
		try {
			final AstToStringVisitor visitor = new AstToStringVisitor(true);
			accept(visitor);
			return visitor.toString();
		} catch(final OakException e) {
			final String error = advisory.getFirstError();
			return "Oak containing error: " + (error == null ? "No error messages" : error);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T toObjectTree(Rule[] rules) throws OakException {
		T result;

		toAst();
		if(!advisory.hasErrors()) {
			final AstToTemplateTree visitor = new AstToTemplateTree(rules, null);
			accept(visitor);
			result = (T)visitor.getRoot();
		} else {
			result = null;
		}
		return result;
	}

	/**
	 * Do as much as we can without knowing if we have to parse an expression or
	 * Oak
	 */
	private void process(ANTLRInputStream inputStream) throws OakException {
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
			advisory.error(e.getMessage());
			throw new OakException(advisory);
		}
	}

	/*
	 * The following methods do a depth first visit of the Dialect AST to create
	 * the rule base.
	 */
	private Rulebase getDialect() {

		// Get all the objects first in case of forward references...
		final Collection<Objet> objects = dialectAst.objectMap.values();
		final Map<String, ObjectRule> objectMap = new HashMap<>();
		for(final Objet object : objects) {
			final String key = object.key;
			final ObjectRule rule = new ObjectRule(key, object.templateClass, object.isRoot);
			objectMap.put(key, rule);
		}

		// Process all the object members...
		for(final Objet object : objects) {
			int index = 0;
			final Relationship[] relationships = new Relationship[object.members.size()];
			for(final Member member : object.members) {
				final Rule rule;
				if(member.key == null) {
					// It's a Property...
					final Property property = member.property;
					rule = new PropertyRule(property.key, property.type, property.constraints);
				} else {
					// It's an Object
					rule = objectMap.get(member.key);
				}
				final Relationship relationship = new Relationship(
						rule,
						member.cardinality,
						member.collectionType);
				relationships[index++] = relationship;
			}

			final ObjectRule rule = objectMap.get(object.key);
			rule.setRelationships(relationships);
		}

		return new Rulebase(
				dialectAst.key,
				objectMap.values().toArray(new ObjectRule[objectMap.size()]));
	}
}
