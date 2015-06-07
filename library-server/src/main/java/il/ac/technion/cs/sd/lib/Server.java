package il.ac.technion.cs.sd.lib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Each client can register to the server. <br>
 * The server will get the clients requests messages and send back the corresponding result. <br>
 * The server is an abstract class - In order to use it the user have to implement the handleMessage method.
 */
public abstract class Server {
	private String m_address;
	private Messenger m_server;
	private BlockingQueue<String> serverIncomingMessages;
	
	
	/**
	 * Represent a server that the clients will register to.
	 * @param serverAddress The server unique address
	 */
	public Server(String serverAddress) {
		m_address = serverAddress;
		serverIncomingMessages = new LinkedBlockingQueue<>();
		m_server = null;
	}
	
	/**
	 * Represent a server that the clients will register to.
	 * @param serverAddress The server unique address
	 * @param server	The messenger that will be used for communication
	 */
	public Server(String serverAddress, Messenger server) {
		this(serverAddress);
		m_server = server;
	}
	
	/**
	 * Start up the server - from now the clients get send messages\request to the server
	 * (Also the server can send messages to the clients).
	 * <br>
	 * The server can handle incoming messages (using the handleMessage method) and send the client
	 * a result data in response (in case the handleMessage return actual data).
	 * @throws MessengerException In case there's a problem in the initialization of the server messenger
	 * @throws RuntimeException In case there's a problem sending the client the result
	 * (or handle the incoming message)
	 */
	public void start() throws MessengerException {
		if (m_server == null)
			m_server = new MessengerFactory().start(getAddress(), (m, x) -> {
				try {
					if (x.equals(""))
						serverIncomingMessages.put(x);
					else {
						MessageWrapper msg = JsonAuxiliary.jsonToMessageWrapper(x);
						m.send(msg.getFromAddress(), "");
						MessageWrapper sendBack = handleMessage(msg);
						if (sendBack != null) {
							do {
								m.send(sendBack.getToAddress(), JsonAuxiliary.messageWrapperToJson(sendBack));
							} while (m.getNextMessage(100) == null);
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
    }
	
	/**
	 * Shut down the server.
	 * The server can't get or send any messages\requests.
	 * @throws MessengerException In case there's a problem to killing the server messenger
	 */
	public void kill() throws MessengerException {
		m_server.kill();
		m_server = null;
	}
	
	/**
	 * An abstract class for handling messages.
	 * <br>
	 * In this method you should implement how the server handle each message type.
	 * @param msgWrapper The messageWrppaer object to handle
	 * @return A result in a format of messageWrapper. If there's no result - you should return null!
	 * @throws MessengerException
	 */
	public abstract MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException;
	
	/**
	 * Represent a server that the clients will register to.
	 * @param serverAddress The server unique address
	 * @param server	The messenger that will be used for communication
	 */
	public BlockingQueue<String> getServerIncomingMessages() {
		return serverIncomingMessages;
	}
	
	/**
	 * @return The address of the server
	 */
	public String getAddress() {
		return m_address;
	}
}
