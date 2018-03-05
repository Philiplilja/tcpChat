/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TCPChat.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import TCPChat.ChatMessage;
import TCPChat.JsonTest;

/**
 * 
 * @author brom
 */
public class ClientConnection extends Thread {
	
	private final String m_name;
	private final Socket socket;

	private boolean disconnected = false;
	
	//Client can mute other clients
	private boolean clientMuted = false;
	
	public boolean disconnected(){
		return disconnected;
	}
	
	public void run() {
		do {

			if(disconnected)
				return;
	
			ChatMessage c = receiveChatMessage();
			
			if (c != null) {
				
				System.out.println("Got " + "\"" +  c.getCommand() + "\" with parameters: " + "\"" + c.getParameters() + "\"" + " from user: " + m_name);
				
				
				switch (c.getCommand()) {
				case "broadcast":
					Server.broadcast(new ChatMessage("broadcast", "<" + m_name + "> " + c.getParameters()));
					break;
				case "stopListening":
					if (c.getParameters().equals("true")) {
						sendMessage(new ChatMessage("stopListening", "You are now on mute"));
						clientMuted = true;
					}
					else
					{
						clientMuted = false;
						sendMessage(new ChatMessage("stopListening", "You are now unmute"));
						
					}
					break;
				case "list":
					String text = "USERS: \n";
					for (ClientConnection cc : Server.m_connectedClients) {
							text += "{" + cc.getUsername() + "}\n";
					}
					sendMessage(new ChatMessage("list", text));
					break;
				case "qotd":
					sendMessage(new ChatMessage("qotd", "QUOTE: " + "\"" + QuoteHandler.getQuote() + "\"" )     );
					break;
				
				case "leave":
					Server.broadcast(new ChatMessage("broadcast", "<server> " + m_name + " is now disconnected"));
					
					//Client can safely disconnect from socket
					boolean successful = sendMessage(new ChatMessage("leave", ""));
					
					//Client crash when it tries to disconnect. Will not be removed
					if(successful)
						Server.removeClient(m_name);
					return;

				default:
					String[] splitCommand = c.getCommand().split(" ");
					if (splitCommand[0].equals("tell")) {
						Server.sendPrivateMessage(new ChatMessage(c.getCommand(),"<"  + m_name + ">"  + c.getParameters()), splitCommand[1]);
					}
					
					break;
				}
			}
				else {
					System.out.println(m_name + ": null message received");
				}
			
			} while (true);
	}

	public ClientConnection(String name, Socket m_socket) {
		m_name = name;
		socket = m_socket;
		start();
		Server.broadcast(new ChatMessage("broadcast", "<server>" + m_name + "  is now in the chat" ));
		
	}

	private ChatMessage receiveChatMessage() {
    	byte[] cMessage = null;
    	try {
			DataInputStream m_iStream = new DataInputStream(socket.getInputStream());
			int length = m_iStream.readInt();
			if (length > 0) {
				cMessage = new byte[length];
				m_iStream.readFully(cMessage, 0, cMessage.length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			disconnected = true;
			return null;
		}
    	
    	if(cMessage != null)
    		return JsonTest.deserializeChatmessage(cMessage);
    	else
    		return null;
    }
	
	
	public boolean sendMessage(ChatMessage c) {
		//If disconnected or mute don't send message
		if (disconnected || clientMuted) {
			return false;
		}
		byte[] bytes = JsonTest.serializeChatMessage(c);
    	try {
			DataOutputStream m_oStream = new DataOutputStream(socket.getOutputStream());
			m_oStream.writeInt(bytes.length);
			m_oStream.write(bytes);	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			disconnected = true;
			return false;
		}
		return true;
	}
	
	public boolean hasUserName(String testName) {
		return testName.equals(m_name);
	}
		
	public String getUsername(){
		  return m_name;
	  }

	
}
