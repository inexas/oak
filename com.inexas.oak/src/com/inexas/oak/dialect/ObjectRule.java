package com.inexas.oak.dialect;

import java.lang.reflect.*;
import java.util.*;
import com.inexas.exception.*;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.*;
import com.inexas.tad.Context;
import com.inexas.util.*;

public class ObjectRule extends Rule {
	private final Class<?> templateClass;
	private final boolean isRoot;
	private Relation[] relations;
	private int relationCount;
	private String[] childNames;
	private Method constructorMethod;
	private Constructor<?> constructor;
	private Boolean hasChildren;

	public ObjectRule(
			String name,
			Class<?> templateClass,
			boolean isRoot,
			Constraint... constraints) throws Exception {
		super(name, constraints);

		assert Character.isUpperCase(name.charAt(0)) : "Not an object name: " + name;
		assert templateClass != null;

		this.templateClass = templateClass;
		this.isRoot = isRoot;
	}

	public Object construct(Object[] parameters) throws OakException, OakRuntimeException {
		final Object result;

		try {
			if(constructor == null) {
				result = constructorMethod.invoke(null, parameters);
			} else {
				result = constructor.newInstance(parameters);
			}
		} catch(final InvocationTargetException e) {
			final Throwable cause = e.getCause();
			if(cause instanceof OakException) {
				throw (OakException)cause;
			}
			throw new OakRuntimeException("Error constructing " + name + ": " + e.getMessage(), e);
		} catch(final Exception e) {
			throw new RuntimeException("Error constructing " + name + ": " + e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Return for a Relation given its name.
	 *
	 * @param relationName
	 *            The name of the Relation.
	 * @return The Relation or null if not found.
	 */
	public Relation getRelation(String relationName) {
		Relation result = null;
		for(int i = 0; i < relationCount; i++) {
			if(childNames[i].equals(relationName)) {
				result = relations[i];
				break;
			}
		}
		return result;
	}

	public Relation[] getRelations() {
		return relations;
	}

	public Class<?> getTemplateClass() {
		return templateClass;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public int getRelationCount() {
		return relationCount;
	}

	public String[] getChildNames() {
		return childNames;
	}

	/**
	 * @return True if Object has child Objects
	 */
	public boolean hasChildren() {
		if(hasChildren == null) {
			hasChildren = Boolean.FALSE;
			for(final Relation relation : relations) {
				if(relation.subjectIsObject) {
					hasChildren = Boolean.TRUE;
					break;
				}
			}
		}
		return hasChildren.booleanValue();
	}

	public void setRelations(Relation... relations) throws Exception {
		assert relations != null;
		relationCount = relations.length;

		if(relationCount == 0) {
			error(this, "Object has no valid children");
		} else {
			this.relations = relations;

			// Figure out which constructor/factory method we need...

			// Get the parameter details...
			final Class<?>[] parameterTypes = new Class<?>[relationCount];
			childNames = new String[relationCount];
			for(int i = 0; i < relationCount; i++) {
				final Relation relation = relations[i];

				// Parameter name...
				final String parameterName = relation.subjectName;
				childNames[i] = parameterName;

				// Parameter type...
				final Class<?> parameterType;
				if(relation.subjectIsObject) {
					switch(relation.collection) {
					case list:
						parameterType = List.class;
						break;

					case map:
						parameterType = Map.class;
						break;

					case set:
						parameterType = Set.class;
						break;

					case singleton:
						parameterType = ((ObjectRule)relation.subject).templateClass;
						break;

					default:
						throw new UnexpectedException("Missing case: " + relation.collection.name());
					}
				} else { // It a Property
					switch(relation.collection) {
					case singleton:
						final PropertyRule subject = (PropertyRule)relation.subject;
						final DataType dataType = subject.dataType;
						switch(dataType) {
						case bool:
							parameterType = Boolean.class;
							break;

						case cardinality:
							parameterType = Cardinality.class;
							break;

						case date:
						case datetime:
						case time:
							parameterType = Date.class;
							break;

						case identifier:
						case path:
						case text:
							parameterType = String.class;
							break;

						case integer:
							parameterType = Long.class;
							break;

						case ANY:
							parameterType = Object.class;
							break;

						case precision:
						case decimal:
							// !todo Implement me
							throw new ImplementMeException();

							// $CASES-OMITTED$
						default:
							throw new UnexpectedException("Missing case: " + dataType.name());
						}
						break;

					case list:
						parameterType = List.class;
						break;

					case map:
						parameterType = Map.class;
						break;

					case set:
						parameterType = Set.class;
						break;

					default:
						throw new UnexpectedException("Missing case: " + relation.collection.name());
					}
				}
				parameterTypes[i] = parameterType;
			}

			// Look for static constructor method first...
			try {
				final String methodName = "new" + templateClass.getSimpleName();
				constructorMethod = templateClass.getDeclaredMethod(methodName, parameterTypes);
				constructorMethod.setAccessible(true);
			} catch(final NoSuchMethodException e) {
				// No static method, look for normal constructor...
				try {
					constructor = templateClass.getConstructor(parameterTypes);
					constructor.setAccessible(true);
				} catch(final NoSuchMethodException e1) {
					throw new Exception("Missing constructor or factory method. Implement...\n\t"
							+ getConstructorName(templateClass, parameterTypes)
							+ "\n...or...\n\t"
							+ getStaticConstructorName(templateClass, parameterTypes)
							+ "\n...in " + templateClass.getName());
				} catch(final SecurityException e1) {
					throw new Exception(
							"Error accessing " + getConstructorName(templateClass, parameterTypes)
							+ " in " + templateClass.getName(),
							e);
				}
			} catch(final SecurityException e) {
				throw new Exception(
						"Error accessing "
								+ getStaticConstructorName(templateClass, parameterTypes)
								+ " in " + templateClass.getName(),
								e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Rule<" + name
				+ ", " + (templateClass == null ? "no-class" : templateClass.getSimpleName())
				+ ", " + (isRoot ? "root" : "non-root")
				+ '>';
	}

	private String getConstructorName(Class<?> constructorClass, Class<?>[] parameterTypes) {
		final TextBuilder result = new TextBuilder();
		result.append("public ");
		result.append(constructorClass.getSimpleName());
		addParameters(parameterTypes, result);
		result.append(" throws OakConstructorException");
		return result.toString();
	}

	private String getStaticConstructorName(Class<?> constructorClass, Class<?>[] parameterTypes) {
		// static new
		final TextBuilder result = new TextBuilder();
		result.append("public static ");
		final String typeName = constructorClass.getSimpleName();
		result.append(typeName);
		result.append(" new");
		result.append(typeName);
		addParameters(parameterTypes, result);
		return result.toString();
	}

	private void addParameters(Class<?>[] parameterTypes, TextBuilder result) {
		result.append('(');

		for(int i = 0; i < relationCount; i++) {
			result.delimit();

			final Class<?> parameterType = parameterTypes[i];
			final String typeName = parameterType.getSimpleName();
			final Relation relation = relations[i];
			final String parameterName = childNames[i];
			if(relation.collection != CollectionType.singleton) {
				final String dataType;
				if(relation.subjectIsObject) {
					dataType = ((ObjectRule)relation.subject).templateClass.getSimpleName();
				} else {
					dataType = ((PropertyRule)relation.subject).dataType.javaClass.getSimpleName();
				}
				if(typeName.equals("Map")) {
					result.append("Map<String, ");
					result.append(dataType);
					result.append("> ");
				} else {
					result.append(typeName);
					result.append('<');
					result.append(dataType);
					result.append("> ");
				}
				firstCharToLower(parameterName, result);
				result.append(typeName);
			} else {
				result.append(typeName);
				result.append(' ');
				firstCharToLower(parameterName, result);
			}
		}
		result.append(')');
	}

	/**
	 * @param string
	 *            ForExample.
	 * @param result
	 *            As the name says.
	 * @return forExample.
	 */
	private void firstCharToLower(String string, TextBuilder result) {
		final char c = string.charAt(0);
		if(c <= 'Z') {
			result.append((char)(c + ('a' - 'A')));
			result.append(string.substring(1));
		} else {
			result.append(string);
		}
	}

	private void error(Object locus, String message) {
		final Advisory advisory = Context.get(Advisory.class);
		advisory.error(locus, message);
	}
}