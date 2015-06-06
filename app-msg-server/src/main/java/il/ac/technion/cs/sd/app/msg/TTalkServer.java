package il.ac.technion.cs.sd.app.msg;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import il.ac.technion.cs.sd.lib.JsonAuxiliary;
import il.ac.technion.cs.sd.lib.MessageWrapper;
import il.ac.technion.cs.sd.lib.Serializer;
import il.ac.technion.cs.sd.lib.Server;

/**
 * 
 * The TTalkServer represent a server in the TTalk application. <br>
 * The Server will listen to the client requests and handle them according to the handle method
 * (also send back a reply if needed).
 */
public class TTalkServer extends Server {
	HashSet<String> m_onlineUsers;
	HashMap<String, Set<String>> m_friendsLists;
	HashMap<String, List<MessageWrapper>> m_outgoingMessages;
	
	
	/**
	 * Construct a new server in the TTalk application
	 * @param serverAddress The server unique address
	 */
	public TTalkServer(String serverAddress) {
		super(serverAddress);
		m_onlineUsers = new HashSet<String>();
		m_friendsLists = new HashMap<String, Set<String>>();
		m_outgoingMessages = new HashMap<String, List<MessageWrapper>>();
	}
	
	
	/**
	 * The method will get a messageWrapper object and handle it according to the message type.
	 * <br>
	 * In case there's no answer for that type of message - return null <br>
	 * The server will send the message\request to the other client only if that client is online
	 * (If not, the server will keep the message and send it when the client is online)
	 */
	@Override 
	public MessageWrapper handleMessage(MessageWrapper msgWrapper) {
		String from = msgWrapper.getFromAddress();
		String to = msgWrapper.getToAddress();
		String data = msgWrapper.getMessageData();
		MessageWrapper $ = null;
		switch (TTalkMessageType.values()[msgWrapper.getMessageType()]) {
			case SEND:
				if (m_onlineUsers.contains(to))
					$ = msgWrapper;
				else {
					if (!m_outgoingMessages.containsKey(to))
						m_outgoingMessages.put(to, new ArrayList<MessageWrapper>());
					m_outgoingMessages.get(to).add(msgWrapper);
				}
				break;
			case LOGIN:
				m_onlineUsers.add(from);
				if (!m_friendsLists.containsKey(from))
					m_friendsLists.put(from, new HashSet<String>());
				if (!m_outgoingMessages.containsKey(from))
					m_outgoingMessages.put(from, new ArrayList<MessageWrapper>());
				$ = new MessageWrapper(getAddress(), from, JsonAuxiliary.messageWrapperListToJson(m_outgoingMessages.get(from)),
				        TTalkMessageType.RETREIVE.getValue());
				m_outgoingMessages.get(from).clear();
				break;
			case FRIEND_REQUEST:
				if (m_onlineUsers.contains(to))
					$ = new MessageWrapper(getAddress(), to, from, TTalkMessageType.FRIEND_REQUEST.getValue());
				else {
					if (!m_outgoingMessages.containsKey(to))
						m_outgoingMessages.put(to, new ArrayList<MessageWrapper>());
					m_outgoingMessages.get(to).add(new MessageWrapper(getAddress(), to, from, TTalkMessageType.FRIEND_REQUEST.getValue()));
				}
				break;
			case FRIEND_REQUEST_ACCEPT:
				if (!m_friendsLists.containsKey(data))
					m_friendsLists.put(data, new HashSet<String>());
				m_friendsLists.get(from).add(data);
				m_friendsLists.get(data).add(from);
				$ = new MessageWrapper(getAddress(), data, from, TTalkMessageType.FRIEND_REQUEST_ACCEPT.getValue());
				break;
			case FRIEND_REQUEST_DECLINE:
				$ = new MessageWrapper(getAddress(), data, from, TTalkMessageType.FRIEND_REQUEST_DECLINE.getValue());
				break;
			case LOGOUT:
				m_onlineUsers.remove(from);
				break;
			case IS_ONLINE:
				if (m_friendsLists.get(from).contains(data))
					$ = new MessageWrapper(getAddress(), from, m_onlineUsers.contains(data) ? "1" : "0",
					        TTalkMessageType.IS_ONLINE.getValue());
				else
					$ = new MessageWrapper(getAddress(), from, "-1", TTalkMessageType.IS_ONLINE.getValue());
				break;
			default:
				// TODO: exception?
				break;
		}
		return $;
	}
	
	
	/**
	 * Start the server action (The server will start listen and handle client requests).
	 * <br>
	 * If there's previous data - the server will load it on start.
	 */
	@SuppressWarnings("unchecked")
	@Override 
	public void start() {
		try {
			super.start();
			
			// loadResources();
			Path path = Paths.get("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_fl");
			byte[] data = Files.readAllBytes(path);
			m_friendsLists = (HashMap<String, Set<String>>) Serializer.deserialize(data);
			path = Paths.get("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_om");
			data = Files.readAllBytes(path);
			m_outgoingMessages = (HashMap<String, List<MessageWrapper>>) Serializer.deserialize(data);
		}
		catch (NoSuchFileException e){
			//Do nothing - there's no data to load
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * Stop the server action (the server will stop listening to clients).
	 * If there's data on the server - it will be saved to an external file.
	 */
	@Override 
	public void kill() {
		// saveResources();
		try {
			FileOutputStream out = new FileOutputStream("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_fl", false);
			out.write(Serializer.serialize(m_friendsLists));
			out.close();
			out = new FileOutputStream("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_om", false);
			out.write(Serializer.serialize(m_outgoingMessages));
			out.close();
			
			super.kill();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
}
