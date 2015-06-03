package il.ac.technion.cs.sd.lib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Represent a single client who is registered to a specific server. <br>
 * The client can request the server for data (using a message that will be send to the server).
 */
public abstract class Client {
	private String m_serverAddress;
	private Messenger m_client;
	protected BlockingQueue<String> clientIncomingMessages;
	protected BlockingQueue<String> clientIncomingACKs;
	
	/**
	 * Construct a new client. The client have a unique address and will register to a specific server.
	 * 
	 * @param address
	 *            The address of the client
	 * @param serverAddress
	 *            The address of the server
	 * @throws MessengerException
	 *             In case there's a problem to initialize the client messenger
	 */
	public Client(String address, String serverAddress) throws MessengerException {
		m_serverAddress = serverAddress;
		clientIncomingMessages = new LinkedBlockingQueue<>();
		clientIncomingACKs = new LinkedBlockingQueue<>();
		m_client = new MessengerFactory().start(address, (m, x) -> {
			try {
				
				System.out.println("Client " + m_client.getAddress() + " received: " + (x.equals("") ? "ACK" : x));
				
				if (x.equals(""))
					clientIncomingACKs.put(x);
				else {
					System.out.println("Client " + m_client.getAddress() + " sending ACK to server");
					m.send(m_serverAddress, "");
					handleMessage(JsonAuxiliary.jsonToMessageWrapper(x));
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
	}
	
	/**
	 * Shut down the client.
	 * 
	 * @throws MessengerException
	 *             In case there's a problem to killing the client messenger
	 */
	public void stopClient() throws MessengerException {
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
	
	/**
	 * Sends the server a MessageWrapper object in a reliable way and return the data that got back from the server in response. <br>
	 * If there's no data to return from the server for that type of message - return null
	 * 
	 * @param toAddress
	 * @param data
	 *            The message data
	 * @param type
	 *            The message type
	 * @return The respond from the server (In case there's none - return null)
	 * @throws MessengerException
	 *             In case there's a problem sending the server the message
	 * @throws InterruptedException
	 *             In case the client got interrupted in the process
	 */
	public void sendMessage(String toAddress, String data, int type) throws MessengerException, InterruptedException {
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
			
			//System.out.println("ack is: " + ack);
			
		}
	}
	
	public String sendMessageAndWaitForResult(String toAddress, String data, int type) throws MessengerException,
	        InterruptedException {
		sendMessage(toAddress, data, type);
		String $ = clientIncomingMessages.take();
		System.out.println("Client " + getClientAddress()+ " checked queue and found: " + $);
		return $;
	}
	
	public void sendRequest(String data, int type) throws MessengerException, InterruptedException {
		sendMessage(getServerAddress(), data, type);
	}
	
	public String sendRequestAndWaitForResult(String data, int type) throws MessengerException, InterruptedException {
		return sendMessageAndWaitForResult(getServerAddress(), data, type);
	}
	
	/**
	 * An abstract class for handling messages. <br>
	 * In this method you should implement how the server handle each message type.
	 * 
	 * @param msgWrapper
	 * @return Whether 
	 */
	public abstract void handleMessage(MessageWrapper msgWrapper) throws MessengerException;
}
