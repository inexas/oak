/*
 * Copyright (C) 2016 Inexas. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak.examples;

import java.time.LocalDate;
import com.inexas.oak.*;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.ast.Function;

/**
 * @author kwhittingham, @date 10 Dec 2016
 */
public class ExpressionExample1 {
	public static class ExampleFuncLib implements Library {
		@Function
		public String todayAsDay() {
			return LocalDate.now().getDayOfWeek().name();
		}

		@Override
		public Object resolve(String identifier) {
			return "userName".equals(identifier) ? "Oak" : null;
		}
	}

	public static void main(String[] args) throws OakException {
		final String example = "\"Hello \" + userName + \" today is \" + todayAsDay() + \"!\"";
		final Expression expression = new Expression(example, new ExampleFuncLib());
		System.out.println(expression.toString());
	}
}
