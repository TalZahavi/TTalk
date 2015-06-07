package il.ac.technion.cs.sd.lib;

import il.ac.technion.cs.sd.msg.MessengerException;

/**
 * This class just for the ServerTest.
 * Because Server is abstract - you have to implement handleMessage method
 */
public class ExtentedServer extends Server {

	public ExtentedServer(String serverAddress) {
		super(serverAddress);
	}

	@Override
	public MessageWrapper handleMessage(MessageWrapper msgWrapper)
			throws MessengerException {
		return null;
	}

}
