import java.io.*;
import java.net.*;

public class JokeClientAdmin {
	
	public static void main(String[] args) {
		System.out.println("---------------------------------------------------------------");
		String serverName;
		// Set the servername (the server where clients sends msgs to) 
		// to localhost if not specified
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];

		// System.out.println("Luis Norman's Joke Client Admin is starting.\n");
		System.out.println("Server one: " + serverName + ", Port 5050");

		// Initialize input stream to read client's input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// While true, wait until the user presses enter then
		// fire of the server for a joke or a proverb
		do {
			String action;
			try {
				System.out.println("\nPress <Enter> to change mode. Enter \"quit\" to exit.");
				action = in.readLine();
				if (action.indexOf("quit") < 0) 
					changeMode(serverName);
				else 
					break;
			}
			catch (IOException x) {
				System.out.println("Error: "+x);
			}	
		} while(true);
		System.out.println("Quitting client per user request.");

	}

	private static void changeMode(String serverName) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;

		try {
			// Open a socket connection to send messages to the servername at the specified port
			sock = new Socket(serverName, 5050);

			// Initialize input stream to read response from server
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			// Initialize output stream to send uuid to server
			toServer = new PrintStream(sock.getOutputStream());

			// send uuid to get your next joke or proverb
			toServer.println("");
			toServer.flush();

			// read and print the joke or proverb from server
			textFromServer = fromServer.readLine();
			System.out.println(textFromServer);

			// close the communication socket
			sock.close();
		}
		catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}

}