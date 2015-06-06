package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ServerTest {
	
	class FakeServer extends Server {
		public FakeServer(String serverName) throws MessengerException {
			super(serverName);
		}
		
		public FakeServer(String serverName, Messenger messenger) {
			super(serverName,messenger);
        }

		@Override public MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException {
	        return msgWrapper;
        }
	}
	
	private final String serverName = "server";
	private FakeServer server;
	private Messenger messenger;
	private final String to = "someone";
	private final String from = "client";
	private final String data = "something";
	private final int type = 1;
	private final MessageWrapper m = new MessageWrapper(to,from, data, type);
	private final String j = JsonAuxiliary.messageWrapperToJson(m);
	
	@Before
	public void setUp() throws Exception {
		messenger = Mockito.mock(Messenger.class);
		Mockito.when(messenger.getAddress()).thenReturn(serverName);
		Mockito.doNothing().when(messenger).send(Mockito.anyString(),Mockito.anyString());
		Mockito.when(messenger.getNextMessage(Mockito.anyLong())).thenReturn("");
		server = new FakeServer(serverName, messenger);
	}
	
	@After
	public void tearDown() throws Exception {
		server.kill();
		server = null;
	}
	
	@Test
	public void getServerNameReturnsTheServersName() throws Exception {
		assertEquals(server.getAddress(),serverName);
	}
	
	@Test public void test() {
		fail("Not yet implemented");
	}
}