package com.inexas.oak.examples;

import com.inexas.oak.dialect.OakConstructorException;

public class Email {
	final String text;
	final boolean isHome;

	@SuppressWarnings("unused")
	public Email(String text, Boolean isHome) throws OakConstructorException {
		this.text = text;
		this.isHome = isHome.booleanValue();
	}
}
