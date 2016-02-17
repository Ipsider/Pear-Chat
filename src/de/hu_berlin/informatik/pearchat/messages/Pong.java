package de.hu_berlin.informatik.pearchat.messages;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * The response to a Ping.
 * 
 * @author Philipp Seiter
 * @author Daniel Titz
 *
 */
public class Pong implements Serializable {

	/**
	 * Version number to verify that the server and the client have the same classes.
	 */
	private static final long serialVersionUID = 42L;
	
	private ArrayList<InetAddress> Neighbors;
	private InetAddress inetAddressSender;
	
	/**
	 * The type for this specific message.
	 */
	private static byte TYPE_ID = (byte) 0x01;

	public Pong(ArrayList<InetAddress> Neighbors) {
		this.Neighbors = Neighbors;
	}
	
	public Pong (InetAddress inetAddress) {
		this.inetAddressSender = inetAddress;
	}
	
	public ArrayList<InetAddress> getNeigbors() {
		return this.Neighbors;
	}

	public InetAddress getInetAddress() {
		return this.inetAddressSender;
	}
	
}
