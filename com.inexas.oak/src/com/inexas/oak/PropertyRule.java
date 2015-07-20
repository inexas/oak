package com.inexas.oak;

import java.util.*;
import com.inexas.oak.advisory.OakException;

public class PropertyRule extends Rule {
	final DataType dataType;

	public PropertyRule(String name, DataType dataType, Constraint... constraints) throws OakException {
		super(name, constraints);

		assert Character.isLowerCase(name.charAt(0)) : "Not a property name: " + name;
		assert dataType != null;

		this.dataType = dataType;

		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				constraint.setDataType(dataType);
			}
		}
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

	public void validate(Map<String, Object> map) throws OakException {
		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				constraint.validate(map);
			}
		}
	}

	public void isValid(Collection<Object> collection) throws OakException {
		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				constraint.validate(collection);
			}
		}
	}

	public void validate(Object object) throws OakException {
		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				constraint.validate(object);
			}
		}
	}
}