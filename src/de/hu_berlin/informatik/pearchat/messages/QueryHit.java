package de.hu_berlin.informatik.pearchat.messages;

import java.io.Serializable;

/**
 * The response to a Query.
 * 
 * @author Philipp Seiter
 * @author Daniel Titz
 *
 */
public class QueryHit implements Serializable {

	/**
	 * Version number to verify that the server and the client have the same classes.
	 */
	private static final long serialVersionUID = 42L;
	
	/**
	 * The type for this specific message.
	 */
	private static byte TYPE_ID = (byte) 0x03;

	public QueryHit() {

	}
}
