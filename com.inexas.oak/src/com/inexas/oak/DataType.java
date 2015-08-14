package com.inexas.oak;

import java.math.*;
import java.util.Date;
import com.inexas.exception.ImplementMeException;
import com.inexas.util.*;

public enum DataType {
	// @formatter:off
	integer(		0x0001,			Long.class),		// Must be 1
	INTEGER(		0x0002,			BigInteger.class),
	decimal(		0x0004,			Double.class),
	DECIMAL(		0x0008,			BigDecimal.class),	// Do not change order or value
	text(			0x0010,			String.class),
	identifier(		0x0020,			String.class),
	path(			0x0040,			String.class),
	bool(			0x0080,			Boolean.class),
	cardinality(	0x0100,			Cardinality.class),
	date(			0x0200,			Date.class),
	time(			0x0400,			Date.class),
	datetime(		0x0800,			Date.class),
	any(			0x1000 - 1,		Object.class);
	// @formatter:on

	public final int bit;
	public final boolean isNumeric;
	public final boolean isTemporal;
	public final Class<?> javaClass;

	DataType(int id, Class<?> javaClass) {
		this.bit = id;
		// ?todo Should 'any' be considered here?
		isNumeric = (bit & (0x0001 | 0x0002 | 0x0004 | 0x0008)) > 0;
		isTemporal = (bit & (0x0200 | 0x0400 | 0x0800)) > 0;
		this.javaClass = javaClass;
	}

	public Object parse(String value) {
		final Object result;
		switch(this) {
		case text:
		case identifier:
		case path:
			result = value;
			break;

		case bool:
			result = Boolean.valueOf(value);
			break;

		case integer:
			result = new Long(value);
			break;

		case INTEGER:
			result = new BigInteger(value);
			break;

		case decimal:
			result = new Double(value);
			break;

		case DECIMAL:
			result = new BigDecimal(value);
			break;

		case cardinality:
			result = Cardinality.newInstance(value);
			break;

		case date:
		case time:
		case datetime:
			result = DateU.parseDateTimeStandard(value);
			break;

		case any:
			// !todo Implement me
			throw new ImplementMeException();

		default:
			throw new ImplementMeException(value);
		}

		return result;
	}

	public void toString(TextBuilder result, String indent) {
		result.append(indent);
		result.append(toString());
	}

	/**
	 * This returns the same as name() except for booleans where "boolean" is
	 * returned.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this == bool ? "boolean" : name();
	}
}