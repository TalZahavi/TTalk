package il.ac.technion.cs.sd.app.msg;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.json.simple.parser.ParseException;

import il.ac.technion.cs.sd.lib.Client;
import il.ac.technion.cs.sd.lib.JsonAuxiliary;
import il.ac.technion.cs.sd.lib.MessageWrapper;
import il.ac.technion.cs.sd.msg.MessengerException;

/**
 * The TTalkClient represent a single client in the TTalk application.
 * <br>
 * Each client can send messages to other clients and also ask for their friendship.
 * <br>
 * Each client can handle the different messages in his own way. When the client connect to the server,
 * he will get any messages that sent to him while he was offline
 */
public class TTalkClient extends Client {
	private boolean m_isLoggedIn;
	private Consumer<MessageWrapper> m_messageConsumer;
	private Function<String, Boolean> m_friendshipRequestHandler;
	private BiConsumer<String, Boolean> m_friendshipReplyConsumer;
	
	
	/**
	 * Construct a new client in the TTalk application
	 * @param address The address of the client
	 * @param serverAddress The address of the server the client will register to
	 */
	public TTalkClient(String address, String serverAddress) {
		super(address, serverAddress);
		this.m_isLoggedIn = false;
	}
	
	/**
	 * The client will login to the server and get any messages that sent to him
	 * while he was offline (also if this is the first time he created). <br>
	 * The client can perform actions with the server only if he logged in
	 */
	public void login() {
		this.m_isLoggedIn = true;
		try {
			start();
			sendRequest(null, TTalkMessageType.LOGIN.getValue());
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * The client will be logged out of the server. <br>
	 * While the client is offline, he wont receive any new messages
	 * (he will get the messages when he come back online)
	 */
	public void logout() {
		this.m_isLoggedIn = false;
		try {
			sendRequest(null, TTalkMessageType.LOGOUT.getValue());
			stopClient();
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * @return If the client is logged in to the server
	 */
	public boolean isLoggedIn() {
		return this.m_isLoggedIn;
	}
	
	
	/**
	 * Set a consumer for handling incoming messages
	 * @param m_messageConsumer The consumer to handle the messages
	 */
	public void setMessageConsumer(Consumer<MessageWrapper> m_messageConsumer) {
		this.m_messageConsumer = m_messageConsumer;
	}
	
	
	/**
	 * Set a consumer for handling incoming friendship requests
	 * @param m_friendshipRequestHandler The consumer to handle the friendship requests
	 */
	public void setFriendshipRequestHandler(Function<String, Boolean> m_friendshipRequestHandler) {
		this.m_friendshipRequestHandler = m_friendshipRequestHandler;
	}
	
	
	/**
	 * Set a consumer for handling incoming friendship reply
	 * @param m_friendshipReplyConsumer The consumer to handle the friendship reply
	 */
	public void setFriendshipReplyConsumer(BiConsumer<String, Boolean> m_friendshipReplyConsumer) {
		this.m_friendshipReplyConsumer = m_friendshipReplyConsumer;
	}
	
	
	/**
	 * Send a new message to another client (can send a message to yourself). <br>
	 * If the other client is offline - he will get the messages when he got online.
	 * @param to The address of the client the messages is for
	 * @param data The data of the message
	 */
	public void sendMessage(String to, String data) {
		try {
			sendMessage(to, data, TTalkMessageType.SEND.getValue());
		} catch (MessengerException  e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Send a friendship request from another client. <br>
	 * If the other client is offline - he will get the request when he got online. <br>
	 * The other client will answer back his reply.
	 * @param who The client to ask his friendship
	 */
	public void requestFriendship(String who) {
		try {
			sendMessage(who, null, TTalkMessageType.FRIEND_REQUEST.getValue());
		} catch (MessengerException  e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Check if another client is online. The client can check only with his friends.
	 * @param who The client to check if he is online
	 * @return  A wrapped <code>true</code> if the user is a friend and is offline; a wrapped <code>false</code> if the
	 *         user is a friend and is offline; an empty {@link Optional} if the user isn't a friend of the client
	 */
	public Optional<Boolean> isOnline(String who) {
		Optional<Boolean> retval = Optional.empty();
		try {
			String answer = sendRequestAndWaitForResult(who, TTalkMessageType.IS_ONLINE.getValue());
			switch (answer) {
				case "1":
					retval = Optional.of(true);
					break;
				case "0":
					retval = Optional.of(false);
					break;
				case "-1":
				default:
					retval = Optional.empty();
					break;
			}
		} catch (MessengerException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return retval;
	}
	
	
	/**
	 * This method handle each incoming message. <br>
	 * The method will check what type is the messages - and apply the right consumer
	 * (The client get the consumers on construction)
	 */
	@Override 
	public MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException {
		MessageWrapper $ = null;
		switch (TTalkMessageType.values()[msgWrapper.getMessageType()]) {
			case SEND:
				m_messageConsumer.accept(msgWrapper);
				break;
			case FRIEND_REQUEST:
				String requestFrom = msgWrapper.getMessageData();
				boolean shouldAccept = m_friendshipRequestHandler.apply(requestFrom);
				if (shouldAccept)
					$ = new MessageWrapper(getClientAddress(), getServerAddress(), requestFrom,
					        TTalkMessageType.FRIEND_REQUEST_ACCEPT.getValue());
				else
					$ = new MessageWrapper(getClientAddress(), getServerAddress(), requestFrom,
					        TTalkMessageType.FRIEND_REQUEST_DECLINE.getValue());
				break;
			case FRIEND_REQUEST_ACCEPT:
				m_friendshipReplyConsumer.accept(msgWrapper.getMessageData(), true);
				break;
			case FRIEND_REQUEST_DECLINE:
				m_friendshipReplyConsumer.accept(msgWrapper.getMessageData(), false);
				break;
			case IS_ONLINE:
				try {
					clientIncomingMessages.put(msgWrapper.getMessageData());
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				break;
			case RETREIVE:
				try {
					List<MessageWrapper> msgList = JsonAuxiliary.jsonToMessageWrapperList(msgWrapper.getMessageData());
					for (MessageWrapper m : msgList)
						$ = handleMessage(m);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
				break;
			default:
				// TODO: exception?
				break;
		}
		return $;
	}
}
