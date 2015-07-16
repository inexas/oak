package com.inexas.oak.examples;

import java.util.List;

public class Person {
	public final String userId;
	public final List<Email> emails;

	public Person(String userId, List<Email> emailList) {
		this.userId = userId;
		this.emails = emailList;
	}

	@Override
	public String toString() {
		return "Person: " + userId + ", emails: " + emails;
	}
}
