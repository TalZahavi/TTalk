package il.ac.technion.cs.sd.app.msg;

/**
* TTalkMessageType will represent the different messages types that the client can send the server
* @author Boaz
*
*/
public enum TTalkMessageType {
	SEND(0),
	LOGIN(1),
	FRIEND_REQUEST(2),
	FRIEND_REQUEST_ACCEPT(3),
	FRIEND_REQUEST_DECLINE(4),
	LOGOUT(5),
	IS_ONLINE(6),
	RETREIVE(7);

   private final int value;
	
	private TTalkMessageType(int value) {
       this.value = value;
   }

   public int getValue() {
       return value;
   }
}