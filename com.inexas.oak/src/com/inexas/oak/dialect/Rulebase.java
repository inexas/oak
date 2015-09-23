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
import com.inexas.oak.Identifier;
import com.inexas.oak.template.Constraint;
import com.inexas.util.*;

public class Rulebase {
	public final Identifier name;
	public final ObjectRule[] rules;
	private final Date now = new Date();

	public Rulebase(Identifier name, ObjectRule[] rules) {
		this.name = name;
		this.rules = rules;
		Arrays.sort(rules);
	}

	// todo Move this out of here
	/**
	 * Convert the Dialog AST to a file. {@inheritDoc}
	 */
	public String getDialectText(String packageName) {
		final Text t = new Text();

		final String className = name + "Dialect";

		// e.g. // Created by Oak: 14AUG15 09:10:30
		sign(t);

		// File header...

		t.append("package ");
		t.append(packageName);
		t.append(';');
		t.newline();
		t.newline();

		t.writeline("import static com.inexas.oak.dialect.CollectionType.*;");
		t.writeline("import com.inexas.oak.DataType;");
		t.writeline("import com.inexas.oak.*;");
		t.writeline("import com.inexas.oak.dialect.*;");
		t.writeline("import com.inexas.util.Cardinality;");
		t.newline();

		/*
		 * Instead of using a
		 * 
		 * t.writeline("@SuppressWarnings(\"unused\")");
		 * 
		 * in Eclipse, right click on the directory containing the generated
		 * files Properties > Java Compiler > Ignore optional compile problems
		 */
		t.append("public class ");
		t.append(className);
		t.append(" {");
		t.newline();
		t.indentMore();

		t.writeline("public static Rulebase rulebase;");
		t.newline();

		t.writeline("private static Relationship relate(Rule key, "
				+ "Cardinality cardinality, "
				+ "CollectionType collection) {");
		t.indentMore();
		t.writeline("return new Relationship(key, cardinality, collection);");
		t.indentLess();
		t.writeline("}");
		t.newline();

		t.writeline("private static void setRules(ObjectRule... rules) {");
		t.indentMore();
		t.indent();
		t.append(name);
		t.append("Dialect.rulebase = new Rulebase(new Identifier(\"Dialect\"), rules);");
		t.newline();
		t.indentLess();
		t.writeline("}");
		t.newline();

		// The static code block...

		t.writeline("static {");
		t.indentMore();
		t.writeline("try {");
		t.indentMore();

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

			t.indent();
			t.append("final ObjectRule ");
			t.append(_objectName);
			t.append(" = new ObjectRule(");
			t.newline();
			t.indentMore();
			t.indentMore();

			t.indent();
			t.append("new Identifier(\"");
			t.append(object.key);
			t.append("\"),");
			t.newline();

			// Inner classes contain $ which become '.'
			t.indent();
			t.append(object.getTemplateClass().getName().replace('$', '.'));
			t.append(".class,");
			t.newline();

			t.indent();
			t.append(object.isRoot() ? "true);" : "false);");
			t.newline();

			t.indentLess();
			t.indentLess();
			t.newline();
		}

		// Properties...

		for(final Entry<String, PropertyRule> entry : properties.entrySet()) {
			final String _name = entry.getKey();
			final PropertyRule property = entry.getValue();

			t.indent();
			t.append("final PropertyRule ");
			t.append(_name);
			t.append(" = new PropertyRule(");
			t.newline();

			t.indentMore();
			t.indentMore();
			t.indent();
			t.append("new Identifier(\"");
			t.append(property.key);
			t.append("\"),");
			t.newline();

			t.indent();
			t.append("DataType.");
			t.append(property.dataType.name());

			final Constraint[] constraints = property.constraints;
			if(constraints == null) {
				t.append(");");
			} else {
				t.append(",");
				t.newline();

				// new ChoiceConstraint("map", "list", ...));
				final int count = constraints.length;
				for(int i = 0; i < count; i++) {
					final Constraint constraint = constraints[i];
					t.indent();
					t.append("new ");
					t.append(constraint.getClass().getSimpleName());
					t.append('(');
					final Object[] values = constraint.values;
					t.restartDelimiting();
					for(final Object value : values) {
						t.delimit();
						t.append('"');
						t.append(StringU.toJavaString(value.toString()));
						t.append('"');
					}
					t.append(i == count - 1 ? "));" : "),");
				}
			}
			t.newline();

			t.indentLess();
			t.indentLess();
			t.newline();
		}

		// Relationships...

		for(final ObjectRule object : rules) {
			final String objectName = "_" + object.key;
			t.indent();
			t.append(objectName);
			t.append(".setRelationships(");
			t.newline();

			t.indentMore();
			t.indentMore();

			final Relationship[] children = object.getRelationships();
			final int count = children.length;
			for(int i = 0; i < count; i++) {
				final Relationship relation = children[i];
				final String end = i == count - 1 ? ");" : ",";

				t.indent();
				t.append("relate(");
				if(!relation.subjectIsObject) {
					t.append(objectName);
				}
				t.append('_');
				t.append(relation.subject.key);
				t.append(", ");
				t.append("Cardinality.newInstance(\"");
				t.append(relation.cardinality.text);
				t.append("\")");
				t.append(", ");
				t.append(relation.collection.name());
				t.append(')');
				t.append(end);
				t.newline();
			}
			t.indentLess();
			t.indentLess();
			t.newline();
		}

		// setRules(key, clazz, ...);
		t.indent();
		t.append("setRules(");
		t.append(StringU.stringify(names));
		t.append(");");
		t.newline();

		t.indentLess(); // End try
		t.writeline("} catch(final Exception e) {");
		t.indentMore();
		t.indent();
		t.append("throw new RuntimeException(\"Error building ");
		t.append(className);
		t.append("\", e);");
		t.newline();

		t.indentLess(); // End catch
		t.writeline("}");

		t.indentLess(); // End static
		t.writeline("}");

		t.indentLess();
		t.writeline("}"); // End class

		return t.toString();
	}

	public String getVisitorText(String packageName) {

		final Text t = new Text();

		sign(t);

		// srcgen/com/inexas/willow/WillowVisitor.java
		final String interfaceName = name + "Visitor";
		t.append("package ");
		t.append(packageName);
		t.append(';');
		t.newline();

		t.append("public interface ");
		t.append(interfaceName);
		t.append(" {");
		t.newline();
		t.newline();
		t.indentMore();

		// Base class...

		t.indent();
		t.append("public static class Base implements ");
		t.append(interfaceName);
		t.append(" {");
		t.newline();
		t.indentMore();

		for(final ObjectRule object : rules) {
			final String parameterList = getParameterList(object);
			if(object.hasChildren()) {
				// @Override public void enter(Xxx xxx) { /**/ }
				t.indent();
				t.append("@Override public void enter(");
				t.append(parameterList);
				t.append(") { /**/ }");
				t.newline();

				// @Override public void exit(Xxx xxx) { /**/ }
				t.indent();
				t.append("@Override public void exit(");
				t.append(parameterList);
				t.append(") { /**/ }");
				t.newline();
			} else {
				// @Override public void exit(Xxx xxx) { /**/ }
				t.indent();
				t.append("@Override public void visit(");
				t.append(parameterList);
				t.append(") { /**/ }");
				t.newline();
			}
		}

		t.indentLess();
		t.writeline("}");
		t.newline();

		// Interface body...

		for(final ObjectRule object : rules) {
			final String parameterList = getParameterList(object);
			if(object.hasChildren()) {
				// void enter(Application application);
				t.indent();
				t.append("void enter(");
				t.append(parameterList);
				t.append(");");
				t.newline();

				// void exit(Application application);
				t.indent();
				t.append("void exit(");
				t.append(parameterList);
				t.append(");");
				t.newline();
			} else {
				// void visit(Application application);
				t.indent();
				t.append("void visit(");
				t.append(parameterList);
				t.append(");");
				t.newline();
			}
		}

		t.indentLess();
		t.writeline("}"); // End interface

		return t.toString();
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
		final Text result = new Text();
		final char c = className.charAt(0);
		if(c <= 'Z') {
			result.append((char)(c + ('a' - 'A')));
			result.append(className.substring(1));
		} else {
			result.append(className);
		}
		return result.toString();
	}

	private void sign(Text t) {
		t.append("// Created by Oak: ");
		t.append(DateU.formatDatetimeAirline(now));
		t.newline();
	}
}
