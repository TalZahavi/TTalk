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

public class TTalkClient extends Client {
	private boolean m_isLoggedIn;
	private Consumer<MessageWrapper> m_messageConsumer;
	private Function<String, Boolean> m_friendshipRequestHandler;
	private BiConsumer<String, Boolean> m_friendshipReplyConsumer;
	
	public TTalkClient(String address, String serverAddress) throws MessengerException {
		super(address, serverAddress);
		this.m_isLoggedIn = false;
	}
	
	public void login() {
		this.m_isLoggedIn = true;
		try {
			start();
			sendRequest(null, TTalkMessageType.LOGIN.getValue());
		} catch (MessengerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void logout() {
		this.m_isLoggedIn = false;
		try {
			sendRequest(null, TTalkMessageType.LOGOUT.getValue());
			stopClient();
		} catch (MessengerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isLoggedIn() {
		return this.m_isLoggedIn;
	}
	
	public void setMessageConsumer(Consumer<MessageWrapper> m_messageConsumer) {
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
			sendMessage(to, data, TTalkMessageType.SEND.getValue());
		} catch (MessengerException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void requestFriendship(String who) {
		try {
			sendMessage(who, null, TTalkMessageType.FRIEND_REQUEST.getValue());
		} catch (MessengerException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}
	
	@Override public MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException {
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
				System.out.println("Client " + getClientAddress() + " is putting IS_ONLINE answer on queue");
				try {
					clientIncomingMessages.put(msgWrapper.getMessageData());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case RETREIVE:
				try {
					List<MessageWrapper> msgList = JsonAuxiliary.jsonToMessageWrapperList(msgWrapper.getMessageData());
					for (MessageWrapper m : msgList)
						$ = handleMessage(m);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			default:
				// TODO: exception?
				break;
		}
		return $;
	}
}
