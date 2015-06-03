package il.ac.technion.cs.sd.app.msg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import il.ac.technion.cs.sd.lib.MessageWrapper;
import il.ac.technion.cs.sd.lib.Server;
import il.ac.technion.cs.sd.msg.MessengerException;

public class TTalkServer extends Server {
	HashSet<String> m_onlineUsers;
	HashMap<String, Set<String>> m_friendsLists;
	HashMap<String, Consumer<TTalkMessage>> m_messageConsumers;
	HashMap<String, Function<String, Boolean>> m_friendshipRequestHandlers;
	HashMap<String, BiConsumer<String, Boolean>> m_friendshipRequestConsumers;
	
	public TTalkServer(String serverAddress) throws MessengerException {
		super(serverAddress);
		m_onlineUsers = new HashSet<String>();
		m_friendsLists = new HashMap<String, Set<String>>();
		m_messageConsumers = new HashMap<String, Consumer<TTalkMessage>>();
		m_friendshipRequestHandlers = new HashMap<String, Function<String, Boolean>>();
		m_friendshipRequestConsumers = new HashMap<String, BiConsumer<String, Boolean>>();
	}
	
	@Override public String handleMessage(MessageWrapper msgWrapper) throws MessengerException {
		String from = msgWrapper.getFromAddress();
		String to = msgWrapper.getToAddress();
		String data = msgWrapper.getMessageData();
		String retVal = null;
		switch (TTalkMessageType.values()[msgWrapper.getMessageType()]) {
			case SEND:
				send(to, data, TTalkMessageType.SEND.getValue());
				retVal = null;
				break;
			case LOGIN:
				m_onlineUsers.add(from);
				// TODO: retrieve offline messages
				retVal = null;
				break;
			case FRIEND_REQUEST:
				send(to, from, TTalkMessageType.FRIEND_REQUEST.getValue());
				retVal = null;
				break;
			case FRIEND_REQUEST_ACCEPT:
				if (!m_friendsLists.containsKey(from))
					m_friendsLists.put(from, new HashSet<String>());
				if (!m_friendsLists.containsKey(to))
					m_friendsLists.put(to, new HashSet<String>());
				m_friendsLists.get(from).add(to);
				m_friendsLists.get(to).add(from);
				retVal = null;
				break;
			case FRIEND_REQUEST_DECLINE:
				retVal = null;
				break;
			case LOGOUT:
				m_onlineUsers.remove(from);
				retVal = null;
				break;
			case IS_ONLINE:
				if (m_friendsLists.get(from).contains(to))
					retVal = m_onlineUsers.contains(to) ? "1" : "0";
				else
					retVal = "-1";
				break;
			default:
				// TODO: exception?
				break;
		}
		return retVal;
	}
	
	public void setMessageConsumer(String who, Consumer<TTalkMessage> messageConsumer) {
		m_messageConsumers.put(who, messageConsumer);
	}
	
	public void setFriendshipRequestHandler(String who, Function<String, Boolean> friendshipRequestHandler) {
		m_friendshipRequestHandlers.put(who, friendshipRequestHandler);
	}
	
	public void setFriendshipRequestConsumer(String who, BiConsumer<String, Boolean> friendshipRequestConsumer) {
		m_friendshipRequestConsumers.put(who, friendshipRequestConsumer);
	}
}
