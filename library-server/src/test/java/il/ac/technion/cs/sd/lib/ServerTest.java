package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.MessengerException;

import org.junit.*;

public class ServerTest {
	
	@Test(expected = NullPointerException.class)
	public void cantKillServerTwice() throws MessengerException {
		ExtentedServer server = new ExtentedServer("serverAdr");
		server.start();
		server.kill();
		server.kill();
	}
	
	@Test
	public void startServerTwiceDoesNothing() throws MessengerException {
		ExtentedServer server = new ExtentedServer("serverAdr");
		server.start();
		server.start();
		server.kill();
	}
	
	@Test
	public void canStartServerAgain() throws MessengerException {
		ExtentedServer server = new ExtentedServer("serverAdr");
		server.start();
		server.kill();
		server.start();
		server.kill();
		
	}
	
	@Test(expected = MessengerException.class)
	public void serverAddressIsUnique() throws MessengerException {
		ExtentedServer server1 = new ExtentedServer("serverAdr");
		server1.start();
		ExtentedServer server2 = new ExtentedServer("serverAdr");
		server2.start();
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void serverAddressCantBeEmpty() throws MessengerException {
		ExtentedServer server1 = new ExtentedServer("");
		server1.start();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void serverAddressCantBeSpaces() throws MessengerException {
		ExtentedServer server1 = new ExtentedServer("      ");
		server1.start();
	}
	
	@Test(expected = NullPointerException.class)
	public void cantKillServerBeforeStart() throws MessengerException {
		ExtentedServer server = new ExtentedServer("serverAdr");
		server.kill();
	}
	
	@Test
	public void returnServerAddress() {
		ExtentedServer server = new ExtentedServer("serverAdr");
		assertEquals(server.getAddress(),"serverAdr");
	}
}
