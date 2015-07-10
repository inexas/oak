package com.inexas.oak;

/**
 * Implemented by Nodes that appear in an Oak file with a name. For example:
 * "MyName {...}". Don't confuse this with the key which is optional in internal
 * to the Objet or Property
 */
interface Named {
	/**
	 * @return The name of the Node.
	 */
	String getName();
}
