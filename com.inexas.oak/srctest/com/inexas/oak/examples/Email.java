package com.inexas.oak.examples;

public class Email {
	public final String text;
	public final boolean isHome;

	public Email(String text, Boolean isHome) {
		this.text = text;
		this.isHome = isHome.booleanValue();
	}

	@Override
	public String toString() {
		return text + (isHome ? "(home)" : "(work)");
	}
}
