package com.inexas.oak;

import java.util.*;
import com.inexas.oak.advisory.Locus;

public class Grammar extends Locus.Base {
	final String key;
	final Map<String, Objet> objectMap;
	final Map<String, Property> propertyMap;
	final List<String> visitorsList;

	public Grammar(
			String key,
			Map<String, Objet> objectMap,
			Map<String, Property> propertyMap,
			List<String> visitorsList) {
		this.key = key;
		this.objectMap = objectMap;
		this.propertyMap = propertyMap;
		this.visitorsList = visitorsList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean pretty) {
		final GrammarVisitor visitor = new ToStringGrammarVisitor(pretty);
		accept(visitor);
		return visitor.toString();
	}

	void accept(GrammarVisitor visitor) {
		visitor.enter(this);

		for(final Objet objet : objectMap.values()) {
			objet.accept(visitor);
		}

		for(final Property property : propertyMap.values()) {
			property.accept(visitor);
		}

		visitor.exit(this);
	}

}
