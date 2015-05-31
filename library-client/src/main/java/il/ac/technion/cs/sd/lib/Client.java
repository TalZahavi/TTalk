package il.ac.technion.cs.sd.lib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Represent a single client.
 * <br>
 * TODO: complete function.
 */
public class Client {
	private String m_serverAddress;
	private Messenger m_client;
	private BlockingQueue<String> clientIncomingMessages;
	
	/**
	 * Construct a new client.
	 * The client have a unique address and will register to a specific server.
	 * <br>
	 * Each client have a method for handling incoming messages from the server.
	 * @param address The address of the client
	 * @param serverAddress The address of the server
	 * @throws MessengerException In case there's a problem to initialize the client messenger
	 */
	public Client(String address, String serverAddress)
			throws MessengerException {
		m_serverAddress = serverAddress;
		clientIncomingMessages = new LinkedBlockingQueue<>();
		m_client = new MessengerFactory().start(address, (m,x) -> {
			try {
				clientIncomingMessages.put(x);
				if (x == null || !x.equals("")) {
					m.send(m_serverAddress, "");
				}
			} catch (Exception e) {}}); //Not Going to happen
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
	
	
	/**
	 * Sends the server a MessageWrapper object in a reliable way and return the data that
	 * got back from the server in response.
	 * <br>
	 * If there's no data to return from the server for that type of message - return null 
	 * @param toAddress
	 * @param data
	 * @param type
	 * @return
	 * @throws MessengerException
	 * @throws InterruptedException 
	 */
	public String sendMessageWithResult(String toAddress, String data, int type) throws MessengerException, InterruptedException {
		MessageWrapper msgWrap = new MessageWrapper(m_client.getAddress(),toAddress, data, type);
		
		JsonAuxiliary json = new JsonAuxiliary();
		String jsonMsg = json.messageWrapperToJson(msgWrap);
		
		String ack = null;
		while (ack == null) {
			m_client.send(m_serverAddress, jsonMsg);
			try {
				ack = clientIncomingMessages.poll(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new MessengerException(e.getMessage());
			}
		}
		
		String resultData = clientIncomingMessages.take();
		
		return resultData;
	}

}
