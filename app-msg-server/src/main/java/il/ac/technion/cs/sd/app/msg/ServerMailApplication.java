package il.ac.technion.cs.sd.app.msg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import il.ac.technion.cs.sd.msg.MessengerException;


/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerMailApplication {
	
	TTalkServer m_server;
	
    /**
     * Starts a new mail server. Servers with the same name retain all their information until
     * {@link ServerMailApplication#clean()} is called.
     *
     * @param name The name of the server by which it is known.
     */

	public ServerMailApplication(String string) {
		try {
	        m_server = new TTalkServer(string);
        } catch (MessengerException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return m_server.getAddress();
	}
	
	/**
	 * Starts the server; any previously sent mails, data and indices are loaded.
	 * This should be a <b>non-blocking</b> call.
	 */
	public void start() {
		try {
	        m_server.start();
        } catch (MessengerException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}
	
	/**
	 * Stops the server. A stopped server can't accept messages, but doesn't delete any data (messages that weren't received).
	 */
	public void stop() {
		try {
	        m_server.kill();
        } catch (MessengerException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		try {
			Files.deleteIfExists(Paths.get("..\\app-msg-server\\src\\main\\resources\\" + m_server.getAddress() + "_fl"));
			Files.deleteIfExists(Paths.get("..\\app-msg-server\\src\\main\\resources\\" + m_server.getAddress() + "_om"));
        } catch (IOException e) {
        	throw new RuntimeException();
        }
	}
}
