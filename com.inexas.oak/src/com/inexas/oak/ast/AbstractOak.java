package com.inexas.oak.ast;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.antlr.v4.runtime.*;
import com.inexas.oak.Library;
import com.inexas.oak.advisory.*;
import com.inexas.oak.ast.LibraryRegistry.*;
import com.inexas.tad.TadContext;

/**
 * This is the abstract base for both Oak and Expression
 */
public abstract class AbstractOak {
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

	protected final Advisory advisory;
	protected final LibraryRegistry registry = new LibraryRegistry();
	protected OakParser parser;

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
	protected AbstractOak(File file) throws OakException {

		advisory = new Advisory(file);
		TadContext.pushAttach(advisory);

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

		TadContext.detach(advisory);
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
	 * @param libraries
	 *            Optional list of function libraries to load.
	 * @throws OakException
	 *             Thrown if an error is detected when processing the input
	 *             file.
	 */
	protected AbstractOak(String string, Library... libraries) throws OakException {

		advisory = new Advisory(string);
		TadContext.pushAttach(advisory);

		try {
			registry.register(libraries);

			if(string == null || string.trim().length() == 0) {
				advisory.error("Null or empty string");
			} else {
				try(final InputStream stream = new ByteArrayInputStream(
						string.getBytes(StandardCharsets.UTF_8))) {
					final ANTLRInputStream inputStream = new ANTLRInputStream(stream);
					process(inputStream);
				} catch(final IOException e) {
					advisory.error("IO error: " + e.getMessage());
				}
			}
		} catch(LibraryException | InvalidMethodException e) {
			advisory.error(e.getMessage());
		}

		TadContext.detach(advisory);
		if(advisory.hasErrors()) {
			throw new OakException(advisory);
		}
	}

	/**
	 * @return the registry
	 */
	public LibraryRegistry getRegistry() {
		return registry;
	}

	/**
	 * Retrieve the error message Advisory.
	 *
	 * @return The error message Advisory.
	 */
	public Advisory getAdvisory() {
		return advisory;
	}

	public abstract void accept(AstVisitor visitor) throws OakException;

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
}
