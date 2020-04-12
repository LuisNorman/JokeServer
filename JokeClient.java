import java.io.*;
import java.net.*;
import java.util.Random; // to generate random uuid

public class JokeClient {

	public static void main(String[] args) {
		String serverName;

		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];

		System.out.println("---------------------------------------------------------");
		System.out.println("Server one: " + serverName + ", Port 4545");

		int uuid = generateUUID();

		// Initialize input stream to read client's input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("\nWhat is your name? ");
		String username = null;

		// Retrieve client's name
		try {
			username = in.readLine();
		}
		catch (IOException x) {
			System.out.println("Error: " + x);
		}

		// While true, wait until the user presses enter then
		// fire of the server for a joke or a proverb
		do {
			String action;
			try {
				System.out.println("\nPress <Enter> to get joke or proverb. Enter \"quit\" to exit.");
				action = in.readLine();
				if (action.indexOf("quit") < 0) 
					getJokeOrProverb(uuid, serverName, username);
				else
					break;
			}
			catch (IOException x) {
				System.out.println("Error: " + x);
			}	
		} while(true);
		System.out.println("Quitting client per user request.");
	}

	static void getJokeOrProverb(int uuid, String serverName, String username) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;

		try {
			// Open a socket connection to send messages to the servername at the specified port
			sock = new Socket(serverName, 4545);

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

	static int generateUUID() {
		int min = 1;
		int max = 1000000;
		Random r = new Random();
		int uuid = r.nextInt((max - min) + 1) + min;
		return uuid;
	}

}