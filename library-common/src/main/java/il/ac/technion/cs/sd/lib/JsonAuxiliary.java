package il.ac.technion.cs.sd.lib;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * An auxiliary class to transfer the messageWrapper class from\to a JSON string
 */
public class JsonAuxiliary {
	private JSONObject m_jsonObject;
	
	public JsonAuxiliary() {
		m_jsonObject = new JSONObject();
	}
	
	/**
	 * Get a message wrapper data and return a corresponding JSON string format
	 * @param msgWrapper The messageWrapper to transfer to JSON
	 * @return A JSON string representing the message wrapper
	 */
	@SuppressWarnings("unchecked")
	public String messageWrapperToJson(MessageWrapper msgWrapper) {
		m_jsonObject.put("fromAddress", msgWrapper.getFromAddress());
		m_jsonObject.put("toAddress", msgWrapper.getToAddress());
		m_jsonObject.put("msgData", msgWrapper.getMeesageData());
		m_jsonObject.put("msgType", new Integer(msgWrapper.getMessageType()));
		return m_jsonObject.toJSONString();
	}
	
	/**
	 * Get a JSON string format and return a corresponding messageWrapper object
	 * @param jsonString The JSON string to transfer to a messageWrapper object
	 * @return A messageWrapper object representing the JSON string
	 * @throws ParseException In case there's a problem with the parsing of the JSON string
	 */
	public MessageWrapper jsonToMessageWrapper(String jsonString) throws ParseException {	
		  Object obj=JSONValue.parse(jsonString);
		  JSONObject jsonObject = (JSONObject) obj;
		  String fromAdr = (String) jsonObject.get("fromAddress");
		  String toAdr = (String) jsonObject.get("toAddress");
		  String messageData = (String) jsonObject.get("msgData");
		  int type = ((Long) jsonObject.get("msgType")).intValue();
		  return new MessageWrapper(fromAdr, toAdr, messageData, type);
	}
	
}
