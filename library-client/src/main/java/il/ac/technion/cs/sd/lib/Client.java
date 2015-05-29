package il.ac.technion.cs.sd.lib;

import java.util.function.Consumer;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Represent a single client.
 * <br>
 * TODO: complete function
 */
public class Client {
	private String m_serverAddress;
	private Messenger m_client;
	
	/**
	 * Construct a new client.
	 * The client have a unique address and will register to a specific server.
	 * <br>
	 * Each client have a method for handling incoming messages from the server.
	 * @param address The address of the client
	 * @param serverAddress The address of the server
	 * @param handleMessage The method to handle the incoming messages
	 * @throws MessengerException In case there's a problem to initialize the client messenger
	 */
	public Client(String address, String serverAddress, Consumer<String> handleMessage)
			throws MessengerException {
		m_client = new MessengerFactory().start(address, handleMessage);
		m_serverAddress = serverAddress;
	}
	
	/**
	 * Shut down the client.
	 * @throws MessengerException In case there's a problem to killing the client messenger
	 */
	public void stopClient() throws MessengerException{
		m_client.kill();
	}
	
	/**
	 * @return The client address
	 */
	public String getClientAddress() {
		return m_client.getAddress();
	}
	
	/**
	 * @return The address of the server that the client is registered to
	 */
	public String getServerAddress() {
		return m_serverAddress;
	}

}
