package Client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	
	//constructor
	public Client(String hostName){
		super("Client");
		serverIP = hostName;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
				new ActionListener(){
				public void actionPerformed(ActionEvent event){
					sendMessage(event.getActionCommand());
					userText.setText("Please insert Text!");
				}
			}
		);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(800, 600); //Sets the window size
		setVisible(true);
	}
	
	//connect to server
	public void startRunning(){
		try{
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eof){
			showMessage("\n Client terminated the connection");
		}catch(IOException io){
			io.printStackTrace();
		}finally{
			closeConnection();
		}
	}
	
	//verbinden mit server
	private void connectToServer() throws IOException{
		showMessage("Verbindung wird erstellt... \n");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Verbindung hergestellt! Verbunden mit: " + connection.getInetAddress().getHostName());
	}
	
	//streams einrichten
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams sind eingerichtet! \n");
	}
	
	//whaerend des chattens
	private void whileChatting() throws IOException{
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException cNFException){
				showMessage("Unknown data received!");
			}
		}while(!message.equals("SERVER - END"));	
	}
	
	//schleissen
	private void closeConnection(){
		showMessage("\n Verbindungen beenden!");
		ableToType(false);
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//send message to server
	private void sendMessage(String message){
		try{
			output.writeObject("CLIENT - " + message);
			output.flush();
			showMessage("\nCLIENT - " + message);
		}catch(IOException ioException){
			chatWindow.append("\n Etwas funktioniert nicht!");
		}
	}
	
	//chatfenster aktuallisieren
	private void showMessage(final String message){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(message);
				}
			}
		);
	}
	
	//tippen erlauben
	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(tof);
				}
			}
		);
	}
}