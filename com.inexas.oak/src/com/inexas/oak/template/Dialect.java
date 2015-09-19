package com.inexas.oak.template;

import java.util.Map;
import com.inexas.oak.advisory.Locus;
import com.inexas.oak.path.Identifier;

public class Dialect extends Locus.Base {
	public final String key;
	public final Map<String, Objet> objectMap;

	public Dialect(Identifier key, Map<String, Objet> objectMap) {
		this.key = key.toString();
		this.objectMap = objectMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean pretty) {
		final DialectVisitor visitor = new ToStringVisitor(pretty);
		accept(visitor);
		return visitor.toString();
	}

	public void accept(DialectVisitor visitor) {
		visitor.enter(this);

		for(final Objet objet : objectMap.values()) {
			objet.accept(visitor);
		}

		visitor.exit(this);
	}
}
