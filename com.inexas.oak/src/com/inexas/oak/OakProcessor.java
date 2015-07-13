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

	public OakProcessor(File dialectFile, String[] visitorList, File sourceFile) {
		final Rule[] rulebase = getRulebase(dialectFile);
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
	 * Read an Oak dialect file into a rulebase
	 *
	 * @param dialectFile
	 *            The dialect file to read (typically something.oakd).
	 * @return Return null if there's a failure.
	 */
	private Rule[] getRulebase(File dialectFile) {
		Rule[] result = null;

		// Parse Oak dialect...
		final Oak oak = new Oak(dialectFile);
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
				final Dialect dialect = (Dialect)transformer.getRoot();
				final GenerateSourceDialectVisitor visitor = new GenerateSourceDialectVisitor(advisory);
				dialect.accept(visitor);
				result = visitor.getRulebase();
			}
		}

		return result;
	}

	/**
	 * Read an Oak dialect file into a rulebase
	 *
	 * @param dialectFile
	 *            The dialect file to read (typically something.oakd).
	 * @return Return null if there's a failure.
	 */
	private void transform(Rule[] rulebase, String[] visitorList, File sourceFile) {

		// Parse Oak dialect...
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
