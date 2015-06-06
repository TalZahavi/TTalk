package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {
	
	class FakeServer extends Server {
		public FakeServer(String serverName) throws MessengerException {
			super(serverName);
		}
		
		@Override public MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException {
	        return msgWrapper;
        }
	}
	
	private final String serverName = "server";
	private FakeServer server;
	private final String to = "someone";
	private final String from = "client";
	private final String data = "something";
	private final int type = 1;
	private final MessageWrapper m = new MessageWrapper(to,from, data, type);
	
	@Before
	public void setUp() throws Exception {
		server = new FakeServer(serverName);
	}
	
	@After
	public void tearDown() throws Exception {
		server.kill();
		server = null;
	}
	
	@Test public void test() {
		fail("Not yet implemented");
	}
}
