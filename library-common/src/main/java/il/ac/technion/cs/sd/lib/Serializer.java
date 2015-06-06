package il.ac.technion.cs.sd.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * A generic class to do a serialization process
 */
public class Serializer {
	
	/**
	 * Serialize the given object to an array of bytes
	 * @param o The object to serialize
	 * @return The object as an array of bytes
	 * @throws IOException In case there was a problem in the serialization process
	 */
	public static byte[] serialize(Object o) throws IOException  {
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutput out = null;
		byte[] bytes = null;
		try {
			out = new ObjectOutputStream(bos) ;
			out.writeObject(o);
			bytes = bos.toByteArray();
		}
		catch (IOException ex){
			throw new IOException(ex.getMessage());
		}
		finally {
			  try {
			    if (out != null) {
			      out.close();
			    }
			  } 
			  catch (IOException ex) {}
			  try {
			    bos.close();
			  } 
			  catch (IOException ex) {}
		}
		return bytes;
	}
	
	/**
	 * Deserialize the given array to an object
	 * @param byteData The array of bytes to deserialize
	 * @return The object after the deserialize process
	 * @throws IOException In case there was a problem in the deserialize process
	 */
	public static Object deserialize(byte[] byteData) throws IOException  {
		ByteArrayInputStream bis = new ByteArrayInputStream(byteData);
		ObjectInput in = null;
		Object obj = null;
		try {
		  in = new ObjectInputStream(bis);
		  obj = (Object)in.readObject();
		} catch (Exception ex) {
			throw new IOException(ex.getMessage());
		} finally {
		  try {
		    bis.close();
		  } catch (IOException ex) {}
		  try {
		    if (in != null) {
		      in.close();
		    }
		  } catch (IOException ex) {}
		}
		return obj;
	}
}
