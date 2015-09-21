package com.inexas.oak.ast;

import java.lang.reflect.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.*;
import com.inexas.oak.DataType;
import com.inexas.tad.Tad;
import com.inexas.util.*;

/**
 * This is a storage place for all the built-in functions and extension.
 *
 * Functions are loaded from the class path. They are static methods within a
 * named class.
 */
public class FunctionRegistry implements Tad {
	static class FunctionException extends Exception {
		private static final long serialVersionUID = -7905035932384696885L;

		FunctionException(String message) {
			super(message);
		}
	}

	class InvalidMethodException extends Exception {
		private static final long serialVersionUID = -3033605219136324389L;

		InvalidMethodException(Method method, String message) {
			super("Error loading function: " + method.getName() + ": " + message);
		}
	}

	class Function {
		private final static int requiredModifiers = Modifier.STATIC | Modifier.PUBLIC;
		final Method method;
		final String key;
		final DataType returnType;
		final boolean isStatic;
		final int argumentCount;
		final DataType[] argumentTypes;
		final String signature;
		final String methodName;

		/**
		 * @param method
		 *            Method to load.
		 * @throws InvalidMethodException
		 */
		Function(Method method) throws InvalidMethodException {
			this.method = method;

			// The method must be static...
			if((method.getModifiers() & requiredModifiers) != requiredModifiers) {
				throw new InvalidMethodException(method, "Java method must be public and static");
			}

			// Return type must be valid...
			final Class<?> methodReturnType = method.getReturnType();
			returnType = typeMap.get(methodReturnType);
			if(returnType == null) {
				throw new InvalidMethodException(
						method,
						"Return type not valid: " + methodReturnType.getName());
			}

			// Note the convention used here: parameters are for the method,
			// arguments for the function.

			// Parameters must be valid...
			final Parameter[] parameters = method.getParameters();
			argumentCount = parameters.length;
			argumentTypes = new DataType[argumentCount];

			for(int i = 0; i < argumentCount; i++) {
				final Class<?> parameterType = parameters[i].getType();
				final DataType argumentType = typeMap.get(parameterType);
				if(argumentType == null) {
					throw new InvalidMethodException(
							method,
							"Parameter type not valid: " + parameterType.getName());
				}
				argumentTypes[i] = argumentType;
			}

			// Got this far then good enough

			methodName = method.getName();
			key = toKey(methodName, argumentCount);

			// Static? Check the annotations...
			final com.inexas.oak.ast.Function annotation =
					method.getAnnotationsByType(com.inexas.oak.ast.Function.class)[0];
			isStatic = !annotation.dynamic();

			// Signature...
			signature = toSignature(returnType, methodName, argumentTypes);
		}

		ConstantNode invoke(ParserRuleContext context, Object[] arguments) {
			final ConstantNode result;

			try {
				// Prepare the parameters...
				final Class<?>[] parameterTypes = method.getParameterTypes();
				final Object[] parameters = new Object[argumentCount];
				for(int i = 0; i < argumentCount; i++) {
					final Object argument = arguments[i];
					switch(argumentTypes[i]) {
					case bool:
						parameters[i] = argument;
						break;

					case date:
						parameters[i] = argument;
						break;

					case z:
						final Class<?> clazz = parameterTypes[i];
						if(clazz == BigDecimal.class) {
							parameters[i] = new BigDecimal(((Integer)argument).intValue());
						} else if(clazz == Float.class) {
							parameters[i] = new Float(((Integer)argument).intValue());
						} else {
							parameters[i] = argument;
						}
						break;

					case Z:
					case F:
					case f: {
						parameters[i] = argument;
						break;

					}
					case text:
						parameters[i] = argument == null ? null : argument.toString();
						break;

					case cardinality:
					case identifier:
					case datetime:
					case path:
					case time:
						// todo Implement me
						throw new ImplementMeException();

					case any:
					default:
						throw new UnexpectedException("invoke: ");
					}
				}

				// Invoke the method
				final Object returnValue = method.invoke(null, arguments);

				// Process the return type...
				switch(returnType) {
				case bool:
					result = new ConstantNode(context, (Boolean)returnValue);
					break;

				case datetime: {
					result = new ConstantNode(context, (LocalDateTime)returnValue);
					break;
				}

				case date: {
					result = new ConstantNode(context, (LocalDate)returnValue);
					break;
				}

				case time: {
					result = new ConstantNode(context, (LocalTime)returnValue);
					break;
				}

				case identifier:
				case path:
				case text:
					result = new ConstantNode(context, (String)returnValue);
					break;

				case z: {
					final Integer i = DataType.convert(returnValue, Integer.class);
					result = new ConstantNode(context, i);
					break;
				}

				case Z: {
					final BigInteger bi = DataType.convert(returnValue, BigInteger.class);
					result = new ConstantNode(context, bi);
					break;
				}

				case f: {
					final Float f = DataType.convert(returnValue, Float.class);
					result = new ConstantNode(context, f);
					break;
				}

				case F: {
					final BigDecimal b = DataType.convert(returnValue, BigDecimal.class);
					result = new ConstantNode(context, b);
					break;
				}

				case cardinality: {
					result = new ConstantNode(context, (Cardinality)returnValue);
					break;
				}

				case any:
				default:
					throw new UnexpectedException("invoke: ");
				}

			} catch(final ClassCastException e) {
				throw new RuntimeException("Probable data type mismatch", e);
			} catch(final Exception e) {
				throw new RuntimeException("Error invoking method", e);
			}

			return result;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object rhsObject) {
			return signature.equals(((Function)rhsObject).signature);
		}

		@Override
		public String toString() {
			return signature;
		}
	}

	private final static Map<Class<?>, DataType> typeMap = new HashMap<>();
	static {
		typeMap.put(int.class, DataType.z);
		typeMap.put(Integer.class, DataType.z);

		typeMap.put(long.class, DataType.Z);
		typeMap.put(Long.class, DataType.Z);

		typeMap.put(BigInteger.class, DataType.Z);

		typeMap.put(float.class, DataType.f);
		typeMap.put(Float.class, DataType.f);

		typeMap.put(double.class, DataType.F);
		typeMap.put(Double.class, DataType.F);

		typeMap.put(BigDecimal.class, DataType.F);

		typeMap.put(String.class, DataType.text);
		typeMap.put(boolean.class, DataType.bool);
		typeMap.put(Boolean.class, DataType.bool);
		typeMap.put(LocalDate.class, DataType.date);
		typeMap.put(LocalTime.class, DataType.time);
		typeMap.put(LocalDateTime.class, DataType.datetime);
		typeMap.put(Cardinality.class, DataType.cardinality);
	}

	void register(Class<?>[] funclibs) throws FunctionException, InvalidMethodException {
		for(final Class<?> funclib : funclibs) {
			register(funclib);
		}
	}

	final Map<String, Function[]> map = new HashMap<>();
	private final List<Class<?>> libraries = new ArrayList<>();

	void register(Class<?> funclibClass) throws FunctionException, InvalidMethodException {
		load(funclibClass);
		libraries.add(funclibClass);
	}

	public List<Class<?>> getLibaries() {
		return libraries;
	}

	Function getFunction(String name, ExpressionNode[] parameters)
			throws FunctionException {

		final int parameterCount = parameters.length;
		final DataType[] parameterTypes = new DataType[parameterCount];
		for(int i = 0; i < parameterCount; i++) {
			parameterTypes[i] = parameters[i].getType();
		}

		final String key = toKey(name, parameterCount);
		final Function[] functions = map.get(key);
		if(functions != null) {
			for(final Function function : functions) {
				boolean soFarSoGood = true;
				for(int i = 0; i < parameterCount; i++) {
					if(parameterTypes[i] != function.argumentTypes[i]) {
						soFarSoGood = false;
						break;
					}
				}
				if(soFarSoGood) {
					return function;
				}
			}
		}

		final String signature = toSignature(null, name, parameterTypes);
		throw new FunctionException("Function not found: " + signature);
	}

	private void load(Class<?> funclibClass)
			throws InvalidMethodException, FunctionException {
		boolean foundAtLeastOne = false;
		for(final Method method : funclibClass.getMethods()) {
			// Ignore non-annotated methods...
			if(method.getAnnotationsByType(com.inexas.oak.ast.Function.class).length == 0) {
				continue;
			}

			final Function function = new Function(method);

			// Find where in the register we need to add the function...
			final String key = function.key;
			Function[] array = map.get(key);
			final int index;
			if(array == null) {
				// No function with the same key...
				array = new Function[1];
				index = 0;
			} else {
				// Already function(s) with same key...
				for(final Function existing : array) {
					if(existing.equals(function)) {
						throw new InvalidMethodException(
								method,
								"Function with this signature already exists");
					}
				}

				final Function[] old = array;
				index = old.length;
				array = new Function[index + 1];
				System.arraycopy(old, 0, array, 0, index);
			}

			// Add the function...
			array[index] = function;
			map.put(key, array);

			foundAtLeastOne = true;
		}

		if(!foundAtLeastOne) {
			throw new FunctionException("No functions found in " + funclibClass.getName());
		}
	}

	private String toKey(String methodName, int parameterCount) {
		// methodName:numParams...
		final Text t = new Text();
		t.append(methodName);
		t.append(':');
		t.append(parameterCount);
		return t.toString();
	}

	private String toSignature(DataType returnType, String methodName, DataType[] parameterTypes) {
		// methodName(f64,...)
		final Text t = new Text();
		t.append(methodName);
		t.append('(');
		for(final DataType parameterType : parameterTypes) {
			t.delimit();
			t.append(parameterType.getJavaClass().getSimpleName());
		}
		if(returnType == null) {
			t.append(')');
		} else {
			t.append("):");
			t.append(returnType.name());
		}
		return t.toString();
	}

}
