package com.inexas.oak.examples;

import java.util.List;
import com.inexas.oak.Identifier;

public class Person {
	public final Identifier userId;
	public final List<Email> emails;

	public Person(Identifier userId, List<Email> emailList) {
		this.userId = userId;
		this.emails = emailList;
	}

	@Override
	public String toString() {
		return "Person: " + userId + ", emails: " + emails;
	}
}
