package com.inexas.oak.ast.testlibs;

import com.inexas.oak.Library;
import com.inexas.oak.ast.Function;

public class TestFunclib2 implements Library {

	@Function()
	int f() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object resolve(String identifier) {
		return UNRESOLVED;
	}
}
