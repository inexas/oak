package com.inexas.oak;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.oak.OakParser.PairContext;

/**
 * Pair nodes have a name and a value (which may be an array of values)
 */
public abstract class PairNode extends Node {
	protected String name;

	PairNode(ParserRuleContext context, String name) {
		super(context);

		this.name = name;
	}

	PairNode(ParserRuleContext context) {
		super(context);
	}

	public String getName() {
		return name;
	}

	void setDetails(PairContext context, String theName) {
		this.context = context;
		name = theName;
	}

}
