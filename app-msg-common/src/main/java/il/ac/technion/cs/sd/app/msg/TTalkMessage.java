package il.ac.technion.cs.sd.app.msg;

import il.ac.technion.cs.sd.lib.MessageWrapper;

public class TTalkMessage extends MessageWrapper {

	public TTalkMessage(String fromAddress, String toAddress, String msgData, TTalkMessageType msgType) {
	    super(fromAddress, toAddress, msgData, msgType.getValue());
    }
	
}
