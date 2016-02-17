package de.hu_berlin.informatik.pearchat.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;

import de.hu_berlin.informatik.pearchat.gui.ChatGUI;
import de.hu_berlin.informatik.pearchat.messages.ACK;
import de.hu_berlin.informatik.pearchat.messages.Chat;
import de.hu_berlin.informatik.pearchat.messages.Message;
import de.hu_berlin.informatik.pearchat.messages.Pong;

/**
 * Handler processing retrieved messages from peers. Analyzes the messages and
 * autonomously answers at the same socket. Only called by SocketHandler as a
 * 1-to-1 relationship.
 * 
 * @author Philipp Seiter
 * @author Daniel Titz
 */
public class MessageHandler {

	private static final byte TTL = 5;
	private SocketHandler socketHandler;

	/**
	 * @param SocketHandler
	 */
	public void setSocketHandler(SocketHandler SocketHandler) {
		this.socketHandler = SocketHandler;
	}

	/**
	 * Consumes a message received by the connected socket by forwarding it to
	 * its specified sub function.
	 * 
	 * @param message
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void digestMessage(Message message) throws IOException, ClassNotFoundException {

		switch (message.getMessageType()) {
		case Ping:
			this.pingDigest(message);
			break;
		case Pong:
			this.pongDigest(message);
			break;
		case Query:
			break;
		case QueryHit:
			break;
		case Push:
			break;
		case Chat:
			this.chatDigest(message);
			break;
		case Bye:
			this.byeDigest(message);
			break;
		case SYN:
			this.synDigest(message);
			break;
		case ACK:
			this.ackDigest(message);
			break;
		}
	}

	/**
	 * @param obj
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(byteArray);
		objOut.writeObject(obj);
		return byteArray.toByteArray();
	}

	/**
	 * @param bytes
	 * @return byte[]
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
		ObjectInputStream objIn = new ObjectInputStream(byteArray);
		return objIn.readObject();
	}

	/**
	 * Reacts with the proper actions to a received ping message. If the TTL of
	 * the message is bigger than zero and not yet seen, the ping is stored in
	 * the forwarding table and a pong is sent back. For the other neighbor
	 * peers, the ping is forwarded to.
	 * 
	 * @param message
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void pingDigest(Message message) throws IOException, ClassNotFoundException {

		System.out.println(Arrays.toString(message.getGuid()) + "Ping received");
		message.setTtl((byte) (message.getTtl() - 1));
		message.setHops((byte) (message.getHops() + 1));

		// If the message is still alive and...
		if (message.getTtl() > (byte) 0x00) {

			// was not seen before...
			if (!Communication.getMessageIDToAddress().containsKey(Arrays.toString(message.getGuid()))) {

				InetAddress inetAddress = this.socketHandler.getConnectedAddress();
				Communication.rememberID(Arrays.toString(message.getGuid()), inetAddress);
				System.out.print("new ping remembered.");

				// forward it to every peer...
				Iterator<SocketHandler> SocketListIterator = Communication.getSocketList().values().iterator();
				for (Iterator<SocketHandler> iter = SocketListIterator; iter.hasNext();) {

					SocketHandler tmpSocketHandler = (SocketHandler) iter.next();
					System.out.println(Communication.getSocketList());
					System.out.println(tmpSocketHandler);
					if (tmpSocketHandler != this.socketHandler) {

						if (tmpSocketHandler != null) {
							tmpSocketHandler.sendMessage(message);
							System.out.println("Ping forwarded");
						}
						
					// except for the sender, which gets a pong response.
					} else {

						Pong pong = new Pong(InetAddress.getLocalHost());
						byte[] pongBytes = MessageHandler.serialize(pong);
						message.setMessageType(Message.MessageType.Pong);
						message.setPayload(pongBytes);
						message.setTtl(TTL);
						message.setHops((byte) 0);

						tmpSocketHandler.sendMessage(message);
						System.out.println("Pong responded");
					}
				}
			}
		}
	}

	/**
	 * Reacts with the proper actions to a received pong message. If the maximum amount of
	 * peers is not reached yet, add the peer in the payload. If the message is alive and
	 * not seen before, forward it in the proper direction (from where it came from).
	 * @param message
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void pongDigest(Message message) throws IOException, ClassNotFoundException {

		message.setTtl((byte) (message.getTtl() - 1));
		message.setHops((byte) (message.getHops() + 1));

		Pong pong = (Pong) MessageHandler.deserialize(message.getPayload());
		InetAddress newlyFoundInetAddress = pong.getInetAddress();
		System.out.println(newlyFoundInetAddress + " (Pong received)");

		// If the peer still needs neighbors, it doesn't matter if the pong
		// is the answer to a ping from itself or not.
		if (!Communication.isFull()) {
			if (!newlyFoundInetAddress.equals(socketHandler.getConnectedAddress()))
				Communication.addSocket(newlyFoundInetAddress);
		}

		// Check if the message is still alive.
		if (message.getTtl() > (byte) 0x00) {

			// Check if the message was seen before.
			if (Communication.getMessageIDToAddress().containsKey(Arrays.toString(message.getGuid()))) {

				// Forward it to the proper direction.
				InetAddress senderInetAddress = Communication.getMessageIDToAddress()
						.get(Arrays.toString(message.getGuid()));

				Iterator<SocketHandler> SocketListIterator = Communication.getSocketList().values().iterator();
				for (Iterator<SocketHandler> iter = SocketListIterator; iter.hasNext();) {

					SocketHandler tmpSocketHandler = (SocketHandler) iter.next();

					if (tmpSocketHandler.getConnectedAddress().equals(senderInetAddress)) {
						tmpSocketHandler.sendMessage(message);
						System.out.println(newlyFoundInetAddress + " (Pong forwarded)");
					}
				}
			}
		}
	}

	/**
	 * digestMessage sub function for processing chat messages. Floods the network with unknown chat messages.
	 * 
	 * @param message
	 *            containing Chat as payload
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void chatDigest(Message message) throws IOException, ClassNotFoundException {
		System.out.println("Chat received!");
		
		// If the chat message was not seen before...
		if (!Communication.getReceivedChats().contains(Arrays.toString(message.getGuid()))) {
			
			// Remember it and...
			Communication.addReceivedChat(message);
			
			// Save it and...
			Chat ChatContent = (Chat) MessageHandler.deserialize(message.getPayload());
			System.out.println(ChatContent.getUserName() + ChatContent.getText());
			MessageSaver.saveMessage(ChatContent.getUserName(), ChatContent.getText());
			System.out.println("ID: " + Arrays.toString(message.getGuid())+ "TEXT: " + ChatContent.getText());

			ChatGUI.scrollDown();

			// Forward it to every other peer.
			Iterator<SocketHandler> SocketListIterator = Communication.getSocketList().values().iterator();
			for (Iterator<SocketHandler> iter = SocketListIterator; iter.hasNext();) {

				SocketHandler tmpSocketHandler = (SocketHandler) iter.next();
				System.out.println(Communication.getSocketList());
				System.out.println(tmpSocketHandler);
				if (tmpSocketHandler != this.socketHandler) {

					if (tmpSocketHandler != null) {
						message.setTtl((byte) 2);
						tmpSocketHandler.sendMessage(message);
						System.out.println("Chat forwarded");
					}
				}
			}
		}
	}
	
	/**
	 * digestMessage sub function for processing SYN-messages.
	 * 
	 * @param synMsg
	 *            Message containing SYN as payload
	 * @throws IOException
	 */
	private void synDigest(Message synMsg) throws IOException {
		System.out.println("SYN received");
		boolean FreeConnectionAvailable = (Communication.getSocketList().size() <= 5);
		ACK AnswerACK = new ACK(FreeConnectionAvailable);
		byte[] AnswerACKSerialize = MessageHandler.serialize(AnswerACK);
		Message AnswerMessage = new Message(Message.MessageType.ACK, AnswerACKSerialize);
		AnswerMessage.setTtl((byte) 1);
		AnswerMessage.setHops((byte) 0);
		this.socketHandler.sendMessage(AnswerMessage);
		System.out.println("ACK sent");
	}

	/**
	 * digestMessage subfunction for processing ACK-messages.
	 * 
	 * @param ACKMsg
	 *            Message containing ACK as payload
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void ackDigest(Message ACKMsg) throws ClassNotFoundException, IOException {
		System.out.println("ACK received");
		Object TmpObj = MessageHandler.deserialize(ACKMsg.getPayload());
		ACK ReceivedACK = (ACK) TmpObj;
		if (!ReceivedACK.getConnectionAccepted()) {
			System.out.println(ReceivedACK.getConnectionAccepted());
		}
	}

	/**
	 * digestMessage sub function for processing Bye-messages
	 * 
	 * @param ByeMsg
	 *            containing Bye as payload
	 * @throws IOException
	 */
	private void byeDigest(Message ByeMsg) throws IOException {
		this.socketHandler.close();
	}
}
