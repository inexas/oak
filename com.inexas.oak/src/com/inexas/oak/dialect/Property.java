package com.inexas.oak.dialect;

import java.util.List;
import com.inexas.exception.ImplementMeException;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.Locus;

public class Property extends Locus.Base implements Keyed {
	final String key;
	final DataType type;
	final Constraint[] constraints;

	public Property(String key, String type, List<Constraint> constraints) {
		this(
				key,
				type,
				constraints == null ? null : constraints.toArray(new Constraint[constraints.size()]));
	}

	public Property(String key, String type, Constraint[] constraints) {
		this.key = key;
		if("expression".equals(type)) {
			this.type = DataType.ANY;
		} else if("boolean".equals(type)) {
			this.type = DataType.bool;
		} else {
			try {
				// Default type is text
				this.type = type == null ? DataType.text : DataType.valueOf(type);
			} catch(final IllegalArgumentException e) {
				// !todo Implement me
				throw new ImplementMeException("Invalid data type: " + type);
			}
		}
		this.constraints = constraints;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		return key;
	}

	void accept(DialectVisitor visitor) {
		visitor.enter(this);
		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				try {
					constraint.accept(visitor);
				} catch(final Exception e) {
					// !todo Implement me
					throw new ImplementMeException();
				}
			}
		}
		visitor.exit(this);
	}
}
