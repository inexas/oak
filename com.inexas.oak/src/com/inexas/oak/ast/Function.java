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

import java.lang.annotation.*;

/**
 * Defines a method as an extension (function) to be used in Oak expressions.
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Function {
	/**
	 * @return Return true if the value returned changes with time or according
	 *         to the environment, false otherwise. Default is false.
	 */
	boolean dynamic() default false;
}
