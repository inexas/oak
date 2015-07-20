package com.inexas.oak.path;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;

public class TestPath {
	private class TestSource implements Source {
		private final Map<String, Element> protocols = new HashMap<>();

		void add(Element root) {
			protocols.put(root.name, root);
		}

		@Override
		public Navigable getRoot(String protocol) {
			return protocols.get(protocol);
		}
	}

	private class Element implements Navigable {
		final Element parent;
		final List<Element> childList = new ArrayList<>();
		final Map<String, Element> childMap = new HashMap<>();
		final String name;

		public Element(Element parent, String name) {
			this.parent = parent;
			this.name = name;
		}

		@Override
		public String getPath() {
			final StringBuilder sb = new StringBuilder();
			getPath(sb);
			return sb.toString();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Navigable> T getParent() {
			return (T)parent;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Navigable> T getRoot() {
			final T result;
			if(parent == null) {
				result = (T)this;
			} else {
				result = parent.getRoot();
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Navigable> T getChild(String childsName) {
			return (T)childMap.get(childsName);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Navigable> T getChild(int index) {
			final T result;

			if(childList.size() == 1) {
				throw new UnsupportedOperationException();
			}
			result = (T)childList.get(index);

			return result;
		}

		@Override
		public int hashCode() {
			return name == null ? 0 : name.hashCode();
		}

		@Override
		public boolean equals(Object object) {
			return this == object;
		}

		@Override
		public String toString() {
			return getPath();
		}

		Element addChild(String childName) {
			final Element result = new Element(this, childName);
			childList.add(result);
			childMap.put(childName, result);
			return result;
		}

		private void getPath(StringBuilder sb) {
			if(parent != null) {
				parent.getPath(sb);
				final int index = parent.getIndex(this);
				if(index >= 0) {
					sb.append('[');
					sb.append(index);
					sb.append(']');
				} else {
					sb.append('/');
					sb.append(name);
				}
			} else {
				sb.append('/');
				sb.append(name);
			}
		}

		private int getIndex(Element child) {
			final int result;

			if(childList.size() == 1) {
				result = -1;
			} else {
				int index = 0;
				boolean found = false;
				for(final Element element : childList) {
					if(element == child) {
						found = true;
						break;
					}
					index++;
				}

				if(!found) {
					index = -1;
				}

				result = index;
			}

			return result;
		}

		@Override
		public <T extends Navigable> T locate(String pathText) {
			final Path path = new Path(pathText, this);
			@SuppressWarnings("unchecked")
			final T result = (T)path.locate();
			return result;
		}

	}

	private final TestSource source = new TestSource();
	private Element role, person, a, b, c, d, e, john, mary, sarah;

	@Before
	public void before() {
		role = new Element(null, "role");
		source.add(role);
		a = role.addChild("a");
		b = role.addChild("b");
		c = a.addChild("c");
		d = a.addChild("d");
		e = d.addChild("e");

		person = new Element(null, "person");
		source.add(person);
		john = person.addChild("john");
		mary = person.addChild("mary");
		sarah = john.addChild("sarah");
	}

	@Test
	public void testDots() {
		assertEquals(a, new Path(".", a).locate());
		assertEquals(b, new Path(".", b).locate());
		assertEquals(c, new Path(".", c).locate());
		assertEquals(a, new Path("..", c).locate());
		assertEquals(role, new Path("..", a).locate());
		assertEquals(mary, new Path("../../mary", sarah).locate());
	}

	@Test(expected = Exception.class)
	public void testDotsExceptions1() {
		new Path(".", source).locate();
	}

	@Test(expected = Exception.class)
	public void testDotsExceptions2() {
		new Path("..", source).locate();
	}

	@Test(expected = Exception.class)
	public void testDotsExceptions3() {
		new Path("..", role).locate();
	}

	@Test
	public void testRelative() {
		assertEquals(c, new Path("c", a).locate());
		assertEquals(e, new Path("e", d).locate());
	}

	@Test
	public void testAbsolute() {
		assertEquals(role, new Path("/", c).locate());
		assertEquals(a, new Path("/a", c).locate());
		assertEquals(c, new Path("/a/c", b).locate());
	}

	@Test
	public void testProtocols() {
		assertEquals(c, new Path("role:/a/c", source).locate());
	}

	@Test
	public void testSelectors() {
		assertEquals(c, new Path("a[c]", role).locate());
		assertEquals(c, new Path("a[0]", role).locate());
		assertEquals(d, new Path("a[d]", role).locate());
		assertEquals(d, new Path("a[1]", role).locate());
	}

	@Test
	public void testRecursives() {
		assertEquals(role, new Path("/@", role).locate());
		assertEquals(a, new Path("/a/@", role).locate());
		assertEquals(a, new Path("a/@", role).locate());

		assertFalse(new Path("/", role).isRecursive());
		assertTrue(new Path("/@", role).isRecursive());
		assertFalse(new Path("/@", role).isNodeIncluded());

		assertTrue(new Path("/@@", role).isRecursive());
		assertTrue(new Path("/@@", role).isNodeIncluded());
	}
}
