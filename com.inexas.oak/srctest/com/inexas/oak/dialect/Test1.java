/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.dialect;

import com.inexas.oak.Identifier;

/**
 * @author kwhittingham
 */
public class Test1 {
	String key1;
	Boolean key2;

	public Test1(Identifier key1, Boolean key2) {
		this.key1 = key1.toString();
		this.key2 = key2;
	}
}
