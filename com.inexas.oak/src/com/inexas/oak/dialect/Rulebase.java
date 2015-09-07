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
import java.util.Map.Entry;
import com.inexas.oak.template.Constraint;
import com.inexas.util.*;

public class Rulebase {
	public final String name;
	public final ObjectRule[] rules;
	private final Date now = new Date();

	public Rulebase(String name, ObjectRule[] rules) {
		this.name = name;
		this.rules = rules;
		Arrays.sort(rules);
	}

	// todo Move this out of here
	/**
	 * Convert the Dialog AST to a file. {@inheritDoc}
	 */
	public String getDialectText(String packageName) {
		final TextBuilder tb = new TextBuilder();

		final String className = name + "Dialect";

		// e.g. // Created by Oak: 14AUG15 09:10:30
		sign(tb);

		// File header...

		tb.append("package ");
		tb.append(packageName);
		tb.append(';');
		tb.newline();
		tb.newline();

		tb.writeline("import static com.inexas.oak.dialect.CollectionType.*;");
		tb.writeline("import com.inexas.oak.DataType;");
		tb.writeline("import com.inexas.oak.dialect.*;");
		tb.writeline("import com.inexas.util.Cardinality;");
		tb.newline();

		/*
		 * Instead of using a
		 * 
		 * tb.writeline("@SuppressWarnings(\"unused\")");
		 * 
		 * in Eclipse, right click on the directory containing the generated
		 * files Properties > Java Compiler > Ignore optional compile problems
		 */
		tb.append("public class ");
		tb.append(className);
		tb.append(" {");
		tb.newline();
		tb.indentMore();

		tb.writeline("public static Rulebase rulebase;");
		tb.newline();

		tb.writeline("private static Relationship relate(Rule key, "
				+ "Cardinality cardinality, "
				+ "CollectionType collection) {");
		tb.indentMore();
		tb.writeline("return new Relationship(key, cardinality, collection);");
		tb.indentLess();
		tb.writeline("}");
		tb.newline();

		tb.writeline("private static void setRules(ObjectRule... rules) {");
		tb.indentMore();
		tb.indent();
		tb.append(name);
		tb.append("Dialect.rulebase = new Rulebase(\"Dialect\", rules);");
		tb.newline();
		tb.indentLess();
		tb.writeline("}");
		tb.newline();

		// The static code block...

		tb.writeline("static {");
		tb.indentMore();
		tb.writeline("try {");
		tb.indentMore();

		// Objects...

		final List<String> names = new ArrayList<>();
		final SortedMap<String, PropertyRule> properties = new TreeMap<>();
		for(final ObjectRule object : rules) {
			// Take care of reserved words like 'class'
			final String _objectName = "_" + object.key;
			names.add(_objectName);

			// Get the list of properties
			for(final Relationship relationship : object.getRelationships()) {
				final Rule rule = relationship.subject;
				if(rule instanceof PropertyRule) {
					final PropertyRule property = (PropertyRule)rule;
					properties.put(_objectName + '_' + property.key, property);
				}
			}

			tb.indent();
			tb.append("final ObjectRule ");
			tb.append(_objectName);
			tb.append(" = new ObjectRule(");
			tb.newline();
			tb.indentMore();
			tb.indentMore();

			tb.indent();
			tb.append('"');
			tb.append(object.key);
			tb.append("\",");
			tb.newline();

			// Inner classes contain $ which become '.'
			tb.indent();
			tb.append(object.getTemplateClass().getName().replace('$', '.'));
			tb.append(".class,");
			tb.newline();

			tb.indent();
			tb.append(object.isRoot() ? "true);" : "false);");
			tb.newline();

			tb.indentLess();
			tb.indentLess();
			tb.newline();
		}

		// Properties...

		for(final Entry<String, PropertyRule> entry : properties.entrySet()) {
			final String _name = entry.getKey();
			final PropertyRule property = entry.getValue();

			tb.indent();
			tb.append("final PropertyRule ");
			tb.append(_name);
			tb.append(" = new PropertyRule(");
			tb.newline();

			tb.indentMore();
			tb.indentMore();
			tb.indent();
			tb.append('"');
			tb.append(property.key);
			tb.append("\",");
			tb.newline();

			tb.indent();
			tb.append("DataType.");
			tb.append(property.dataType.name());

			final Constraint[] constraints = property.constraints;
			if(constraints == null) {
				tb.append(");");
			} else {
				tb.append(",");
				tb.newline();

				// new ChoiceConstraint("map", "list", ...));
				final int count = constraints.length;
				for(int i = 0; i < count; i++) {
					final Constraint constraint = constraints[i];
					tb.indent();
					tb.append("new ");
					tb.append(constraint.getClass().getSimpleName());
					tb.append('(');
					final Object[] values = constraint.values;
					tb.restartDelimiting();
					for(final Object value : values) {
						tb.delimit();
						tb.append('"');
						tb.append(StringU.toJavaString(value.toString()));
						tb.append('"');
					}
					tb.append(i == count - 1 ? "));" : "),");
				}
			}
			tb.newline();

			tb.indentLess();
			tb.indentLess();
			tb.newline();
		}

		// Relationships...

		for(final ObjectRule object : rules) {
			final String objectName = "_" + object.key;
			tb.indent();
			tb.append(objectName);
			tb.append(".setRelationships(");
			tb.newline();

			tb.indentMore();
			tb.indentMore();

			final Relationship[] children = object.getRelationships();
			final int count = children.length;
			for(int i = 0; i < count; i++) {
				final Relationship relation = children[i];
				final String end = i == count - 1 ? ");" : ",";

				tb.indent();
				tb.append("relate(");
				if(!relation.subjectIsObject) {
					tb.append(objectName);
				}
				tb.append('_');
				tb.append(relation.subject.key);
				tb.append(", ");
				tb.append("Cardinality.newInstance(\"");
				tb.append(relation.cardinality.toString());
				tb.append("\")");
				tb.append(", ");
				tb.append(relation.collection.name());
				tb.append(')');
				tb.append(end);
				tb.newline();
			}
			tb.indentLess();
			tb.indentLess();
			tb.newline();
		}

		// setRules(key, clazz, ...);
		tb.indent();
		tb.append("setRules(");
		tb.append(StringU.stringify(names));
		tb.append(");");
		tb.newline();

		tb.indentLess(); // End try
		tb.writeline("} catch(final Exception e) {");
		tb.indentMore();
		tb.indent();
		tb.append("throw new RuntimeException(\"Error building ");
		tb.append(className);
		tb.append("\", e);");
		tb.newline();

		tb.indentLess(); // End catch
		tb.writeline("}");

		tb.indentLess(); // End static
		tb.writeline("}");

		tb.indentLess();
		tb.writeline("}"); // End class

		return tb.toString();
	}

	public String getVisitorText(String packageName) {

		final TextBuilder tb = new TextBuilder();

		sign(tb);

		// srcgen/com/inexas/willow/WillowVisitor.java
		final String interfaceName = name + "Visitor";
		tb.append("package ");
		tb.append(packageName);
		tb.append(';');
		tb.newline();

		tb.append("public interface ");
		tb.append(interfaceName);
		tb.append(" {");
		tb.newline();
		tb.newline();
		tb.indentMore();

		// Base class...

		tb.indent();
		tb.append("public static class Base implements ");
		tb.append(interfaceName);
		tb.append(" {");
		tb.newline();
		tb.indentMore();

		for(final ObjectRule object : rules) {
			final String parameterList = getParameterList(object);
			if(object.hasChildren()) {
				// @Override public void enter(Xxx xxx) { /**/ }
				tb.indent();
				tb.append("@Override public void enter(");
				tb.append(parameterList);
				tb.append(") { /**/ }");
				tb.newline();

				// @Override public void exit(Xxx xxx) { /**/ }
				tb.indent();
				tb.append("@Override public void exit(");
				tb.append(parameterList);
				tb.append(") { /**/ }");
				tb.newline();
			} else {
				// @Override public void exit(Xxx xxx) { /**/ }
				tb.indent();
				tb.append("@Override public void visit(");
				tb.append(parameterList);
				tb.append(") { /**/ }");
				tb.newline();
			}
		}

		tb.indentLess();
		tb.writeline("}");
		tb.newline();

		// Interface body...

		for(final ObjectRule object : rules) {
			final String parameterList = getParameterList(object);
			if(object.hasChildren()) {
				// void enter(Application application);
				tb.indent();
				tb.append("void enter(");
				tb.append(parameterList);
				tb.append(");");
				tb.newline();

				// void exit(Application application);
				tb.indent();
				tb.append("void exit(");
				tb.append(parameterList);
				tb.append(");");
				tb.newline();
			} else {
				// void visit(Application application);
				tb.indent();
				tb.append("void visit(");
				tb.append(parameterList);
				tb.append(");");
				tb.newline();
			}
		}

		tb.indentLess();
		tb.writeline("}"); // End interface

		return tb.toString();
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
		writeFile(
				destinationPath,
				packageName,
				"Dialect",
				getDialectText(packageName));
		writeFile(
				destinationPath,
				packageName,
				"Visitor",
				getVisitorText(packageName));
	}

	private void writeFile(String sourceDirectory, String packageName, String suffix, String text) {

		final String className = name + suffix;
		final String fileDirectory = sourceDirectory + '/' + packageName.replace('.', '/') + '/';
		final String path = fileDirectory + className + ".java";

		final File sourceFile = new File(sourceDirectory);
		if(!sourceFile.isDirectory()) {
			throw new RuntimeException("Source directory does not exist: " + sourceDirectory);
		}
		final File file = new File(fileDirectory);
		if(file.isDirectory()) {
			// OK
		} else if(file.exists()) {
			throw new RuntimeException("Cannot create: " + fileDirectory);
		} else {
			if(!file.mkdirs()) {
				throw new RuntimeException("Cannot create: " + fileDirectory);
			}
		}
		try(final FileWriter writer = new FileWriter(path, false)) {
			writer.write(text);
		} catch(final IOException e) {
			throw new RuntimeException("Error writing: " + sourceDirectory, e);
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

	private void sign(TextBuilder tb) {
		tb.append("// Created by Oak: ");
		tb.append(DateU.formatDateTimeAirline(now));
		tb.newline();
	}
}
