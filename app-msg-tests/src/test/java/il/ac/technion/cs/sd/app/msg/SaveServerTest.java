package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import java.util.concurrent.*;

import org.junit.*;

public class SaveServerTest {
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
	public void serverSaveDataBeforeClientCreated() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.sendMessage("Boaz", "Hi Boaz");
		tal.sendMessage("Boaz", "How are you?");
		ClientMsgApplication boaz = buildClient("Boaz");
		tal.requestFriendship("Boaz");
		server.stop();
		server.start();
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "Hi Boaz"));
		assertEquals(messages.take(), new InstantMessage("Tal", "Boaz", "How are you?"));
		assertEquals(true, friendshipReplies.take());
		assertEquals(Optional.of(true), tal.isOnline("Boaz"));
		assertEquals(Optional.of(true), boaz.isOnline("Tal"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void serverRememberFriendship() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		tal.requestFriendship("Boaz");
		assertEquals(true, friendshipReplies.take());
		server.stop();
		server.start();
		assertEquals(Optional.of(true), tal.isOnline("Boaz"));
		assertEquals(Optional.of(true), boaz.isOnline("Tal"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void serverForgetFriendshipAfterClean() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(Optional.empty(), tal.isOnline("Boaz"));
		tal.requestFriendship("Boaz");
		assertEquals(true, friendshipReplies.take());
		server.stop();
		server.clean();
		server.start();
		assertEquals(Optional.empty(), tal.isOnline("Boaz"));
		//assertEquals(Optional.empty(), boaz.isOnline("Tal"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void serverForgetMessagesAfterClean() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.sendMessage("Boaz", "Hi Boaz");
		tal.sendMessage("Boaz", "How are you?");
		ClientMsgApplication boaz = buildClient("Boaz");
		server.stop();
		server.clean();
		server.start();
		boaz.login(x -> messages.add(x), x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(null,messages.poll(200, TimeUnit.MILLISECONDS));
		assertEquals(true,messages.isEmpty());
		tal.logout();
		boaz.logout();
	}
}
