package com.inexas.oak;

import java.lang.reflect.*;
import java.util.*;
import com.inexas.exception.*;
import com.inexas.oak.advisory.*;
import com.inexas.util.*;

/**
 * Visit an Oak 'Node structure' using list of rules to transform it into a
 * hierarchical 'Constructor model' where the nodes map to constructors for
 * Template Objects. For example the transformation might generate a workflow
 * constructor model of Process, Activity and Artifact constructors. This model
 * can be used to parse source files that define workflows.
 *
 * todo Add a verifier class, check for at least one Property/Object
 */
public class Transformer extends OakVisitor.Base {
	private class State {
		/** The type of object we are current parsing */
		final String context;

		/** This maintains the stack in a linked list */
		final State previousInChain;

		/** The Rule for this context */
		final Relation relation;

		/** Set true if we have encountered an error while parsing this block */
		boolean seenError;

		/**
		 * The contents of this state: a Map<key, value> where value is either a
		 * node or a list, map or set of nodes depending on the rule for that
		 * key.
		 */
		final Map<String, Object> contents = new HashMap<>();

		State(PairNode node) {

			context = node.name;
			if(state == null) {
				// Root Object
				relation = rootMap.get(context);
				if(relation == null) {
					advisory.reportError(node, "No such root object: " + context
							+ ", expected: " + StringU.stringify(rootMap.values()));
				}
			} else {
				if(state.relation == null) {
					relation = getBestGuess(context);
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

		Relation getChildRelation(Node node, String name) {
			final Relation result;

			if(relation.subjectIsObject) {
				final ObjectRule object = (ObjectRule)relation.subject;
				result = object.getRelation(name);
			} else {
				result = null;
			}
			if(result == null) {
				advisory.reportError
						(node, "'" + name + "' is not a valid child, expecting: " + getExpected());
			}

			return result;
		}

		void add(Node node, Relation childRelation, Object child) {

			if(!seenError) {
				final String name = childRelation.subjectName;
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
					final String key = ((Keyed)child).getKey();
					if(map.put(key, child) != null) {
						advisory.reportError(node, "Duplicate value for key: " + key);
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
						advisory.reportError(node, "Duplicate value");
					}
					break;

				case singleton:
					if(contents.containsKey(name)) {
						advisory.reportError(node, "Redefinition of value for: " + name);
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
					} catch(final OakRuntimeException e) {
						advisory.reportError(
								node, "Error constructing " + objectRule.name + ": " + e.getMessage());
						result = null;
					}
				}
			}

			return result;
		}

		@SuppressWarnings("unused")
		Object getPropertyValue(ValuePairNode node) {
			final Object result;

			final String name = node.name;
			// !todo Need to check this
			final PropertyRule propertyRule = (PropertyRule)relation.subject;
			if(propertyRule == null) {
				advisory.reportError(node, "Invalid property: " + name + " in " + context);
				result = null;
			} else if(!propertyRule.isCompatibleDataType(node.getType())) {
				advisory.reportError(
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

				case integer:
					result = node.asInteger();
					break;

				case bool:
					result = node.asBoolean();
					break;

				case ANY:
					result = node.asAny();
					break;

				case date:
				case time:
				case datetime:
				case decimal:
				case precision:
					// $CASES-OMITTED$
				default:
					// !todo Implement me
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
				for(final Relation child : ((ObjectRule)relation.subject).getRelations()) {
					final String name = child.subjectName;
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
							try {
								final PropertyRule property = (PropertyRule)relation.subject;
								property.validate(map);
							} catch(final OakException e) {
								advisory.reportError(node, e.getMessage());
							}
						}
					} else if(object instanceof Collection) {
						// Either a List or a Set
						@SuppressWarnings("unchecked")
						final Collection<Object> collection = (Collection<Object>)object;
						objectCount = collection.size();

						if(!relation.subjectIsObject) {
							try {
								final PropertyRule property = (PropertyRule)relation.subject;
								property.validate(collection);
							} catch(final OakException e) {
								advisory.reportError(node, e.getMessage());
							}
						}
					} else {
						objectCount = 1;

						if(!child.subjectIsObject) {
							try {
								final PropertyRule property = (PropertyRule)child.subject;
								property.validate(object);
							} catch(final OakException e) {
								advisory.reportError(node, e.getMessage());
							}
						}
					}
					final Cardinality cardinality = child.cardinality;
					if(!cardinality.isValidCardinality(objectCount)) {
						advisory.reportError(
								node,
								"Need " + cardinality + " " + name + "(s)" + " in " + context);
					}
				}

				// Check for unprocessed parts...
				for(final String childName : unprocessedChildren.keySet()) {
					advisory.reportError(node, "Invalid member: " + childName);
				}
			}

			return !seenError;
		}

		private String getExpected() {
			final String[] childNames = ((ObjectRule)relation.subject).getChildNames();
			return childNames.length == 0 ? "no child elements" : StringU.stringify(childNames);
		}

		/**
		 * If we fail to find a Relation, for example the name is spelled wrong
		 * in the input file, then do the best we can to find a matching on so
		 * that we can carry on generating useful error messages
		 *
		 * @param relationName
		 * @return The best guess relationName
		 */
		private Relation getBestGuess(String relationName) {
			// todo Implement something that makes sense
			return null;
		}

	}

	private State state;
	private final Advisory advisory;
	private final Map<String, Relation> rootMap = new HashMap<>();
	private Object root;

	private final List<Pair<Locus, Object>> templates = new ArrayList<>();
	private final String[] visitors;

	public Transformer(Rule[] rules, String[] visitors, Advisory advisory) {
		for(final Rule rule : rules) {
			if(rule instanceof ObjectRule) {
				final ObjectRule objectRule = (ObjectRule)rule;
				if(objectRule.isRoot()) {
					final Relation relation = new Relation(objectRule);
					if(rootMap.put(objectRule.name, relation) != null) {
						throw new InexasRuntimeException("Two root objects with same name");
					}
				}
			}
		}
		this.visitors = visitors;
		this.advisory = advisory;
	}

	public Object getRoot() {
		return root;
	}

	@Override
	public void exit(Oak oak) {
		if(!advisory.hasErrors() && visitors != null) {
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
					throw new InexasRuntimeException("Checker error", e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ObjectArrayPairNode node) {
		push(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ObjectArrayPairNode node) {
		if(state.seenError) {
			pop();
		} else {
			final Relation relation = state.relation;
			final Object object = state.contents.get(relation.subjectName);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ObjectNode node) {
		push(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ObjectNode node) {
		popObject(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ObjectPairNode node) {
		push(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ObjectPairNode node) {
		popObject(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ValuePairNode node) {
		push(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ValuePairNode node) {
		if(state.seenError) {
			pop();
		} else {
			final Relation relation = state.relation;
			final Object value = state.contents.get(relation.subjectName);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void enter(ValueArrayPairNode node) {
		push(node);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit(ValueArrayPairNode node) {
		if(state.seenError) {
			pop();
		} else {
			final Relation relation = state.relation;
			@SuppressWarnings("unchecked")
			final List<Object> values = (List<Object>)state.contents.get(relation.subjectName);
			pop();
			for(final Object value : values) {
				state.add(node, relation, value);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(PathNode node) {
		if(!state.seenError) {
			final PropertyRule rule = (PropertyRule)state.relation.subject;
			if(rule != null) {
				final Object value;
				switch(rule.dataType) {
				case identifier:
					// todo Check no switches: /asdf/
				case path:
				case ANY: // For cases line Constraint/value
					value = node.path;
					break;

				// $CASES-OMITTED$
				default:
					// !todo React to bad types
					throw new ImplementMeException(rule.dataType.name());
				}
				state.add(node, state.relation, value);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(ConstantNode node) {
		if(state.relation != null) {
			final PropertyRule rule = (PropertyRule)state.relation.subject;
			final Object value;
			switch(rule.dataType) {
			case identifier:
			case path:
			case text:
				value = node.getString();
				break;

			case integer:
				value = node.getInteger();
				break;

			case bool:
				value = node.getBoolean();
				break;

			case ANY:
				value = node.getValue();
				break;

				// $CASES-OMITTED$
			default:
				// !todo React to bad types
				// !todo Implement default and other values
				throw new ImplementMeException();
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
			switch(rule.dataType) {
			case cardinality:
				value = node.cardinality;
				break;

				// $CASES-OMITTED$
			default:
				// !todo React to bad types
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
			final Relation relation = state.relation;
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

}
