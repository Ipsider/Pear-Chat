package de.hu_berlin.informatik.pearchat.communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hu_berlin.informatik.pearchat.messages.Chat;
import de.hu_berlin.informatik.pearchat.messages.Message;
import de.hu_berlin.informatik.pearchat.messages.Message.MessageType;

/**
 * The peer node, which acts as server and client in the p2p network.
 * 
 * @author Philipp Seiter
 * @author Daniel Titz
 *
 */
public class Communication implements Runnable {
	
	private static final int PORT = 22222;
	private static final int MAX_PEERS = 5;
	
	private ArrayList<InetAddress> gWebCache;
	private ServerSocket serverSocket;
	private static ExecutorService threadPool;
	private boolean isRunning;
	
	private static Hashtable<InetAddress, SocketHandler> socketList;
	private static Hashtable<String, InetAddress> messageIDToAddress;
	private static Hashtable<byte[], InetAddress> incomingPingToAddress;
	private static ArrayList<String> receivedChats;

	/**
	 * @param gWebCache
	 * @throws IOException
	 */
	public Communication(ArrayList<InetAddress> gWebCache) throws IOException {
		this.gWebCache = gWebCache;
		this.isRunning = false;
		
		// Cached Thread Pool : A thread pool that create as many threads it
		// needs to execute the task in parallel. The old available threads will 
		// be reused for the new tasks. If a thread is not used during 60 seconds,
		// it will be terminated and removed from the pool.
		Communication.threadPool = Executors.newCachedThreadPool();
		Communication.socketList = new Hashtable<InetAddress, SocketHandler>();
		Communication.messageIDToAddress = new Hashtable<String, InetAddress>();
		Communication.receivedChats = new ArrayList<String>();
	}

	@Override
	public void run() {
		try {
			this.serverSocket = new ServerSocket(Communication.PORT);
		} catch (IOException e) {
			System.out.println("Failed to create server socket.");
			e.printStackTrace();
		}
		this.isRunning = true;
		
		initGWebCache();
		
		while (this.isRunning) {
			SocketHandler connectionHandler = null;
			try {
				Socket socket = this.serverSocket.accept();
				System.out.println("Socket created.");
				connectionHandler = new SocketHandler(socket);
				Communication.socketList.put(socket.getInetAddress(), connectionHandler);
			} catch (IOException e) {
				System.out.println("Failed to create socket.");
				e.printStackTrace();
			}
			Communication.threadPool.execute(connectionHandler);
			System.out.println("Connected to: " + socketList.keys().nextElement().getHostAddress());
		}
	}

	/**
	 * @return socketList
	 */
	public static Hashtable<InetAddress, SocketHandler> getSocketList() {
		return socketList;
	}

	/**
	 * @return messageIDToAddress
	 */
	public static Hashtable<String, InetAddress> getMessageIDToAddress() {
		return messageIDToAddress;
	}

	public static void rememberID(String guid, InetAddress inetAddress) {
		messageIDToAddress.put(guid, inetAddress);
	}

	/**
	 * @return incomingPingToAddress
	 */
	public static Hashtable<byte[], InetAddress> getIncomingPingToAddress() {
		return incomingPingToAddress;
	}

	/**
	 * @return MAX_PEERS
	 */
	public static int getMaxPeers() {
		return MAX_PEERS;
	}
	
	/**
	 * @return receivedChats
	 */
	public static ArrayList<String> getReceivedChats() {
		return receivedChats;
	}
	
	public static void addReceivedChat(Message message) {
		Communication.receivedChats.add(Arrays.toString(message.getGuid()));
	}

	/**
	 * @return
	 */
	public static boolean isFull() {
		return Communication.getSocketList().size() >= Communication.getMaxPeers();
	}

	/**
	 * Adds a socket to the socket list. Maintains the connections to the
	 * neighbors.
	 * 
	 * @param inetAddress
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static void addSocket(InetAddress inetAddress) throws UnknownHostException, IOException {
		if (!Communication.getSocketList().containsKey(inetAddress)) {
			InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, Communication.PORT);

			System.out.println("Connection try to " + inetAddress);
			SocketHandler thisConnectionHandler = null;
			try {
				thisConnectionHandler = new SocketHandler(inetSocketAddress);
				Communication.socketList.put(inetSocketAddress.getAddress(), thisConnectionHandler);
			} catch (IOException e) {
				System.out.println("Failed to create socketHandler.");
				e.printStackTrace();
			}
			Communication.threadPool.execute(thisConnectionHandler);
		}
	}

	/**
	 * Removes a socket from the socket list. If a ping send process fails to
	 * connect to the given socket, the socket is deleted from the list.
	 * 
	 * @param inetAddress
	 * @throws IOException
	 */
	public static void removeSocket(InetAddress inetAddress) throws IOException {
		Communication.socketList.remove(inetAddress);
	}

	/**
	 * Gets called when the user sends a text message to the network.
	 * 
	 * @param username
	 * @param text
	 * @throws IOException
	 */
	public void sendChat(String username, String text) throws IOException {
		Chat chat = new Chat(username, text);
		Iterator<SocketHandler> socketListIterator = Communication.socketList.values().iterator();
		Message message = new Message(MessageType.Chat, MessageHandler.serialize(chat));
		message.createGUID();
		Communication.addReceivedChat(message);
		for (Iterator<SocketHandler> iter = socketListIterator; iter.hasNext();) {
			SocketHandler tmpSocketHandler = (SocketHandler) iter.next();
			message.setTtl((byte) 2);
			tmpSocketHandler.sendMessage(message);
			System.out.println("Message sent to " + tmpSocketHandler.getConnectedAddress().toString());
		}
	}

	/**
	 * @throws IOException
	 */
	public void stop() throws IOException {
		this.isRunning = false;
		this.serverSocket.close();
	}

	/**
	 * Initializes the GWebCache with the known entry nodes of the network.
	 */
	public void initGWebCache() {
		System.out.println("Initial connections:");
		for (byte i = 0; i < this.gWebCache.size(); ++i) {
			InetAddress thisInetAdress = this.gWebCache.get(i);
			System.out.println("Connection try #" + String.valueOf(i) + " to " + thisInetAdress);
			InetSocketAddress thisInetSocketAddress = new InetSocketAddress(thisInetAdress, PORT);
			SocketHandler thisConnectionHandler = null;
			try {
				thisConnectionHandler = new SocketHandler(thisInetSocketAddress);
				Communication.socketList.put(thisInetSocketAddress.getAddress(), thisConnectionHandler);
			} catch (IOException e) {
				System.out.println("Failed to create socketHandler.");
				e.printStackTrace();
			}
			Communication.threadPool.execute(thisConnectionHandler);
		}
	}
}