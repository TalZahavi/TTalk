package il.ac.technion.cs.sd.lib;


/**
 * In order to use the server class, you have to implement a class that extends
 * the server class and implement its handleMessage method.
 * The extended class will define how to handle each message\request according to the message type.
 * <br>
 * The SimpleServer use the methods of the Server class for listening and sending messages to the clients
 *
 */
public class SimpleServer extends Server{
	
	//Only for testing
	private String requestCounter;

	public SimpleServer(String serverAddress) {
		super(serverAddress);
		requestCounter = "Off";
	}

	public String getRequestCounter() {
		return requestCounter;
	}
	
	/**
	 * In case you don't want the server to send something to another client - leave msgWrap null
	 */
	@Override
	public MessageWrapper handleMessage(MessageWrapper msgWrapper){
		MessageWrapper msgWrap = null;
		
		int type = msgWrapper.getMessageType();
		String data = msgWrapper.getMessageData();
		if (type == SimpleMessageTypes.REQUEST_DATA_AND_RESULT.getValue()) {
			//For example, we decide the server append "Server Answer!" to the data (for this kind of message)
			msgWrap = new MessageWrapper(getAddress(),"client",data + " Server Answer!",type);
		}
		if (type == SimpleMessageTypes.REQUEST_DATA.getValue()) {
			//For example, we decide the server will only change the counter
			requestCounter = "On";
		}
		if (type == SimpleMessageTypes.MESSAGE.getValue()) {
			//For example, we decide the server will only change the counter of the other client
			msgWrap = new MessageWrapper(getAddress(),msgWrapper.getToAddress(),"",type);
		}
		return msgWrap;
	}
}
