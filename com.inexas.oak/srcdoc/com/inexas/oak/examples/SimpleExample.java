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
 * @author kwhittingham, @date 25 Dec 2016
 */
public class SimpleExample {

	public static class ExampleLibrary implements Library {
		@Function
		public static String todayAsDay() {
			return LocalDate.now().getDayOfWeek().name();
		}

		@Override
		public Object resolve(String symbol) {
			final Object result;

			switch(symbol) {
			case "user":
				result = "oak";
				break;
			default:
				result = Library.UNRESOLVED;
				break;
			}

			return result;
		}
	}

	public static void main(String[] args) throws OakException {
		final String example = "\"Hello \" + user + \", today is \" + todayAsDay() + \"!\"";
		final Expression expression = new Expression(example, new ExampleLibrary());
		System.out.println(expression.toString());
	}
}
