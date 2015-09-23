package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.Identifier;
import com.inexas.oak.advisory.Advisory;
import com.inexas.oak.ast.OakParser.PairContext;
import com.inexas.tad.Context;

/**
 * Pair nodes have a name and a value (which may be an array of values)
 */
public abstract class PairNode extends Node {
	protected Identifier name;

	PairNode(ParserRuleContext context, Identifier name) {
		super(context);

		this.name = name;

		/*
		 * The characters in the name will be valid characters but not for
		 * length or upper case and lower case.
		 */
		final String message;
		if(name.length() > 32) {
			message = "Name too long: '" + name + "', maximum allowed is 32";
		} else {
			final char c = name.charAt(0);
			final boolean isUpper = c >= 'A' && c <= 'Z';
			if((this instanceof ObjectArrayPairNode) || (this instanceof ObjectPairNode)) {
				message = isUpper ?
						null
						: "Object names must start with Upper case letter: '" + name + '\'';
			} else {
				message = isUpper ?
						"Property names must start with lower case letter: '" + name + '\''
						: null;
			}
		}
		if(message != null) {
			final Advisory advisory = Context.get(Advisory.class);
			advisory.error(this, message);
		}
	}

	PairNode(ParserRuleContext context) {
		super(context);
	}

	public Identifier getName() {
		return name;
	}

	void setDetails(PairContext context, Identifier theName) {
		this.context = context;
		name = theName;
	}
}
