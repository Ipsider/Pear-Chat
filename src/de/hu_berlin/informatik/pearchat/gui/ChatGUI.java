package de.hu_berlin.informatik.pearchat.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import de.hu_berlin.informatik.pearchat.communication.Communication;
import de.hu_berlin.informatik.pearchat.communication.MessageSaver;

/**
 * The GUI for our simple p2p chat application. Initializes the swing components
 * and starts a new peer node to join the network. Entry nodes to the network
 * can be provided by a cache, in the default case, the IP has to be typed in.
 * 
 * @author Philipp Seiter
 * @author Daniel Titz
 *
 */
@SuppressWarnings("serial")
public class ChatGUI extends JFrame {

	private static final int HEIGHT = 400;
	private static final int WIDTH = 500;
	private static final String TITLE = "P2P Chat";

	private Communication communication;
	private MessageSaver messageSaver;
	
	private JPanel contentPane;
	private JPanel connectingPane;
	private JPanel sendingPane;
	private JLabel iPLabel;
	private JTextField iPText;
	private JButton connectButton;
	private JTextArea messageHistory;
	private static JScrollPane scroll;
	private JTextField messageArea;
	private JButton sendButton;
	private String userName;
	private Timer timer;

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				ChatGUI gui;
				try {
					gui = new ChatGUI();
					gui.setVisible(true);
				} catch (IOException e) {
					System.out.println("GUI failed to initialize.");
					e.printStackTrace();
				}
			}
		});
		System.out.println("GUI initialized...");
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 */
	public ChatGUI() throws IOException {
		communication = null;
		messageSaver = new MessageSaver();
		initialize();
	}

	/**
	 * Initialize the frame.
	 * 
	 * @throws IOException
	 */
	public void initialize() throws IOException {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(WIDTH, HEIGHT);
		this.setTitle(TITLE);
		this.setLocationRelativeTo(null);

		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		sendingPane = new JPanel();
		sendingPane.setLayout(new FlowLayout());
		connectingPane = new JPanel();
		connectingPane.setLayout(new FlowLayout());

		iPLabel = new JLabel("IP: ");
		iPText = new JTextField();
		iPText.setColumns(10);
		iPText.addActionListener(new ConnectListener());
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ConnectListener());
		messageHistory = new JTextArea();
		messageHistory.setEditable(false);
		scroll = new JScrollPane(messageHistory);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		messageArea = new JTextField(20);
		messageArea.addActionListener(null);
		messageArea.requestFocus();
		sendButton = new JButton("Send");
		sendButton.addActionListener(null);

		contentPane.add(connectingPane, "North");
		contentPane.add(scroll, "Center");
		contentPane.add(sendingPane, "South");
		connectingPane.add(iPLabel);
		connectingPane.add(iPText);
		connectingPane.add(connectButton);
		sendingPane.add(messageArea);
		sendingPane.add(sendButton);

		userName = JOptionPane.showInputDialog("Please enter a user name:");
		this.setContentPane(contentPane);
		startUpdateTimer();
	}

	/**
	 * Adds the listener to the appropriate buttons and fields.
	 */
	public void lateInitialize() {
		messageArea.addActionListener(new SendMessageListener());
		sendButton.addActionListener(new SendMessageListener());
	}

	/**
	 * Updates the GUI to keep the message history up to date.
	 */
	public void startUpdateTimer() {
		timer = new Timer(100, new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				FileReader fileReader;
				BufferedReader bufferedReader;
				try {
					PrintWriter printWriter = new PrintWriter(
							new BufferedWriter(new FileWriter("persMsgHistory.txt", true)));
					fileReader = new FileReader("persMsgHistory.txt");
					bufferedReader = new BufferedReader(fileReader);
					messageHistory.read(bufferedReader, null);
					printWriter.close();
				} catch (FileNotFoundException e1) {
					System.out.println("Failed to update messages file.");
					e1.printStackTrace();
				} catch (IOException e1) {
					System.out.println("Failed to get messages.");
					e1.printStackTrace();
				}
			}
		});
		timer.start();
	}

	/**
	 * The listener for the connect process. The user can enter a known IP
	 * address to enter the network.
	 * 
	 * @author Philipp Seiter
	 * @author Daniel Titz
	 *
	 */
	class ConnectListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				@Override
				public Void doInBackground() throws IOException {
					String firstIP = iPText.getText();
					ArrayList<InetAddress> GWebCache = new ArrayList<InetAddress>();
					if (!firstIP.isEmpty()) {
						try {
							GWebCache.add(InetAddress.getByName(firstIP));
						} catch (UnknownHostException e1) {
							System.out.println("Failed to add IP to GWebCache.");
							e1.printStackTrace();
						}
					}
					Thread CommunicationThread;
					System.out.println("Servent started...");
					communication = new Communication(GWebCache);
					CommunicationThread = new Thread(communication);
					CommunicationThread.start();
					lateInitialize();
					return null;
				}
			};
			worker.execute();
		}
	}

	/**
	 * The listener for sending a message.
	 * 
	 * @author Philipp Seiter
	 * @author Daniel Titz
	 *
	 */
	class SendMessageListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				@SuppressWarnings("static-access")
				@Override
				public Void doInBackground() throws IOException {
					
					String thisMessage = messageArea.getText();
					messageSaver.saveMessage("Me", thisMessage);
					communication.sendChat(userName, thisMessage);
					scrollDown();
					return null;
				}

				@Override
				public void done() {
					System.out.println("Message sent");
					messageArea.setText("");
					messageArea.requestFocus();
				}
			};
			worker.execute();
		}
	}

	/**
	 * Keeps the focus on the newest message.
	 */
	public static void scrollDown() {
		JScrollBar sb = ChatGUI.scroll.getVerticalScrollBar();
		sb.setValue(sb.getMaximum());
	}
}