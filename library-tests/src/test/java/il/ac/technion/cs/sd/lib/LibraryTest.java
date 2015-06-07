package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.MessengerException;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.*;

/**
 * 
 * This is a simple test for the client-server library.
 * The test also demonstrate how to use the library methods
 */
public class LibraryTest {

	// a helper method and list for creating clients, so we can remember to kill them
	private final Collection<SimpleClient>	clients			= new ArrayList<>();
	
	// all listened to incoming messages will be written here by default
	private final BlockingQueue<String>	waitQueue	= new LinkedBlockingQueue<>();
	
	private SimpleServer server;
	
	// Create and add to the clients list a new client
	private SimpleClient createAndAddToList(String address, String serverAddress) {
		SimpleClient $ = new SimpleClient(address,serverAddress);
		clients.add($);
		return $;
	}
	
	@Before
	public void startUpServer() throws MessengerException {
		server = new SimpleServer("server");
		//First, the server should start listening
		server.start();
	}
	
	@After
	public void teardown() throws Exception {
		// it is very important to kill all clients
		for (SimpleClient m: clients)
			try {
				m.stopClient();
			} catch (Exception e) {/* do nothing */}
		server.kill();
		
		
	}
	
	/** The sendMessageAndWaitForResult work the same**/
	@Test
	public void sendRequestAndWaitForResultTest() throws Exception {
		
		//The client is created (the server address given as a parameter)
		SimpleClient client = createAndAddToList("client","server");
		
		//The client now can receive and send messages to other clients
		client.start();
		
		//Send the server a request and wait for his result.
		//In our case, the server take the data and append to it "Server Answer!"
		String result = client.sendRequestAndWaitForResult("Only part of", SimpleMessageTypes.REQUEST_DATA_AND_RESULT.getValue());
		
		//The server send back his result.
		//Important - if you use the waitingForResult methods -
		//The client handle method MUST insert the result into the incomingMessaages
		assertEquals(result,"Only part of Server Answer!");
	}
	
	@Test
	public void sendRequestTest() throws MessengerException, InterruptedException {
		
	//The client is created (the server address given as a parameter)
	SimpleClient client = createAndAddToList("client","server");
				
	//The client now can receive and send messages to other clients
	client.start();
				
	//Send the server a request - no need to wait for a result.
    //In our case, the server just update his counter
	client.sendRequest("Hello", SimpleMessageTypes.REQUEST_DATA.getValue());
				
	//You need to Wait a little bit to make sure the data got sent
	waitQueue.poll(200, TimeUnit.MILLISECONDS);
	
	//The server got the request
	assertEquals("On",server.getRequestCounter());
	}
	
	@Test
	public void sendMessageTest() throws MessengerException, InterruptedException {
		//The client is created (the server address given as a parameter)
		SimpleClient client1 = createAndAddToList("client1","server");
		SimpleClient client2 = createAndAddToList("client2","server");
					
		//The client now can receive and send messages to other clients
		client1.start();
		client2.start();
					
		//Send Another client the message
	    //In our case, the other client just update his counter
		client1.sendMessage("client2", "hi", SimpleMessageTypes.MESSAGE.getValue());
					
		//You need to Wait a little bit to make sure the data got sent
		waitQueue.poll(200, TimeUnit.MILLISECONDS);
		
		//Client2 got the message that client1 sent him
		assertEquals("On",client2.getMessagesCounter());
	}
	
}
