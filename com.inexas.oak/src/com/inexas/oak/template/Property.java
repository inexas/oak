package com.inexas.oak.template;

import java.util.List;
import com.inexas.exception.ImplementMeException;
import com.inexas.oak.DataType;
import com.inexas.oak.advisory.Locus;
import com.inexas.oak.dialect.Keyed;

public class Property extends Locus.Base implements Keyed {
	public final String key;
	public final DataType type;
	public final Constraint[] constraints;

	public Property(String key, String type, List<Constraint> constraints) {
		this(
				key,
				type,
				constraints == null ? null : constraints.toArray(new Constraint[constraints.size()]));
	}

	public Property(String key, String type, Constraint[] constraints) {
		this.key = key;
		this.type = type == null ? DataType.text : DataType.valueOfCorrected(type);
		this.constraints = constraints;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final ToStringVisitor visitor = new ToStringVisitor(true);
		accept(visitor);
		return visitor.toString();
	}

	public void accept(DialectVisitor visitor) {
		visitor.enter(this);
		if(constraints != null) {
			for(final Constraint constraint : constraints) {
				try {
					constraint.accept(visitor);
				} catch(final Exception e) {
					// todo Implement me
					throw new ImplementMeException();
				}
			}
		}
		visitor.exit(this);
	}
}
