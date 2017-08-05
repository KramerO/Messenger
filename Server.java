package Server;

import java.awt.BorderLayout;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame{

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket socketServer;
	private Socket connector;
	
	
	public Server(){
		super("PrivatMessander");
		userText = new JTextField();
		userText.setEditable(false); // ohne verbindung nicht editirbar
		userText.addActionListener(e -> sendMessage("nichts"));
		userText.setText("");
		add(userText,BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300,150);
		setVisible(true);
	}
	
	private void showMessage(final String text) {
		SwingUtilities.invokeLater(
				() -> chatWindow.append(text)
		);
	}
	
	public void startRunning(){
		try{
			socketServer = new ServerSocket(6789,100);
			while(true){
				try{
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException eof){
					showMessage("\n EOF fehler");
				}finally{
					closeCrape();
				}
			}
		}catch(IOException io){
			System.out.println("io fehler");
		}
	}

	private void whileChatting() throws IOException{

		String message = "Yana!";
		showMessage(message);
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException cnf){
				showMessage("\n wtf you are sending");
			}
		}while(!message.equals("CLIENT - END"));
		
	}

	private void ableToType(final boolean b) {
		SwingUtilities.invokeLater(
				() -> userText.setEditable(b)
		);
		
	}

	private void setupStreams() throws IOException{
		
		output = new ObjectOutputStream(connector.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connector.getInputStream());
		showMessage("\n Streams are now setup! \n");
		
	}

	private void waitForConnection() throws IOException {
		showMessage("Warte auf verbindungs partner!");
		connector = socketServer.accept();
		showMessage("No Connected to " + connector.getInetAddress().getHostName());
	}

	private void closeCrape() {
		
		showMessage("\n Verbindungen beenden!");
		ableToType(false);
		try{
			output.close();
			input.close();
			connector.close();
		}catch(IOException io){
			io.printStackTrace();
		}
		
	}

	private void sendMessage(String s) {
		try{
			output.writeObject("Server - " +s);
			output.flush();
			showMessage("\nServer - " + s);
		}catch(IOException io){
			chatWindow.append("\n Fehler Dude");
		}
	}

	

}
