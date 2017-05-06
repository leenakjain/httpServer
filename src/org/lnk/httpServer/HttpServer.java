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
		//System.out.println("Listening for connection on port 9900 ....");
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

			if (!file.exists()) {
				status = protocol + " 404 Not Found\r\n";
				file = new File("./404.html");
				System.out.println(" file not found");
				fileContent = Files.readAllBytes(file.toPath());
			} else {
				if (file.isDirectory()) {
					// List<String> results = new ArrayList<String>();
					StringBuffer results = new StringBuffer("");
					results.append("<table>").append("<tr><td>File Name</td><td>Last Modified</td><td>Size</td></a>");
					File[] files = file.listFiles();
					for (File subfile : files) {
						// if (subfile.isFile()) {
						results.append("<tr><td><a href=").append(subfile.getPath()).append(">")
								.append(subfile.getName()).append("</a></td>").append("<td>")
								.append(LocalDateTime.ofInstant(Instant.ofEpochMilli(subfile.lastModified()),
										ZoneId.systemDefault()))
								.append("</td><td>").append(subfile.length()).append("</td>");
					}
					results.append("</table>");
					fileContent = results.toString().getBytes();
				} else {

					fileContent = Files.readAllBytes(file.toPath());
				}
			}
			// Pass the output to the client browser
			displayContent(clientSocket, status, fileContent);

		}
	}

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
