package com.inexas.oak.ast.testlibs;

import com.inexas.oak.Library;
import com.inexas.oak.ast.Function;

public class TestFunclib1 implements Library {

	@Function()
	public void vf() {
		//
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object resolve(String identifier) {
		return UNRESOLVED;
	}
}
