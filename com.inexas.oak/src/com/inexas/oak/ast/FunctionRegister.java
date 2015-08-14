package com.inexas.oak.ast;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import com.inexas.exception.*;
import com.inexas.oak.DataType;
import com.inexas.tad.Tad;

/**
 * This is a storage place for all the built-in functions and extension.
 *
 * Functions are loaded from the class path. They are static methods within a
 * named class.
 */
public class FunctionRegister implements Tad {
	private final Map<String, Function[]> map = new HashMap<>();
	private final static int requiredModifiers = Modifier.STATIC | Modifier.PUBLIC;

	public void loadMath() {
		try {
			final Method method = Math.class.getMethod("abs", long.class);
			register("abs", method, DataType.integer, true, DataType.integer);
			// todo Add rest of Math
		} catch(final Exception e) {
			throw new InexasRuntimeException("Error loading function", e);
		}
	}

	public void register(
			String name,
			Method method,
			DataType returnType,
			boolean isStatic,
			DataType... parameterTypes) {
		final Function function = new Function(name, method, returnType, isStatic, parameterTypes);

		// Check the method is compatible...

		// Check the modifiers...
		if(!((method.getModifiers() & requiredModifiers) == requiredModifiers)) {
			throw new InexasRuntimeException("Method must be public static: " + function.signature);
		}

		// Check parameters count...
		final Class<?>[] methodParameterTypes = method.getParameterTypes();
		final int parameterTypeCount = parameterTypes.length;
		if(methodParameterTypes.length != parameterTypeCount) {
			throw new InexasRuntimeException("Method does not have the right number of parameters:  "
					+ function.signature);
		}

		// Check parameter types...
		for(int i = 0; i < parameterTypeCount; i++) {
			final Class<?> type = methodParameterTypes[i];
			final DataType parameterType = parameterTypes[i];
			final boolean valid;
			switch(parameterType) {
			case bool:
				valid = type == Boolean.class || type == boolean.class;
				break;

			case date:
				valid = type == Date.class;
				break;

			case decimal:
				valid = type == Double.class || type == double.class || type == BigDecimal.class;
				break;

			case integer:
				valid = type == Long.class || type == long.class
						|| type == Double.class || type == double.class
						|| type == BigDecimal.class;
				break;
			case INTEGER:
				valid = type == BigDecimal.class;
				break;

			case text:
				valid = true;
				break;

			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("register: ");
			}

			if(!valid) {
				throw new InexasRuntimeException("Incompatible types: " + function.signature);
			}
		}

		// Find where in the register we need to add the function...
		final String key = name + ':' + parameterTypeCount;
		Function[] array = map.get(key);
		final int index;
		if(array == null) {
			// No function with the same key...
			array = new Function[1];
			index = 0;
		} else {
			// Already function with same key...
			final Function[] old = array;
			index = old.length;
			array = new Function[index + 1];
			System.arraycopy(old, 0, array, 0, index);
		}

		// Add the function...
		array[index] = function;
		map.put(key, array);
	}

	public Function getFunction(String name, ExpressionNode[] parameters) {
		Function result = null;

		final Function[] functions = map.get(name + ':' + parameters.length);
		if(functions != null) {
			int bestDistance = Integer.MAX_VALUE;
			final int parameterCount = parameters.length;
			for(final Function function : functions) {
				/*
				 * We know the function we're looking at has the same number of
				 * parameters now we want to check that the types are compatible
				 * and find the function with the minimum distance from
				 * arguments we've been passed
				 */
				int distance = 0;
				for(int i = 0; i < parameterCount; i++) {
					final DataType parameterType = parameters[i].getType();
					final DataType functionParameterType = function.argumentsTypes[i];
					final int thisDistance = functionParameterType.bit - parameterType.bit;
					if(thisDistance < 0) {
						// E.g. cannot convert decimal to long
						distance = -1;
						break;
					}
					distance += thisDistance;
				}

				if(distance == 0) {
					result = function;
					break;
				}

				if(distance < 0) {
					continue;
				}

				if(distance < bestDistance) {
					result = function;
					bestDistance = distance;
				}
			}
		}

		if(result == null) {
			throw new InexasRuntimeException("Function not found: " + name);
		}

		return result;
	}
}
