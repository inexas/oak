/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.dialect;

import java.io.*;
import java.util.*;
import com.inexas.exception.InexasRuntimeException;
import com.inexas.util.*;

/**
 * An Oak dialect.
 */
public class Dialect {
	public final String type;
	public final Rule[] rules;
	private FileWriter writer;
	private List<String> visitorsList;
	private String indent = "";
	private final List<ObjectRule> objects = new ArrayList<>();

	public Dialect(String type, Rule[] rules) {
		this.type = type;
		this.rules = rules;
	}

	/**
	 * Write the dialect into a path in the file system. The dialect is written
	 * as a Java class with the name &lt;Grammar&gt;Dialect.java and a visitor
	 * pattern interface is also generated.
	 *
	 * @param destinationPath
	 *            The file path to write the dialect files.
	 * @param packageName
	 *            The Java package name to use in the files, e.g.
	 *            "com.example.hr"
	 */
	public void write(String destinationPath, String packageName) {
		writeDialect(destinationPath, packageName);
		writeVisitor(destinationPath, packageName);
	}

	private void writeDialect(String sourceDirectoryName, String packageName) {

		final String className = type + "Dialect";

		final String sourceDirectory = FileU.ROOT + '/' + sourceDirectoryName + '/';
		final String fileDirectory = sourceDirectory + packageName.replace('.', '/') + '/';
		final String path = fileDirectory + className + ".java";

		try {
			final File sourceFile = new File(sourceDirectory);
			if(!sourceFile.isDirectory()) {
				throw new InexasRuntimeException("Source directory does not exits: " + sourceDirectory);
			}

			final File file = new File(fileDirectory);
			if(file.isDirectory()) {
				// OK
			} else if(file.exists()) {
				throw new InexasRuntimeException("Cannot create: " + fileDirectory);
			} else {
				if(!file.mkdirs()) {
					throw new InexasRuntimeException("Cannot create: " + fileDirectory);
				}
			}

			writer = new FileWriter(path, false);
			indentAppendNewline("// Created by Oak: " + DateU.formatDateTimeAirline(new Date()));
			indentAppendNewline("package " + packageName + ';');
			newline();

			indentAppendNewline("import static com.inexas.oak.CollectionType.*;");
			indentAppendNewline("import com.inexas.oak.*;");
			indentAppendNewline("import com.inexas.exception.InexasRuntimeException;");
			indentAppendNewline("import com.inexas.util.Cardinality;");
			newline();

			// indentAppendNewline("@SuppressWarnings(\"unused\")");
			indentAppendNewline("public class " + className + " {");
			indentMore();

			indentAppendNewline("public static Dialect dialect;");
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

			indentAppendNewline("private static void setRules(Rule... rules) {");
			indentMore();
			indentAppendNewline(type + "Dialect.dialect = new Dialect(\"Dialect\", rules);");
			indentLess();
			indentAppendNewline("}");
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

			// The static code block...
			indentAppendNewline("static {");
			indentMore();
			indentAppendNewline("try {");
			indentMore();

			// Rules...
			final List<String> names = new ArrayList<>();
			for(final Rule rule : rules) {
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
					objects.add(object);
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
			throw new InexasRuntimeException("Error writing: " + sourceDirectory, e);
		}
	}

	private void writeVisitor(String sourceDirectory, String packageName) {

		// srcgen/com/inexas/willow/WillowVisitor.java
		final String interfaceName = type + "Visitor";

		final String path = FileU.ROOT + '/' + sourceDirectory + '/'
				+ packageName.replace('.', '/') + '/' + interfaceName + ".java";

		try {
			writer = new FileWriter(path, false);
			indentAppendNewline("// Created by Oak: " + DateU.formatDateTimeAirline(new Date()));
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

	private String getParameterList(ObjectRule rule) {
		final String result;

		final Class<?> templateClazz = rule.getTemplateClass();
		final String className = templateClazz.getName().replace('$', '.');
		final String valiableName = getVariableName(templateClazz.getSimpleName().replace('$', '.'));
		result = className + ' ' + valiableName;

		return result;
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
