package TCPChat.Server;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import TCPChat.ChatMessage;
import TCPChat.JsonTest;

public class Server {
	
    public static ArrayList<ClientConnection> m_connectedClients = new ArrayList<ClientConnection>();
    private ServerSocket s_Socket;

    public static void main(String[] args){
	if(args.length < 1) {
	    System.err.println("Usage: java Server portnumber");
	    System.exit(-1);
	}
	
	QuoteHandler.initialize();
	
	try {
	    Server instance = new Server(Integer.parseInt(args[0]));
	    instance.listenForClientConnection();
	} catch(NumberFormatException e) {
	    System.err.println("Error: port number must be an integer.");
	    System.exit(-1);
	}
	
	
	
	
    }
    
    

    private Server(int portNumber) {
	//create a socket, attach it to port based on portNumber, and assign it to m_socket
    	try {
			s_Socket = new ServerSocket(portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void listenForClientConnection() {
	System.out.println("Waiting for client messages... ");

	do {
	   
		Socket cSocket = null;
		try {
			cSocket = s_Socket.accept();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(cSocket != null)
		clientHandshake(cSocket);
		
		// TODO: Listen for client messages.
	    // On reception of message, do the following:
	    // * Unmarshal message
	    // * Depending on message type, either
	    //    - Try to create a new ClientConnection using addClient(), send 
	    //      response message to client detailing whether it was successful
	    //    - Broadcast the message to all connected users using broadcast()
	    //    - Send a private message to a user using sendPrivateMessage()
		
	} while (true);
    }
    
    public boolean addClient(String name, Socket m_socket) {
    	
	ClientConnection c;
	for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
	    c = itr.next();
	    if(c.hasUserName(name)) {
	    	//Client has crasched, will be added again
	    	if (c.disconnected()) {
	    		removeClient(c.getUsername());
	    		m_connectedClients.add(new ClientConnection(name, m_socket));
				return true;
			}
	    	else
	    		return false; // Already exists a client with this name
	    }
	}
	m_connectedClients.add(new ClientConnection(name, m_socket));
	
	return true;
    }
    
    //get username and checks if client can join or not
    private void clientHandshake(Socket m_socket){
    	ChatMessage userack = receiveChatMessage(m_socket);
    	
    	//Handshake success
    	if (addClient(userack.getParameters(), m_socket)) {
			sendMessage(new ChatMessage("handshake", "successful"), m_socket);
			sendMessage(new ChatMessage("broadcast", "<server>" + "You are now in the chat" ), m_socket);
		}
    	//Handshake failed
    	else {
    		sendMessage(new ChatMessage("handshake", "failed"), m_socket);
		}
    }
    
    public static synchronized void removeClient(String username){
		
		for (int i = 0; i < m_connectedClients.size(); i++) {
			if(m_connectedClients.get(i).hasUserName(username)){
				m_connectedClients.remove(i);
				return;
			
			
			}
		}
	}
		
    
    private void sendMessage(ChatMessage c, Socket socket) {
		
		byte[] bytes = JsonTest.serializeChatMessage(c);
    	try {
			DataOutputStream m_oStream = new DataOutputStream(socket.getOutputStream());
			m_oStream.writeInt(bytes.length);
			m_oStream.write(bytes);			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
    public static synchronized boolean sendPrivateMessage(ChatMessage c, String name) {
		ClientConnection cConnect;
		for(Iterator<ClientConnection> itr = m_connectedClients.iterator(); itr.hasNext();) {
		    cConnect = itr.next();
		    if(cConnect.hasUserName(name)) {
			cConnect.sendMessage(c);
			return true;
		    }
		}
		return false;
		}
    
    
    
	
	
	public static synchronized void broadcast(ChatMessage c) {
		for(Iterator<ClientConnection> itr = Server.m_connectedClients.iterator(); itr.hasNext();) {
		    itr.next().sendMessage(c);
		}
		}
		
	private ChatMessage receiveChatMessage(Socket socket) {
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
			e.printStackTrace();
		}
    	
    	if(cMessage != null)
    		return JsonTest.deserializeChatmessage(cMessage);
    	else
    		return null;
    }
		

	
    




}
