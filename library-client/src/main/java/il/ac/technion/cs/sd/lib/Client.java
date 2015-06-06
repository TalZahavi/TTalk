package il.ac.technion.cs.sd.lib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Represent a single client who is registered to a specific server. <br>
 * The client can request the server for different data (using a message that will be send to the server).
 * The client is an abstract class - In order to use it the user have to implement the handleMessage method.
 */
public abstract class Client {
	private String m_serverAddress;
	private String m_clientAddress;
	private Messenger m_client;
	protected BlockingQueue<String> clientIncomingMessages;
	protected BlockingQueue<String> clientIncomingACKs;
	
	/**
	 * Construct a new client.
	 * The client have a unique address and will register to a specific server.
	 * @param address
	 *            The unique address of the client
	 * @param serverAddress
	 *            The address of the server (that the client will register to)
	 */
	public Client(String address, String serverAddress) {
		m_serverAddress = serverAddress;
		m_clientAddress = address;
		clientIncomingMessages = new LinkedBlockingQueue<>();
		clientIncomingACKs = new LinkedBlockingQueue<>();
		m_client = null;
	}
	
	/**
	 * Register the client to the server and start the listening\sending messages process.
	 * <br>
	 * The client can handle incoming messages (using the handleMessage method) and send the server
	 * a result data in response (in case the handleMessage return actual data).
	 * @throws MessengerException In case there's a problem in the initialization of the client messenger
	 * @throws RuntimeException In case there's a problem sending the server the result
	 * (or handle the incoming message)
	 */
	public void start() throws MessengerException {
		if (m_client == null)
			m_client = new MessengerFactory().start(getClientAddress(), (m, x) -> {
				try {	
					if (x.equals(""))
						clientIncomingACKs.put(x);
					else {
						m.send(m_serverAddress, "");		
						MessageWrapper sendBack = handleMessage(JsonAuxiliary.jsonToMessageWrapper(x));
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
	 * Shut down the client.
	 * The client can't get or send any messages.
	 * @throws MessengerException In case there's a problem to killing the client messenger
	 */
	public void stopClient() throws MessengerException {
		m_client.kill();
		m_client = null;
	}
	
	/**
	 * @return The client address
	 */
	public String getClientAddress() {
		return m_clientAddress;
	}
	
	/**
	 * @return The address of the server that the client is registered to
	 */
	public String getServerAddress() {
		return m_serverAddress;
	}
	
	/**
	 * Send the server a message in a safe way (the server will definitely get the message).
	 * <br>
	 * The message is for another client.
	 * @param toAddress The address of the other client
	 * @param data The data of the message
	 * @param type The type of the message (you should use an enum)
	 * @throws MessengerException In case there's a problem sending the message to the server 
	 * (or someone interrupt the client while waiting for a confirmation from the server)
	 */
	public void sendMessage(String toAddress, String data, int type) throws MessengerException {
		MessageWrapper msgWrap = new MessageWrapper(m_client.getAddress(), toAddress, data, type);
		String jsonMsg = JsonAuxiliary.messageWrapperToJson(msgWrap);
		String ack = null;
		while (ack == null) {
			m_client.send(m_serverAddress, jsonMsg);
			try {
				ack = clientIncomingACKs.poll(200, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new MessengerException(e.getMessage());
			}
		}
	}
	
	/**
	 * Same as sendMessage method, but in this case the client wait for result from the server.
	 * You should not use this method if your message type don't need a result back
	 * (The client will be blocked until a result came back)
	 * @param toAddress The address of the other client
	 * @param data The data of the message
	 * @param type The type of the message (you should use an enum)
	 * @return The result from the server
	 * @throws MessengerException In case there's a problem sending the message to the server 
	 * @throws InterruptedException In case someone interrupt the client while waiting for a result
	 */
	public String sendMessageAndWaitForResult(String toAddress, String data, int type) throws MessengerException, InterruptedException {
		sendMessage(toAddress, data, type);
		String $ = clientIncomingMessages.take();
		return $;
	}
	
	/**
	 * Send the server a request in a safe way (the server will definitely get the request).
	 * @param data The data of the request
	 * @param type The type of the request
	 * @throws MessengerException In case there's a problem sending the message to the server 
	 * (or someone interrupt the client while waiting for a confirmation from the server)
	 */
	public void sendRequest(String data, int type) throws MessengerException {
		sendMessage(getServerAddress(), data, type);
	}
	
	/**
	 * Same as sendRequest method, but in this case the client wait for result from the server.
	 * You should not use this method if your request  don't need a result back.
	 * @param data The data of the request
	 * @param type The type of the request
	 * @return The result from the server
	 * @throws MessengerException In case there's a problem sending the request to the server
	 * @throws InterruptedException In case someone interrupt the client while waiting for a result
	 */
	public String sendRequestAndWaitForResult(String data, int type) throws MessengerException, InterruptedException {
		return sendMessageAndWaitForResult(getServerAddress(), data, type);
	}
	
	/**
	 * An abstract class for handling messages.
	 * <br>
	 * In this method you should implement how the server handle each message type.
	 * @param msgWrapper The messageWrapper object to handle
	 * @return A result in a format of messageWrapper. If there's no result - you should return null!
	 * @throws MessengerException
	 */
	public abstract MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException;
}
