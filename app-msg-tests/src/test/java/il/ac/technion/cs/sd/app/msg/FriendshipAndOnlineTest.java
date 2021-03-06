package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import java.util.concurrent.*;

import org.junit.*;

public class FriendshipAndOnlineTest {
	private ServerMailApplication			server				= new ServerMailApplication("Server");
	// all listened to incoming messages will be written here
	// a blocking queue is used to overcome threading issues
	private BlockingQueue<Boolean>			friendshipReplies	= new LinkedBlockingQueue<>();
	
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
	public void clientHaveNoFriends() {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(Optional.empty(), tal.isOnline("Boaz"));
		tal.logout();
	}
		
	@Test
	public void clientAreNotFriends() {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(Optional.empty(), tal.isOnline("Boaz"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void clientAreFriends() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.requestFriendship("Boaz"); //says yes
		assertEquals(true, friendshipReplies.take());
		assertEquals(Optional.of(true), tal.isOnline("Boaz"));
		assertEquals(Optional.of(true), boaz.isOnline("Tal"));
		tal.logout();
		boaz.logout();
	}
	
	@Test
	public void friendIsLoggedOut() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.requestFriendship("Boaz"); //says yes
		assertEquals(true, friendshipReplies.take());
		boaz.logout();
		assertEquals(Optional.of(false), tal.isOnline("Boaz"));
		tal.logout();
	}
	
	@Test
	public void cliendRejectFriendship() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> {}, x -> false, (x, y) -> friendshipReplies.add(y));
		tal.requestFriendship("Boaz"); //says no
		assertEquals(false, friendshipReplies.take());
		assertEquals(Optional.empty(), tal.isOnline("Boaz"));
		boaz.logout();
		tal.logout();
	}
	
	@Test
	public void onlyOneOfTheClintsWantsFriendship() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> {}, x -> false, (x, y) -> friendshipReplies.add(y));
		tal.requestFriendship("Boaz"); //says no
		assertEquals(false, friendshipReplies.take());
		boaz.requestFriendship("Tal"); //says no
		assertEquals(true, friendshipReplies.take());
		assertEquals(Optional.of(true), tal.isOnline("Boaz"));
		assertEquals(Optional.of(true), boaz.isOnline("Tal"));
		boaz.logout();
		tal.logout();
	}
	
	@Test
	public void getFriendshipRequesBeforeLoggin() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		tal.requestFriendship("Boaz");
		boaz.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));	
		assertEquals(true, friendshipReplies.take());
		assertEquals(Optional.of(true), tal.isOnline("Boaz"));
		assertEquals(Optional.of(true), boaz.isOnline("Tal"));
		boaz.logout();
		tal.logout();
	}
	
	public void getMoreFriendshipRequestBeforeLogin() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));	
		tal.requestFriendship("Ron");
		boaz.requestFriendship("Ron");
		ClientMsgApplication ron = buildClient("Ron");
		ron.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));	
		assertEquals(true, friendshipReplies.take());
		assertEquals(true, friendshipReplies.take());
		assertEquals(Optional.of(true), ron.isOnline("Boaz"));
		assertEquals(Optional.of(true), ron.isOnline("Tal"));
		ron.logout();
		assertEquals(Optional.of(false), tal.isOnline("Ron"));
		tal.logout();
		boaz.logout();
		
	}
	
	@Test
	public void getFriendshipRequestAfterReconnect() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> {}, x -> false, (x, y) -> friendshipReplies.add(y));
		
		boaz.logout();
		tal.requestFriendship("Boaz");
		boaz.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(true, friendshipReplies.take());
		assertEquals(Optional.of(true), tal.isOnline("Boaz"));
		assertEquals(Optional.of(true), boaz.isOnline("Tal"));
		boaz.logout();
		tal.logout();
	}
	
	@Test
	public void getFriendshipAnswerOnLogging() throws InterruptedException {
		ClientMsgApplication tal = buildClient("Tal");
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.requestFriendship("Boaz");
		tal.logout();
		ClientMsgApplication boaz = buildClient("Boaz");
		boaz.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		tal.login(x -> {}, x -> true, (x, y) -> friendshipReplies.add(y));
		assertEquals(true, friendshipReplies.take());
		assertEquals(Optional.of(true), tal.isOnline("Boaz"));
		assertEquals(Optional.of(true), boaz.isOnline("Tal"));
		boaz.logout();
		tal.logout();
	}
}
