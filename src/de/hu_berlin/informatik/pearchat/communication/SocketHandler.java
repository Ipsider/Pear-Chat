package de.hu_berlin.informatik.pearchat.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.hu_berlin.informatik.pearchat.messages.Message;
import de.hu_berlin.informatik.pearchat.messages.Message.MessageType;

/**
 * @author Philipp Seiter
 * @author Daniel Titz
 *
 */
public class SocketHandler implements Runnable {

	private Socket socket;
	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private MessageHandler messageHandler;
	private Timer periodicPingTimer;
	private boolean socketOpen;

	/**
	 * @param socket
	 * @throws SocketException
	 */
	public SocketHandler(Socket socket) throws SocketException {
		this.socket = socket;
		this.socket.setKeepAlive(true);
		socketOpen = true;
		this.messageHandler = new MessageHandler();
		this.messageHandler.setSocketHandler(this);
	}

	/**
	 * @param inetSocketAdress
	 * @throws IOException
	 */
	public SocketHandler(InetSocketAddress inetSocketAdress) throws IOException {
		String ipAdress = inetSocketAdress.getAddress().getHostAddress();
		int port = inetSocketAdress.getPort();
		this.socket = new Socket(ipAdress, port);
		this.socket.setKeepAlive(true);
		socketOpen = true;
		this.messageHandler = new MessageHandler();
		this.messageHandler.setSocketHandler(this);
	}

	@Override
	public void run() {
		OutputStream outputStream = null;
		try {
			outputStream = socket.getOutputStream();
		} catch (IOException e1) {
			System.out.println("Failed to create output stream.");
			e1.printStackTrace();
		}
		try {
			this.objectOutputStream = new ObjectOutputStream(outputStream);
		} catch (IOException e1) {
			System.out.println("Failed to create object output stream.");
			e1.printStackTrace();
		}

		InputStream inputStream = null;
		try {
			inputStream = socket.getInputStream();
		} catch (IOException e1) {
			System.out.println("Failed to create input stream.");
			e1.printStackTrace();
		}
		try {
			this.objectInputStream = new ObjectInputStream(inputStream);
		} catch (IOException e1) {
			System.out.println("Failed to create object input stream.");
			e1.printStackTrace();
		}
		
		initPeriodicPing();

		while (socketOpen) {
			Message incomingMessage = null;
			try {
				incomingMessage = (Message) objectInputStream.readObject();
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Failed to get message.");
				e.printStackTrace();
			}
			try {
				this.messageHandler.digestMessage(incomingMessage);
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Failed to digest message.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return
	 */
	public MessageHandler getMessageHandler() {
		return this.messageHandler;
	}

	/**
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(Message message) throws IOException {
		this.objectOutputStream.writeObject(message);
		this.objectOutputStream.flush();
	}

	/**
	 * @return
	 */
	public InetAddress getConnectedAddress() {
		return this.socket.getInetAddress();
	}

	/**
	 * Activates periodic ping flooding.
	 */
	public void initPeriodicPing() {
		this.periodicPingTimer = new Timer();
		Random random = new Random();
		this.periodicPingTimer.schedule(new TimerTask() {
			
			public void run() {
				Message message = new Message(MessageType.Ping, new byte[2]);
				message.createGUID();
				message.setTtl((byte) 5);
				System.out.println("Amount of neighbor peers: " + Communication.getSocketList().size());
				try {
					Communication.rememberID(Arrays.toString(message.getGuid()), InetAddress.getLocalHost());
				} catch (UnknownHostException e1) {
					System.out.println("Failed to remember ping GUID.");
					e1.printStackTrace();
				}
				try {
					sendMessage(message);
					System.out.println("Ping sent:");
					System.out.println(InetAddress.getLocalHost().getHostAddress());
					System.out.println(Arrays.toString(message.getGuid()));
				} catch (IOException e) {
					try {
						Communication.removeSocket(getConnectedAddress());
					} catch (IOException e1) {
						System.out.println("Failed to remove unreachable socket.");
						e1.printStackTrace();
					}
					System.out.println("Broken pipe.");
					close();
					e.printStackTrace();
				}
			}
		}, (long) random.nextInt(8 * 1000) + 8 * 1000, (long) random.nextInt(2 * 1000) + 9 * 1000);
	}

	/**
	 * Closes the connection to the peer.
	 */
	public void close() {
		try {
			socketOpen = false;
			periodicPingTimer.cancel();
			this.socket.getInputStream().close();
			this.socket.getOutputStream().close();
			socket.close();
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			System.out.println("Failed to close socket.");
			e.printStackTrace();
		}
	}
}