package com.inexas.oak;

import java.util.List;
import com.inexas.oak.advisory.Locus;

/**
 * Couldn't use the name Object as it causes too many problems, so use the
 * french word instead.
 */
public class Objet extends Locus.Base implements Keyed {
	final String key;
	final String templateClass;
	final boolean isRoot;
	final List<Member> members;

	public Objet(
			String key,
			String templateClass,
			Boolean isRoot,
			List<Member> members) {
		this.key = key;
		this.templateClass = templateClass;
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

	void accept(GrammarVisitor visitor) {
		visitor.enter(this);
		for(final Member member : members) {
			member.accept(visitor);
		}
		visitor.exit(this);
	}
}
