package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.*;

import org.junit.*;

public class SendMessageTest {
	private ServerMailApplication			server				= new ServerMailApplication("Server");
	// all listened to incoming messages will be written here
	// a blocking queue is used to overcome threading issues
	private BlockingQueue<Boolean>			friendshipReplies	= new LinkedBlockingQueue<>();
	private BlockingQueue<InstantMessage>	messages			= new LinkedBlockingQueue<>();
	
	private ClientMsgApplication buildClient(String login) {
		return new ClientMsgApplication(server.getAddress(), login);
	}
	
	@Before
	public void setp() {
		server.start(); // non-blocking
	}
	
	@After
	public void teardown() {
		server.stop();
		server.clean();
	}
	
	@Test
	public void sendSimpleMessage() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		tal.sendMessage("Boaz", "Hi Boaz");
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "Hi Boaz"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void sendMessageBeforeClientCreated() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.sendMessage("Boaz", "Hi Boaz");
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "Hi Boaz"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void sendMoreMessageBeforeClientCreated() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.sendMessage("Boaz", "Hi Boaz");
		tal.sendMessage("Boaz", "How are you?");
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "Hi Boaz"));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "How are you?"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void getMessageOnReconnect() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		boaz.logout();
		tal.sendMessage("Boaz", "And again");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "And again"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void getMoreMessageOnReconnect() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		boaz.logout();
		tal.sendMessage("Boaz", "Hi there");
		tal.sendMessage("Boaz", "And again");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "Hi there"));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "And again"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void conversationBetweenClients() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		tal.sendMessage("Boaz", "Hi Boaz");
		boaz.sendMessage("Tal", "Hello there");
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "Hi Boaz"));
		assertEquals(messages.take(), new InstantMessage("Boaz", "Tal", "Hello there"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void sendMessageToYourself() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		tal.sendMessage("Tal", "Hi me");
		assertEquals(messages.take(), new InstantMessage("Tal", "Tal", "Hi me"));
		tal.logout();
	}
	
	@Test
	public void getMessageonlyWhenClientLogged() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.sendMessage("Boaz", "Hi Boaz");
		ClientMsgApplication boaz = buildClient("Boaz");
		assertEquals(messages.poll(100, TimeUnit.MILLISECONDS), null);
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "Hi Boaz"));
		tal.logout();
		boaz.logout();
	}
}
