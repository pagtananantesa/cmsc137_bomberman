import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GameBoard extends JPanel implements Runnable, Constants{
	private final int DIMENSION = 50;
	Thread t=new Thread(this);
	String name="Juan";
	String pname;
	String server="0.0.0.0";
	// char[][] board;	//map in string
	boolean connected=false; //Flag to indicate whether this player has connected
    DatagramSocket socket = new DatagramSocket(); //get a datagram socket
	String serverData; //Placeholder for data received from server

	int x,y,prevX,prevY; //for board config
	int xPos, yPos; //for player movement UI
	JLabel player = new JLabel();
	boolean hasBomb;
	int xBomb, yBomb; //bomb position
	char[][] board = new char[11][15];
	ArrayList<Point> grassPlots = new ArrayList<Point>();
	HashMap<Point, JLabel> boxMap = new HashMap<Point, JLabel>();
	Container c;

	public GameBoard(String server,String name, Container c) throws Exception{
		this.c = c;
		this.server=server;
		this.name=name;

		socket.setSoTimeout(100); //set some timeout for the socket

		//GUI
		this.setFocusable(true);
		this.setLayout(null);
		this.setPreferredSize(new Dimension(750, 550));
		this.setBackground(Color.PINK);

		//set initial board config
		try {
	        FileInputStream in = new FileInputStream("board1.txt");
	         
	        int chr;
	        int row = 0;
	        int col = 0;
	        while ((chr = in.read()) != -1) {
                switch (chr) {
                    case 'w':
                        // System.out.println((char)chr);
                        board[row][col] = (char)chr;
                        col++;
                        break;
                    case 'b':
                        board[row][col] = (char)chr;
                        JLabel box = new JLabel();
                        box.setIcon(new ImageIcon("img/box.png"));
                        box.setBounds(col*50+50, row*50+100, 50, 50);
                        this.add(box);

                        boxMap.put(new Point(row,col), box);
                        col++;
                        break;
                    case '-':
                        board[row][col] = (char)chr;
                        Point p = new Point(row, col);
                        grassPlots.add(p);
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
        //choose player position
        Random rand = new Random();
		Point position = grassPlots.get(rand.nextInt((int)grassPlots.size()));
		this.x = (int)position.getX();
		this.y = (int)position.getY();
		board[this.x][this.y] = 'X';
		this.yPos = 50+DIMENSION*y;
		this.xPos = 100+DIMENSION*x;

		player.setIcon(new ImageIcon("img/lisa.png"));
		player.setBounds(this.yPos, this.xPos, DIMENSION, DIMENSION);
		this.add(player);

		//background image
		JLabel background = new JLabel(new ImageIcon("img/BACKGROUND.png"));
		this.add(background);
		this.setBounds(50,100,750,550);
		background.setBounds(50, 100, 750, 550);

		this.addKeyListener(new KeyHandler());
		this.addMouseListener(new MouseListener(){
			public void mousePressed(MouseEvent me){
		        requestFocus();
		        System.out.println("Mouse Pressed in JPanel");
		    }

		    public void mouseReleased(MouseEvent me){}

		    public void mouseClicked(MouseEvent me){}

		    public void mouseEntered(MouseEvent me){}

		    public void mouseExited(MouseEvent me){}

		});
		
		this.addFocusListener(new FocusListener(){
		    public void focusGained(FocusEvent fe){
		        System.out.println("Focus gained in JPanel");
		    }

		    public void focusLost(FocusEvent fe){
		        System.out.println("Focus lost in JPanel");
		    }
		});

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

			//Study the following kids. 
			if (!connected && serverData.startsWith("CONNECTED")){
				System.out.println("serverdata: "+serverData);
				connected=true;
				System.out.println("Connected.");
				// Random rand = new Random();
				// this.x = rand.nextInt(11) + 0;
				// this.y = rand.nextInt(15) + 0;
				// this.board[x][y] = 'p';
				send("PLAYER "+name+" "+x+" "+y);

			}else if (!connected){
				System.out.println("serverdata: "+serverData);
				System.out.println("Connecting..");				
				send("CONNECT "+name);
			}else if (connected){
				System.out.println("serverdata: "+serverData);
							
			}			
		}
	}

	class KeyHandler extends KeyAdapter{
		public void keyPressed(KeyEvent ke){
			if(ke.getKeyCode()==KeyEvent.VK_RIGHT){
				// System.out.println("----------------RIGHT");

				if(yPos%DIMENSION == 0 &&  board[x][y+1] != '-'){
				}else{
					if(xPos%DIMENSION <=4 || xPos%DIMENSION >= 46){
						yPos += 2;
						player.setBounds(yPos, xPos, DIMENSION, DIMENSION);
					
						if(yPos%DIMENSION == 24){
						//MOVE
							if(board[x][y] == 'B'){
								board[x][y] = 'B';
							}else{
								board[x][y] = '-';
							}
							y = (yPos-50)/DIMENSION+1;
							board[x][y] = 'X';
						}
					}
					
				}
			}
			if(ke.getKeyCode()==KeyEvent.VK_LEFT){
				// System.out.println("----------------LEFT");

				if(yPos%DIMENSION == 0 &&  board[x][y-1] != '-'){
				}else{
					if(xPos % DIMENSION <= 4 || xPos % DIMENSION >= 46){
						yPos -= 2;
						player.setBounds(yPos, xPos, DIMENSION, DIMENSION);

						if(yPos % DIMENSION == 24){
							if(board[x][y] == 'B'){
								board[x][y] = 'B';
							}else{
								board[x][y] = '-';
							}
							y = (yPos-50)/DIMENSION;
							board[x][y] = 'X';
						}
					}
				}
			}
			if(ke.getKeyCode()==KeyEvent.VK_UP){
				// System.out.println("----------------UP");

				if(xPos%DIMENSION == 0 &&  board[x-1][y] != '-'){
				}else{
					if(yPos % DIMENSION <= 4 || yPos % DIMENSION >= 46){
						xPos -= 2;
						player.setBounds(yPos, xPos, DIMENSION, DIMENSION);
						if(xPos % DIMENSION == 24){
							if(board[x][y] == 'B'){
								board[x][y] = 'B';
							}else {
								board[x][y] = '-';
							}
							x = (xPos-100)/DIMENSION;
							board[x][y] = 'X';
						}
					}
				}
			}
			if(ke.getKeyCode()==KeyEvent.VK_DOWN){
				// System.out.println("----------------DOWN");

				if(xPos%DIMENSION == 0 &&  board[x+1][y] != '-'){
				}else{
					if(yPos % DIMENSION <= 4 || yPos % DIMENSION >= 46){
						xPos += 2;
						player.setBounds(yPos, xPos, DIMENSION, DIMENSION);
						if(xPos % DIMENSION == 24){
							if(board[x][y] == 'B'){
								board[x][y] = 'B';
							}else{
								board[x][y] = '-';
							}
							x = (xPos-100)/DIMENSION+1;
							board[x][y] = 'X';
						}
					}
				}
			}
			if(ke.getKeyCode()==KeyEvent.VK_ENTER){
				// System.out.println("----------------BOMB");
				board[x][y] = 'B';

				if(!hasBomb){
					hasBomb = true;
					xBomb = xPos;
					yBomb = yPos;

					Bomb b = new Bomb(x, y);
					add(b, 0);


					ArrayList<Integer> variables= new ArrayList<Integer>();
					variables.add(x); //index 0 xPos
					variables.add(y); //index 1 yPos


					JLabel flameUp = new JLabel(new ImageIcon("img/flame_up.gif"));
					JLabel flameDown = new JLabel(new ImageIcon("img/flame_down.gif"));
					JLabel flameRight = new JLabel(new ImageIcon("img/flame_right.gif"));
					JLabel flameLeft = new JLabel(new ImageIcon("img/flame_left.gif"));
				    JLabel flameCenter = new JLabel(new ImageIcon("img/flame_body.gif"));

					javax.swing.Timer bombT = new javax.swing.Timer(3000,  new ActionListener() {
						@Override
				        public void actionPerformed(ActionEvent evt) {
				            remove(b);
				            int x = variables.get(0);
				            int y = variables.get(1);
				            board[x][y] = '-';


				            revalidate();
				            repaint();


				            //FLAME UP
				            if(board[x-1][y] == 'b'){ //if box
				            	remove((JLabel)boxMap.get(new Point(x-1, y)));
				            	board[x-1][y] = '-';
				            }

				            
				            if(board[x-1][y] == '-' || board[x-1][y] == 'X'){
					            flameUp.setBounds(50+DIMENSION*y, 100+DIMENSION*(x-1), DIMENSION, DIMENSION);
					            add(flameUp, 0);
				            }

				            //FLAME DOWN
				            if(board[x+1][y] == 'b'){
				            	remove((JLabel)boxMap.get(new Point(x+1, y)));
				            	board[x+1][y] = '-';
				            }
				            
				            if(board[x+1][y] == '-' || board[x+1][y] == 'X'){
					            flameDown.setBounds(50+DIMENSION*y, 100+DIMENSION*(x+1), DIMENSION, DIMENSION);
					            add(flameDown, 0);
				            }

				            //FLAME RIGHT
				            if(board[x][y+1] == 'b'){
				            	remove((JLabel)boxMap.get(new Point(x, y+1)));
				            	board[x][y+1] = '-';
				            }
				            
				            if(board[x][y+1] == '-' || board[x][y+1] == 'X'){
					            flameRight.setBounds(50+DIMENSION*(y+1), 100+DIMENSION*x, DIMENSION, DIMENSION);
					            add(flameRight, 0);
				            }

				            //FLAME LEFT
				            if(board[x][y-1] == 'b'){
				            	remove((JLabel)boxMap.get(new Point(x, y-1)));
				            	board[x][y-1] = '-';
				            }
				            
				            if(board[x][y-1] == '-' || board[x][y-1] == 'X'){
					            flameLeft.setBounds(50+DIMENSION*(y-1), 100+DIMENSION*x, DIMENSION, DIMENSION);
					            add(flameLeft, 0);
				            }


				            flameCenter.setBounds(50+DIMENSION*y, 100+DIMENSION*x, DIMENSION, DIMENSION);
				            add(flameCenter, 0);

				            revalidate();
				            repaint();
				            c.revalidate();
				            c.repaint();

				        }

				    });
				    bombT.start();

				   	new java.util.Timer().schedule( 
			        new java.util.TimerTask() {
			            @Override
			            public void run() {
			                bombT.stop();
			                remove(flameUp);
			                remove(flameDown);
			                remove(flameLeft);
			                remove(flameRight);
			                remove(flameCenter);

			                revalidate();
				            repaint();
				            c.revalidate();
				            c.repaint();
							hasBomb = false;
							System.out.println("NO BOMB");
			            }
			        }, 
			        4000 
					);
				}else{
					xBomb = 0;
					yBomb = 0;
				}
			}

	        revalidate();
	        repaint();
	        c.revalidate();
			c.repaint();

			send("PLAYER "+name+" "+x+" "+y);
		}
		
	}

	// public static void main(String args[]) throws Exception{
	// 	JFrame frame = new JFrame("Bombayah");
	// 	frame.setPreferredSize(new Dimension(1200, 700));
	// 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// 	Container c = frame.getContentPane();
	// 	c.setLayout(new BorderLayout());

	// 	GameBoard gamePanel = new GameBoard(args[0],args[1]);
	// 	c.add(gamePanel);

	// 	frame.pack();
	// }	

}