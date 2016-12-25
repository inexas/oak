package com.inexas.oak.examples;

import java.io.File;
import com.inexas.oak.Oak;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.examples.PersonDialect;

public class DialectExample {
	public static void main(String[] args) {
		try {
			final Oak personOak = new Oak(new File("resources/John.person"));
			final Person john = personOak.toObjectTree(PersonDialect.class);
			System.out.println(john.emailList.size());
		} catch(final OakException e) {
			System.out.println(e.getAdvisory());
		}
	}
}
