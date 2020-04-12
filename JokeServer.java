import java.io.*;
import java.net.*;
import java.util.HashMap;

// Hashmap to store all clients and its current joke/proverb index position
class UserMap extends HashMap {
	HashMap<Integer, HashMap<String, Integer>> userMap;

	// Initialize user map
	public UserMap() {
		userMap = new HashMap<Integer, HashMap<String, Integer>>();
	}



	// Method to increment the index position to point to the next joke or proverb
	@SuppressWarnings("unchecked")
	public void incrementIndex(int uuid) {
		HashMap<String, Integer> indexMap = (HashMap<String, Integer>)this.get(uuid);
		int index;
		int newIndex;
		if (Mode.getMode() == "jokes") {
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

		
		this.put(uuid, indexMap);
	}

	// Method to get index and store it
	@SuppressWarnings("unchecked")
	public int getIndex(int uuid) {
		HashMap<String, Integer> indexMap;
		if (((HashMap<Integer, HashMap>)this).containsKey(uuid)) {
			indexMap = (HashMap<String, Integer>)this.get(uuid);
		}
		else {
			indexMap = new HashMap<String, Integer>();
			indexMap.put("proverbs", 0);
			indexMap.put("jokes", 0);
			this.put(uuid, indexMap);
		}

		int index;
		if (Mode.getMode() == "jokes") {
			index = indexMap.get("jokes");
		}
		else {
			index = indexMap.get("proverbs");
		}
		return index;
	}

}

class Worker extends Thread {
	Socket sock;
	static UserMap userMap;

	Worker(Socket sock, UserMap userMap) {
		this.sock = sock;
		this.userMap = userMap;
	}

	// Send joke or proverb
	public void run() {
		// Create output stream to send message to client
		PrintStream out = null;
		// Create input stream to read message from the client
		BufferedReader in = null;

		try {
			// Set up the input stream to read the uuid from the client
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Set up the output stream to send to the socket 
			out = new PrintStream(sock.getOutputStream());

			try {
				String incoming = in.readLine();
				String[] msg = incoming.split(",");
				int uuid = Integer.valueOf(msg[0]);
				String username = msg[1];
				sendJokeorProverb(out, username, uuid);
			}
			catch (IOException ioe) {
				System.out.println("Error: " + ioe);
			}
		}
		catch (IOException ioe) {
			System.out.println("Error: "+ioe);
		}
	}

	// Suppressing the casting error on line 113
	@SuppressWarnings("unchecked")
	static void sendJokeorProverb(PrintStream out, String username, int uuid) {
		// Get current mode
		String mode = Mode.getMode();
		// Get users current index map for jokes and proverbs
		int index = userMap.getIndex(uuid);
		// After recieving index, increment it
		userMap.incrementIndex(uuid);

		// Init jikes and proverbs
		String[] jokes = new String[]{"My wife and I decied we don't want to have children. We will be telling them tonight.", "I found a pen that writes underwater. It writes other words too.", "Why do you never see hippos hiding in trees. Because they are very good at it.", "To the person who stole my copy of Microsoft Office, I will find you. You have my word."};
		String[] proverbs = new String[]{"Comparison is the thief of joy", "A picture is worth a thousand words.", "No good dead will go unpunished.", "A truly happy person is one who can enjoy the scenery on a detour."};
		String message;
		String prefix = null;
		boolean reset = false;
		
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
		// Send joke or proverb to user
		out.println(message);
		if (index == 3) {
			if (mode == "jokes")
				out.println("JOKE CYCLE ENDED");
			else 
				out.println("PROVERB CYCLE ENDED");
		}
		else
			out.println("");
		
	}

}

class Mode {
	// Initial mode is jokes
	static String mode = "jokes";

	// Add the ability for admin to change mode
	public void changeMode(PrintStream out){
		if (mode == "proverb") {
			mode = "jokes";
			out.println("Changed mode from proverb to jokes");
		}
		else {
			mode = "proverb";
			out.println("Changed mode from jokes to proverb");
		}
	}

	// A method to allow the worker 
	// thread get the current mode
	public static String getMode() {
		return mode;
	}
}

class AdminWorker extends Thread {
	Socket sock;
	Mode mode;

	AdminWorker(Socket sock, Mode mode) {
		this.sock = sock;
		this.mode = mode;
	}

	public void run() {
		// Create input stream to read message from the client
		BufferedReader in = null;
		// Create output stream to send message to client
		PrintStream out = null;

		try {
			// Set up the input stream to read from the socket
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Set up the output stream to send to the socket 
			out = new PrintStream(sock.getOutputStream());
			in.readLine();
			mode.changeMode(out);
		}
		catch(IOException ioe) {
			System.out.println("Error: " + ioe);
		}
	}
}

class AdminLooper implements Runnable {
	Mode mode; 

	AdminLooper(Mode mode) {
		this.mode = mode;
	}

	public static boolean adminControlSwitch = true;

	public void run() {
		System.out.println("Entered Admin Thread");
		int q_len = 6;
		int port = 5050;
		Socket sock;

		try {
			ServerSocket servsock = new ServerSocket(port, q_len);
			while(adminControlSwitch) {
				sock = servsock.accept();
				new AdminWorker(sock, mode).start();
			}
		}
		catch(IOException ioe) {
			System.out.println("Error: " + ioe);
		}
	}
}


public class JokeServer {

	public static void main(String[] args) throws IOException {
		// Initialize user map to keep track of all clients
		UserMap userMap = new UserMap();
		// Initialize Mode 
		Mode mode = new Mode();
		int q_len = 10;
		int port1 = 4545;
		Socket sock1;

		AdminLooper AL = new AdminLooper(mode); 
		Thread t = new Thread(AL);
		t.start();

		ServerSocket serversock1 = new ServerSocket(port1, q_len);

		System.out.println("Luis Norman's Joke server starting up listening at 4545");

		while (true) {
			// Accept incoming client connection request
			sock1 = serversock1.accept();
			// Once we get a socket and accept the 
			// connection, start a worker thread
			new Worker(sock1, userMap).start();

		}
	}

	
}