package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.*;

import org.junit.*;

public class TTalkClientTest {
	
	@Test(expected = RuntimeException.class)
	public void cantLoginClientBeforeServer() {
		TTalkClient client = new TTalkClient("myAddress","serverAddress");
		client.login();
	}
	
	@Test(expected = RuntimeException.class)
	public void cantLogoutBeforeServer() {
		TTalkClient client = new TTalkClient("myAddress","serverAddress");
		client.logout();
	}
	
	@Test
	public void returnLoggedInStatus() {
		TTalkClient client = new TTalkClient("myAddress","serverAddress");
		assertEquals(false,client.isLoggedIn());
	}
	
	@Test(expected = RuntimeException.class)
	public void cantSendMessageWithoutServer() {
		TTalkClient client = new TTalkClient("myAddress","serverAddress");
		client.sendMessage("myAddress", "hi");
	}
	
	@Test(expected = RuntimeException.class)
	public void cantCheckWhoOnlineWithoutServer() {
		TTalkClient client = new TTalkClient("myAddress","serverAddress");
		client.isOnline("avi");
	}
	
	@Test(expected = RuntimeException.class)
	public void cantRequestFriendshipWithoutServer() {
		TTalkClient client = new TTalkClient("myAddress","serverAddress");
		client.requestFriendship("avi");
	}

}
