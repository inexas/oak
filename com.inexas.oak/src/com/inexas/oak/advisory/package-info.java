/**
 * Classes in this package provide an error and warning handling
 * mechanism for Oak.
 *
 * Typically we are parsing a file and there may be several errors in it.
 * In general it is better to parse as much as possible and collect the errors
 * so that they can be fixed en masse. To do this we pass an instance of an Advisory class
 * to the various components that are involve in the processing of the file.
 *
 */
package com.inexas.oak.advisory;