package com.inexas.oak.path;

import java.util.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.inexas.exception.UnexpectedException;
import com.inexas.util.Text;

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
			final Text t = new Text();
			toString(t);
			return t.toString();
		}

		private void toString(Text t) {
			switch(type) {
			case SLASH:
				t.append('/');
				break;
			case SELF:
				t.append('.');
				break;
			case PARENT:
				t.append("..");
				break;
			case NAMED:
				t.append(name);
				break;
			default:
				throw new UnexpectedException("toString: " + type);
			}
			if(index != null) {
				t.append('[');
				t.append(index.toString());
				t.append(']');
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

	private final static Text.Checker oneToNine = new Text.Checker() {
		@Override
		public boolean isValid(int offset, char c) {
			return c >= '1' && c <= '9';
		}
	};
	private final static Text.Checker zeroToNine = new Text.Checker() {
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
		final Text t = new Text();
		t.append(string);
		return parse(t);
	}

	/**
	 * Parse a path from a TextBuilder. e.g. `/Abc`
	 *
	 * @param tb
	 *            Source to parse.
	 * @return Either a Path or null if a path could not be parsed.
	 */
	@Nullable
	public static Path parse(Text t) {
		final Path result;

		// Set up for parse...
		final int save = t.cursor();
		final Recurse recurse;
		final String protocol;
		@Nullable
		final Element[] elementList;

		// '`' path: protocol? elementList recurse? '`' ;
		if(t.consume('`')
				&& ((protocol = protocol(t)) != null || true)
				&& ((elementList = elementList(t)) != null)
				&& ((recurse = recurse(t)) != null || true)
				&& t.consume('`')) {
			assert elementList != null;
			result = new Path(protocol, elementList, recurse);
		} else {
			t.setCursor(save);
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
	private static String protocol(Text t) {
		final String result;

		// Identifier ':'
		final int save = t.cursor();
		if(Identifier.consume(t) && t.consume(':')) {
			result = t.getString(save, t.cursor() - 1); // -1 for ':'
		} else {
			t.setCursor(save);
			result = null;
		}

		return result;
	}

	@Nullable
	private static Element[] elementList(Text t) {
		@Nullable
		final Element[] result;

		final List<Element> elementList = new ArrayList<>();

		// elementList
		// . : slash
		// . | slash? element (slash element)*
		// . ;
		slash(t, elementList);

		if(element(t, elementList)) {
			while(true) {
				final int save = t.cursor();
				final int saveElementList = elementList.size();
				if(slash(t, elementList) && element(t, elementList)) {
					// Keep going
				} else {
					t.setCursor(save);
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

	private static boolean element(Text t, List<Element> elementList) {
		final boolean result;

		// element: ('.' |'..' | Id) '[' ( posint | Id) ']'
		final int type;
		final String name;
		Object index;
		if(t.consume('.')) {
			if(t.consume('.')) {
				type = PARENT;
				name = "..";
			} else {
				type = SELF;
				name = ".";
			}
			result = true;
		} else {
			type = NAMED;
			name = identifier(t);
			result = name != null;
		}

		if(result) {
			final int save = t.cursor();
			if(t.consume('[')
					&& (((index = posint(t)) != null) || ((index = identifier(t)) != null))
					&& t.consume(']')) {
				// index is set up
			} else {
				t.setCursor(save);
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

	private static String identifier(Text t) {
		final String result;

		final int start = t.cursor();
		if(Identifier.consume(t)) {
			result = t.getString(start);
		} else {
			result = null;
		}

		return result;
	}

	@Nullable
	private static Integer posint(Text t) {
		final Integer result;

		// '0' | ([1-9][0-9]*)
		final int save = t.cursor();
		if(t.consume('0')) {
			result = new Integer(0);
		} else if(t.parse(oneToNine) != null) {
			t.parse(zeroToNine);
			final String string = t.getString(save);
			result = new Integer(string);
		} else {
			result = null;
		}

		return result;
	}

	private static boolean slash(Text t, List<Element> elementList) {
		final boolean result;

		if(t.consume('/')) {
			elementList.add(new Element(SLASH, "/", null));
			result = true;
		} else {
			result = false;
		}

		return result;
	}

	private static Recurse recurse(Text t) {
		final Recurse result;

		if(t.consume('*')) {
			result = t.consume('*') ? Recurse.deep : Recurse.shallow;
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
		final Text t = new Text();
		toString(t);
		return t.toString();
	}

	public void toString(Text t) {
		t.append('`');

		if(protocol != null) {
			t.append(protocol);
			t.append(':');
		}

		for(final Element element : elements) {
			element.toString(t);
		}

		switch(recurse) {
		case none:
			break;

		case shallow:
			t.append('*');
			break;

		case deep:
			t.append("**");
			break;

		default:
			throw new UnexpectedException("toString: " + recurse);
		}
		t.append('`');
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
