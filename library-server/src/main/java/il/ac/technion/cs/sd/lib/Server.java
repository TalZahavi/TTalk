package il.ac.technion.cs.sd.lib;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
	
	public Server(String serverAddress) throws MessengerException {
		m_address = serverAddress;
		serverIncomingMessages = new LinkedBlockingQueue<>();
		m_server = null;
	}
	
	/**
	 * Send the client a messageWrapper object. <br>
	 * In this case, the message in not an answer to a request of the client. The server is the one who decide the send the message
	 * first.
	 * 
	 * @param fromAddress
	 *            In case the message is originally from some other client (and not the server)
	 * @param toAddress
	 *            The address of the client to send the message to
	 * @param data
	 *            The data of the message
	 * @param type
	 *            The type of the message
	 * @throws MessengerException
	 *             In case there's a problem sending the client the message
	 */
	public void sendMessage(String toAddress, String data, int type) throws MessengerException {
		MessageWrapper msgWrap = new MessageWrapper(getAddress(), toAddress, data, type);
		String jsonMsg = JsonAuxiliary.messageWrapperToJson(msgWrap);
		String ack = null;
		while (ack == null) {
			m_server.send(toAddress, jsonMsg);
			try {
				ack = serverIncomingMessages.poll(200, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new MessengerException(e.getMessage());
			}
		}
	}
	
	/**
	 * Shut down the server.
	 * 
	 * @throws MessengerException
	 *             In case there's a problem to killing the server messenger
	 */
	public void kill() throws MessengerException {
		m_server.kill();
	}
	
	/**
	 * @return The address of the server
	 */
	public String getAddress() {
		return m_address;
	}
	
	/**
	 * An abstract class for handling messages. <br>
	 * In this method you should implement how the server handle each message type.
	 * 
	 * @param msgWrapper
	 * @return
	 */
	public abstract MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException;

	public void start() throws MessengerException {
		if (m_server == null)
			m_server = new MessengerFactory().start(getAddress(), (m, x) -> {
				try {
					System.out.println("Server received: " + x);
					if (x.equals(""))
						serverIncomingMessages.put(x);
					else {
						MessageWrapper msg = JsonAuxiliary.jsonToMessageWrapper(x);
						System.out.println("Server sending ACK to client " + msg.getFromAddress());
						m.send(msg.getFromAddress(), "");

						MessageWrapper sendBack = handleMessage(msg);
						if (sendBack != null) {
							System.out.println("Server received: " + sendBack);
							do {
								m.send(sendBack.getToAddress(), JsonAuxiliary.messageWrapperToJson(sendBack));
							} while (m.getNextMessage(100) == null);
						}
						
						
					}
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			});
    }

}
