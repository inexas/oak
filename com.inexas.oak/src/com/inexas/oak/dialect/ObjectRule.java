package com.inexas.oak.dialect;

import java.lang.reflect.*;
import java.math.*;
import java.util.*;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.*;
import com.inexas.oak.advisory.*;
import com.inexas.tad.Context;
import com.inexas.util.*;

/**
 * ObjectRule defines an Object within the Dialect of which it is a member
 */
public class ObjectRule extends Rule {
	private final Class<?> templateClass;
	private final boolean isRoot;
	private Relationship[] relationships;
	private int relationshipCount;
	private Identifier[] childNames;
	private Method constructorMethod;
	private Constructor<?> constructor;
	private Boolean hasChildren;

	public ObjectRule(Identifier name, Class<?> templateClass, boolean isRoot) {
		super(name);

		assert templateClass != null;

		if(!Character.isUpperCase(name.charAt(0))) {
			final Advisory advisory = Context.get(Advisory.class);
			advisory.error(this, "Object names must start with an upper case letter: + '" + name + '\'');
		}

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
			throw new OakRuntimeException("Error constructing " + key + ": " + e.getMessage(), e);
		} catch(final Exception e) {
			throw new RuntimeException("Error constructing " + key + ": " + e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Return for a Relationship given its name.
	 *
	 * @param relationshipName
	 *            The name of the Relationship.
	 * @return The Relationship or null if not found.
	 */
	public Relationship getRelationship(Identifier relationshipName) {
		Relationship result = null;
		for(int i = 0; i < relationshipCount; i++) {
			if(childNames[i].equals(relationshipName)) {
				result = relationships[i];
				break;
			}
		}
		return result;
	}

	public Relationship[] getRelationships() {
		return relationships;
	}

	public Class<?> getTemplateClass() {
		return templateClass;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public int getRelationshipCount() {
		return relationshipCount;
	}

	public Identifier[] getChildNames() {
		return childNames;
	}

	/**
	 * @return True if Object has child Objects
	 */
	public boolean hasChildren() {
		if(hasChildren == null) {
			hasChildren = Boolean.FALSE;
			for(final Relationship relationship : relationships) {
				if(relationship.subjectIsObject) {
					hasChildren = Boolean.TRUE;
					break;
				}
			}
		}
		return hasChildren.booleanValue();
	}

	public void setRelationships(Relationship... relationships) {
		assert relationships != null;
		relationshipCount = relationships.length;

		if(relationshipCount == 0) {
			error(this, "Object has no valid children");
		} else {
			this.relationships = relationships;

			// Figure out which constructor/factory method we need...

			// Get the parameter details...
			final Class<?>[] parameterTypes = new Class<?>[relationshipCount];
			childNames = new Identifier[relationshipCount];
			for(int i = 0; i < relationshipCount; i++) {
				final Relationship relationship = relationships[i];

				// Parameter name...
				final Identifier parameterName = relationship.subjectKey;
				childNames[i] = parameterName;

				// Parameter type...
				final Class<?> parameterType;
				if(relationship.subjectIsObject) {
					switch(relationship.collection) {
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
						parameterType = ((ObjectRule)relationship.subject).templateClass;
						break;

					default:
						throw new UnexpectedException("Missing case: " + relationship.collection.name());
					}
				} else { // It a Property
					switch(relationship.collection) {
					case singleton:
						final PropertyRule subject = (PropertyRule)relationship.subject;
						final DataType dataType = subject.dataType;
						switch(dataType) {
						case identifier:
							parameterType = Identifier.class;
							break;
						case path:
							parameterType = Path.class;
							break;
						case text:
							parameterType = String.class;
							break;
						case z:
							parameterType = Long.class;
							break;
						case Z:
							parameterType = BigInteger.class;
							break;
						case f:
							parameterType = Float.class;
							break;
						case F:
							parameterType = BigDecimal.class;
							break;
						case date:
						case datetime:
						case time:
							parameterType = Date.class;
							break;
						case bool:
							parameterType = Boolean.class;
							break;
						case cardinality:
							parameterType = Cardinality.class;
							break;
						case any:
							parameterType = Object.class;
							break;
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
						throw new UnexpectedException("Missing case: " + relationship.collection.name());
					}
				}
				parameterTypes[i] = parameterType;
			}

			/*
			 * Check that there is a ctor that we can use, either a normal ctor
			 * or a static method so that a factory method can be used.
			 */

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
					error(this, "Missing constructor or factory method. Implement...\n\t"
							+ getConstructorName(templateClass, parameterTypes)
							+ "\n...or...\n\t"
							+ getStaticConstructorName(templateClass, parameterTypes)
							+ "\n...in " + templateClass.getName());
				} catch(final SecurityException e1) {
					error(this,
							"Error accessing " + getConstructorName(templateClass, parameterTypes)
									+ " in " + templateClass.getName() + ' ' + e.getMessage());
				}
			} catch(final SecurityException e) {
				error(this,
						"Error accessing " + getStaticConstructorName(templateClass, parameterTypes)
						+ " in " + templateClass.getName() + ' ' + e.getMessage());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Rule<key:" + key
				+ ", " + (templateClass == null ? "no-template" : (templateClass.getSimpleName() + ".java"))
				+ ", " + (isRoot ? "root" : "non-root")
				+ '>';
	}

	private String getConstructorName(Class<?> constructorClass, Class<?>[] parameterTypes) {
		final Text result = new Text();
		result.append("public ");
		result.append(constructorClass.getSimpleName());
		addParameters(parameterTypes, result);
		result.append(" throws OakConstructorException");
		return result.toString();
	}

	private String getStaticConstructorName(Class<?> constructorClass, Class<?>[] parameterTypes) {
		// static new
		final Text result = new Text();
		result.append("public static ");
		final String typeName = constructorClass.getSimpleName();
		result.append(typeName);
		result.append(" new");
		result.append(typeName);
		addParameters(parameterTypes, result);
		return result.toString();
	}

	private void addParameters(Class<?>[] parameterTypes, Text result) {
		result.append('(');

		for(int i = 0; i < relationshipCount; i++) {
			result.delimit();

			final Class<?> parameterType = parameterTypes[i];
			final String typeName = parameterType.getSimpleName();
			final Relationship relationship = relationships[i];
			final Identifier parameterName = childNames[i];
			if(relationship.collection != CollectionType.singleton) {
				final String dataType;
				if(relationship.subjectIsObject) {
					dataType = ((ObjectRule)relationship.subject).templateClass.getSimpleName();
				} else {
					dataType = ((PropertyRule)relationship.subject).dataType.javaClass.getSimpleName();
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
	 * @param identifier
	 *            ForExample.
	 * @param result
	 *            As the name says.
	 * @return forExample.
	 */
	private void firstCharToLower(Identifier identifier, Text result) {
		final char c = identifier.charAt(0);
		if(c <= 'Z') {
			result.append((char)(c + ('a' - 'A')));
			result.append(identifier.toString().substring(1));
		} else {
			result.append(identifier);
		}
	}

	private void error(Object locus, String message) {
		final Advisory advisory = Context.get(Advisory.class);
		advisory.error(locus, message);
	}
}