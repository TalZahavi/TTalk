package il.ac.technion.cs.sd.lib;

import il.ac.technion.cs.sd.msg.MessengerException;

/**
 * This class just for the ClientTest.
 * Because Client is abstract - you have to implement handleMessage method
 */
public class ExtentedClient extends Client {

	public ExtentedClient(String address, String serverAddress) {
		super(address, serverAddress);
	}

	@Override
	public MessageWrapper handleMessage(MessageWrapper msgWrapper)
			throws MessengerException {
		return null;
	}

}
