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
	private BiConsumer<String, Boolean> m_friendshipRequestConsumer;
	
	public TTalkClient(String address, String serverAddress) throws MessengerException {
		super(address, serverAddress);
		this.m_isLoggedIn = false;
	}
	
	public void login() {
		this.m_isLoggedIn = true;
		try {
	        sendRequest(null, TTalkMessageType.LOGIN.getValue());
        } catch (MessengerException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}
	
	public void logout() {
		this.m_isLoggedIn = false;
		try {
	        sendRequest(null, TTalkMessageType.LOGOUT.getValue());
        } catch (MessengerException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (InterruptedException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
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
		this.m_friendshipRequestConsumer = m_friendshipReplyConsumer;
	}
	
	public void sendMessage(String to, String data) {
		try {
			sendMessage(to, data, TTalkMessageType.SEND.getValue());
		} catch (MessengerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void requestFriendship(String who) {
		try {
			sendMessage(who, null, TTalkMessageType.FRIEND_REQUEST.getValue());
		} catch (MessengerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void acceptFriendship(String who) {
		try {
			sendRequest(who, TTalkMessageType.FRIEND_REQUEST_ACCEPT.getValue());
		} catch (MessengerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void declineFriendship(String who) {
		try {
			sendRequest(who, TTalkMessageType.FRIEND_REQUEST_DECLINE.getValue());
		} catch (MessengerException | InterruptedException e) {
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
	
	@Override public void handleMessage(MessageWrapper msgWrapper) throws MessengerException {
		if (msgWrapper == null) {
			return;
		}
		TTalkMessage tmsg = new TTalkMessage(msgWrapper.getFromAddress(), msgWrapper.getToAddress(), msgWrapper.getMessageData(),
		        TTalkMessageType.values()[msgWrapper.getMessageType()]);
		switch (TTalkMessageType.values()[msgWrapper.getMessageType()]) {
			case SEND:
				m_messageConsumer.accept(tmsg);
				break;
			case FRIEND_REQUEST:
				String requestFrom = tmsg.getMessageData();
				boolean shouldAccept = m_friendshipRequestHandler.apply(requestFrom);
				m_friendshipRequestConsumer.accept(requestFrom, shouldAccept);
				if (shouldAccept)
					acceptFriendship(requestFrom);
				else
					declineFriendship(requestFrom);
				break;
			case IS_ONLINE:
				System.out.println("Client " + getClientAddress()+ " is putting IS_ONLINE answer on queue");
				try {
	                clientIncomingMessages.put(tmsg.getMessageData());
                } catch (InterruptedException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
				break;
			default:
				// TODO: exception?
				break;
		}
	}
}
