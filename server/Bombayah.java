import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;


import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Bombayah extends JPanel implements Runnable, Constants{

	JFrame frame= new JFrame();
	int x,y,prevX,prevY;
	
	Thread t=new Thread(this);
	
	String name="Juan";
	String pname;
	String server="0.0.0.0";
	char[][] board;	//map in string
	boolean connected=false; //Flag to indicate whether this player has connected
    DatagramSocket socket = new DatagramSocket(); //get a datagram socket
	String serverData; //Placeholder for data received from server
	BufferedImage offscreen; //Offscreen image for double buffering, for some real smooth animation

	
	public Bombayah(String server,String name) throws Exception{
		this.server=server;
		this.name=name;
		
		frame.setTitle(APP_NAME+":"+name);

		socket.setSoTimeout(100); //set some timeout for the socket

		//GUI
		frame.setPreferredSize(new Dimension(200, 200));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = frame.getContentPane();
		c.setLayout(new BorderLayout());
		//c.add(gamePanel, BorderLayout.CENTER);

		this.board = new char[11][15];
		//ArrayList<Point> grassPlots = new ArrayList<Point>();
		try {
	        FileInputStream in = new FileInputStream("board1.txt");
	         
	        int chr;
	        int row = 0;
	        int col = 0;
	        while ((chr = in.read()) != -1) {
                switch (chr) {
                    case 'w':
                        // System.out.println((char)chr);
                        this.board[row][col] = (char)chr;
                        col++;
                        break;
                    case 'b':
                        this.board[row][col] = (char)chr;
                        // JLabel box = new JLabel();
                        // box.setIcon(new ImageIcon("img/box.png"));
                        // box.setBounds(col*50+50, row*50+100, 50, 50);
                        // gamePanel.add(box);

                        // boxMap.put(new Point(row,col), box);
                        col++;
                        break;
                    case '-':
                        this.board[row][col] = (char)chr;
                        // Point p = new Point(row, col);
                        // grassPlots.add(p);
                        col++;
                        break;
                    case '\n':
                    	col = 0;
                    	row++;
                    default:
                        break;
                }
            }
	       
	        in.close();


	    }catch(Exception e) { 
			System.out.println(e.getMessage()); 
		}


		frame.pack();
		frame.setVisible(true);


		
		//create the buffer
		//offscreen=(BufferedImage)this.createImage(640, 480);
		
		//Some gui stuff again...
		frame.addKeyListener(new KeyHandler());		

		//tiime to play*/
		t.start();		
	}
	
	public void send(String msg){
		try{
			byte[] arrayOfByte = msg.getBytes();
		    InetAddress localInetAddress = InetAddress.getByName(server);
		    DatagramPacket localDatagramPacket = new DatagramPacket(arrayOfByte, arrayOfByte.length, localInetAddress, 4444);
		    socket.send(localDatagramPacket);
        }catch (Exception localException) {}
		
	}
	
	
	public void run(){
		for (;;){
    		try{
        		Thread.sleep(1L);
      		}catch (Exception localException1) {}
      
      		byte[] arrayOfByte = new byte['Ā'];
      		DatagramPacket localDatagramPacket = new DatagramPacket(arrayOfByte, arrayOfByte.length);
      		try{
        		socket.receive(localDatagramPacket);
      		}catch (Exception localException2) {}
      
      		serverData = new String(arrayOfByte);
      		serverData = serverData.trim();

			
			if (!connected && serverData.startsWith("CONNECTED")){
				System.out.println("serverdata: "+serverData);
				connected=true;
				System.out.println("Connected.");
				Random rand = new Random();
				this.x = rand.nextInt(11) + 0;
				this.y = rand.nextInt(15) + 0;
				this.board[x][y] = 'p';
				send("PLAYER "+name+" "+x+" "+y);

			}else if (!connected){
				System.out.println("serverdata: "+serverData);
				System.out.println("Connecting..");				
				send("CONNECT "+name);
			}else if (connected){
				System.out.println("serverdata: "+serverData);
				//offscreen.getGraphics().clearRect(0, 0, 640, 480);
				// if (serverData.startsWith("PLAYER")){
				//   	String[] playersInfo = serverData.split(":");
				//  	for (int i=0;i<playersInfo.length;i++){
				//  	//	System.out.println(playersInfo[i]);
				//  		String[] playerInfo = playersInfo[i].split(" ");
				// 		String pname =playerInfo[1];
				// 		int x = Integer.parseInt(playerInfo[2]);
				// 		int y = Integer.parseInt(playerInfo[3]);
				//  		//draw on the offscreen image
				// 		//offscreen.getGraphics().fillOval(x, y, 20, 20);
				// 	 	//offscreen.getGraphics().drawString(pname,x-10,y+30);					
				//  	}
				// 	//show the changes
				// 	// frame.repaint();
				// }			
			}			
		}
	}
	
	
	/*public void paintComponent(Graphics g){
		g.drawImage(offscreen, 0, 0, null);
	}
	*/
	
	
	class KeyHandler extends KeyAdapter{
		public void keyPressed(KeyEvent ke){
			prevX=x;prevY=y;
			switch (ke.getKeyCode()){
			case KeyEvent.VK_DOWN:
				/*if(board[x+1][y] != 'b' || board[x+1][y] != 'B' || board[x+1][y] != 'w'){
					board[x][y] = 'p';
				}else*/ 


				if(x<=10){
					if(board[x+1][y] == '-'){
						board[x][y] = '-';
						board[x+1][y] = 'p';
						x+=1;
					}else{
						System.out.println("May harang na something");
					}
				}else{
					System.out.println("Out of bound!");
				}
				

				
				
				for(int i=0; i<11; i++){
					for(int j=0; j<15; j++){
						System.out.print(board[i][j]+" ");
					}
					System.out.println();
				}
				break;
			case KeyEvent.VK_UP:
				/*if(board[x-1][y] != 'b' || board[x-1][y] != 'B' || board[x-1][y] != 'w'){
					board[x][y] = 'p';
				}else*/ 


				if(x>=0){
					if(board[x-1][y] == '-'){
						board[x][y] = '-';
						board[x-1][y] = 'p';
						x-=1;
					}else{
						System.out.println("May harang na something");
					}
				}else{
					System.out.println("Out of bound!");
				}
				
				

				for(int i=0; i<11; i++){
					for(int j=0; j<15; j++){
						System.out.print(board[i][j]+" ");
					}
					System.out.println();
				}

				break;
			case KeyEvent.VK_LEFT:
				/*if(board[x][y-1] != 'b' || board[x][y-1] != 'B' || board[x][y-1] != 'w'){
					board[x][y] = 'p';
				}else*/ 
				if(y>=0){
					if(board[x][y-1] == '-'){
						board[x][y] = '-';
						board[x][y-1] = 'p';
						y-=1;
					}else{
						System.out.println("May harang na something");
					}
				}else{
					System.out.println("Out of bound!");
				}
				
				
				for(int i=0; i<11; i++){
					for(int j=0; j<15; j++){
						System.out.print(board[i][j]+" ");
					}
					System.out.println();
				}

				break;
			case KeyEvent.VK_RIGHT:
				/*if(board[x][y+1] != 'b' || board[x][y+1] != 'B' || board[x][y+1] != 'w'){
					board[x][y] = 'p';
				}else*/ 


				if(y<=14){
					if(board[x][y+1] == '-'){
						board[x][y] = '-';
						board[x][y+1] = 'p';
						y+=1;
					}else{
						System.out.println("May harang na something");
					}

				}else{
					System.out.println("Out of bound!");
				}

				
				for(int i=0; i<11; i++){
					for(int j=0; j<15; j++){
						System.out.print(board[i][j]+" ");
					}
					System.out.println();
				}

				break;
			/*case KeyEvent.VK_ENTER:
				if(board[this.x][this.y] == '-'){
					board[this.x][this.y] = 'B';
					board[this.x][this.y] = 'p';
				}
				x+=1;
				break;*/
			}
			if (prevX != x || prevY != y){
				send("PLAYER "+name+" "+x+" "+y);
			}
		}	
		
	}
	
	
	public static void main(String args[]) throws Exception{
	
		new Bombayah(args[0],args[1]);
	}
}
