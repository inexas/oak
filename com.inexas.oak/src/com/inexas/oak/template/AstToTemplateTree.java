package com.inexas.oak.template;

import java.lang.reflect.*;
import java.util.*;
import com.inexas.exception.*;
import com.inexas.oak.*;
import com.inexas.oak.advisory.*;
import com.inexas.oak.ast.*;
import com.inexas.oak.dialect.*;
import com.inexas.tad.Context;
import com.inexas.util.*;

/**
 * Visit an AST using list of rules to transform it into a template tree. For
 * example the transformation might generate a workflow model.
 *
 * Strategy. We're sent around an Oak AST. We process the input depth first and
 * as we go down a level by entering an object, we push the existing state onto
 * a stack and create a new one. Then, every object and property we encounter
 * are added to the state as we exit the state we use the state we've created to
 * create a new Object.
 *
 * todo Add a verifier class, check for at least one Property/Object
 */
public class AstToTemplateTree extends AstVisitor.Base {
	private class State {
		/** The type of object we are current parsing */
		final String context;

		/** This maintains the stack in a linked list */
		final State previousInChain;

		/** The Rule for this state */
		final Relationship relation;

		/** Set true if we have encountered an error while parsing this block */
		boolean seenError;

		/**
		 * The contents of this state: a Map<key, value> where value is either a
		 * node or a list, map or set of nodes depending on the rule for that
		 * key.
		 */
		final Map<String, Object> contents = new LinkedHashMap<>();

		State(PairNode node) {

			context = node.getName();
			if(state == null) {
				// Root Object
				relation = rootMap.get(context);
				if(relation == null) {
					final TextBuilder tb = new TextBuilder();
					for(final Relationship relationship : rootMap.values()) {
						tb.delimit();
						tb.append(relationship.subjectKey);
					}
					error(node, "No such root object: '" + context + "', expected: " + tb.toString());
				}
			} else {
				if(state.relation == null) {
					relation = getBestGuess(node, context);
				} else {
					relation = state.getChildRelation(node, context);
				}
			}

			// Put this State on the linked list...
			previousInChain = state;
			state = this;
			seenError = relation == null;
		}

		State(Node node) {
			assert !(node instanceof PairNode);

			context = state.context;
			relation = state.relation;
			previousInChain = state;
			state = this;
		}

		@Override
		public String toString() {
			return "State:" + context + ' ' + relation + ' ' + contents.keySet()
					+ (seenError ? " ERROR" : "");
		}

		Relationship getChildRelation(Node node, String name) {
			final Relationship result;

			if(relation.subjectIsObject) {
				final ObjectRule object = (ObjectRule)relation.subject;
				result = object.getRelationship(name);
			} else {
				result = null;
			}
			if(result == null) {
				error(
						node,
						"'" + name + "' is not a valid child, expecting: " + getExpected());
			}

			return result;
		}

		void add(Node node, Relationship childRelation, Object child) {

			if(!seenError) {
				final String name = childRelation.subjectKey;
				switch(childRelation.collection) {
				case list:
					@SuppressWarnings("unchecked")
					List<Object> list = (List<Object>)contents.get(name);
					if(list == null) {
						list = new ArrayList<>();
						contents.put(name, list);
					}
					list.add(child);
					break;

				case map:
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>)contents.get(name);
					if(map == null) {
						map = new HashMap<>();
						contents.put(name, map);
					}
					try {
						final String key = ((Keyed)child).getKey();
						if(map.put(key, child) != null) {
							error(node, "Key already defined in this map: " + key);
						}
					} catch(final ClassCastException e) {
						// todo Implement me
						throw new ImplementMeException(child.getClass().getName()
								+ " must implement Keyed if it is to be used in a map");
					}
					break;

				case set:
					@SuppressWarnings("unchecked")
					Set<Object> set = (Set<Object>)contents.get(name);
					if(set == null) {
						set = new HashSet<>();
						contents.put(name, set);
					}
					if(!set.add(child)) {
						error(node, "Duplicate value");
					}
					break;

				case singleton:
					if(contents.containsKey(name)) {
						error(node, "Redefinition of value for: " + name);
					} else {
						contents.put(name, child);
					}
					break;
				}
			}
		}

		Object toObject(Node node) {
			Object result;

			if(relation == null) {
				result = null;
			} else {
				// Retrieve the parameters...
				final ObjectRule objectRule = (ObjectRule)relation.subject;
				final String[] parameterNames = objectRule.getChildNames();
				final int count = parameterNames.length;
				final Object[] parameters = new Object[count];
				for(int i = 0; i < count; i++) {
					parameters[i] = contents.get(parameterNames[i]);
				}

				if(seenError) {
					result = null;
				} else {
					try {
						result = objectRule.construct(parameters);
						templates.add(new Pair<>(node, result));
						advisory.associate(node, result);
					} catch(final OakException e) {
						error(node, "Error constructing " + objectRule.key + ": " + e.getMessage());
						result = null;
					}
				}
			}

			return result;
		}

		@SuppressWarnings("unused")
		Object getPropertyValue(ValuePairNode node) {
			final Object result;

			final String name = node.getName();
			final PropertyRule propertyRule = (PropertyRule)relation.subject;
			if(propertyRule == null) {
				error(node, "Invalid property: " + name + " in " + context);
				result = null;
			} else if(!propertyRule.isCompatibleDataType(node.getType())) {
				error(
						node,
						"Invalid data type for: " + name + ", expected " + propertyRule.dataType.name());
				result = null;
			} else {
				switch(propertyRule.dataType) {
				case cardinality:
					result = node.asCardinality();
					break;

				case identifier:
				case path:
				case text:
					result = node.asString();
					break;

				case z:
					result = node.asInteger();
					break;

				case bool:
					result = node.asBoolean();
					break;

				case any:
					result = node.asAny();
					break;

				case date:
				case time:
				case datetime:
				case f:
				case F:
				case Z:
				default:
					// todo Implement me
					throw new ImplementMeException(propertyRule.dataType.name());
				}
			}

			return result;
		}

		/**
		 * Validate the node. Checks performed.
		 * <ol>
		 * <li>All children are present</li>
		 * <li>No extraneous children</li>
		 * <li>Each child is passes any constraints</li>
		 * </ol>
		 *
		 * @param node
		 * @return
		 */
		boolean valid(Node node) {
			if(!seenError) {
				assert relation.subjectIsObject;

				final Map<String, Object> unprocessedChildren = new HashMap<>(contents);
				for(final Relationship child : ((ObjectRule)relation.subject).getRelationships()) {
					final String name = child.subjectKey;
					final Object object = unprocessedChildren.remove(name);

					// Check the cardinality...
					final int objectCount;
					if(object == null) {
						objectCount = 0;
					} else if(object instanceof Map) {
						@SuppressWarnings("unchecked")
						final Map<String, Object> map = (Map<String, Object>)object;
						objectCount = map.size();

						if(!relation.subjectIsObject) {
							final PropertyRule property = (PropertyRule)relation.subject;
							property.validateMap(map);
						}
					} else if(object instanceof Collection) {
						// Either a List or a Set
						@SuppressWarnings("unchecked")
						final Collection<Object> collection = (Collection<Object>)object;
						objectCount = collection.size();

						if(!relation.subjectIsObject) {
							final PropertyRule property = (PropertyRule)relation.subject;
							property.validateObject(collection);
						}
					} else {
						objectCount = 1;

						if(!child.subjectIsObject) {
							final PropertyRule property = (PropertyRule)child.subject;
							property.validateObject(object);
						}
					}
					final Cardinality cardinality = child.cardinality;
					if(!cardinality.isValidCardinality(objectCount)) {
						error(node, "Need " + cardinality + " " + name + "(s)" + " in " + context);
					}
				}

				// Check for unprocessed parts...
				for(final String childName : unprocessedChildren.keySet()) {
					error(node, "Invalid member: " + childName);
				}
			}

			return !seenError;
		}

		private String getExpected() {
			final String[] childNames = ((ObjectRule)relation.subject).getChildNames();
			return childNames.length == 0 ? "no child elements" : StringU.toDelimitedString(childNames);
		}

		/**
		 * If we fail to find a Relation, for example the name is spelled wrong
		 * in the input file, then do the best we can to find a matching on so
		 * that we can carry on generating useful error messages
		 *
		 * @param relationName
		 * @return The best guess relationName
		 */
		private Relationship getBestGuess(Node node, String name) {
			final Relationship result;

			// todo Implement something that makes sense
			if(relation != null && relation.subjectIsObject) {
				final ObjectRule object = (ObjectRule)relation.subject;
				result = object.getRelationship(name);
			} else {
				result = null;
			}
			if(result == null) {
				error(
						node,
						"'" + name + "' is not a valid child, expecting: " + getExpected());
			}

			return result;
		}

	}

	private State state;
	private final Map<String, Relationship> rootMap = new HashMap<>();
	private Object root;

	private final List<Pair<Locus, Object>> templates = new ArrayList<>();
	private final String[] visitors;
	private final Advisory advisory;

	public AstToTemplateTree(Rule[] rules, String[] visitors) {
		advisory = Context.get(Advisory.class);

		for(final Rule rule : rules) {
			// Find the possible root(s)...
			if(rule instanceof ObjectRule) {
				final ObjectRule objectRule = (ObjectRule)rule;
				if(objectRule.isRoot()) {
					// Got the root,
					final Relationship relation = new Relationship(
							objectRule,
							Cardinality.ONE_ONE,
							CollectionType.singleton);
					if(rootMap.put(objectRule.key, relation) != null) {
						throw new RuntimeException("Two root objects with same name");
					}
				}
			}
		}
		this.visitors = visitors;
	}

	public Object getRoot() {
		return root;
	}

	@Override
	public void exit(Oak oak) {
		if(!advisory.hasErrors() && visitors != null) {
			// Send any visitors around...

			// ?todo If we are going to support visitors properly we need to do
			// a better job here with error handling.
			for(final String checkerClassName : visitors) {
				try {
					final Class<?> checkerClass = Class.forName(checkerClassName);
					final Constructor<?> constructor = checkerClass.getConstructor(
							Advisory.class,
							root.getClass());
					final Object checker = constructor.newInstance(advisory, root);

					for(final Pair<Locus, Object> template : templates) {
						try {
							final Method method = checkerClass.getMethod(
									"check",
									Locus.class,
									template.object2.getClass());
							method.invoke(checker, template.object1, template.object2);
						} catch(final NoSuchMethodException e) {
							// No method, no checks
						}
					}
				} catch(final Exception e) {
					throw new RuntimeException("Checker error", e);
				}
			}
		}
	}

	// E.g. "MyObject [{ key:a; }, { key:b; }, { key:c; }]"...

	@Override
	public void enter(ObjectArrayPairNode node) {
		push(node);
	}

	@Override
	public void exit(ObjectArrayPairNode node) {
		if(state.seenError) {
			pop();
		} else {
			final Relationship relation = state.relation;
			final Object object = state.contents.get(relation.subjectKey);
			pop();
			if(object instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> values = (Map<String, Object>)object;
				for(final Object member : values.values()) {
					state.add(node, relation, member);
				}
			} else {
				final Collection<?> collection = (Collection<?>)object;
				for(final Object member : collection) {
					state.add(node, relation, member);
				}
			}
		}
	}

	@Override
	public void enter(ObjectNode node) {
		push(node);
	}

	@Override
	public void exit(ObjectNode node) {
		popObject(node);
	}

	// E.g. "MyObject { key:a; }"...

	@Override
	public void enter(ObjectPairNode node) {
		push(node);
	}

	@Override
	public void exit(ObjectPairNode node) {
		popObject(node);
	}

	// E.g. "myProperty: 42;"...

	@Override
	public void enter(ValuePairNode node) {
		push(node);
	}

	@Override
	public void exit(ValuePairNode node) {
		if(state.seenError) {
			pop();
		} else {
			final Relationship relation = state.relation;
			final Object value = state.contents.get(relation.subjectKey);
			pop();
			if(value != null) {
				if(value instanceof List) {
					final List<?> values = (List<?>)value;
					for(final Object member : values) {
						state.add(node, relation, member);
					}
				} else {
					state.add(node, relation, value);
				}
			}
		}
	}

	// E.g. "myProperty[ 1, 2, 3 ]"...

	@Override
	public void enter(ValueArrayPairNode node) {
		push(node);
	}

	@Override
	public void exit(ValueArrayPairNode node) {
		if(state.seenError) {
			pop();
		} else {
			final Relationship relation = state.relation;
			@SuppressWarnings("unchecked")
			final List<Object> values = (List<Object>)state.contents.get(relation.subjectKey);
			pop();
			for(final Object value : values) {
				state.add(node, relation, value);
			}
		}
	}

	// E.g. "myProperty: /abc/def;"...

	@Override
	public void visit(PathNode node) {
		if(!state.seenError) {
			final PropertyRule rule = (PropertyRule)state.relation.subject;
			// ?todo Why might rule be null here?
			if(rule != null) {
				final Object value = node.path;
				if(rule.dataType == DataType.path || rule.dataType == DataType.any) {
					state.add(node, state.relation, value);
				} else {
					advisory.error(node,
							"Wrong data type; expected path but got: " + rule.dataType
							+ " '" + (value == null ? "null" : value.toString()));
					state.add(node, state.relation, null);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(IdentifierNode node) {
		if(!state.seenError) {
			final PropertyRule rule = (PropertyRule)state.relation.subject;
			if(rule != null) {
				final Object value = node.identifier;
				if(rule.dataType == DataType.identifier || rule.dataType == DataType.any) {
					state.add(node, state.relation, value);
				} else {
					advisory.error(node,
							"Wrong data type; expected path but got: " + rule.dataType
							+ " '" + (value == null ? "null" : value.toString()));
					state.add(node, state.relation, null);
				}
			}
		}
	}

	// E.g. "myProperty: 42;"...

	@Override
	public void visit(ConstantNode node) {
		if(state.relation != null) {
			final PropertyRule rule = (PropertyRule)state.relation.subject;
			final Object value;
			switch(rule.dataType) {
			case identifier:
				value = node.getIdentifierValue();
				break;
			case path:
				value = node.getPathValue();
				break;
			case text:
				value = node.getTextValue();
				break;
			case z:
				value = node.getInteger();
				break;
			case Z:
				value = node.getBigInteger();
				break;
			case f:
				value = node.getFloat();
				break;
			case F:
				value = node.getBigFloat();
				break;
			case bool:
				value = node.getBooleanValue();
				break;
			case cardinality:
				value = node.getCardinality();
				break;
			case datetime:
				value = node.getDatetime();
				break;
			case date:
				value = node.getDate();
				break;
			case time:
				value = node.getTime();
				break;
			case any:
				value = node.getValue();
				break;
			default:
				throw new UnexpectedException("visit: " + rule.dataType);
			}
			state.add(node, state.relation, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(CardinalityNode node) {
		if(!state.seenError) {
			final PropertyRule rule = (PropertyRule)state.relation.subject;
			final Object value;
			if(rule.dataType == DataType.cardinality) {
				value = node.cardinality;
			} else {
				// todo Implement me
				throw new ImplementMeException();
			}
			state.add(node, state.relation, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final String result;
		if(state == null) {
			result = "Transformer-stateless";
		} else {
			result = "Transformer-" + state.toString();
		}
		return result;
	}

	private void popObject(Node node) {
		if(state.valid(node)) {
			final Object object = state.toObject(node);
			final Relationship relation = state.relation;
			pop();
			if(object != null) {
				if(state == null) {
					root = object;
					// visitorsList = ((Dialect)object).visitorsList;
				} else {
					state.add(node, relation, object);
				}
			}
		} else {
			pop();
			if(state != null) {
				state.seenError = true;
			}
		}
	}

	private void push(PairNode context) {
		state = new State(context);
	}

	private void push(Node context) {
		state = new State(context);
	}

	private void pop() {
		state = state.previousInChain;
	}

	private void error(Object locus, String message) {
		advisory.error(locus, message);
	}

}
