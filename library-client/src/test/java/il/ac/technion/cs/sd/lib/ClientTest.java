package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.MessengerException;

import org.junit.*;

public class ClientTest {
	
	@Test(expected = MessengerException.class)
	public void clientAddressIsUnique() throws MessengerException {
		ExtentedClient client1 = new ExtentedClient("myAddress","serverAddress");
		client1.start();
		ExtentedClient client2 = new ExtentedClient("myAddress","anotherServerAddress");
		client2.start();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void clientAddressNotEmpty() throws MessengerException {
		ExtentedClient client1 = new ExtentedClient("","serverAddress");
		client1.start();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void clientAddressNoSpaces() throws MessengerException {
		ExtentedClient client1 = new ExtentedClient("     ","serverAddress");
		client1.start();
	}
		
	@Test(expected = MessengerException.class)
	public void cantKillClientTwice() throws MessengerException {
		ExtentedClient client = new ExtentedClient("myAddress","serverAddress");
		client.start();
		client.stopClient();
		client.stopClient();
	}
	
	@Test(expected = NullPointerException.class)
	public void cantKillClientBeforeStart() throws MessengerException {
		ExtentedClient client = new ExtentedClient("myAddress","serverAddress");
		client.stopClient();
	}
	
	@Test
	public void getAddressTest() {
		ExtentedClient client = new ExtentedClient("myAddress","serverAddress");
		assertEquals(client.getClientAddress(),"myAddress");
	}
	
	@Test
	public void getServerAddressTest() {
		ExtentedClient client = new ExtentedClient("myAddress","serverAddress");
		assertEquals(client.getServerAddress(),"serverAddress");
	}
	
	
	@Test(expected = MessengerException.class)
	public void serverIsNotOnline() throws MessengerException {
		ExtentedClient client1 = new ExtentedClient("myAddress","serverAddress");
		client1.start();
		client1.sendMessage("myAddress", "Hi", 2);
	}
	
	@Test(expected = MessengerException.class)
	public void killedClientCantSend() throws MessengerException {
		ExtentedClient client1 = new ExtentedClient("myAddress","serverAddress");
		client1.start();
		client1.stopClient();
		client1.sendMessage("myAddress", "Hi", 2);
	}
	
}
