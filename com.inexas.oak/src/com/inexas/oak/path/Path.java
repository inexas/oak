package com.inexas.oak.path;

import java.io.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import com.inexas.exception.*;
import com.inexas.oak.path.PathParser.ElementListContext;
import com.inexas.oak.path.PathParser.ProtocolContext;
import com.inexas.oak.path.PathParser.SelectorContext;

public class Path {
	/**
	 * Thrown to indicate that the path is not valid
	 */
	public class PathException extends InexasRuntimeException {
		private static final long serialVersionUID = -5142183516183575168L;

		PathException(String message) {
			super(message);
		}

		PathException() {
			super("Invalid path: '" + text
					+ (start == null ? "" : "' relative to " + start.getPath()));
		}
	}

	private class MyErrorListener extends BaseErrorListener {
		@Override
		public void syntaxError(
				Recognizer<?, ?> recognizer,
				Object offendingSymbol,
				int line, int column,
				String message,
				RecognitionException e) {
			final String errorMessage = text + ' ' + line + ':' + column + ' ' + message;
			if(errorMessages == null) {
				errorMessages = new ArrayList<>();
			}
			errorMessages.add(errorMessage);
		}
	}

	private class Visitor extends PathBaseListener {
		private final static int START = 0;
		private final static int SEEN_SLASH = 1;
		private final static int SEEN_KEY = 2;
		private int state = START;
		private boolean expectProtocol, expectSelector;

		@Override
		public void enterElementList(ElementListContext ctx) {
			// Nothing to do
		}

		@Override
		public void enterProtocol(ProtocolContext protocol) {
			expectProtocol = true;
		}

		@Override
		public void enterSelector(SelectorContext selector) {
			expectSelector = true;
		}

		@Override
		public void visitTerminal(TerminalNode terminal) {
			final int token = terminal.getSymbol().getType();
			switch(token) {
			case PathLexer.Self:
				if(current == null) {
					throw new PathException();
				}
				state = SEEN_KEY;
				break;

			case PathLexer.Parent:
				if(current == null) {
					throw new PathException();
				}
				current = current.getParent();
				if(current == null) {
					throw new PathException();
				}
				state = SEEN_KEY;
				break;

			case PathLexer.Switch:
				if(state == START) {
					if(current == null) {
						throw new PathException();
					}
					current = current.getRoot();
					if(current == null) {
						throw new InexasRuntimeException("Path can't have null root");
					}
				} else if(state == SEEN_KEY) {
					state = SEEN_SLASH;
				} else {
					throw new UnexpectedException("visitTerminal: //");
				}
				break;

			case PathLexer.Key:
				if(expectProtocol) {
					if(source == null) {
						throw new PathException();
					}
					final String protocol = terminal.getText();
					current = source.getRoot(protocol);
					if(current == null) {
						throw new PathException("No such protocol: " + text);
					}
					expectProtocol = false;
				} else if(expectSelector) {
					// Get named child...
					final String name = terminal.getText();
					current = current.getChild(name);
					// Null is OK and means child does not exist
					expectSelector = false;
				} else {
					if(state == SEEN_KEY) {
						throw new UnexpectedException("visitTerminal: //");
					}
					if(current == null) {
						throw new PathException();
					}
					// Get named child...
					final String name = terminal.getText();
					current = current.getChild(name);
					// Null is OK and means child does not exist
					state = SEEN_KEY;
				}
				break;

			case PathLexer.Posint:
				if(current == null) {
					throw new PathException();
				}
				final int index = Integer.parseInt(terminal.getText());
				current = current.getChild(index);
				expectSelector = false;
				break;

			case PathLexer.Colon: // Ignore
			case PathLexer.Square:
			case PathLexer.Erauqs:
				break;

			case PathLexer.Recurse:
				recursive = true;
				nodeIncluded = terminal.getText().length() == 2;
				break;

			default:
				throw new InexasRuntimeException("Type: " + terminal.getSymbol().getType());
			}
		}
	}

	private final String text;
	private final Navigable start;
	private Navigable current;
	private final Source source;
	private boolean recursive;
	private List<String> errorMessages;
	private boolean nodeIncluded = true;

	public Path(String text, Navigable current) {
		this(text, null, current);

		if(current == null) {
			throw new InexasRuntimeException("Null object as current starting point");
		}
	}

	public Path(String text, Source source) {
		this(text, source, null);

		if(source == null) {
			throw new InexasRuntimeException("Null object as source");
		}
	}

	private Path(String text, Source source, Navigable start) {
		this.text = text;
		this.source = source;
		current = this.start = start;

		process(new StringReader(text));
		if(errorMessages != null) {
			throw new InexasRuntimeException(errorMessages.get(0) + ": " + text);
		}
	}

	public <T extends Navigable> T locate() {
		@SuppressWarnings("unchecked")
		final T result = (T)current;
		return result;
	}

	/**
	 * Recursive paths end with a "/@" or "/@@" and indicate that either the all
	 * descendents are indicated or the descendents and the node itself
	 * respectively.
	 *
	 *
	 * @return True if the path is recursive, i.e. like /abc/@
	 * @see #isNodeIncluded()
	 */
	public boolean isRecursive() {
		return recursive;
	}

	/**
	 *
	 * @return True if the .../@@, i.e. the Node is to be included in the
	 *         result.
	 */
	public boolean isNodeIncluded() {
		return nodeIncluded;
	}

	private void process(Reader reader) {
		try {
			// Create the lexer...
			final ANTLRInputStream inputStream = new ANTLRInputStream(reader);
			final PathLexer lexer = new PathLexer(inputStream);

			// Create the parser...
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final PathParser parser = new PathParser(tokens);

			// Fix up error listener...
			final MyErrorListener errorListener = new MyErrorListener();
			lexer.removeErrorListeners();
			lexer.addErrorListener(errorListener);
			parser.removeErrorListeners(); // Remove ConsoleErrorListener
			parser.addErrorListener(errorListener);

			// Walk the tree to convert it to an AST...
			final ParserRuleContext ruleContext = parser.path();
			final ParseTreeWalker walker = new ParseTreeWalker();
			walker.walk(new Visitor(), ruleContext);
		} catch(final Exception e) {
			if(errorMessages == null) {
				errorMessages = new ArrayList<>();
			}
			errorMessages.add(text + ": Parsing error. " + e.getMessage());
		}
	}

}
