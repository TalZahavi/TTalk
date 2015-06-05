package il.ac.technion.cs.sd.app.msg;

import java.io.FileOutputStream;
import java.io.IOException;
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
import il.ac.technion.cs.sd.msg.MessengerException;

public class TTalkServer extends Server {
	HashSet<String> m_onlineUsers;
	HashMap<String, Set<String>> m_friendsLists;
	HashMap<String, List<MessageWrapper>> m_outgoingMessages;
	
	public TTalkServer(String serverAddress) throws MessengerException {
		super(serverAddress);
		m_onlineUsers = new HashSet<String>();
		m_friendsLists = new HashMap<String, Set<String>>();
		m_outgoingMessages = new HashMap<String, List<MessageWrapper>>();
	}
	
	@Override public MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException {
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
	
	@SuppressWarnings("unchecked") @Override public void start() throws MessengerException {
		super.start();
		// loadResources();
		try {
			Path path = Paths.get("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_fl");
			byte[] data = Files.readAllBytes(path);
			m_friendsLists = (HashMap<String, Set<String>>) Serializer.deserialize(data);
			path = Paths.get("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_om");
			data = Files.readAllBytes(path);
			m_outgoingMessages = (HashMap<String, List<MessageWrapper>>) Serializer.deserialize(data);
		} catch (NoSuchFileException e) {
		} catch (Exception e) {
		}
	}
	
	@Override public void kill() throws MessengerException {
		// saveResources();
		try {
			FileOutputStream out = new FileOutputStream("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_fl", false);
			out.write(Serializer.serialize(m_friendsLists));
			out.close();
			out = new FileOutputStream("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_om", false);
			out.write(Serializer.serialize(m_outgoingMessages));
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.kill();
	}
	// private void loadResources() {
	// loadFriendsLists();
	// loadOutgoingMessages();
	// }
	//
	// private void loadFriendsLists() {
	// JSONParser parser = new JSONParser();
	// try {
	// JSONArray jArray = (JSONArray) parser.parse(new FileReader("..\\app-msg-server\\src\\main\\resources\\" + getAddress()
	// + "_fl"));
	// for (Object o : jArray) {
	// JSONObject $ = (JSONObject) o;
	// $.get("user");
	// }
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (ParseException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private void loadOutgoingMessages() {
	// // TODO Auto-generated method stub
	// }
	//
	// private void saveResources() {
	// saveFriendsLists();
	// saveOutgoingMessages();
	// }
	//
	// @SuppressWarnings({ "unchecked", "rawtypes" }) private void saveFriendsLists() {
	// try {
	// FileWriter file = new FileWriter("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_fl");
	// JSONArray jArray = new JSONArray();
	// try {
	// for (Entry<String, Set<String>> user : m_friendsLists.entrySet()) {
	// JSONObject $ = new JSONObject();
	// $.put("user", user.getKey());
	// for (String friend : user.getValue()) {
	// //$.get("user").put(user.getKey(), friend);
	// jArray.add($);
	// }
	// }
	// file.write(jArray.toJSONString());
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// file.flush();
	// file.close();
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// @SuppressWarnings("unchecked") private void saveOutgoingMessages() {
	// try {
	// FileWriter file = new FileWriter("..\\app-msg-server\\src\\main\\resources\\" + getAddress() + "_om");
	// JSONArray jArray = new JSONArray();
	// try {
	// for (Entry<String, List<MessageWrapper>> user : m_outgoingMessages.entrySet())
	// jArray.add(JsonAuxiliary.messageWrapperListToJson(user.getValue()));
	// file.write(jArray.toJSONString());
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// file.flush();
	// file.close();
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
}
