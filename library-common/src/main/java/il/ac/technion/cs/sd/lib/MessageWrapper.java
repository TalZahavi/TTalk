package il.ac.technion.cs.sd.lib;

import java.io.Serializable;

/**
 * MessageWrapper object wrap the message data (String)
 * with more details - address of the sender\receiver and the type of the message 
 */
public class MessageWrapper implements Serializable {

	private static final long serialVersionUID = -4434215640450204918L;
	private String m_fromAddress;
	private String m_toAddress;
	private String m_msgData;
	private int m_msgType;
	
	/**
	 * Construct a new message wrapper
	 * @param fromAddress The address of the client who sent the message
	 * @param toAddress The address of the client who the message if for
	 * @param msgData The data of the message (the actual message)
	 * @param msgType An integer representing the message type
	 */
	public MessageWrapper(String fromAddress, String toAddress, String msgData, int msgType) {
		m_fromAddress = fromAddress;
		m_toAddress = toAddress;
		m_msgData = msgData;
		m_msgType = msgType;
	}
	
	/**
	 * @return The address of the client who sent the message
	 */
	public String getFromAddress() {
		return m_fromAddress;
	}
	
	/**
	 * @return The address of the client who the message if for
	 */
	public String getToAddress() {
		return m_toAddress;
	}
	
	/**
	 * @return The data of the message (the actual message)
	 */
	public String getMessageData() {
		return m_msgData;
	}
	
	/**In your implementation - you can define an enum with different messages types
	 * @return An integer representing the message type
	 */
	public int getMessageType() {
		return m_msgType;
	}
}
