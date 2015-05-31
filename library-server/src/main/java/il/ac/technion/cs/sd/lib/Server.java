package il.ac.technion.cs.sd.lib;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Each client can register to the server.
 * <br>
 * The server will get the clients requests messages and send back the corresponding result.
 * <br>
 * The server is an abstract class - In order to use it the user have to implement
 * the handleMessage method.
 */
public abstract class Server {

	private String m_address;
	private Messenger m_server;
	
	public Server(String serverAddress)
			throws MessengerException {
		m_address = serverAddress;
		
		m_server = new MessengerFactory().start(serverAddress, (m,x) -> {
			JsonAuxiliary json = new JsonAuxiliary();
			MessageWrapper msgWrap = null;
			try {
				msgWrap = json.jsonToMessageWrapper(x);
			
				String senderAddress = msgWrap.getFromAddress();
				
				if (!x.equals(""))
					m.send(senderAddress, "");
			
				String sendBack = handleMessage(msgWrap);
				
			
				do {
					m.send(senderAddress, sendBack);
				}
				while(m.getNextMessage(100)==null);
			
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
		}});
	}
	
	/**
	 * Shut down the server.
	 * @throws MessengerException In case there's a problem to killing the server messenger
	 */
	public void stopServer() throws MessengerException {
		m_server.kill();
	}
	
	/**
	 * @return The address of the server
	 */
	public String getAddress() {
		return m_address;
	}
	
	/**
	 * An abstract class for handling messages.
	 * <br>
	 * In this method you should implement how the server handle each message type.
	 * @param msgWrapper
	 * @return
	 */
	public abstract  String handleMessage(MessageWrapper msgWrapper);
	
}
