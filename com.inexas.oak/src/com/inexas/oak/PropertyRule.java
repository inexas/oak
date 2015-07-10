package com.inexas.oak;

import java.util.*;

public class PropertyRule extends Rule {
	final DataType dataType;

	public PropertyRule(String name, DataType dataType, Constraint... constraints) {
		super(name, constraints);

		assert Character.isLowerCase(name.charAt(0)) : "Not a property name: " + name;
		assert dataType != null;

		this.dataType = dataType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Property<" + name
				+ ", " + dataType
				+ '>';
	}

	/**
	 * Check if the node has a valid data type
	 *
	 * @param candidate
	 *            A data type to be tested
	 * @return True if the candidate data type is compatible with this
	 *         Property's data type
	 */
	boolean isCompatibleDataType(DataType candidate) {
		final boolean result;

		if(dataType == DataType.ANY
				|| dataType == DataType.identifier && candidate == DataType.path) {
			result = true;
		} else {
			result = dataType == candidate;
		}

		return result;
	}

	public boolean isValid(Map<String, Object> map) {
		boolean result;

		if(constraints == null) {
			result = true;
		} else {
			result = true;
			for(final Constraint constraint : constraints) {
				if(!constraint.isValid(map)) {
					result = false;
					break;
				}
			}
		}

		return result;
	}

	public boolean isValid(Collection<Object> collection) {
		boolean result;

		if(constraints == null) {
			result = true;
		} else {
			result = true;
			for(final Constraint constraint : constraints) {
				if(!constraint.isValid(collection)) {
					result = false;
					break;
				}
			}
		}

		return result;
	}

	public boolean isValid(Object object) {
		boolean result;

		if(constraints == null) {
			result = true;
		} else {
			result = true;
			for(final Constraint constraint : constraints) {
				if(!constraint.isValid(object)) {
					result = false;
					break;
				}
			}
		}

		return result;
	}
}