package com.inexas.oak.examples;

import java.util.List;
import com.inexas.oak.Identifier;
import com.inexas.oak.dialect.OakConstructorException;

public class Person {
	final String userId;
	final List<Email> emailList;

	@SuppressWarnings("unused")
	public Person(Identifier userId, List<Email> emailList) throws OakConstructorException {
		this.userId = userId.toString();
		this.emailList = emailList;
	}
}
