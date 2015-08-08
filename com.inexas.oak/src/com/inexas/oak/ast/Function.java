package com.inexas.oak.ast;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.*;
import com.inexas.oak.DataType;

class Function {
	final String name;
	final Method method;
	final DataType returnType;
	final boolean isStatic;
	final DataType[] argumentsTypes;
	final String signature;
	final int argumentCount;

	public Function(
			String name,
			Method method,
			DataType returnType,
			boolean isStatic,
			DataType... argumentTypes) {
		this.name = name;
		this.method = method;
		this.returnType = returnType;
		this.isStatic = isStatic;
		// Convention Methods: parameters, Functions: arguments
		this.argumentsTypes = argumentTypes;
		argumentCount = argumentTypes.length;

		// Build signature...
		final StringBuilder sb = new StringBuilder();

		sb.append(name);
		sb.append('(');
		String delimiter = "";
		for(final DataType parameterType : argumentTypes) {
			sb.append(delimiter);
			delimiter = ", ";
			sb.append(parameterType.name());
		}
		sb.append("):");
		sb.append(returnType.name());

		signature = sb.toString();
	}

	public ConstantNode invoke(ParserRuleContext context, Object[] arguments) {
		final ConstantNode result;

		try {
			final Class<?>[] parameterTypes = method.getParameterTypes();
			final Object[] parameters = new Object[argumentCount];
			for(int i = 0; i < argumentCount; i++) {
				final Object argument = arguments[i];
				switch(argumentsTypes[i]) {
				case bool:
					parameters[i] = argument;
					break;

				case date:
					parameters[i] = argument;
					break;

				case precision:
					parameters[i] = argument;
					break;

				case decimal: {
					final Class<?> clazz = parameterTypes[i];
					if(clazz == BigDecimal.class) {
						parameters[i] = new BigDecimal(((Double)argument).doubleValue());
					} else {
						parameters[i] = argument;
					}
					break;
				}

				case integer:
					final Class<?> clazz = parameterTypes[i];
					if(clazz == BigDecimal.class) {
						parameters[i] = new BigDecimal(((Long)argument).doubleValue());
					} else if(clazz == Double.class) {
						parameters[i] = new Double(((Long)argument).doubleValue());
					} else {
						parameters[i] = argument;
					}
					break;

				case text:
					parameters[i] = argument == null ? null : argument.toString();
					break;

				// $CASES-OMITTED$
				default:
					throw new UnexpectedException("invoke: ");
				}
			}

			final Object returnValue = method.invoke(null, arguments);

			switch(returnType) {
			case bool:
				result = new ConstantNode(context, (Boolean)returnValue);
				break;

			case date:
				result = new ConstantNode(context, (Date)returnValue);
				break;

			case text:
				result = new ConstantNode(context, (String)returnValue);
				break;

			case precision: {
				final Class<?> clazz = returnValue.getClass();
				if(clazz == BigDecimal.class) {
					result = new ConstantNode(context, (BigDecimal)returnValue);
				} else if(clazz == Double.class || clazz == Float.class) {
					result = new ConstantNode(context, new BigDecimal(((Number)returnValue).doubleValue()));
				} else {
					result = new ConstantNode(context, new BigDecimal(((Number)returnValue).longValue()));
				}
				break;
			}

			case decimal: {
				final Class<?> clazz = returnValue.getClass();
				if(clazz == Double.class) {
					result = new ConstantNode(context, (Double)returnValue);
				} else {
					result = new ConstantNode(context, ((Number)returnValue).doubleValue());
				}
				break;
			}

			case integer: {
				final Class<?> clazz = returnValue.getClass();
				if(clazz == Long.class) {
					result = new ConstantNode(context, (Long)returnValue);
				} else {
					result = new ConstantNode(context, ((Number)returnValue).longValue());
				}
				break;
			}

			// $CASES-OMITTED$
			default:
				throw new UnexpectedException("invoke: ");
			}

		} catch(final ClassCastException e) {
			throw new InexasRuntimeException("Probable data type mismatch", e);
		} catch(final Exception e) {
			throw new InexasRuntimeException("Error invoking method", e);
		}

		return result;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
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