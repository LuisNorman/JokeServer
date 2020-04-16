/*--------------------------------------------------------

1. Name: Luis Norman / Date: April 16th, 2020

2. Java version used: 1.8, if not the official version for the class:

e.g. build 1.8.0_242-8u242

3. Precise command-line compilation examples / instructions:

> javac JokeClientAdmin.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

or to run optional secondary server
> java JokeServer secondary
> java JokeClient <IPAddr> <IPAddr>
> java JokeClientAdmin secondary

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

If there are two servers and one is running at
140.192.1.22 and the other is running at 140.192.1.45  
then you would type:

> java JokeClient 140.192.1.22 140.192.1.45  
> java JokeClientAdmin 140.192.1.22 140.192.1.45  

5. List of files needed for running the program.

 a. Luis Norman's Joke Server Checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

----------------------------------------------------------*/

import java.io.*; // java's input/output library
import java.net.*; // java's networking library 

public class JokeClientAdmin {

	static int currentServer = 1; // Assign default server to be primary server
	
	public static void main(String[] args) {
		System.out.println("---------------------------------------------------------------");

		String serverName; // Data structure to store server one name
		String serverName2 = null; // Data structure to store server two name
		boolean secondary = false; // Initialize the secondary server flag to false

		// Get serverName if specified otherwise default to localhost
		// Check if client did not specify any server
		if (args.length < 1) 
			serverName = "localhost";
		// Check if client specified server one
		else if (args.length == 1)
			serverName = args[0];
		// Check if client specified server one and two
		else {
			serverName = args[0]; // Get the arg for server one
			serverName2 = args[1]; // Get the arg for server two 
			secondary = true; // Set the secondary server flag to true

		}

		// Check if client specified two servers
		if (secondary) {
			System.out.println("Server one: " + serverName + ", Port 5050"); // Print out info for server one
			System.out.println("Server two: " + serverName2 + ", Port 5051"); // Print out info for server two
		}
		else
			System.out.println("Server one: " + serverName + ", Port 5050"); // Print out info for server one


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
					if (currentServer == 1) { // If switching from primary server to secondary server
						changeServer(); // Switch from primary server to secondary one
						System.out.println("\nNow communicating with: "+serverName2 +", 4546");
					}
					else { // If on secondary server
						changeServer();  // Switch from secondary server to primary one
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
		Socket sock; // Socket created to communicate with server
		BufferedReader fromServer; // Input stream to read from server
		PrintStream toServer; // Output stream to send to server
		String textFromServer; // Data structure to store the server's response
		int port; // Port to communicate with server on


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

	// Method to get the current server
	static public int getCurrentServer() {
		return currentServer;
	}

	// Method to set the current server
	static public void setCurrentServer(int server) {
		currentServer = server;
	} 

}