package il.ac.technion.cs.sd.lib;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * An auxiliary class to transfer the messageWrapper class from\to a JSON string
 */
public class JsonAuxiliary {
	/**
	 * Get a message wrapper data and return a corresponding string in a JSON format
	 * 
	 * @param msgWrapper
	 *            The messageWrapper to transfer to JSON
	 * @return A JSON string representing the message wrapper
	 */
	@SuppressWarnings("unchecked") public static String messageWrapperToJson(MessageWrapper msgWrapper) {
		JSONObject $ = new JSONObject();
		$.put("fromAddress", msgWrapper.getFromAddress());
		$.put("toAddress", msgWrapper.getToAddress());
		$.put("msgData", msgWrapper.getMessageData());
		$.put("msgType", new Integer(msgWrapper.getMessageType()));
		return $.toJSONString();
	}
	
	/**
	 * Get a string in JSON format and return a corresponding messageWrapper object
	 * 
	 * @param jsonString
	 *            The JSON string to transfer to a messageWrapper object
	 * @return A messageWrapper object representing the JSON string
	 * @throws ParseException
	 *             In case there's a problem with the parsing of the JSON string
	 */
	public static MessageWrapper jsonToMessageWrapper(String jsonString) throws ParseException {
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
		String fromAdr = (String) jsonObject.get("fromAddress");
		String toAdr = (String) jsonObject.get("toAddress");
		String messageData = (String) jsonObject.get("msgData");
		int type = ((Long) jsonObject.get("msgType")).intValue();
		return new MessageWrapper(fromAdr, toAdr, messageData, type);
	}
	
	@SuppressWarnings("unchecked") public static String messageWrapperListToJson(List<MessageWrapper> l) {
		JSONArray $ = new JSONArray();
		for (MessageWrapper m : l) {
			$.add(messageWrapperToJson(m));
		}
		return $.toJSONString();
	}
	
	public static List<MessageWrapper> jsonToMessageWrapperList(String s) throws ParseException {
		JSONArray jArray = (JSONArray) JSONValue.parse(s);
		List<MessageWrapper> $ = new ArrayList<MessageWrapper>(); 
		for (Object o : jArray) {
			$.add(jsonToMessageWrapper((String)o));
		}
		return $;
	}
}
