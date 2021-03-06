package de.hu_berlin.informatik.pearchat.messages;

import java.io.Serializable;

/**
 * @author Philipp Seiter
 * @author Daniel Titz
 *
 */
public class Chat implements Serializable {

	/**
	 * Version number to verify that the server and the client have the same classes.
	 */
	private static final long serialVersionUID = 42L;
	
	private String Text = "";
	private String UserName = "";
	
	/**
	 * The type for this specific message.
	 */
	private static byte TYPE_ID = (byte) 0x06;

	public Chat(String Username, String Text) {
		this.Text = Text; this.UserName = Username;
	}

	public String getText() {
		return Text;
	}

	public String getUserName() {
		return UserName;
	}
	
}
