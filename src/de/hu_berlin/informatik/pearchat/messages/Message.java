package de.hu_berlin.informatik.pearchat.messages;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Provides basic features for the messages.
 * Implementation is following the Request for Comments (RFC) for Gnutella version 0.6.
 * The message header is 23 bytes distributed over the fields of the class.
 * 
 * @author	Philipp Seiter
 * @author	Daniel Titz
 *
 */
public class Message implements Serializable {
		
	/**
	 * version number to verify that the server and the client have the same classes.
	 */
	private static final long serialVersionUID = 42L;

	/**
	 * A Random instance to generate a GUID.
	 */
	private static Random random = new Random();

	/**
	 * A 16-byte string (GUID) uniquely identifying the message on the network. 
	 */
	private byte[] guid;
	
	/**
	 * Indicates the type of message.
	 */
	private MessageType messageType;
	
	/**
	 * Time To Live. The number of times the message will be forwarded by servents.
	 */
	private byte ttl;
	
	/**
	 * The number of times the message has been forwarded.
	 */
	private byte hops;
	
	/**
	 * The length of the message immediately following this header.
	 */
	private byte[] payloadLength;
	
	/**
	 * The actual payload
	 */
	private byte[] payload;
	
	
	/**
	 * @return guid
	 */
	public byte[] getGuid() {
		return guid;
	}
	
	public void setGuid(byte[] Guid) {
		this.guid = Guid;
	}

	/**
	 * @return payloadType
	 */
	public MessageType getMessageType() {
		return messageType;
	}

	/**
	 * @param messageType
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	/**
	 * @return ttl
	 */
	public byte getTtl() {
		return ttl;
	}

	/**
	 * @param ttl
	 */
	public void setTtl(byte ttl) {
		this.ttl = ttl;
	}

	/**
	 * @return hops
	 */
	public byte getHops() {
		return hops;
	}

	/**
	 * @param hops
	 */
	public void setHops(byte hops) {
		this.hops = hops;
	}

	/**
	 * @return payloadLength
	 */
	public byte[] getPayloadLength() {
		return payloadLength;
	}

	/**
	 * @param payloadLength
	 */
	public void setPayloadLength(byte[] payloadLength) {
		this.payloadLength = payloadLength;
	}
	
	/**
	 * @return payload
	 */
	public byte[] getPayload() {
		return this.payload;
	}
	
	/**
	 * @param payload
	 */
	public void setPayload(byte[] payload) {
		this.payload = payload;
		this.payloadLength = ByteBuffer.allocate(4).putInt(payload.hashCode()).array();
	}
	
	
	public boolean decreaseTTL() {
		if(this.ttl > 0) {
			this.ttl = (byte) (this.ttl - 1);
			return true;
		}
		
		return false;
	}
	
	public boolean incereaseHops() {
		if (this.hops < 5) {
			this.hops = (byte) (this.hops + 1);
			return true;
		}
		
		return false;
	}
	
	 
	/**
	 * TODO: encrypt GUID.
	 * Constructor for a newly created message
	 * 
	 * @param messageType
	 */
	public Message(MessageType messageType, byte[] payload) {
	    this.messageType = messageType;
	    this.payloadLength = ByteBuffer.allocate(4).putInt(payload.hashCode()).array();
	    this.payload = payload;
	}
	
	public void createGUID(){
		guid = new byte[16];
		random.nextBytes(guid); 
	}
	
	public Message(Message ToBeCopied) {
		this(ToBeCopied.getMessageType(), ToBeCopied.getPayload());
		this.setHops(ToBeCopied.getHops()); this.setTtl(ToBeCopied.getTtl());
	}
	
	
public static enum MessageType {
			
		Ping ((byte) 0x00),
		Pong ((byte)0x01),
		Query ((byte)0x02),
		QueryHit ((byte)0x03),
		Push ((byte)0x04),
		Bye ((byte)0x05),
		Chat ((byte)0x06),
		
		SYN ((byte)0x10),
		ACK ((byte)0x11);
	
		
		private final byte value;
		
		MessageType(byte value) {
			this.value = value;
		}
		
		private byte value() {
			return value;
		}
	}
}