package TCPChat;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

//JsonTest was given and been changed

public class JsonTest {

	
	static public byte[] serializeChatMessage(ChatMessage c){
		
		ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
		ObjectOutput objectOutput = null;
		byte[] array;
		try {
			objectOutput = new ObjectOutputStream(byteOStream);
			objectOutput.writeObject(c);
			array = byteOStream.toByteArray();
			
			if (objectOutput != null) {
				objectOutput.close();
			}
			byteOStream.close();
			
		} catch (IOException e) {
			return null;
		}
		
		return array;
	}
	
	static public ChatMessage deserializeChatmessage(byte[] bytes){
		ByteArrayInputStream byteIStream = new ByteArrayInputStream(bytes);
		ObjectInput input = null;
		ChatMessage c;
		
		try {
			input = new ObjectInputStream(byteIStream);
			c = (ChatMessage) input.readObject();
			
			if (input != null) {
				input.close();
			}
			byteIStream.close();
			
		} catch (IOException e) {
			return null;
		}
		catch (ClassNotFoundException e) {
			return null;
		}
		
		return c;
	}
}
