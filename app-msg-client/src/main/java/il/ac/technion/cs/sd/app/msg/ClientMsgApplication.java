package il.ac.technion.cs.sd.app.msg;

import il.ac.technion.cs.sd.msg.MessengerException;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The client side of the TMail application. Allows sending and getting messages to and from other clients using a server. <br>
 * You should implement all the methods in this class
 */
public class ClientMsgApplication {
	
	TTalkClient m_client;
	
	/**
	 * Creates a new application, tied to a single user
	 * 
	 * @param serverAddress The address of the server to connect to for sending and receiving messages
	 * @param username The username that will be sending and accepting the messages using this object
	 */
	public ClientMsgApplication(String serverAddress, String username) {
	   m_client = new TTalkClient(username, serverAddress);
	}
	
	/**
	 * Logs the client to the server. Any incoming messages from the server will be routed to the provided consumer. If
	 * the client missed any messages while he was offline, all of these will be first routed to client in the order
	 * that they were sent
	 * 
	 * @param messageConsumer The consumer to handle all incoming messages
	 * @param friendshipRequestHandler The callback to handle all incoming friend requests. It accepts the user requesting
	 *        the friendship as input and outputs the reply.
	 * @param friendshipReplyConsumer The consumer to handle all friend requests replies (replies to outgoing
	 *        friends requests). The consumer accepts the user requested and his reply.	
	 */
	public void login(Consumer<InstantMessage> messageConsumer,
			Function<String, Boolean> friendshipRequestHandler,
			BiConsumer<String, Boolean> friendshipReplyConsumer) {
		m_client.setMessageConsumer(x -> messageConsumer.accept(new InstantMessage(x.getFromAddress(), x.getToAddress(), x.getMessageData())));
		m_client.setFriendshipRequestHandler(friendshipRequestHandler);
		m_client.setFriendshipReplyConsumer(friendshipReplyConsumer);
		m_client.login();
	}
	
	/**
	 * Logs the client out, cleaning any resources the client may be using. A logged out client cannot accept any
	 * messages. A client can login (using {@link ClientMsgApplication#login(Consumer, Function, BiConsumer)} after logging out.
	 */
	public void logout() {
		m_client.logout();
	}
	
	/**
	 * Sends a message to another user
	 * 
	 * @param target The recipient of the message
	 * @param what The message to send
	 */
	public void sendMessage(String target, String what) {
		try {
	        m_client.sendMessage(target, what, 0);
        } catch (MessengerException e) {
        	throw new RuntimeException(e);
        }
	}
	
	/**
	 * Requests the friendship of another user. Friends can see each other online using
	 * {@link ClientMsgApplication#isOnline(String)}. Friend requests are handled similarly to messages. An incoming
	 * friend request is consumed by the friendRequestsConsumer. An incoming friend request <i>reply</i> is consumed by
	 * the friendRequestRepliesConsumer.
	 * 
	 * @param who The recipient of the friend request.
	 */
	public void requestFriendship(String who) {
		m_client.requestFriendship(who);
	}
	
	/**
	 * Checks if another user is online; the client can only ask if friends are online
	 * 
	 * @param who The person to check if he is online
	 * @return A wrapped <code>true</code> if the user is a friend and is offline; a wrapped <code>false</code> if the
	 *         user is a friend and is offline; an empty {@link Optional} if the user isn't a friend of the client
	 */
	public Optional<Boolean> isOnline(String who) {
		return m_client.isOnline(who);
	}
	
    /**
     * A stopped client does not use any system resources (e.g., messengers).
     * This is mainly used to clean resource use in test cleanup code.
     * You can assume that a stopped client won't be restarted using {@link ClientMsgApplication#login(Consumer, Function, BiConsumer)}
     */
    public void stop() {
        try {
	        m_client.stopClient();
        } catch (MessengerException e) {
        	throw new RuntimeException(e);
        }
    }

}
