package com.inexas.oak.dialect;

import com.inexas.oak.path.Identifier;

public class TestObject {
	String p;
	String q;

	public TestObject(Identifier p, Identifier q) {
		this.p = p.toString();
		this.q = q.toString();
	}
}
