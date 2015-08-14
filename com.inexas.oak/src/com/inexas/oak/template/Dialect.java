package com.inexas.oak.template;

import java.util.*;
import com.inexas.oak.advisory.Locus;

public class Dialect extends Locus.Base {
	public final String key;
	public final List<String> visitorList;
	public final Map<String, Objet> objectMap;

	public Dialect(
			String key,
			Map<String, Objet> objectMap,
			List<String> visitorList) {
		this.key = key;
		this.objectMap = objectMap;
		this.visitorList = visitorList;
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
