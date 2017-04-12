import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Greeshma Reddy
 * Server receives a hashmap with BigIntegers with their unique Ids. When Server receives C from the client,
 * it calculates C1= C1.modPow(m1, nsquare)*C2.modPow(m2, nsquare)*..*Cn.modPow(mn, nsquare)
 * and sends C1 back to the client
 */
public class Server {
	
	ServerSocket serverSocket=null;
	int serverPort;
	LinkedHashMap<Integer, BigInteger> hm;
	
	public Server(int serverPort) 
	{
		this.serverPort = serverPort;
		hm= new LinkedHashMap<Integer, BigInteger>();
	}
	
	public static void main(String[] args)
	{
		// Check the number of arguments 
		if(args.length!=1)
		{
			System.out.println("Port number not entered or extra arguments entered on commandline..Exiting Server Program");
			System.exit(1);
		}
		int serverport=Integer.parseInt(args[0]);
		Server server=new Server(serverport);
		server.serverProcess();		
	}

	//	serverProcess() is responsible for reciving data, checking it's validity/integrity. Responsible for 
	//  replying back to the client
	
	@SuppressWarnings("unchecked")
	public void serverProcess() 
	{
		try {
			// Creates a server socket on given serverPort
	        serverSocket = new ServerSocket();
	        serverSocket.bind(new InetSocketAddress("localhost", serverPort));
	        System.out.println("Starting server on "+serverSocket.getLocalPort());
	        Socket clientSocket=null;
			while(true)
			{
				// Accepts connections on this socket
				clientSocket = serverSocket.accept();				
				ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());			
				
				// Receives the HashMap with BigIntegers and their Unique Ids
				hm= (LinkedHashMap<Integer, BigInteger>) ois.readObject();		
				BigInteger nsquare=(BigInteger) ois.readObject();
				System.out.println("Received data from Client..");	
				ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
				ArrayList<BigInteger> C=null;
				
				// Waits for client and receives C 
				while(true)
				{
					C= (ArrayList<BigInteger>) ois.readObject();
					// Calculates C1.modPow(m1, nsquare)*C2.modPow(m2, nsquare)*..*Cn.modPow(mn, nsquare)
					BigInteger C1=C.get(0).modPow(hm.get(1), nsquare);
					for(int i=1;i<hm.size();i++)
					{
						C1=C1.multiply((C.get(i).modPow(hm.get(i+1), nsquare)));
					}
					// Sends C1 back to the client
					oos.writeObject(C1);
					oos.flush();					
				}				
			}			
			
		} catch (Exception e) {
			System.out.println("Error occured"+ e.getMessage()+"\nExiting the Server");
			e.printStackTrace();
		}
	}
	
	
}