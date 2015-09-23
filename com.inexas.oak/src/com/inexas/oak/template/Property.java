package com.inexas.oak.template;

import java.util.List;
import com.inexas.exception.ImplementMeException;
import com.inexas.oak.*;
import com.inexas.oak.advisory.Locus;
import com.inexas.oak.dialect.Keyed;

public class Property extends Locus.Base implements Keyed {
	public final Identifier key;
	public final DataType type;
	public final Constraint[] constraints;

	public Property(Identifier key, Identifier type, List<Constraint> constraints) {
		this(key,
				type == null ? null : type,
						constraints == null ? null : constraints.toArray(new Constraint[constraints.size()]));
	}

	public Property(Identifier key, Identifier type, Constraint[] constraints) {
		this.key = key;
		this.type = type == null ? DataType.text : DataType.valueOfCorrected(type.toString());
		this.constraints = constraints;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identifier getKey() {
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
