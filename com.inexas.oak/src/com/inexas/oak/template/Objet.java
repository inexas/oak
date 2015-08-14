package com.inexas.oak.template;

import java.util.List;
import com.inexas.oak.advisory.*;
import com.inexas.oak.dialect.Keyed;
import com.inexas.tad.Context;

/**
 * Couldn't use the name Object as it causes too many problems, so use the
 * french word instead.
 */
public class Objet extends Locus.Base implements Keyed {
	public final String key;
	public final Class<?> templateClass;
	public final boolean isRoot;
	public final List<Member> members;

	public Objet(
			String key,
			String templateClass,
			Boolean isRoot,
			List<Member> members) {
		this.key = key;
		Class<?> tmp;
		try {
			tmp = Class.forName(templateClass);
		} catch(final ClassNotFoundException e) {
			final Advisory advisory = Context.get(Advisory.class);
			advisory.error(this, "Template class not found: " + templateClass);
			tmp = null;
		}
		this.templateClass = tmp;
		this.isRoot = isRoot == null ? false : isRoot.booleanValue();
		this.members = members;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getKey() {
		return key;
	}

	public void accept(DialectVisitor visitor) {
		visitor.enter(this);
		for(final Member member : members) {
			member.accept(visitor);
		}
		visitor.exit(this);
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
}
