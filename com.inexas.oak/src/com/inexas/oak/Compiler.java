/*
 * Copyright (C) 2015 Processwide AG. All Rights Reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is provided as-is without warranty of merchantability or fitness for a
 * particular purpose.
 *
 * See http://www.inexas.com/license for license details.
 */

package com.inexas.oak;

import java.io.File;
import com.inexas.oak.advisory.OakException;
import com.inexas.oak.dialect.Rulebase;

/**
 * Use this class to compile dialects and write out the Rulebase and Visitor to
 * your source..
 *
 */
public class Compiler {
	/*
	 * On my machine the command line is
	 * 
	 * /Users/keith/Dev/gitrepos/oak/com.inexas.oak/data/oak/Oak.dialect
	 * com.inexas.oak /Users/keith/Dev/gitrepos/oak/com.inexas.oak/srcgen/
	 */

	public static void main(String[] args) {
		if(args.length != 3) {
			System.err.println("Usage is: \n"
					+ "\t...Compiler dialectFile packageName destination\n"
					+ "\n"
					+ "e.g.\n"
					+ "\n"
					+ "\t...Compiler /some/dir/Example.dialect com.inexas.willow /some/dir\n"
					+ "\n"
					+ "where\n"
					+ "\n"
					+ "\tdialectFile: Dialect file path\n"
					+ "\tpackageName: The target dialect Java package\n"
					+ "\tdestinationDirectory: Where the files are to be written\n"
					);
			System.exit(1);
		}

		final String source = args[0];
		final String packageName = args[1];
		final String tmp = args[2];
		final boolean gotSwitch = (tmp.endsWith("/") || tmp.endsWith("\\"));
		final String destination = gotSwitch ? tmp.substring(0, tmp.length() - 1) : tmp;

		System.out.println("Compiling: " + source);
		System.out.println("\tpackage: " + packageName);
		System.out.println("\tto: " + destination);

		final File sourceFile = new File(source);
		if(!sourceFile.isFile()) {
			throw new RuntimeException("Cannot find file: " + source);
		}

		try {
			final Oak oak = new Oak(sourceFile);
			final Rulebase rulebase = oak.toDialect();
			rulebase.write(destination, packageName);
			System.out.println("Compiled OK");
		} catch(final OakException e) {
			System.out.println(e.getAdvisory());
			throw new RuntimeException("Error building dialect", e);
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}
}
