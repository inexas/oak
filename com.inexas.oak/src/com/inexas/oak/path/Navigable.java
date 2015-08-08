package com.inexas.oak.path;

import com.inexas.exception.UnsupportedException;

public interface Navigable {
	/**
	 * Typically implemented as:
	 *
	 * <pre>
	 * public final String getPath() {
	 *     final TextBuilder result = new TextBuilder();
	 *     getPath(result);
	 *     return result.toString();
	 * }
	 *
	 *  ...and...
	 *
	 * private void getPath(TextBuilder result) {
	 *     if(parent == null) {
	 *         result.append('/');
	 *     } else {
	 *     if(result.length() &gt; 1) {
	 *         // Not directly beneath root
	 *         result.append('/');
	 *         }
	 *     }
	 * }
	 * </pre>
	 *
	 * @return The path as a String, e.g. /My/Object/Path
	 */
	String getPath();

	/**
	 * Return the parent of this element.
	 *
	 * @param <T>
	 *            A object that extends Navigable.
	 * @return Parent or null if the element is the root element.
	 */
	<T extends Navigable> T getParent();

	/**
	 * Return the root element of this element tree.
	 *
	 * Typically implemented as:
	 *
	 * <pre>
	 * &#064;SuppressWarnings(&quot;unchecked&quot;)
	 * final T result = parent == null ? (T)this : parent.getParent();
	 * return result;
	 * </pre>
	 *
	 * @param <T>
	 *            A object that extends Navigable.
	 * @return The root element of this element tree.
	 */
	<T extends Navigable> T getRoot();

	/**
	 * Get a named child.
	 *
	 * @param <T>
	 *            A object that extends Navigable.
	 * @param name
	 *            The name of the child to retrieve.
	 * @return Child or null if the element has no child of that name.
	 * @throws UnsupportedException
	 *             If this node cannot have children.
	 */
	<T extends Navigable> T getChild(String name) throws UnsupportedException;

	/**
	 * Get an indexed child.
	 *
	 * @param <T>
	 *            A object that extends Navigable.
	 * @param index
	 *            The index of the child to retrieve.
	 * @return child The child element.
	 * @throws UnsupportedException
	 *             If this element is not an array or the element cannot have
	 *             children.
	 * @throws ArrayIndexOutOfBoundsException
	 *             Index out of range: index &lt; 0 || index &gt;= size().
	 */
	<T extends Navigable> T getChild(int index) throws UnsupportedException;

	/**
	 * <h1>Typical implementation</h1>
	 *
	 * <Pre>
	 * final Path locator = new Path(path, this);
	 * &#064;SuppressWarnings(&quot;unchecked&quot;)
	 * final T result = (T)locator.locate();
	 * return result;
	 * </Pre>
	 *
	 * @param <T>
	 *            A object that extends Navigable.
	 * @param path
	 *            The path of the node to search for, e.g. /a/bc.
	 * @return The node that has been located.
	 */
	<T extends Navigable> T locate(String path);
	// !todo Exception for error in path?
}
