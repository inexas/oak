package com.inexas.oak.ast.testlibs;

import com.inexas.oak.Library;

public class TestFunclib4 implements Library {

	public int f() {
		return 0;
	}

	@Override
	public Object resolve(String identifier) {
		return UNRESOLVED;

	}
}
