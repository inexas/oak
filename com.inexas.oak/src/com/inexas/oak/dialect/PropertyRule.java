package com.inexas.oak.dialect;

import java.util.*;
import com.inexas.oak.*;
import com.inexas.oak.template.Constraint;

public class PropertyRule extends Rule {
	public final DataType dataType;
	public final Constraint[] constraints;

	public PropertyRule(Identifier name, DataType dataType, Constraint... constraints) {
		super(name);

		assert dataType != null;

		this.dataType = dataType;
		this.constraints = constraints == null || constraints.length == 0 ? null : constraints;

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
		return "Property<" + key
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
	public boolean isCompatibleDataType(DataType candidate) {
		final boolean result;

		if(dataType == DataType.any
				|| dataType == DataType.identifier && candidate == DataType.path) {
			result = true;
		} else {
			result = dataType == candidate;
		}

		return result;
	}

	public boolean validateMap(Map<String, Object> map) {
		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				constraint.validate(map);
			}
		}
		return true;
	}

	public boolean validateCollection(Collection<Object> collection) {
		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				constraint.validate(collection);
			}
		}
		return true;
	}

	public boolean validateObject(Object object) {
		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				constraint.validate(object);
			}
		}
		return true;
	}
}