/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import com.inexas.exception.UnexpectedException;
import com.inexas.oak.DataType;

/**
 * ErrorNode is used for error processing. If a syntax error is discovered then
 * an error node is created and place in the AST so that processing can continue
 * and more errors discovered.
 */
public class ErrorNode extends Node {

	public ErrorNode(ParserRuleContext context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(AstVisitor visitor) {
		throw new UnexpectedException("accept: Should not be called");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataType getType() {
		throw new UnexpectedException("accept: Should not be called");
	}

}
