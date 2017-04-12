import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Greeshma Reddy
 * 
 * Client accepts following details as command line arguments from the server- i.
 * i. Port on which server is running- serverPort
 * ii. No of BigIntegers that needs be generated- noOfBigIntegers
 * iii. Size of each BigInteger in terms of bits- blockSizeinBits
 * Generates Assigns a unique id to each BigInteger generated and sends them to Server.
 * Then this program waits for command from client. Client can execute only "Retrieve i" command to retrieve BigInteger mi
 * Client generates C which is c1, c2,..cn where for an i, ci=Encrypt(1) and others are ci=Encrypt(0)
 * Sends to C to the server and receives C1 which on decryption gives mi.
 */
public class Client {
	
	Socket clientSocket = null;
	int serverPort, noOfBigIntegers, blockSizeinBits;
	byte[] message1, message2;
	LinkedHashMap<Integer, BigInteger> hm;
	
	public Client(int serverPort, int noOfBlocks, int blockSizeinBits)
	{
		this.serverPort = serverPort;
		this.noOfBigIntegers=noOfBlocks;
		this.blockSizeinBits=blockSizeinBits;
		hm= new LinkedHashMap<Integer, BigInteger>();
	}
	
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException
	{
		// Check if all the required arguments are given by user
		if(args==null || args.length<3)
		{
			System.out.println("Please enter all arguments required to run the program- "
					+ "Server Port, number of blocks to export, size of each block in bits");
			System.exit(1);
		}
		int serverport=Integer.parseInt(args[0]);
		int noOfBlocks= Integer.parseInt(args[1]);
		int blockSizeinBits= Integer.parseInt(args[2]);
		Client client=new Client(serverport, noOfBlocks, blockSizeinBits);
		client.clientProcess();
	}
	
	
	void clientProcess()
	{
	    try {
	    	// Create a client socket and this is bound to port 4555 on localhost
	    	clientSocket = new Socket();
	        SocketAddress SA= new InetSocketAddress("localhost", 4555);
	    	clientSocket.bind(SA);
	    	clientSocket.connect(new InetSocketAddress("localhost", serverPort));
	    	System.out.println("Client started on "+ clientSocket.getLocalPort());
	    	
	    	// Invokes Paillier object which inturn generates keys
	    	Paillier paillier= new Paillier(128);
	    	System.out.println("Blocks on client side:");
	    	
	    	// Generates noOfBigIntegers- random  BigIntegers each one of size- blockSizeinBits
	    	for(int i=1;i<=noOfBigIntegers;i++)
	    	{
	    		BigInteger randomBigInt = new BigInteger(blockSizeinBits, new Random());	   
	    		hm.put(i, randomBigInt);
	    		System.out.println(i+". "+randomBigInt);	    		
	    	}
	    	
	    	OutputStream os = clientSocket.getOutputStream();
	    	ObjectOutputStream oos = new ObjectOutputStream(os);
	    	
	    	// Sends the hashmap with all the BigIntegers generated
	    	oos.writeObject(hm);
    		oos.writeObject(paillier.nsquare);
	    	oos.reset();	
	    	System.out.println("Data outsourced to the server");     	
	    	
	    	Scanner s= new Scanner(System.in);
			ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

	    	while(true)
	    	{
	    		System.out.println("Enter the command: Retrieve i");
	    		// Read user's command and process it
	    		String[] command= s.nextLine().split("\\s");
	    		int cmd= Integer.parseInt(command[1]);
    			ArrayList<BigInteger> ci= new ArrayList<BigInteger>();
    			// Check if the command is retrieve and i is between 1 and total number of BigIntegers generated
	    		if(command[0].equalsIgnoreCase("retrieve") && cmd>=1 && cmd<=noOfBigIntegers)
	    		{
	    			for(int i=0;i<noOfBigIntegers;i++)
	    			{
	    				// All values of C except Ci should be Encryption(0). Ci is Encrypt(1)
	    				if(i==cmd-1)
	    				{
	    					ci.add(paillier.Encryption(BigInteger.ONE));
	    				}
	    				else
	    					ci.add(paillier.Encryption(BigInteger.ZERO));
	    			}
	    		}
	    		else
	    		{
	    			// Incase of invalid command, print a message and continue with accepting a new command from the user
	    			System.out.println("You have entered an invalid command...Please try again");
	    			continue;
	    		}
	    		//Send C to the server
	    		oos.writeObject(ci);
	    		oos.flush();
	    		
	    		// Receive C1 from the server
	    		BigInteger c1= (BigInteger) ois.readObject();	
	    		
	    		// Output the Decryption(C1)= mi
	    		System.out.println(paillier.Decryption(c1));
	    	} 	    	
		} catch (Exception e) {
			System.out.println("Error occured"+ e.getMessage());
			System.out.println("Terminating Connection");
		}

	}
}
