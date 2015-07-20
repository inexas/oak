package com.inexas.oak;

import java.math.BigDecimal;
import java.util.Date;
import com.inexas.exception.ImplementMeException;
import com.inexas.util.*;

public enum DataType {
	integer(1, Long.class),
	decimal(2, Double.class),
	precision(4, BigDecimal.class),
	bool(8, Boolean.class),
	text(16, String.class),
	date(32, Date.class),
	time(64, Date.class),
	datetime(96, Date.class),
	identifier(128, String.class),
	path(256, String.class),
	cardinality(512, Cardinality.class),
	ANY(1024 - 1, Object.class),
	NULL(1024, null);

	public final int bit;
	public final boolean isNumeric;
	public final boolean isTemporal;
	public final Class<?> javaClass;

	DataType(int id, Class<?> javaClass) {
		this.bit = id;
		isNumeric = bit <= 4;
		isTemporal = (bit & 96) > 0;
		this.javaClass = javaClass;
	}

	public Object parse(String value) {
		Object result = null;
		switch(this) {
		case ANY:
			// !todo Implement me
			throw new ImplementMeException();
	
		case NULL:
			// result is already null
			break;
	
		case bool:
			result = Boolean.valueOf(value);
			break;
	
		case cardinality:
			result = Cardinality.newInstance(value);
			break;
	
		case date:
		case datetime:
		case time:
			result = DateU.parseDateTimeStandard(value);
			break;
	
		case decimal:
		case precision:
			result = new BigDecimal(value);
			break;
	
		case identifier:
		case path:
		case text:
			result = value;
			break;
	
		case integer:
			result = new Long(value);
			break;
		}
	
		return result;
	}

	public void toString(TextBuilder result, String indent) {
		result.append(indent);
		result.append(toString());
	}

	/**
	 * This returns the same as name() expect for booleans where it returns
	 * "boolean" instead of "bool".
	 *
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this == bool ? "boolean" : name();
	}
}