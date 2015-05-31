package il.ac.technion.cs.sd.lib;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;
import java.util.function.Function;

public class Server {

	private String m_address;
	private Messenger m_server;
	
	public Server(String serverAddress, Function<MessageWrapper,String>handleMessageFunction)
			throws MessengerException {
		m_address = serverAddress;
		
		m_server = new MessengerFactory().start(serverAddress, (m,x) -> {
			JsonAuxiliary json = new JsonAuxiliary();
			MessageWrapper msgWrap = null;
			try {
				msgWrap = json.jsonToMessageWrapper(x);
			
				String senderAddress = msgWrap.getFromAddress();
			
				if (!x.equals(""))
					m.send(senderAddress, "");
			
				String sendBack = handleMessageFunction.apply(msgWrap);
			
				do {
					m.send(senderAddress, sendBack);
				}
				while(m.getNextMessage(100)==null);
			
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
		}});
	}
	
	public void stopServer() throws MessengerException {
		m_server.kill();
	}
	
	/**
	 * @return The address of the server
	 */
	public String getAddress() {
		return m_address;
	}
	
	
}
