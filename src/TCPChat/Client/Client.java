package TCPChat.Client;

import java.awt.event.*;

import TCPChat.ChatMessage;

public class Client implements ActionListener {

    private String m_name = null;
    private final ChatGUI m_GUI;
    private ServerConnection m_connection = null;
    private boolean wantToReJoin = false;
    public static void main(String[] args) {
	if(args.length < 3) {
	    System.err.println("Usage: java Client serverhostname serverportnumber username");
	    System.exit(-1);
	}

	try {
	    Client instance = new Client(args[2]);
	    instance.connectToServer(args[0], Integer.parseInt(args[1]));
	} catch(NumberFormatException e) {
	    System.err.println("Error: port number must be an integer.");
	    System.exit(-1);
	}
	
	
	
    }

    private Client(String userName) {	
	m_name = userName;

	// Start up GUI (runs in its own thread)
	m_GUI = new ChatGUI(this, m_name);
    }

    private void connectToServer(String hostName, int port) {
	do {
		//Create a new server connection
		m_connection = new ServerConnection(hostName, port);
		if(m_connection.handshake(m_name)) {		
		    listenForServerMessages();
		}
		else {
		    System.err.println("handshake failed");
		}
		
		m_connection = null;
		
		//Client can close application or write /join to join the chat again
		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (!wantToReJoin);
		wantToReJoin = false;
		
	} while (true);
	
    }

    private void listenForServerMessages() {
	
	do {
		
		ChatMessage c = m_connection.receiveChatMessage();
		
		if(c.getCommand().equals("leave"))
		{
			m_GUI.displayMessage("You're disconnected from the chat, exit application or write /join to join the chat");
			return;
		}
		
	    m_GUI.displayMessage(c.getParameters());
	} while(true);
    }

    // Sole ActionListener method; acts as a callback from GUI when user hits enter in input field
    @Override
    public void actionPerformed(ActionEvent e) {
	
    String i = m_GUI.getInput();
	
	if(i.equals(""))
    	return;
	else if(i.equals("/join"))
		wantToReJoin = true;
    	
    	String[] iSplit  = i.split(" ");
    	
    	
    if (m_connection != null) {
		
    	switch (iSplit[0]) {
    	
    	//muting others
    	case "/stopListening":
    		if(iSplit.length == 2){
				if (iSplit[1].equals("true")) {
					m_connection.sendChatMessage(new ChatMessage("stopListening", "true"));
				}
				else if (iSplit[1].equals("false")) {
					m_connection.sendChatMessage(new ChatMessage("stopListening", "false"));
				}
				
			}
			else {
    			m_GUI.displayMessage("Wrong input, try again");
			}
    	break;
    	
    	//Leaving the chat
    	case "/leave":
    		if(iSplit.length == 1){
				m_connection.sendChatMessage(new ChatMessage("leave", ""));
    			
			}
			else {
    			m_GUI.displayMessage("Wrong input, try again");
			}

    		break;
    		
    		//The help funktion
    	case "/help":
    		if(iSplit.length == 1){
    			m_GUI.displayMessage("help join leave broadcast tell list qotd stopListening");
			}
    		else if(iSplit.length == 2){
    			if(iSplit[1].equals("qotd")){
        			m_GUI.displayMessage("Returns quote of the day from the server");
        		}
        		else if(iSplit[1].equals("broadcast")){
        			m_GUI.displayMessage("Sends a message to everyone in the chat");
        		}
        		else if(iSplit[1].equals("list")){
        			m_GUI.displayMessage("Gives a list with all the users");
        		}
        		else if(iSplit[1].equals("leave")){
        			m_GUI.displayMessage("This command will disconnect you from the chat");
        		}
        		else if(iSplit[1].equals("join")){
        			m_GUI.displayMessage("This command will connect you to the chat");
        		}
        		else if(iSplit[1].equals("tell")){
        			m_GUI.displayMessage("This command will send a private message to a user");
        		}
        		else if(iSplit[1].equals("stopListening")){
        			m_GUI.displayMessage("This command is /stoplistening true or /stoplistening false. True for mute and false for listen");
        		}
        		else {
        			m_GUI.displayMessage("No command with that name");
    			}
    		}
			else {
    			m_GUI.displayMessage("Wrong input, try again");
			}
    		break;
    		//Lists all the clients
    	case "/list":
    		if(iSplit.length == 1){
    			m_connection.sendChatMessage(new ChatMessage("list", ""));
			}
			else {
    			m_GUI.displayMessage("Wrong input, try again");
			}
    		break;
    		//quote of the day
    	case "/qotd":
    		if(iSplit.length == 1){
    			m_connection.sendChatMessage(new ChatMessage("qotd", ""));
			}
			else {
    			m_GUI.displayMessage("Wrong input, try again");
			}
    		break;
    	//Private message
    	case "/tell":
    		if(iSplit.length > 2){
    			String messageText = i.substring( iSplit[0].length() + iSplit[1].length() + 2, i.length() );
				m_connection.sendChatMessage(new ChatMessage("tell " + iSplit[1], messageText) );
    		}
			else{
    			m_GUI.displayMessage("Wrong input, try again");
			}
    		break;
    		//else is broadcast
    	default:
    		m_connection.sendChatMessage(new ChatMessage("broadcast", i));
    		break;
    	}

	}
    else if (!i.equals("/join")) {
		m_GUI.displayMessage("You are disconnected. Write /join to join the chat ");
	}
		m_GUI.clearInput();
    }

    }
