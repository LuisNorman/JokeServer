import java.io.*;
import java.net.*;

public class JokeClientAdmin {

	static int currentServer = 1; // Assign default server to be primary server
	
	public static void main(String[] args) {
		System.out.println("---------------------------------------------------------------");
		String serverName;
		String serverName2 = null;
		boolean secondary = false;

		// Get serverName if specified otherwise default to localhost
		// Check if client did not specify any server
		if (args.length < 1) 
			serverName = "localhost";
		// Check if client specified server one
		else if (args.length == 1)
			serverName = args[0];
		// Check if client specified server one and two
		else {
			serverName = args[0];
			serverName2 = args[1];
			secondary = true;

		}

		// Check if client specified two servers
		if (secondary) {
			System.out.println("Server one: " + serverName + ", Port 5050");
			System.out.println("Server two: " + serverName2 + ", Port 5051");
		}
		else
			System.out.println("Server one: " + serverName + ", Port 5050");


		// Initialize input stream to read client's input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// Wait until the client's admin presses <Enter> then
		// invoke the server to change the mode
		do {
			String action;
			try {
				System.out.println("\nPress <Enter> to change mode. Enter \"quit\" to exit.");
				action = in.readLine();
				if (secondary && action.equals("s")){
					if (currentServer == 1) {
						changeServer(); // change server on user request (user enters "s")
						System.out.println("\nNow communicating with: "+serverName2 +", 4546");
					}
					else {
						changeServer();
						System.out.println("\nNow communicating with: "+serverName +", port 4545");
					}
				}
				else if (action.indexOf("quit") > 0) 
					break; // break loop if requested by client
				else {
					if (getCurrentServer() == 1) 
						changeMode(serverName); // Change the mode on primary server
					else
						changeMode(serverName2); // Change the mode on secondary server
				}
			}
			catch (IOException x) {
				System.out.println("Error: "+x);
			}	
		} while(true);
		System.out.println("Quitting client per user request.");

	}

	// Method to change the current's server mode from
	// joke to proverb or from proverb to joke
	private static void changeMode(String serverName) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		int port;


		try {
			// Check what server the client is comunnicating with
			if (getCurrentServer() == 1) {
				port = 5050; // assign port to primary server port
			}
			else 
				port = 5051; // assign port to secondary server port

			sock = new Socket(serverName, port); // Open a socket connection to send messages to the servername at the specified port
			
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); // Initialize input stream to read response from server

			// Initialize output stream to send uuid to server
			toServer = new PrintStream(sock.getOutputStream());

			// send uuid to get your next joke or proverb
			toServer.println("");
			toServer.flush();

			// read and print the joke or proverb from server
			textFromServer = fromServer.readLine();
			System.out.println(textFromServer);

			sock.close(); // close the communication socket
		}
		catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}

	// Method to change from server one to server two
	// or change from server two to server one
	static void changeServer() {
		if (currentServer == 1) 
			setCurrentServer(2);
		else 
			setCurrentServer(1);
	}

	static public int getCurrentServer() {
		return currentServer;
	}

	static public void setCurrentServer(int server) {
		currentServer = server;
	} 

}