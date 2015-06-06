package il.ac.technion.cs.sd.lib;

import il.ac.technion.cs.sd.msg.MessengerException;

/**
 * In order to use the client class, you have to implement a class that extends
 * the client class and implement its handleMessage method.
 * The extended class will define how to handle each type of message.
 * <br>
 * The SimpleClient use the methods of the Client class for registering and sending messages to the server
 */
public class SimpleClient extends Client{
	
	//Just for testing
	private String messagesCounter;

	public SimpleClient(String address, String serverAddress) {
		super(address,serverAddress);
		messagesCounter = "Off";
	}
	
	public String getMessagesCounter() {
		return messagesCounter;
	}
	
	/** If the client wait for the result - the client must put the result in the incomingMessages queue**/
	@Override 
	public MessageWrapper handleMessage(MessageWrapper msgWrapper) throws MessengerException {
		MessageWrapper msgWrap = null;
		int type = msgWrapper.getMessageType();
		if (type == SimpleMessageTypes.REQUEST_DATA_AND_RESULT.getValue()) {
			try {
				//This type of message wait for a result - so the client put the result in the incoming
				clientIncomingMessages.put(msgWrapper.getMessageData());
			} catch (InterruptedException e) {
				throw new MessengerException(e.getMessage());
			}
		}
		//The client got a message from a different client
		if (type == SimpleMessageTypes.MESSAGE.getValue()) {
			messagesCounter = "On";
		}
		
		return msgWrap;
		
	}
}
