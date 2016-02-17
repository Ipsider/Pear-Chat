package de.hu_berlin.informatik.pearchat.messages;

import java.io.Serializable;

public class ACK implements Serializable {

	private static final long serialVersionUID = 42L;
	private static byte TYPE_ID = (byte) 0x05;
	private boolean ConnectionAccepted = false;
	
	
	
	public ACK(boolean ConnectionAccepted) {
		this.ConnectionAccepted = ConnectionAccepted;
	}
	
	public boolean getConnectionAccepted() {
		return this.ConnectionAccepted;
	}

}
