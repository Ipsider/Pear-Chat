package de.hu_berlin.informatik.pearchat.communication;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Stores sent messages into a file, to present the chat history on GUI.
 * 
 * @author Philipp Seiter
 * @author Daniel Titz
 *
 */
public class MessageSaver {

	private static PrintWriter printWriter;
	private static Calendar calender;
	private static SimpleDateFormat simpleDateFormat;

	/**
	 * The constructor.
	 */
	public MessageSaver() {
		calender = Calendar.getInstance();
		simpleDateFormat = new SimpleDateFormat("[HH:mm] ");
	}

	/**
	 * @return calender
	 */
	public Calendar getCalender() {
		return calender;
	}

	/**
	 * @return simpleDateFormat
	 */
	public SimpleDateFormat getSimpleDateFormat() {
		return simpleDateFormat;
	}

	/**
	 * @return printWriter
	 */
	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	/**
	 * @param message
	 * @throws IOException
	 */
	public static void saveMessage(String message) throws IOException {
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter("persMsgHistory.txt", true)));
		printWriter.write(" " + simpleDateFormat.format(calender.getTime()) + "Me: " + message + "\n");
		printWriter.close();
	}

	/**
	 * @param user
	 * @param content
	 * @throws IOException
	 */
	public static void saveMessage(String user, String content) throws IOException {
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter("persMsgHistory.txt", true)));
		printWriter.write(" " + simpleDateFormat.format(calender.getTime()) + user + ": " + content + "\n");
		printWriter.close();
	}
}