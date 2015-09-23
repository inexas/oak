package com.inexas.oak;

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
			final Path path = Path.parse(pathText);
			return path.locate(null, this);
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
	public void test() {
		final Path path = Path.parse("`.`");
		final Element result = path.locate(null, a);
		assertEquals(a, result);
	}

	private void doTest(String string) {
		final String quoted = "`" + string + '`';
		final Path path = Path.parse(quoted);
		assertEquals(quoted, path.toString());
	}

	private void testLocate(Element expected, String pathString, Element startAt) {
		final Path path = Path.parse("`" + pathString + '`');
		assertEquals(expected, path.locate(null, startAt));
	}

	private void testLocate(Element expected, String pathString, Source theSource) {
		final Path path = Path.parse("`" + pathString + '`');
		assertEquals(expected, path.locate(theSource, null));
	}

	@Test
	public void testToString() {
		doTest("/");
		doTest(".");
		doTest("..");
		doTest("abc");
		doTest("/abc");
		doTest("/a/b[x]/c[1]/./..**");

		doTest("x:abc");
	}

	@Test
	public void testDots() {
		testLocate(a, ".", a);
		testLocate(a, ".", a);
		testLocate(b, ".", b);
		testLocate(c, ".", c);
		testLocate(a, "..", c);
		testLocate(role, "..", a);
		testLocate(mary, "../../mary", sarah);
	}

	@Test(expected = Exception.class)
	public void testDotsExceptions1() {
		testLocate(a, ".", source);
	}

	@Test(expected = Exception.class)
	public void testDotsExceptions2() {
		testLocate(a, "..", source);
	}

	@Test
	public void testDotsExceptions3() {
		final Path path = Path.parse("`..`");
		assertNull(path.locate(null, role));
	}

	@Test
	public void testRelative() {
		testLocate(c, "c", a);
		testLocate(e, "e", d);
	}

	@Test
	public void testAbsolute() {
		testLocate(role, "/", c);
		testLocate(a, "/a", c);
		testLocate(c, "/a/c", b);
	}

	@Test
	public void testProtocols() {
		testLocate(c, "role:/a/c", source);
	}

	@Test
	public void testSelectors() {
		testLocate(c, "a[c]", role);
		testLocate(c, "a[0]", role);
		testLocate(d, "a[d]", role);
		testLocate(d, "a[1]", role);
	}

	@Test
	public void testRecursives() {
		testLocate(d, "a[1]", role);

		testLocate(role, "/*", role);
		testLocate(a, "/a*", role);
		testLocate(a, "a*", role);

		assertEquals(Path.Recurse.none, Path.parse("`/`").getRecurse());
		assertEquals(Path.Recurse.shallow, Path.parse("`/*`").getRecurse());
		assertEquals(Path.Recurse.deep, Path.parse("`/**`").getRecurse());
	}
}
