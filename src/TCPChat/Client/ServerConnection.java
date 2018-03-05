/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TCPChat.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import TCPChat.ChatMessage;
import TCPChat.JsonTest;

/**
 *
 * @author brom
 */
public class ServerConnection {
	
	//Artificial failure rate of 30% packet loss
	static double TRANSMISSION_FAILURE_RATE = 0.3;
	
    private Socket m_socket = null;
    private InetAddress m_serverAddress = null;
    private int m_serverPort = -1;

    public ServerConnection(String hostName, int port) {
    	
	m_serverPort = port;
	
	try {
		m_serverAddress = InetAddress.getByName(hostName);
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	// TODO:
	// * get address of host based on parameters and assign it to m_serverAddress
	// * set up socket and assign it to m_socket
	
    }
  
    //Handshake to server
    public boolean handshake(String name) {
    	
    	try {
    		m_socket = new Socket(m_serverAddress,m_serverPort);
    	} catch (IOException e) {
    		e.printStackTrace();
    		// TODO Auto-generated catch block
    		System.err.println("ERROR: can not connect to server");
    		return false;
    	}
    	
    	sendChatMessage(new ChatMessage("handshake", name));    	
    	if (receiveChatMessage().getParameters().equals("successful")) {
    		System.out.println("handshake OK");
			return true;
		}
    	
    	// TODO:
    	// * marshal connection message containing user name
    	// * send message via socket
    	// * receive response message from server
    	// * unmarshal response message to determine whether connection was successful
    	// * return false if connection failed (e.g., if user name was taken)

	return false;
    }

    public void sendChatMessage(ChatMessage cm) {
    	
    	byte[] bytes = JsonTest.serializeChatMessage(cm);
    	try {
			DataOutputStream m_oStream = new DataOutputStream(m_socket.getOutputStream());
			m_oStream.writeInt(bytes.length);
			m_oStream.write(bytes);			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	/*
    	if (failure > TRANSMISSION_FAILURE_RATE){
			// TODO: 
			// * marshal message if necessary
			// * send a chat message to the server
    	} else {
    		// Message got lost
    	}
    	*/
    }


    public ChatMessage receiveChatMessage() {
    	byte[] cMessage = null;
    	try {
			DataInputStream m_iStream = new DataInputStream(m_socket.getInputStream());
			int length = m_iStream.readInt();
			if (length > 0) {
				cMessage = new byte[length];
				m_iStream.readFully(cMessage, 0, cMessage.length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	if(cMessage != null)
    		return JsonTest.deserializeChatmessage(cMessage);
    	
    	// TODO: 
    	// * receive message from server
    	// * unmarshal message if necessary
    	
    	// Note that the main thread can block on receive here without
    	// problems, since the GUI runs in a separate thread
    	
    	// Update to return message contents
    	
    	else
    		return null;
    }


    
}
