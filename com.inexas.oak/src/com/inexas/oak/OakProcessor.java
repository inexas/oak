package com.inexas.oak;

import java.io.File;
import com.inexas.oak.advisory.Advisory;

/**
 * Accept to input streams: an Oak
 */
public class OakProcessor {
	private Advisory advisory;
	private Object root;

	public OakProcessor(Rule[] rulebase, String[] visitorList, File sourceFile) {
		transform(rulebase, visitorList, sourceFile);
	}

	public OakProcessor(File grammarFile, String[] visitorList, File sourceFile) {
		final Rule[] rulebase = getRulebase(grammarFile);
		if(advisory.isEmpty()) {
			transform(rulebase, visitorList, sourceFile);
		}
	}

	public Advisory getAdvisory() {
		return advisory;
	}

	public Object getRoot() {
		return root;
	}

	/**
	 * Read an Oak grammar file into a rulebase
	 *
	 * @param grammarFile
	 *            The grammar file to read (typically something.oakg).
	 * @return Return null if there's a failure.
	 */
	private Rule[] getRulebase(File grammarFile) {
		Rule[] result = null;

		// Parse Oak grammar...
		final Oak oak = new Oak(grammarFile);
		advisory = oak.getAdvisory();

		// Transform to AST...
		if(advisory.isEmpty()) {
			final Transformer transformer = new Transformer(
					OakRulebase.rules,
					OakRulebase.visitorList,
					advisory);
			oak.accept(transformer);

			// Convert AST to rulebase...
			if(advisory.isEmpty()) {
				final Grammar grammar = (Grammar)transformer.getRoot();
				final GenerateSourceGrammarVisitor visitor = new GenerateSourceGrammarVisitor(advisory);
				grammar.accept(visitor);
				result = visitor.getRulebase();
			}
		}

		return result;
	}

	/**
	 * Read an Oak grammar file into a rulebase
	 *
	 * @param grammarFile
	 *            The grammar file to read (typically something.oakg).
	 * @return Return null if there's a failure.
	 */
	private void transform(Rule[] rulebase, String[] visitorList, File sourceFile) {

		// Parse Oak grammar...
		final Oak oak = new Oak(sourceFile);
		advisory = oak.getAdvisory();

		// Transform to AST...
		if(advisory.isEmpty()) {
			final Transformer transformer = new Transformer(
					rulebase,
					visitorList,
					advisory);
			oak.accept(transformer);

			if(advisory.isEmpty()) {
				root = transformer.getRoot();
			}
		}
	}

}
