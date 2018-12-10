import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;

public class Bombayah {
	Client client;
	JTextArea textfield;
	JLabel displayLobby;
	JTextArea chatDisplay;
	boolean isconnected;

	private void sendMessage(){
		this.client.sendChat(this.textfield.getText());
		this.textfield.setText("");
	}
	private void sendCreateLobby(String uname){
		String key = this.client.createLobby();
		this.client.joinLobby(key,uname);
		this.displayLobby.setText(key);
	}
	private void sendJoinLobby(String key,String uname){
		this.client.joinLobby(key,uname);
		this.displayLobby.setText(key);
	}
	public void recieveChat(String message){
		this.chatDisplay.append(message);
		this.chatDisplay.append("\n");
	}
	public void setConnected(){
		this.isconnected = true;
	}
	public boolean isConnected(){
		return this.isconnected;
	}

							
	private void startGame() throws Exception{
		this.client = new Client(this);
		this.client.start();

		ArrayList<String> playerAttr = new ArrayList<String>();
		playerAttr.add(0,""); //name


		//MAIN MENU GUI-------------------------------------------
		JFrame menuFrame = new JFrame("Welcome");
		menuFrame.setPreferredSize(new Dimension(450,500));
		menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container menuContain = menuFrame.getContentPane();
		menuContain.setLayout(new BorderLayout());

		JPanel menu = new JPanel();
		menu.setPreferredSize(new Dimension(450,500));
		menu.setOpaque(true);
		menu.setBackground(Color.BLACK);
		menu.setLayout(null);

		JTextField uname = new JTextField("Player");
		uname.setBackground(Color.WHITE);
		uname.setBounds(145,250,170,40);
		menu.add(uname);

		JTextField lobbyName = new JTextField("Join Lobby");
		lobbyName.setBackground(Color.WHITE);
		lobbyName.setBounds(230,350,100,40);
		menu.add(lobbyName);

		JButton find = new JButton("Go");
		find.setBackground(Color.GREEN);
		find.setBounds(330,350,40,40);
		menu.add(find);


		JButton createLobby = new JButton("Create Lobby");
		createLobby.setBackground(Color.PINK);
		createLobby.setBounds(50,350,140,40);
		menu.add(createLobby);


		JLabel logo = new JLabel(new ImageIcon("img/logo.gif"));
		logo.setBounds(135,50,190,170);
		menu.add(logo);
		
		menuFrame.add(menu);
		menuFrame.pack();
		menuFrame.setVisible(true);


		//GAME GUI-----------------------------------------------------
		JFrame frame = new JFrame("Bombayah");
		frame.setPreferredSize(new Dimension(1200, 700));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = frame.getContentPane();
		c.setLayout(new BorderLayout());
		c.setBackground(Color.PINK);

		GameBoard gamePanel = new GameBoard("10.11.184.112", playerAttr.get(0), c);


		
		//CHAT BOX GUI---------------------------------------------------
		this.displayLobby = new JLabel("lobby_ID");
		this.displayLobby.setBackground(Color.BLACK);
		this.displayLobby.setForeground(Color.WHITE);
		this.displayLobby.setBounds(900,200,250,50);
		this.displayLobby.setOpaque(true);
		c.add(this.displayLobby);


		this.chatDisplay = new JTextArea();
		// messages.setBounds(30,30,30,80);
		chatDisplay.setBorder(BorderFactory.createLineBorder(Color.gray));
		chatDisplay.setBackground(Color.WHITE);
		//messages.setLineWrap(true);
		//messages.setWrapStyleWord(true);
		chatDisplay.setOpaque(true);
		chatDisplay.setFocusable(false);


		JScrollPane chatbox = new JScrollPane(this.chatDisplay);
		chatbox.setAutoscrolls(true);
		chatbox.setBounds(900,250,250,300);
		chatbox.setBackground(Color.WHITE);
		chatbox.setBorder(BorderFactory.createLineBorder(Color.gray));
		chatbox.setOpaque(true);
		chatbox.setFocusable(false);
		c.add(chatbox);

		
		
		this.textfield = new JTextArea();
		this.textfield.setBackground(Color.WHITE);
		//textfield.setBounds(910,560,190,60);
		this.textfield.setBounds(0,0,190,60);
		this.textfield.setBorder(BorderFactory.createLineBorder(Color.gray));
		this.textfield.setOpaque(true);
		this.textfield.setLineWrap(true);
		this.textfield.setWrapStyleWord(true);

		JScrollPane scrollpane = new JScrollPane(textfield);
		scrollpane.setBounds(10,10,190,60);
		scrollpane.setOpaque(false);
		//scrollpane.add(textfield);
		
		JButton send = new JButton("send");
		send.setBounds(200,10,45,30);

		send.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				send.setFocusable(false);	   
				sendMessage();
			}
		 });

		JPanel textPanel = new JPanel();
		textPanel.setBounds(900,550,250,80);
		textPanel.setOpaque(true);
		textPanel.setLayout(null);
		
		textPanel.add(scrollpane);
		textPanel.add(send);
		c.add(textPanel);

		c.add(gamePanel, BorderLayout.CENTER);

		frame.pack();
		
		find.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				playerAttr.add(0, uname.getText());
				playerAttr.add(1, lobbyName.getText());
				sendJoinLobby(lobbyName.getText(),uname.getText());
				if(isConnected()){
					menuFrame.setVisible(false);
					frame.setVisible(true);	
				}else{
					System.out.println("NOT CONNECTED");						//Replace with popup and display error
				}

			}

		});

		createLobby.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				playerAttr.add(0, uname.getText());
				sendCreateLobby(uname.getText());
				if(isConnected()){
					menuFrame.setVisible(false);
					frame.setVisible(true);	
				}
			}

		});
	}

	public static void main(String args[]) throws Exception{
		Bombayah game = new Bombayah();
		game.startGame();
	}
}