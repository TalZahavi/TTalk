package il.ac.technion.cs.sd.lib;

/**
* SimpleMessageTypes will represent the different messages\requests
*  types that the client can send the server
*/
public enum SimpleMessageTypes {
	REQUEST_DATA(0),
	REQUEST_DATA_AND_RESULT(1),
	MESSAGE(2);

   private final int value;
	
	private SimpleMessageTypes(int value) {
       this.value = value;
   }

   public int getValue() {
       return value;
   }
}
