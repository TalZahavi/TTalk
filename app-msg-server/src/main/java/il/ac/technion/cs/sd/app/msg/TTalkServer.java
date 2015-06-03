package il.ac.technion.cs.sd.app.msg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import il.ac.technion.cs.sd.lib.MessageWrapper;
import il.ac.technion.cs.sd.lib.Server;
import il.ac.technion.cs.sd.msg.MessengerException;

public class TTalkServer extends Server {
	HashSet<String> m_onlineUsers;
	HashMap<String, Set<String>> m_friendsLists;
	
	public TTalkServer(String serverAddress) throws MessengerException {
		super(serverAddress);
		m_onlineUsers = new HashSet<String>();
		m_friendsLists = new HashMap<String, Set<String>>();
	}
	
	@Override public MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException {
		String from = msgWrapper.getFromAddress();
		String to = msgWrapper.getToAddress();
		String data = msgWrapper.getMessageData();
		switch (TTalkMessageType.values()[msgWrapper.getMessageType()]) {
			case SEND:
				return null;
				//break;
			case LOGIN:
				m_onlineUsers.add(from);
				if (!m_friendsLists.containsKey(from))
					m_friendsLists.put(from, new HashSet<String>());
				// TODO: retrieve offline messages
				return null;
				//break;
			case FRIEND_REQUEST:{
				MessageWrapper msg = new MessageWrapper(getAddress(), to, from, TTalkMessageType.FRIEND_REQUEST.getValue());
				//sendMessage(to, from, TTalkMessageType.FRIEND_REQUEST.getValue());
				return msg;}
				//break;
			case FRIEND_REQUEST_ACCEPT:
				if (!m_friendsLists.containsKey(data))
					m_friendsLists.put(data, new HashSet<String>());
				m_friendsLists.get(from).add(data);
				m_friendsLists.get(data).add(from);
				return null;
				//break;
			case FRIEND_REQUEST_DECLINE:
				return null;
				//break;
			case LOGOUT:
				m_onlineUsers.remove(from);
				return null;
				//break;
			case IS_ONLINE:{
				MessageWrapper msg = null;
				if (m_friendsLists.get(from).contains(data))
					msg = new MessageWrapper(getAddress(), from, m_onlineUsers.contains(data) ? "1" : "0", TTalkMessageType.IS_ONLINE.getValue());
					//sendMessage(from, m_onlineUsers.contains(data) ? "1" : "0", TTalkMessageType.IS_ONLINE.getValue());
				else
					msg = new MessageWrapper(getAddress(), from, "-1", TTalkMessageType.IS_ONLINE.getValue());
					//sendMessage(from, "-1", TTalkMessageType.IS_ONLINE.getValue());
				return msg;}
			default:
				// TODO: exception?
				break;
		}
		return null;
	}
	
	public void start() {
		// TODO Auto-generated method stub
	}
	
	public void clean() {
		// TODO Auto-generated method stub
	}
}
