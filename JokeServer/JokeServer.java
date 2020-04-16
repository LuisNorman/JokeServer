/*--------------------------------------------------------

1. Name: Luis Norman / Date: April 16th, 2020

2. Java version used: 1.8, if not the official version for the class:

e.g. build 1.8.0_242-8u242

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java

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

import java.io.*; // import java's input/output library
import java.net.*; // import java's networking library 
import java.util.HashMap; // import the HashMap class from java's util library
import java.util.Random; // import the Random Class from java's util library

// UserMap (HashMap) to store all clients uuids as key and a 
// hashmap as the value that stores "joke" or "proverb"
// as the key and the client's current index position as the value
class UserMap extends HashMap {

	HashMap<Integer, HashMap<String, Integer>> userMap;

	// Initialize user map
	public UserMap() {
		userMap = new HashMap<Integer, HashMap<String, Integer>>();
	}

	// Method to increment the index position to point to the next joke or proverb
	@SuppressWarnings("unchecked")
	public void incrementIndex(int uuid, boolean secondary) {
		HashMap<String, Integer> indexMap = (HashMap<String, Integer>)this.get(uuid); // Get the client's user index map
		int index; // Data structure to store current index
		int newIndex; // Data structure to store next index

		// Determine if secondary server is not the requested server 
		if (!secondary) {
			// Based on the primary server's current mode, retireve the client's 
			// current positional index, increment it, and store it back to usermap
			if (Mode.getMode1() == "jokes") {
				index = indexMap.get("jokes");
				if (index == 3) 
					newIndex = 0; // If index equals three, restart index to the beginning
				else
					newIndex = index + 1; // Otherwise, just increment it by 1
				indexMap.put("jokes", newIndex); // store the new index into the index map
			}
			else {
				index = indexMap.get("proverbs");
				if (index == 3) 
					newIndex = 0; // If index equals three, restart index to the beginning
				else 
					newIndex = index + 1; // Otherwise, just increment it by 1
				indexMap.put("proverbs", newIndex); // store the new index into the index map
			}
		}
		// Else, secondary server is requested server
		else {
			// Based on the secondary server's current mode, retireve the client's 
			// current positional index, increment it, and store it back to usermap
			if (Mode.getMode2() == "jokes") {
				index = indexMap.get("jokes");
				if (index == 3) 
					newIndex = 0; // If index equals three, restart index to the beginning
				else
					newIndex = index + 1; // Otherwise, just increment it by 1
				indexMap.put("jokes", newIndex); // store the new index into the index map
			}
			else {
				index = indexMap.get("proverbs");
				if (index == 3) 
					newIndex = 0; // If index equals three, restart index to the beginning
				else
					newIndex = index + 1; // Otherwise, just increment it by 1
				indexMap.put("proverbs", newIndex); // store the new index into the index map
			}
		}
		
		this.put(uuid, indexMap); // Update the current client's indexMap
	}

	// Method to return the current joke or proverb index and 
	// store it if is the client's first visit to server
	@SuppressWarnings("unchecked")
	public int getIndex(int uuid, boolean secondary) {
		HashMap<String, Integer> indexMap; // HashMap to store the current client's index map

		// Check if usermap contains the current client's uuid.
		// In other words, check if the  client has visited the server before.
		// If so, get the index map that stores the joke and proverb index
		if (((HashMap<Integer, HashMap>)this).containsKey(uuid)) {
			indexMap = (HashMap<String, Integer>)this.get(uuid);
		}
		// If not, create an index map for the NEW client
		// with and index of 0 and store it in usermap.
		else {
			indexMap = new HashMap<String, Integer>();
			indexMap.put("proverbs", 0);
			indexMap.put("jokes", 0);
			this.put(uuid, indexMap);
		}

		int index;

		// Based on the current mode, retrieve the index from the index map and return it.
		if (!secondary) {
			if (Mode.getMode1() == "jokes") {
				index = indexMap.get("jokes");
			}
			else {
				index = indexMap.get("proverbs");
			}
		}
		else {
			if (Mode.getMode2() == "jokes") {
				index = indexMap.get("jokes");
			}
			else {
				index = indexMap.get("proverbs");
			}
		}
		
		return index;
	}
}

// Client's worker thread to execute the clients request
class Worker extends Thread {
	Socket sock; // Socket created to communicate with client
	static UserMap userMap; // UserMap object to find client's current joke or proverb   
	boolean secondary; // flag to determine if client is communicating with the secondary server

	Worker(Socket sock, UserMap userMap, boolean secondary) {
		this.sock = sock;
		this.userMap = userMap;
		this.secondary = secondary;
	}

	// Execute worker task which is to send joke or proverb back to client
	public void run() {
		
		PrintStream out = null; // Create output stream to send message to client
		BufferedReader in = null; // Create input stream to read message from the client

		try {
			
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // Set up the input stream to read the uuid from the client
			out = new PrintStream(sock.getOutputStream()); // Set up the output stream to send to the socket 

			try {
				String incoming = in.readLine(); // read clients message (cookie) 
				String[] msg = incoming.split(","); // Parse client's message to get uuid, and username
				int uuid = Integer.valueOf(msg[0]); // Extract uuid from client's message arr 
				String username = msg[1]; // Extract username from client's message arr
				sendJokeorProverb(out, username, uuid, secondary); // Call method to send joke or proverb to cleint
			}
			catch (IOException ioe) {
				System.out.println("Error: " + ioe);
			}
		}
		catch (IOException ioe) {
			System.out.println("Error: "+ioe);
		}
	}

	// Method to send joke or proverb
	@SuppressWarnings("unchecked") // Suppressing the casting warning
	static void sendJokeorProverb(PrintStream out, String username, int uuid, boolean secondary) {
		String mode; // data structure to store current mode

		// Get current mode for the respective system
		if (!secondary) 
			mode = Mode.getMode1();
		else
			mode = Mode.getMode2();

		
		int index = userMap.getIndex(uuid, secondary); // Get users current index map for jokes and proverbs
		userMap.incrementIndex(uuid, secondary); // After recieving the index, increment it

		String[] jokes = null; // Data structure to store the client's jokes
		String[] proverbs = null; // Data structure to store the client's proverbs

		// If at beginning of jokes or proverbs, randomize them 
		// and retrieve the new randomized jokes or provebs
		if (index == 0) {
			if (mode == "jokes")
				jokes = Jokes.getRandomizedJokes(uuid);
			else
				proverbs = Proverbs.getRandomizedProverbs(uuid);
		}
		// Else, get the previous stored jokes or proverbs, dependending on mode
		else {
			if (mode == "jokes")
				jokes = Jokes.getOldJokes(uuid);
			else
				proverbs = Proverbs.getOldProverbs(uuid);
		}

		String message; // Data structure to store the output message sent to the client
		String prefix = null; // Data structure to store the message prefix (i.e. JA JB PA PB)
		
		// Determine the prefix based on the index and then 
		// create the output message to send to the client
		if (mode == "jokes") {
			switch (index) {
				case 0 : prefix = "JA"; break;
				case 1 : prefix = "JB"; break;
				case 2 : prefix = "JC"; break;
				case 3 : prefix = "JD"; break;
			}
			message = prefix + " " + username + ": " + jokes[index];
		}
		else {
			switch (index) {
				case 0 : prefix = "PA"; break;
				case 1 : prefix = "PB"; break;
				case 2 : prefix = "PC"; break;
				case 3 : prefix = "PD"; break;
			}
			message = prefix + " " + username + ": " + proverbs[index];
		}
		
		if (secondary) 
			message = "<S2> " + message; // Prepend <S2> if sending message to secondary server
		
		out.println(message); // Send joke or proverb to client

		// Check if server has cycled through all 
		// jokes or proverbs and send msg if so
		if (index == 3) {
			if (mode == "jokes")
				out.println("----------------------JOKE CYCLE ENDED----------------------"); // Print joke cycle ended
			else 
				out.println("----------------------PROVERB CYCLE ENDED----------------------"); // Print proverb cycle
		}
		else
			out.println(""); // Print empty string to avoid client waiting for next input
		
	}

}

// Mode class to store the current mode (joke or proverb)
class Mode {

	static String mode1 = "jokes"; // Initial mode is jokes for primary server
	static String mode2 = "jokes"; // Initial mode is jokes for secondary server
	

	// Add the ability for admin to change mode
	public void changeMode(PrintStream out, boolean secondary){
		// Check if request is for primary or secondary server
		if (!secondary) {
			if (mode1 == "proverb") {
				mode1 = "jokes";
				out.println("Changed mode for primary server's from proverb to jokes");
			}
			else {
				mode1 = "proverb";
				out.println("Changed mode for primary server's from jokes to proverbs");
			}
		}
		else {
			if (mode2 == "proverb") {
				mode2 = "jokes";
				out.println("Changed mode for secondary server's from proverb to jokes");
			}
			else {
				mode2 = "proverb";
				out.println("Changed mode for secondary server's from jokes to proverbs");
			}
		}

	}

	// Method to get the primary server's mode
	public static String getMode1() {
		return mode1;
	}

	// Method to get the secondary server's mode
	public static String getMode2() {
		return mode2;
	}
}

// Admin worker thread to execute the client admin requests
class AdminWorker extends Thread {
	Socket sock; // Socket for communication
	Mode mode; // Current mode (joke or proverb)
	Boolean secondary; // Flag to determine if secondary admin server is on

	// Constructor for AdminWorker (Passing need parameters)
	AdminWorker(Socket sock, Mode mode, boolean secondary) {
		this.sock = sock;
		this.mode = mode;
		this.secondary = secondary;
	}

	public void run() {
		
		BufferedReader in = null; // Create input stream to read message from the client
		PrintStream out = null; // Create output stream to send message to client

		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // Set up the input stream to read from the socket
			out = new PrintStream(sock.getOutputStream()); // Set up the output stream to send to the socket 
			in.readLine(); // read user input
			mode.changeMode(out, secondary); // change the mode
		}
		catch(IOException ioe) {
			System.out.println("Error: " + ioe);
		}
	}
}

// AdminLooper to listen and connect to client admin when requested
class AdminLooper implements Runnable {
	Mode mode; 
	int port;
	boolean secondary = false;

	// Constructor for AdminLooper (Passing needed parameters)
	AdminLooper(Mode mode, int port) {
		this.mode = mode;
		this.port = port;
		if (port == 5051) // Determine if secondary admin server should be turned on
			secondary = true;
	}


	public void run() {
		int q_len = 6; 
		Socket sock; 
		boolean adminControlSwitch = true;

		try {
			ServerSocket servsock = new ServerSocket(port, q_len); // Initialize a serversocket to listen for client admin connections
			while(adminControlSwitch) {
				sock = servsock.accept(); // When a connection request is sent, accept it
				new AdminWorker(sock, mode, secondary).start(); // Run adminworker to execute the task
			}
		}
		catch(IOException ioe) {
			System.out.println("Error: " + ioe);
		}
	}
}

// Class to initiate a new worker thread for secondary server
class SecondaryServerLooper implements Runnable {
	UserMap userMap; // User map to lookup client's current joke or proverb index

	SecondaryServerLooper(UserMap userMap) {
		this.userMap = userMap;
	}

	public void run() {
		int q_len = 6; // Max simultaneous connection is 6 
		int port = 4546; // Secondary server listens on port 4546
		Socket sock;

		try {
			ServerSocket servsock = new ServerSocket(port, q_len); // Initialize a serversocket to listen for client admin connections
			while(true) {
				sock = servsock.accept(); // When a connection request is sent, accept it
				new Worker(sock, userMap, true).start(); // Run adminworker to execute the task
			}
		}
		catch(IOException ioe) {
			System.out.println("Error: " + ioe);
		}
	}
}

class Jokes {

	static HashMap<Integer, String[]> userJokes = new HashMap<>(); // HashMap to store the users joke set
	
	// Method to randomize the jokes
	public static String[] getRandomizedJokes(int uuid) {
		String[] jokes = new String[]{"My wife and I decied we don't want to have children. We will be telling them tonight.", "I found a pen that writes underwater. It writes other words too.", "Why do you never see hippos hiding in trees. Because they are very good at it.", "To the person who stole my copy of Microsoft Office, I will find you. You have my word."};
		String[] newJokeSet = new String[4];
		int size = jokes.length;


		// Loop through jokes array, randomly select jokes,
		// and add the new proverbs set. 
		for (int i = 0; i<size; i++) {
			int min = 0;
			int max = 3-i; // max equals the size of the jokes array that dynamically changes 
			Random r = new Random(); // Instantiate a random object
			int index = r.nextInt((max - min) + 1) + min; // get random index
			newJokeSet[i] = jokes[index]; // store the random joke[index] into the new joke set
			
			// Set up variables needed to remove recently stored jokes
			int size2 = jokes.length;
			int pointer = 0; // pointer to step through temp proverbs array
			String[] tempJokes = new String[jokes.length-1]; // array to store jokes that haven't been selected yet

			// Loop through to remove recently stored joke to avoid duplication
			for (int j=0; j<size2; j++) {
				if (jokes[j] != jokes[index]) {
					tempJokes[pointer] = jokes[j];
					pointer++;
				}
			}
			jokes = tempJokes; // rewrite the jokes array (with recently added joke removed)
		}

		storeJokes(uuid, newJokeSet); // store the new joke set into the user jokes map

		return newJokeSet;
	}

	// Method to store the new joke set
	private static void storeJokes(int uuid, String[] proverbs) {
		userJokes.put(uuid, proverbs);
	}
	// Method to retrieve the client's joke set
	public static String[] getOldJokes(int uuid) {
		return userJokes.get(uuid);
	}
}

class Proverbs {

	static HashMap<Integer, String[]> userProverbs = new HashMap<>(); // Map to store the users proverb set

	// Method to randomize the proverbs
	public static String[] getRandomizedProverbs(int uuid) {
		String[] proverbs = new String[]{"Comparison is the thief of joy", "A picture is worth a thousand words.", "No good dead will go unpunished.", "A truly happy person is one who can enjoy the scenery on a detour."};
		String[] newProverbSet = new String[4];
		int size = proverbs.length;

		// Loop through proverbs array, randomly select proverbs,
		// and add/return them to the new proverbs set. 
		for (int i = 0; i<size; i++) {
			int min = 0; 
			int max = 3-i; // max equals the size of the proverbs array that dynamically changes 
			Random r = new Random(); // Instantiate a random object
			int index = r.nextInt((max - min) + 1) + min; // get random index
			newProverbSet[i] = proverbs[index]; // store the random proverb[index] into the new proverb set
			
			// Set up variables needed to remove recently stored proverb
			int size2 = proverbs.length; 
			int pointer = 0; // pointer to step through temp proverbs array
			String[] tempProverbs = new String[proverbs.length-1]; // array to store proverbs that haven't been selected yet

			// Loop through to remove recently stored proverb to avoid duplication
			for (int j=0; j<size2; j++) { 
				if (proverbs[j] != proverbs[index]) {
					tempProverbs[pointer] = proverbs[j];
					pointer++;
				}
			}
			proverbs = tempProverbs; // rewrite the proverbs array (with recently added proverb removed)
		}

		storeProverbs(uuid, newProverbSet); // store the new proverb set into the user jokes map

		return newProverbSet;
	}

	// Method to store new proverbs in userProverbs map
	private static void storeProverbs(int uuid, String[] proverbs) {
		userProverbs.put(uuid, proverbs);
	}

	// Method to retrieve the client's proverb set
	public static String[] getOldProverbs(int uuid) {
		return userProverbs.get(uuid);
	}
}


// Main (JokeServer) class 
public class JokeServer {

	public static void main(String[] args) throws IOException {

		Mode mode = new Mode(); // Create mode instance
		int q_len = 10; // 10 simultaneous connection allowed
		int port = 4545; // primary server listens on port 4545
		Socket sock; // create communication socket

		
		UserMap userMap = new UserMap(); // Initialize user map to keep track of all clients for server one
		
		System.out.println("Luis Norman's Joke server one starting up listening at 4545"); // Print to the console that the server is up and running 

		boolean secondary = false; // Flag to see if secondary sever is requested

		// Check if secondary server should be started 
		if (args.length > 0) {
			if (args[0].equals("secondary")) {
				secondary = true; // set the secondary flag to true
				System.out.println("\nLuis Norman's Joke server two starting up listening at 4546"); // Print to the console that the secondary server is up and running 
			}
		}

		// Spawn off a thread asynchronously to listen for client admin connections on port 5051
		AdminLooper AL = new AdminLooper(mode, 5050); 
		Thread t = new Thread(AL);
		t.start();

		// Checking if secondary server was requested
		if (secondary == true) {
			// Spawn off an asychronous thread to listen for client connections on different port
			SecondaryServerLooper SL = new SecondaryServerLooper(new UserMap());
			Thread t2 = new Thread(SL);
			t2.start();

			// Spawn off a thread asynchronously to listen for client admin connections on port 5051
			AdminLooper AL2 = new AdminLooper(mode, 5051); 
			Thread t3 = new Thread(AL2);
			t3.start();

		}
		
		ServerSocket serversock = new ServerSocket(port, q_len); // Initialize a serversocket to listen for client connections

		while (true) {
			sock = serversock.accept(); // Accept incoming client connection request
			new Worker(sock, userMap, false).start(); // Once we get a socket and accept the connection, start a worker thread

		}
	}
	
}