package org.lnk.httpServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
 * This Program creates a simple web server
 * It receives a get request for a path and displays the content of that path
 * IF path is a directory, it displays the content of direcorty
 * If path is a file name, it displays the content of the file 
 *
 * @Author   Leena Jain
 * @version  1.0 
 * 
 */
public class HttpServer {

	private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
	private static FileHandler fileHandler = null;

	public static void init() {
		try {
			fileHandler = new FileHandler("serverLog.log", false);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		Logger logger = Logger.getLogger("");
		fileHandler.setFormatter(new SimpleFormatter());
		logger.addHandler(fileHandler);
		logger.setLevel(Level.CONFIG);
	}

	public static void main(String[] args) throws IOException {

		HttpServer.init();
		logger.log(Level.INFO, "log initialized");

		ServerSocket server = new ServerSocket(9900);
		logger.log(Level.INFO, "Listening for connection on port 9900 ....");

		while (true) {
			Socket clientSocket = server.accept();
			InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream());
			BufferedReader reader = new BufferedReader(isr);
			String line = reader.readLine();
			// Split the string to get the details
			String[] userReq = line.split(" ");
			// request type
			String method = userReq[0];
			// requested resource path
			String filePath = userReq[1];
			// request protocol
			String protocol = userReq[2];

			// open the file path
			File file = new File("./" + filePath);

			// If the file does not exist then we open the error file, and
			// change the status to 404

			String status = protocol + " 200 OK\r\n";

			byte[] fileContent;
			// check if the path provided by user exists
			if (!file.exists()) {
				status = protocol + " 404 Not Found\r\n";
				file = new File("./404.html");
				logger.log(Level.SEVERE, "file not found");
				System.out.println(file.toPath());
				fileContent = Files.readAllBytes(file.toPath());
			} else {
				// if the path is a directory then display the directory
				// contents
				if (file.isDirectory()) {

					fileContent = directoryContent(file);
					logger.log(Level.INFO, "DIR  found" + file.toPath());

				} else

				{
					// else if the path is a file , then display the file
					// content
					fileContent = Files.readAllBytes(file.toPath());
					logger.log(Level.INFO, "file  found" + file.toPath());
				}
			}
			// Pass the output to the client browser
			displayContent(clientSocket, status, fileContent);
		}
	}

	/*
	 * This returns the content of the directory in a byte array.
	 */
	private static byte[] directoryContent(File file) {
		
		StringBuffer results = new StringBuffer("");
		
		//create a table to format the contents
		results.append("<table>").append("<tr><td>File Name</td><td>Last Modified</td><td>Size</td></a>");
		
		File[] files = file.listFiles();
		
		for (File subfile : files) {
			results.append("<tr><td><a href=").append(subfile.getPath()).append(">").append(subfile.getName())
					.append("</a></td>").append("<td>").append(LocalDateTime
							.ofInstant(Instant.ofEpochMilli(subfile.lastModified()), ZoneId.systemDefault()))
					.append("</td><td>").append(subfile.length()).append("</td>");
		}
		
		results.append("</table>");

		byte[] contents = results.toString().getBytes();

		return contents;
	}

	/*
	 * This is responsible to create the response for the user
	 */
	private static void displayContent(Socket clientSocket, String status, byte[] fileContent) throws IOException {
		LocalDateTime localDateTime = LocalDateTime.now();
		// Standard HTTP response header
		String header = status + "Location: http://localhost:9900/\r\n" + "Date: " + localDateTime + "\r\n"
				+ "Server: MeuServidor/1.0\r\n" + "Content-Type: text/html\r\n" + "Content-Length: "
				+ fileContent.length + "\r\n" + "Connection: close\r\n" + "\r\n";
		// Creates response channel using outputStream
		OutputStream response = clientSocket.getOutputStream();
		// Writes the headers in bytes
		response.write(header.getBytes());
		// Write the content in bytes
		response.write(fileContent);
		// display the content
		response.flush();
	}

}
