import java.io.*;
import java.net.*;
import java.util.Random; // to generate random uuid

// Main (JokeClient) class
public class JokeClient {

	// Assign default server to be primary server
	static int currentServer = 1;

	public static void main(String[] args) {
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

		System.out.println("---------------------------------------------------------");

		// Check if client specified two servers
		if (secondary) {
			System.out.println("Server one: " + serverName + ", Port 4545");
			System.out.println("Server two: " + serverName2 + ", Port 4546");
		}
		else
			System.out.println("Server one: " + serverName + ", Port 4545");
		
		
		int uuid = generateUUID(); // Generate uuid to identify client
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // Initialize input stream to read client's input

		// Get client's for username for use in the servers output response
		System.out.print("\nWhat is your name? ");
		String username = null;

		try {
			username = in.readLine(); // Retrieve client's name
		}
		catch (IOException x) {
			System.out.println("Error: " + x);
		}

		// Wait until the user presses enter then invoke
		// the server for a joke or a proverb
		do {
			String action;
			try {
				System.out.println("\nPress <Enter> to get joke or proverb. Enter \"quit\" to exit.");
				action = in.readLine();

				// Change server if input is "s"
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

				// Check if client wants to get joke or proverb
				else if (action.indexOf("quit") < 0) {
					if (currentServer == 1) // verify what server you are requesting from
						getJokeOrProverb(uuid, serverName, username); // Get joke or proverb  
					else
						getJokeOrProverb(uuid, serverName2, username); // Get joke or proverb 
				} 
					
				else if (action.indexOf("quit") > 0) 
					break; // break loop if requested by client
			}
			catch (IOException x) {
				System.out.println("Error: " + x);
			}	
		} while(true);
		System.out.println("Quitting client per user request.");
	}

	// Method to get or joke or proverb from server
	static void getJokeOrProverb(int uuid, String serverName, String username) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		int port;

		try {
			// Check what server the client is comunnicating with
			if (getCurrentServer() == 1) 
				port = 4545; // assign port to primary server port
			else 
				port = 4546; // assign port to secondary server port

			// Open a socket connection to send messages to the servername at the specified port
			sock = new Socket(serverName, port);

			// Initialize input stream to read response from server
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			// Initialize output stream to send uuid to server
			toServer = new PrintStream(sock.getOutputStream());

			// send uuid to get your next joke or proverb
			toServer.println(uuid + "," + username);
			toServer.flush();
			
			// read and print the joke or proverb from server
			textFromServer = fromServer.readLine();
			System.out.println(textFromServer);

			// read from the server if the user has seen every joke or proverb
			textFromServer = fromServer.readLine();
			if (textFromServer.length() > 0)
				System.out.println("\n"+textFromServer);

			// close the communication socket
			sock.close();
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

	// method to get the current server the client is communicating with
	static public int getCurrentServer() {
		return currentServer;
	}

	// method to set the current server
	static public void setCurrentServer(int server) {
		currentServer = server;
	} 

	// Method to generate UUID to use to identify clients
	static int generateUUID() {
		int min = 1;
		int max = 1000000;
		Random r = new Random();
		int uuid = r.nextInt((max - min) + 1) + min;
		return uuid;
	}

}