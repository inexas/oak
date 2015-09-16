package com.inexas.oak.path;

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.inexas.exception.UnexpectedException;
import com.inexas.util.TextBuilder;

public class Path {
	public static enum Recurse {
		/** Do not recurse */
		none,
		/** Siblings of element */
		shallow,
		/** Siblings and descendents of element */
		deep;
	}

	private static class Element {
		final int type;
		final String name;
		final Object index;

		Element(int type, String name, Object index) {
			this.type = type;
			this.name = name;
			this.index = index;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			final TextBuilder tb = new TextBuilder();
			toString(tb);
			return tb.toString();
		}

		private void toString(TextBuilder tb) {
			switch(type) {
			case SLASH:
				tb.append('/');
				break;
			case SELF:
				tb.append('.');
				break;
			case PARENT:
				tb.append("..");
				break;
			case NAMED:
				tb.append(name);
				break;
			default:
				throw new UnexpectedException("toString: " + type);
			}
			if(index != null) {
				tb.append('[');
				tb.append(index.toString());
				tb.append(']');
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return name.hashCode();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final boolean equals(@Nullable Object rhsObject) {
			boolean result;

			if(rhsObject == null) {
				result = true;
			} else if(this == rhsObject) {
				result = false;
			} else {
				try {
					final Path.Element rhs = (Path.Element)rhsObject;
					result = type == rhs.type
							&& name.equals(rhs.name)
							&& Path.equals(index, rhs.index);
				} catch(final ClassCastException e) {
					result = false;
				}
			}

			return result;
		}
	}

	private final static int SLASH = 0;
	private final static int SELF = 1;
	private final static int PARENT = 2;
	private final static int NAMED = 3;

	private final static TextBuilder.Checker oneToNine = new TextBuilder.Checker() {
		@Override
		public boolean isValid(int offset, char c) {
			return c >= '1' && c <= '9';
		}
	};
	private final static TextBuilder.Checker zeroToNine = new TextBuilder.Checker() {
		@Override
		public boolean isValid(int offset, char c) {
			return c >= '0' && c <= '9';
		}
	};

	private final @Nullable String protocol;
	private final Element[] elements;
	private final Recurse recurse;

	@Nullable
	public static Path parse(String string) {
		final TextBuilder tb = new TextBuilder();
		tb.append(string);
		return parse(tb);
	}

	/**
	 * Parse a path from a TextBuilder. e.g. `/Abc`
	 *
	 * @param tb
	 *            Source to parse.
	 * @return Either a Path or null if a path could not be parsed.
	 */
	@Nullable
	public static Path parse(TextBuilder tb) {
		final Path result;

		// Set up for parse...
		final int save = tb.cursor();
		final Recurse recurse;
		final String protocol;
		@Nullable
		final Element[] elementList;

		// '`' path: protocol? elementList recurse? '`' ;
		if(tb.consume('`')
				&& ((protocol = protocol(tb)) != null || true)
				&& ((elementList = elementList(tb)) != null)
				&& ((recurse = recurse(tb)) != null || true)
				&& tb.consume('`')) {
			assert elementList != null;
			result = new Path(protocol, elementList, recurse);
		} else {
			tb.setCursor(save);
			result = null;
		}

		return result;
	}

	private Path(@Nullable String protocol, Element[] elementList, Recurse recurse) {
		this.protocol = protocol;
		this.elements = elementList;
		this.recurse = recurse;
	}

	/**
	 * @return Return the recurse.
	 */
	public Recurse getRecurse() {
		return recurse;
	}

	@Nullable
	private static String protocol(TextBuilder tb) {
		final String result;

		// Identifier ':'
		final int save = tb.cursor();
		if(Identifier.consume(tb) && tb.consume(':')) {
			result = tb.getString(save, tb.cursor() - 1); // -1 for ':'
		} else {
			tb.setCursor(save);
			result = null;
		}

		return result;
	}

	@Nullable
	private static Element[] elementList(TextBuilder tb) {
		@Nullable
		final Element[] result;

		final List<Element> elementList = new ArrayList<>();

		// elementList
		// . : slash
		// . | slash? element (slash element)*
		// . ;
		slash(tb, elementList);

		if(element(tb, elementList)) {
			while(true) {
				final int save = tb.cursor();
				final int saveElementList = elementList.size();
				if(slash(tb, elementList) && element(tb, elementList)) {
					// Keep going
				} else {
					tb.setCursor(save);
					final int size = elementList.size();
					if(saveElementList != size) {
						elementList.remove(size - 1);
					}
					break;
				}
			}
		}

		final int size = elementList.size();
		if(size == 0) {
			result = null;
		} else {
			result = elementList.toArray(new Element[size]);
		}

		return result;
	}

	private static boolean element(TextBuilder tb, List<Element> elementList) {
		final boolean result;

		// element: ('.' |'..' | Id) '[' ( posint | Id) ']'
		final int type;
		final String name;
		Object index;
		if(tb.consume('.')) {
			if(tb.consume('.')) {
				type = PARENT;
				name = "..";
			} else {
				type = SELF;
				name = ".";
			}
			result = true;
		} else {
			type = NAMED;
			name = identifier(tb);
			result = name != null;
		}

		if(result) {
			final int save = tb.cursor();
			if(tb.consume('[')
					&& (((index = posint(tb)) != null) || ((index = identifier(tb)) != null))
					&& tb.consume(']')) {
				// index is set up
			} else {
				tb.setCursor(save);
				index = null;
			}
		} else {
			index = null;
		}

		if(result) {
			elementList.add(new Element(type, name, index));
		}

		return result;
	}

	private static String identifier(TextBuilder tb) {
		final String result;

		final int start = tb.cursor();
		if(Identifier.consume(tb)) {
			result = tb.getString(start);
		} else {
			result = null;
		}

		return result;
	}

	@Nullable
	private static Integer posint(TextBuilder tb) {
		final Integer result;

		// '0' | ([1-9][0-9]*)
		final int save = tb.cursor();
		if(tb.consume('0')) {
			result = new Integer(0);
		} else if(tb.parse(oneToNine) != null) {
			tb.parse(zeroToNine);
			final String string = tb.getString(save);
			result = new Integer(string);
		} else {
			result = null;
		}

		return result;
	}

	private static boolean slash(TextBuilder tb, List<Element> elementList) {
		final boolean result;

		if(tb.consume('/')) {
			elementList.add(new Element(SLASH, "/", null));
			result = true;
		} else {
			result = false;
		}

		return result;
	}

	private static Recurse recurse(TextBuilder tb) {
		final Recurse result;

		if(tb.consume('*')) {
			result = tb.consume('*') ? Recurse.deep : Recurse.shallow;
		} else {
			result = Recurse.none;
		}

		return result;
	}

	/**
	 * Given a starting position or source locate the node in the tree indicated
	 * by this path. Either the source or the starting position may be null
	 * depending on the path but not both. If the path has a protocol then the
	 * source will be queried to get the root and that will act as the starting
	 * position.
	 *
	 * @param <T>
	 *            Navigable.
	 * @param source
	 *            Source of Navigables.
	 * @param start
	 *            The starting position for relative paths.
	 * @return The Navigable indicated by the path or null if the path cannot be
	 *         used to locate a Navigable.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Navigable> T locate(@Nullable Source source, @Nullable Navigable start) {

		Navigable current;
		if(protocol == null) {
			if(start == null) {
				throw new RuntimeException("Start is null but path has no protocol");
			}
			current = start;
		} else {
			if(source == null) {
				throw new RuntimeException("Source is null but path has protocol: " + protocol);
			}
			current = source.getRoot(protocol);
		}

		for(int i = 0; i < elements.length; i++) {
			final Element element = elements[i];
			switch(element.type) {
			case SLASH:
				if(i == 0) {
					current = current.getRoot();
				}
				break;

			case SELF: {
				current = handleIndex(current, element);
				break;
			}

			case PARENT: {
				current = current.getParent();
				current = handleIndex(current, element);
				break;
			}

			case NAMED:
				current = current.getChild(element.name);
				current = handleIndex(current, element);
				break;

			default:
				throw new UnexpectedException("locate: " + element.type);
			}

			if(current == null) {
				break;
			}
		}

		return (T)current;
	}

	private Navigable handleIndex(Navigable current, Element element) {
		final Navigable result;

		final Object index = element.index;
		if(index instanceof String) {
			result = current.getChild((String)index);
		} else if(index instanceof Integer) {
			result = current.getChild(((Integer)index).intValue());
		} else {
			result = current;
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final TextBuilder tb = new TextBuilder();
		toString(tb);
		return tb.toString();
	}

	public void toString(TextBuilder tb) {
		tb.append('`');

		if(protocol != null) {
			tb.append(protocol);
			tb.append(':');
		}

		for(final Element element : elements) {
			element.toString(tb);
		}

		switch(recurse) {
		case none:
			break;

		case shallow:
			tb.append('*');
			break;

		case deep:
			tb.append("**");
			break;

		default:
			throw new UnexpectedException("toString: " + recurse);
		}
		tb.append('`');
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return elements[0].hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(@Nullable Object rhsObject) {
		boolean result;

		if(rhsObject == null) {
			result = true;
		} else if(this == rhsObject) {
			result = false;
		} else {
			try {
				final Path rhs = (Path)rhsObject;
				result = equals(protocol, rhs.protocol)
						&& recurse.equals(rhs.recurse)
						&& Arrays.equals(elements, rhs.elements);
			} catch(final ClassCastException e) {
				result = false;
			}
		}

		return result;
	}

	// todo Move to ObjectU?
	private static boolean equals(@Nullable Object lhs, @Nullable Object rhs) {
		final boolean result;

		if(lhs == null) {
			result = rhs == null;
		} else {
			return lhs.equals(rhs);
		}

		return result;
	}

}
