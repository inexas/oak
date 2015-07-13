/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak;

import com.inexas.oak.advisory.Advisory;

public class Documentation {

	public static void main(String[] args) {
		processString();
	}

	public static void processString() {
		final Oak oak = new Oak("meaningOfLife:42;");
		oak.processOak();
		final Advisory advisory = oak.getAdvisory();
		if(advisory.hasErrors()) {
			System.err.println(advisory);
		} else {
			final ToStringVisitor toStringVisitor = new ToStringVisitor(true);
			oak.accept(toStringVisitor);
			System.out.println(toStringVisitor.toString());
		}
	}
}
