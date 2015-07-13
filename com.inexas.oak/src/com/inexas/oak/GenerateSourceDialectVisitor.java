package com.inexas.oak;

import java.io.*;
import java.util.*;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.oak.advisory.*;
import com.inexas.util.*;

/**
 * Visit a Dialect tree and output a Rulebase and a Visitor interface.
 */
public class GenerateSourceDialectVisitor extends DialectVisitor.Base {
	/**
	 * In the first pass we just collect all the Object and Property Rules
	 */
	private class FirstPassVisitor extends DialectVisitor.Base {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Objet object) {

			try {
				final Class<?> templateClass = Class.forName(object.templateClass);
				final Rule rule = new ObjectRule(
						object.key,
						templateClass,
						object.isRoot);
				put(rule);
			} catch(final Exception e) {
				error(object, "Class not found: " + object.templateClass);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exit(Property property) {
			final String key = property.key;
			final Rule rule = new PropertyRule(key, property.type, property.constraints);
			rules.put(key, rule);
		}
	}

	public final Advisory advisory;
	private final Map<String, Rule> rules = new HashMap<>();
	private final List<ObjectRule> objects = new ArrayList<>();
	private final List<PropertyRule> properties = new ArrayList<>();
	private final List<Relation> relations = new ArrayList<>();
	private String dialectKey;
	private FileWriter writer;
	private String indent = "";
	private List<String> visitorsList;

	public GenerateSourceDialectVisitor(Advisory advisory) {
		this.advisory = advisory;
	}

	public Rule[] getRulebase() {
		return rules.values().toArray(new Rule[rules.size()]);
	}

	/**
	 *
	 * @param sourceDirectory
	 *            e.g. "srcgen"
	 * @param packageName
	 *            e.g. "com.inexas.oak"
	 */
	public void write(String sourceDirectory, String packageName) {
		if(advisory.hasErrors()) {
			throw new InexasRuntimeException("Source has errors");
		}
		writeRulebase(sourceDirectory, packageName);
		writeVisitor(sourceDirectory, packageName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(Dialect dialect) {
		// First pass visitor collects all the elements
		dialect.accept(new FirstPassVisitor());
		dialectKey = dialect.key;
		visitorsList = dialect.visitorsList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(Member member) {
		final Rule subject = rules.get(member.key);
		if(subject != null) {
			final Relation relation;
			if(subject instanceof PropertyRule) {
				final PropertyRule property = (PropertyRule)subject;
				relation = new Relation(property, member.cardinality);
			} else {
				final ObjectRule object = (ObjectRule)subject;
				relation = new Relation(
						object,
						member.cardinality,
						member.collectionType);
			}
			relations.add(relation);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(Objet object) {
		final ObjectRule rule = (ObjectRule)rules.get(object.key);
		final Relation[] array = relations.toArray(new Relation[relations.size()]);
		relations.clear();
		try {
			rule.setRelations(array);
		} catch(final Exception e) {
			error(object, e.getMessage());
		}
	}

	private void writeRulebase(String sourceDirectory, String packageName) {

		final String className = dialectKey + "Rulebase";

		final String path = FileU.ROOT + '/' + sourceDirectory + '/'
				+ packageName.replace('.', '/') + '/' + className + ".java";

		try {
			writer = new FileWriter(path, false);
			indentAppendNewline("// " + DateU.formatDateTimeAirline(new Date()));
			indentAppendNewline("package " + packageName + ';');
			newline();

			indentAppendNewline("import static com.inexas.oak.CollectionType.*;");
			indentAppendNewline("import com.inexas.oak.*;");
			indentAppendNewline("import com.inexas.exception.InexasRuntimeException;");
			indentAppendNewline("import com.inexas.util.Cardinality;");
			newline();

			indentAppendNewline("public class " + className + " {");
			indentMore();

			indentAppendNewline("private static void setRules(Rule... rules) {");
			indentMore();
			indentAppendNewline(dialectKey + "Rulebase.rules = rules;");
			indentLess();
			indentAppendNewline("}");
			newline();

			indentAppendNewline("private static Relation relate(PropertyRule rule, Cardinality cardinality) {");
			indentMore();
			indentAppendNewline("return new Relation(rule, cardinality);");
			indentLess();
			indentAppendNewline("}");
			newline();

			indentAppendNewline("private static Relation relate(ObjectRule key, "
					+ "Cardinality cardinality, "
					+ "CollectionType collection) {");
			indentMore();
			indentAppendNewline("return new Relation(key, cardinality, collection);");
			indentLess();
			indentAppendNewline("}");
			newline();

			indentAppendNewline("public static Rule[] rules;");
			newline();

			// public static String[] visitorList = { "a.b.c","a.b.c" };
			indentAppendNewline("public static String[] visitorList = {");
			if(visitorsList != null) {
				indentMore();
				boolean delimit = false;
				for(final String visitor : visitorsList) {
					if(delimit) {
						append("\",\n");
					} else {
						delimit = true;
					}
					indentAppend("\"");
					append(visitor);
				}
				append("\"\n");
				indentLess();
			}
			indentAppendNewline("};");
			newline();

			indentAppendNewline("static {");
			indentMore();
			indentAppendNewline("try {");
			indentMore();

			// Rules...
			final List<String> names = new ArrayList<>();
			for(final Rule rule : getRulebase()) {
				// Take care of reserved words like 'class'
				final String name_ = rule.name + '_';
				names.add(name_);
				if(rule instanceof PropertyRule) {
					final PropertyRule property = (PropertyRule)rule;
					indentAppendNewline("final PropertyRule " + name_ + " = new PropertyRule(");
					indentMore();
					indentMore();
					indentAppendNewline('"' + property.name + "\",");
					append(indent);
					append("DataType." + property.dataType.name());
				} else {
					final ObjectRule object = (ObjectRule)rule;
					indentAppendNewline("final ObjectRule " + name_ + " = new ObjectRule(");
					indentMore();
					indentMore();
					indentAppendNewline('"' + object.name + "\",");
					// Inner classes contain $ which become '.'
					indentAppendNewline(object.getTemplateClass().getName().replace('$', '.') + ".class,");
					indentAppend(object.isRoot() ? "true" : "false");
				}

				final Constraint[] constraints = rule.constraints;
				if(constraints == null) {
					appendNewline(");");
				} else {
					appendNewline(",");

					// new ChoiceConstraint("map", "list", ...));
					final int count = constraints.length;
					final TextBuilder tb = new TextBuilder(true);
					for(int i = 0; i < count; i++) {
						final Constraint constraint = constraints[i];
						tb.append("new ");
						tb.append(constraint.getClass().getName());
						tb.append('(');
						final Object[] values = constraint.values;
						// !todo this needs some work for different data
						// types, escaping strings, etc
						for(final Object value : values) {
							tb.delimit();
							tb.append('"');
							tb.append(value.toString());
							tb.append('"');
						}
						tb.append(i == count - 1 ? "));" : "),");
					}
					indentAppendNewline(tb.toString());
				}

				indentLess();
				indentLess();
				newline();

			}

			// Relations...
			for(final ObjectRule object : objects) {
				// Take care of names like 'class'
				final String name_ = object.name + '_';
				indentAppendNewline(name_ + ".setRelations(");
				indentMore();
				indentMore();

				final Relation[] children = object.getRelations();
				final int count = children.length;
				for(int i = 0; i < count; i++) {
					final Relation relation = children[i];
					final String end = i == count - 1 ? ");" : ",";

					if(relation.subjectIsObject) {
						// relate(name_, Cardinality.newInstance("1..*"),
						// set),
						final ObjectRule subject = (ObjectRule)relation.subject;
						final String name = subject.name + "_, ";
						final String cardinality = "Cardinality.newInstance(\""
								+ relation.cardinality.toString() + "\")";
						final String collection = relation.collection.name();
						indentAppendNewline("relate(" + name + cardinality + ", " + collection + ')' + end);
					} else {
						// relate(name_, Cardinality.newInstance(1..*)),
						final PropertyRule subject = (PropertyRule)relation.subject;
						final String name = subject.name + "_, ";
						final String cardinality = "Cardinality.newInstance(\""
								+ relation.cardinality.toString() + "\")";
						indentAppendNewline("relate(" + name + cardinality + ')' + end);
					}
				}
				indentLess();
				indentLess();
				newline();
			}

			// setRules(key, clazz, ...);
			indentAppendNewline("setRules(" + StringU.stringify(names) + ");");

			indentLess(); // End try
			indentAppendNewline("} catch(final Exception e) {");
			indentMore();
			indentAppendNewline("throw new InexasRuntimeException(\"Error building "
					+ className + "\", e);");
			indentLess(); // End catch
			indentAppendNewline("}");

			indentLess(); // End static
			indentAppendNewline("}");

			indentLess();
			indentAppendNewline("}"); // End class

			writer.close();
		} catch(final IOException e) {
			throw new InexasRuntimeException("Error writing: " + path, e);
		}
	}

	private void writeVisitor(String sourceDirectory, String packageName) {

		// srcgen/com/inexas/willow/WillowVisitor.java
		final String interfaceName = dialectKey + "Visitor";

		final String path = FileU.ROOT + '/' + sourceDirectory + '/'
				+ packageName.replace('.', '/') + '/' + interfaceName + ".java";

		try {
			writer = new FileWriter(path, false);
			indentAppendNewline("// " + DateU.formatDateTimeAirline(new Date()));
			indentAppendNewline("package " + packageName + ';');
			newline();

			indentAppendNewline("public interface " + interfaceName + " {");
			indentMore();
			newline();

			// Base class...

			indentAppendNewline("public static class Base implements " + interfaceName + " {");
			indentMore();

			for(final ObjectRule object : objects) {
				final String parameterList = getParameterList(object);
				if(object.hasChildren()) {
					// @Override public void enter(Xxx xxx) { /**/ }
					// @Override public void exit(Xxx xxx) { /**/ }
					indentAppendNewline("@Override public void enter(" + parameterList + ") { /**/ }");
					indentAppendNewline("@Override public void exit(" + parameterList + ") { /**/ }");
				} else {
					// @Override public void exit(Xxx xxx) { /**/ }
					indentAppendNewline("@Override public void visit(" + parameterList + ") { /**/ }");
				}
			}

			indentLess();
			indentAppendNewline("}");
			newline();

			// Interface body...

			for(final ObjectRule object : objects) {
				final String parameterList = getParameterList(object);
				if(object.hasChildren()) {
					// void enter(Application application);
					// void exit(Application application);
					indentAppendNewline("void enter(" + parameterList + ");");
					indentAppendNewline("void exit(" + parameterList + ");");
				} else {
					// void visit(Application application);
					indentAppendNewline("void visit(" + parameterList + ");");
				}
			}

			indentLess();
			indentAppendNewline("}"); // End interface

			writer.close();
		} catch(final IOException e) {
			throw new InexasRuntimeException("Error writing: " + path, e);
		}
	}

	private String getParameterList(ObjectRule rule) {
		final String result;

		final Class<?> templateClazz = rule.getTemplateClass();
		final String className = templateClazz.getName().replace('$', '.');
		final String valiableName = getVariableName(templateClazz.getSimpleName().replace('$', '.'));
		result = className + ' ' + valiableName;

		return result;
	}

	private void put(Rule rule) {
		if(rules.put(rule.name, rule) != null) {
			throw new InexasRuntimeException("Duplicate name: " + rule.name);
		}
		if(rule instanceof ObjectRule) {
			objects.add((ObjectRule)rule);
		} else {
			properties.add((PropertyRule)rule);
		}
	}

	private void error(Locus node, String message) {
		advisory.reportError(node, message);
	}

	private void indentAppendNewline(String string) throws IOException {
		writer.write(indent);
		writer.write(string);
		writer.write('\n');
	}

	private void indentAppend(String string) throws IOException {
		writer.write(indent);
		writer.write(string);
	}

	private void appendNewline(String string) throws IOException {
		writer.write(string);
		writer.write('\n');
	}

	private void append(String string) throws IOException {
		writer.write(string);
	}

	private void newline() throws IOException {
		writer.write('\n');
	}

	private void indentMore() {
		indent += '\t';
	}

	private void indentLess() {
		indent = indent.substring(1);
	}

	private String getVariableName(String className) {
		final TextBuilder result = new TextBuilder();
		final char c = className.charAt(0);
		if(c <= 'Z') {
			result.append((char)(c + ('a' - 'A')));
			result.append(className.substring(1));
		} else {
			result.append(className);
		}
		return result.toString();
	}
}
