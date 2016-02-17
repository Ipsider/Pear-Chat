	package de.hu_berlin.informatik.pearchat.messages;

import java.io.Serializable;

/**
 * Used to contact remote peers, which respond with a Pong.
 * 
 * @author Philipp Seiter
 * @author Daniel Titz
 *
 */
public class Ping implements Serializable {
	
	/**
	 * Version number to verify that the server and the client have the same classes.
	 */
	private static final long serialVersionUID = 42L;
	
	
	/**
	 * The type for this specific message.
	 */
	private static byte TYPE_ID = (byte) 0x00;

	public Ping() {

		
	}
}
