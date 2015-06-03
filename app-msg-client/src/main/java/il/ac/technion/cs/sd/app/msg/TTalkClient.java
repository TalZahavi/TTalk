package il.ac.technion.cs.sd.app.msg;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import il.ac.technion.cs.sd.lib.Client;
import il.ac.technion.cs.sd.lib.MessageWrapper;
import il.ac.technion.cs.sd.msg.MessengerException;

public class TTalkClient extends Client {
	private boolean m_isLoggedIn;
	private Consumer<TTalkMessage> m_messageConsumer;
	private Function<String, Boolean> m_friendshipRequestHandler;
	private BiConsumer<String, Boolean> m_friendshipReplyConsumer;
	
	public TTalkClient(String address, String serverAddress) throws MessengerException {
		super(address, serverAddress);
		this.m_isLoggedIn = false;
	}
	
	public void login() {
		this.m_isLoggedIn = true;
	}
	
	public void logout() {
		this.m_isLoggedIn = false;
	}
	
	public boolean isLoggedIn() {
		return this.m_isLoggedIn;
	}
	
	public void setMessageConsumer(Consumer<TTalkMessage> m_messageConsumer) {
		this.m_messageConsumer = m_messageConsumer;
	}
	
	public void setFriendshipRequestHandler(Function<String, Boolean> m_friendshipRequestHandler) {
		this.m_friendshipRequestHandler = m_friendshipRequestHandler;
	}
	
	public void setFriendshipReplyConsumer(BiConsumer<String, Boolean> m_friendshipReplyConsumer) {
		this.m_friendshipReplyConsumer = m_friendshipReplyConsumer;
	}
	
	public void sendMessage(String to, String data) {
		try {
			sendMessageWithResult(to, data, TTalkMessageType.SEND.getValue());
		} catch (MessengerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void requestFriendship(String who) {
		try {
			sendMessageWithResult(who, null, TTalkMessageType.FRIEND_REQUEST.getValue());
		} catch (MessengerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void acceptFriendship(String who) {
		try {
			sendMessageWithResult(who, null, TTalkMessageType.FRIEND_REQUEST_ACCEPT.getValue());
		} catch (MessengerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void declineFriendship(String who) {
		try {
			sendMessageWithResult(who, null, TTalkMessageType.FRIEND_REQUEST_DECLINE.getValue());
		} catch (MessengerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Optional<Boolean> isOnline(String who) {
		Optional<Boolean> retval = Optional.empty();
		try {
			switch (sendMessageWithResult(who, null, TTalkMessageType.IS_ONLINE.getValue())) {
				case "1":
					retval = Optional.of(true);
				case "0":
					retval = Optional.of(false);
				case "-1":
				default:
					retval = Optional.empty();
			}
		} catch (MessengerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}
}
