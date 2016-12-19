package com.inexas.oak;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.ast.*;
import com.inexas.oak.dialect.*;
import com.inexas.oak.template.*;
import com.inexas.tad.TadContext;

public class Oak extends AbstractOak {
	private Dialect dialectAst;
	private PairNode rootNode;

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
		super(file);
		toAst();
	}

	/**
	 * Construct an Oak process that will parse a String. The constructor parses
	 * the String and constructs an Abstract Syntax Tree representation. If
	 * errors are detected they are collected and, at the end of processing, an
	 * OakException is thrown.
	 *
	 * @param string
	 *            The String to parse.
	 * @param libraries
	 *            Optional list of function libraries to load.
	 * @throws OakException
	 *             Thrown if an error is detected when processing the input
	 *             file.
	 */
	public Oak(String string, Library... libraries) throws OakException {
		super(string, libraries);
		toAst();
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
		final T result;

		TadContext.pushAttach(advisory);
		result = toObjectTree(dialect.rules);
		TadContext.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}

		return result;
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

		TadContext.pushAttach(advisory);

		try {
			final Field field = dialect.getDeclaredField("dialect");
			final Rule[] rules = ((Rulebase)field.get(null)).rules;
			try {
				result = toObjectTree(rules);
			} catch(final OakException e) {
				result = null;
			} catch(final Exception e) {
				throw new RuntimeException("toObjectTree", e);
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

		TadContext.detach(advisory);
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

		TadContext.pushAttach(advisory);

		dialectAst = toObjectTree(OakDialect.rulebase.rules);
		if(dialectAst != null) {
			result = getDialect();
			// final AstToRulesVisitor visitor = new AstToRulesVisitor();
			// dialectAst.accept(visitor);
			// result = visitor.getDialect();
		} else {
			result = null;
		}

		TadContext.detach(advisory);
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
	@Override
	public void accept(AstVisitor visitor) throws OakException {
		if(!advisory.isEmpty()) {
			throw new RuntimeException("Source file had error messages: " + advisory.getFirstError());
		}

		if(rootNode == null) {
			throw new RuntimeException("Call toObjectTree() first");
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

	/**
	 * Return the root of the AST parsed from the input. You should call one of
	 * the process...() methods to process the file and check for errors before
	 * calling this method.
	 *
	 * @return The root of the AST parsed from the input
	 */
	public PairNode getRoot() {
		return rootNode;
	}

	/**
	 * Return the parsed input as an Abstract Syntax Tree.
	 *
	 * @return The root Node of the parsed Abstract Syntax Tree.
	 * @throws OakException
	 *             Thrown on parsing errors.
	 */
	private void toAst() throws OakException {
		TadContext.pushAttach(advisory);

		if(rootNode == null && !advisory.hasErrors()) {
			final ParserRuleContext ruleContext = parser.oak();
			if(!advisory.hasErrors()) {
				final ParseTreeWalker walker = new ParseTreeWalker();
				final AntlrToAstVisitor visitor = new AntlrToAstVisitor();
				walker.walk(visitor, ruleContext);
				rootNode = (PairNode)visitor.getRoot();
			}
		}

		TadContext.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
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

	/*
	 * The following methods do a depth first visit of the Dialect AST to create
	 * the rule base.
	 */
	private Rulebase getDialect() {

		// Get all the objects first in case of forward references...
		final Collection<Objet> objects = dialectAst.objectMap.values();
		final Map<Identifier, ObjectRule> objectMap = new HashMap<>();
		for(final Objet object : objects) {
			final Identifier key = object.key;
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
				if(rule == null) {
					advisory.error(object, "Missing '" + member.key + "' in " + object.key);
				} else {
					final Relationship relationship = new Relationship(
							rule,
							member.cardinality,
							member.collectionType);
					relationships[index] = relationship;
				}
				index++;
			}

			final ObjectRule rule = objectMap.get(object.key);
			rule.setRelationships(relationships);
		}

		return new Rulebase(
				dialectAst.key,
				objectMap.values().toArray(new ObjectRule[objectMap.size()]));
	}
}
