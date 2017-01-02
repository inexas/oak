/*
 * Copyright (C) 2017 Inexas. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */
package com.inexas.oak.advisory;

import static org.junit.Assert.assertTrue;
import java.nio.file.*;
import org.junit.Test;
import com.inexas.util.FileU;

/**
 * @author kwhittingham, @date 2 Jan 2017
 */
public class TestAdvisory {

	@Test
	public void test() {
		final Advisory advisory = new Advisory("Source");
		advisory.error(1, 1, "An error");
		advisory.warning(2, 0, "A warning");
		advisory.info(3, 3, "Some information");
		final Path path = Paths.get(FileU.DATATEST, "advisory.log");
		advisory.write(path);

		assertTrue(true);
	}

}
