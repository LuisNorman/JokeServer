import java.io.*;
import java.net.*;
import java.util.HashMap;

// UserMap (HashMap) to store all clients uuids as key and a 
// hashmap as the value that stores "joke" or "proverb"
// as the key and its current index position as the val
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

		if (!secondary) {
			// Retrieve the current's mode index, increment it, and store it back to usermap
			if (Mode.getMode1() == "jokes") {
				index = indexMap.get("jokes");
				if (index == 3) 
					newIndex = 0;
				else
					newIndex = index + 1;
				indexMap.put("jokes", newIndex);
			}
			else {
				index = indexMap.get("proverbs");
				if (index == 3) 
					newIndex = 0;
				else
					newIndex = index + 1;
				indexMap.put("proverbs", newIndex);
			}
		}
		else {
			// Retrieve the current's mode index, increment it, and store it back to usermap
			if (Mode.getMode2() == "jokes") {
				index = indexMap.get("jokes");
				if (index == 3) 
					newIndex = 0;
				else
					newIndex = index + 1;
				indexMap.put("jokes", newIndex);
			}
			else {
				index = indexMap.get("proverbs");
				if (index == 3) 
					newIndex = 0;
				else
					newIndex = index + 1;
				indexMap.put("proverbs", newIndex);
			}
		}
		
		this.put(uuid, indexMap);
	}

	// Method to return current joke or proverb index and 
	// store it if is the client's first visit to server
	@SuppressWarnings("unchecked")
	public int getIndex(int uuid, boolean secondary) {
		HashMap<String, Integer> indexMap;
		// Check if usermap contains current's client uuid 
		// In other words, check if client has visited the server before
		// If so, get the index map that stores the joke and proverb index
		if (((HashMap<Integer, HashMap>)this).containsKey(uuid)) {
			indexMap = (HashMap<String, Integer>)this.get(uuid);
		}
		// If not, create an index map for the NEW client and store it in usermap
		else {
			indexMap = new HashMap<String, Integer>();
			indexMap.put("proverbs", 0);
			indexMap.put("jokes", 0);
			this.put(uuid, indexMap);
		}

		int index;
		// Based on the current mode, retrieve the index from the index map and return it
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
	Socket sock;
	static UserMap userMap;
	boolean secondary;

	Worker(Socket sock, UserMap userMap, boolean secondary) {
		this.sock = sock;
		this.userMap = userMap;
		this.secondary = secondary;
	}

	// Send joke or proverb
	public void run() {
		
		PrintStream out = null; // Create output stream to send message to client
		BufferedReader in = null; // Create input stream to read message from the client

		try {
			
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // Set up the input stream to read the uuid from the client
			out = new PrintStream(sock.getOutputStream()); // Set up the output stream to send to the socket 

			try {
				String incoming = in.readLine();
				String[] msg = incoming.split(",");
				int uuid = Integer.valueOf(msg[0]);
				String username = msg[1];
				sendJokeorProverb(out, username, uuid, secondary);
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

		// Store jokes and proverbs
		String[] jokes = new String[]{"My wife and I decied we don't want to have children. We will be telling them tonight.", "I found a pen that writes underwater. It writes other words too.", "Why do you never see hippos hiding in trees. Because they are very good at it.", "To the person who stole my copy of Microsoft Office, I will find you. You have my word."};
		String[] proverbs = new String[]{"Comparison is the thief of joy", "A picture is worth a thousand words.", "No good dead will go unpunished.", "A truly happy person is one who can enjoy the scenery on a detour."};
		

		String message;
		String prefix = null;
		
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
				out.println("JOKE CYCLE ENDED"); // Print joke cycle ended
			else 
				out.println("PROVERB CYCLE ENDED"); // Print proverb cycle
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
	Socket sock;
	Mode mode;
	Boolean secondary;

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
		if (port == 5051) 
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
	UserMap userMap;

	SecondaryServerLooper(UserMap userMap) {
		this.userMap = userMap;
	}

	public void run() {
		int q_len = 6;
		int port = 4546;
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


// Main (JokeServer) class 
public class JokeServer {

	public static void main(String[] args) throws IOException {

		Mode mode = new Mode(); // Create mode instance
		int q_len = 10;
		int port = 4545;
		Socket sock;

		
		UserMap userMap = new UserMap(); // Initialize user map to keep track of all clients for server one
		
		System.out.println("Luis Norman's Joke server one starting up listening at 4545"); // Print to the console that the server is up and running 

		boolean secondary = false; // Flag to see if secondary sever is requested

		// Check if secondary server should be started 
		if (args.length > 0) {
			if (args[0].equals("secondary")) {
				secondary = true;
				System.out.println("\nLuis Norman's Joke server two starting up listening at 4546"); // Print to the console that the secondary server is up and running 
			}
		}

		// Spawn off a thread asynchronously to listen for client admin connections
		AdminLooper AL = new AdminLooper(mode, 5050); 
		Thread t = new Thread(AL);
		t.start();

		// Checking if secondary server was requested
		if (secondary == true) {
			// Spawn off an asychronous thread to listen for client connections on different port
			SecondaryServerLooper SL = new SecondaryServerLooper(new UserMap());
			Thread t2 = new Thread(SL);
			t2.start();

			// Spawn off a thread asynchronously to listen for client admin connections
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