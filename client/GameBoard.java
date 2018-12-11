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
	boolean hasBomb;
	int xBomb, yBomb; //bomb position
	int level = 1;
	char[][] board = new char[11][15];
	ArrayList<Point> grassPlots = new ArrayList<Point>();
	ArrayList<Point> prevPosition = new ArrayList<Point>();
	HashMap<String, JLabel> playerList = new HashMap<String, JLabel>();
	HashMap<String, Point> playerPos = new HashMap<String, Point>();
	HashMap<Point, JLabel> boxMap = new HashMap<Point, JLabel>();
	Container c;
	int levelNumber = 1;
	JLabel levelLabel;

	JLabel name1 = new JLabel();
	JLabel name2 = new JLabel();
	JLabel name3 = new JLabel();
	JLabel name4 = new JLabel();

	
	public GameBoard(String server,String name, Container c) throws Exception{
		System.out.println("-----------------------"+name);
		this.c = c;
		this.server=server;
		this.name=name;

		socket.setSoTimeout(100); //set some timeout for the socket

		//GUI
		this.setFocusable(true);
		this.setLayout(null);
		this.setPreferredSize(new Dimension(750, 550));
		this.setBackground(Color.PINK);
		String str = "LEVEL "+levelNumber;
		levelLabel = new JLabel(str);
		levelLabel.setFont(new Font("Arial", Font.PLAIN, 25));
		levelLabel.setBounds(900, 100,200,50);
		c.add(levelLabel,0);
		


		try {
	        FileInputStream in = new FileInputStream("board"+level+".txt");
	         
	        int chr;
	        int row = 0;
	        int col = 0;
	        while ((chr = in.read()) != -1) {
                switch (chr) {
                    case 'w':
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
		this.yPos = 50+DIMENSION*y;
		this.xPos = 100+DIMENSION*x;

		
		//background image
		JLabel background = new JLabel(new ImageIcon("img/BACKGROUND.png"));
		this.add(background);
		this.setBounds(50,100,750,550);
		background.setBounds(50, 100, 750, 550);

		JLabel lisaLogo = new JLabel(new ImageIcon("img/lisa_logo.png"));
		JLabel roseeLogo = new JLabel(new ImageIcon("img/rosee_logo.png"));
		JLabel jisooLogo = new JLabel(new ImageIcon("img/jisoo_logo.png"));
		JLabel jennieLogo = new JLabel(new ImageIcon("img/jennie_logo.png"));

		jennieLogo.setBounds(650,10,50,50);
		jisooLogo.setBounds(500,10,50,50);
		roseeLogo.setBounds(350,10,50,50);
		lisaLogo.setBounds(200,10,50,50);

		c.add(lisaLogo);
		c.add(roseeLogo);
		c.add(jisooLogo);
		c.add(jennieLogo);

		name1.setBounds(650,70,50,20);
		name2.setBounds(500,70,50,20);
		name3.setBounds(350,70,50,20);
		name4.setBounds(200,70,50,20);
		c.add(name1);
		c.add(name2);
		c.add(name3);
		c.add(name4);
		this.addKeyListener(new KeyHandler());
		this.addMouseListener(new MouseListener(){
			public void mousePressed(MouseEvent me){
		        requestFocus();
		    }
		    public void mouseReleased(MouseEvent me){}
		    public void mouseClicked(MouseEvent me){}
		    public void mouseEntered(MouseEvent me){}
		    public void mouseExited(MouseEvent me){}
		});
		
		this.addFocusListener(new FocusListener(){
		    public void focusGained(FocusEvent fe){}
		    public void focusLost(FocusEvent fe){}
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

	public void resetBoard(){
		this.levelNumber+=1;
		this.levelLabel.setText("LEVEL "+levelNumber);
		this.removeAll();
		this.revalidate();
		this.repaint();

		this.playerList.clear();
		this.playerPos.clear();
		this.grassPlots.clear();
		this.boxMap.clear();
		this.prevPosition.clear();

		try {
	        FileInputStream in = new FileInputStream("board"+level+".txt");
	         
	        int chr;
	        int row = 0;
	        int col = 0;
	        while ((chr = in.read()) != -1) {
                switch (chr) {
                    case 'w':
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
		this.yPos = 50+DIMENSION*y;
		this.xPos = 100+DIMENSION*x;

		//background image
		JLabel background = new JLabel(new ImageIcon("img/BACKGROUND.png"));
		this.add(background);
		this.setBounds(50,100,750,550);
		background.setBounds(50, 100, 750, 550);

		send("PLAYER "+name
			+" "+x
			+" "+y
			+" "+xPos
			+" "+yPos
			+" "+hasBomb
			);
	}

	public void boom(String pname, int x, int y){
		hasBomb = false;
		String name = new String(pname);
		this.xBomb = new Integer(x);
		this.yBomb = new Integer(y);
		board[this.xBomb][this.yBomb] = 'V';
		Bomb b = new Bomb(this.xBomb, this.yBomb);
		this.add(b, 0);

		JLabel flameUp = new JLabel(new ImageIcon("img/flame_up.gif"));
		JLabel flameDown = new JLabel(new ImageIcon("img/flame_down.gif"));
		JLabel flameRight = new JLabel(new ImageIcon("img/flame_right.gif"));
		JLabel flameLeft = new JLabel(new ImageIcon("img/flame_left.gif"));
		JLabel flameCenter = new JLabel(new ImageIcon("img/flame_body.gif"));
		javax.swing.Timer bombT = new javax.swing.Timer(3000,  new ActionListener() {
			@Override
	        public void actionPerformed(ActionEvent evt) {
	    		remove(b);
	    		

	            revalidate();
	            repaint();
	            c.revalidate();
	            c.repaint();


				//FLAME UP
		        if(board[xBomb-1][yBomb] == 'b'){ //if box
		        	remove((JLabel)boxMap.get(new Point(xBomb-1, yBomb)));
					board[xBomb-1][yBomb] = '-';
				}
		        if(board[xBomb-1][yBomb] == '-' || board[xBomb-1][yBomb] == 'X'){
		            flameUp.setBounds(50+DIMENSION*yBomb, 100+DIMENSION*(xBomb-1), DIMENSION, DIMENSION);
		            add(flameUp, 0);
		        }

		        //FLAME DOWN
		        if(board[xBomb+1][yBomb] == 'b'){
		        	remove((JLabel)boxMap.get(new Point(xBomb+1, yBomb)));
					board[xBomb+1][yBomb] = '-';
		        }
		        if(board[xBomb+1][yBomb] == '-' || board[xBomb+1][yBomb] == 'X'){
		            flameDown.setBounds(50+DIMENSION*yBomb, 100+DIMENSION*(xBomb+1), DIMENSION, DIMENSION);
		            add(flameDown, 0);
		        }

		        //FLAME RIGHT
		        if(board[xBomb][yBomb+1] == 'b'){
		        	remove((JLabel)boxMap.get(new Point(xBomb, yBomb+1)));
					board[xBomb][yBomb+1] = '-';
		        }
		        
		        if(board[xBomb][yBomb+1] == '-' || board[xBomb][yBomb+1] == 'X'){
		            flameRight.setBounds(50+DIMENSION*(yBomb+1), 100+DIMENSION*xBomb, DIMENSION, DIMENSION);
		            add(flameRight, 0);
		        }

		        //FLAME LEFT
		        if(board[xBomb][yBomb-1] == 'b'){
		        	remove((JLabel)boxMap.get(new Point(xBomb, yBomb-1)));
					board[xBomb][yBomb-1] = '-';
		        }
		        
		        if(board[xBomb][yBomb-1] == '-' || board[xBomb][yBomb-1] == 'X'){
		            flameLeft.setBounds(50+DIMENSION*(yBomb-1), 100+DIMENSION*xBomb, DIMENSION, DIMENSION);
		            add(flameLeft, 0);
				}
				
				flameCenter.setBounds(50+DIMENSION*yBomb, 100+DIMENSION*xBomb, DIMENSION, DIMENSION);
		        add(flameCenter, 0);

				

				Iterator iter = playerPos.keySet().iterator();
				while(iter.hasNext()){
					String key = (String)iter.next();
					if(playerPos.get(key).getX() == xBomb && playerPos.get(key).getY() == yBomb){
						send("DEAD "+key);
						board[xBomb][yBomb] = '-';
						remove(playerList.get(key));
						playerList.remove(key);
					}else if(playerPos.get(key).getX() == xBomb-1 && playerPos.get(key).getY() == yBomb){
						send("DEAD "+key);
						board[xBomb-1][yBomb] = '-';
						remove(playerList.get(key));
						playerList.remove(key);
					}else if(playerPos.get(key).getX() == xBomb+1 && playerPos.get(key).getY() == yBomb){
						send("DEAD "+key);
						board[xBomb+1][yBomb] = '-';
						remove(playerList.get(key));
						playerList.remove(key);
					}else if(playerPos.get(key).getX() == xBomb && playerPos.get(key).getY() == yBomb-1){
						send("DEAD "+key);
						board[xBomb][yBomb-1] = '-';
						remove(playerList.get(key));
						playerList.remove(key);
					}else if(playerPos.get(key).getX() == xBomb && playerPos.get(key).getY() == yBomb+1){
						send("DEAD "+key);
						board[xBomb][yBomb+1] = '-';
						remove(playerList.get(key));
						playerList.remove(key);
					}
					
				}
				
				if(playerList.size() == 1){
					iter = playerPos.keySet().iterator();
					send("WINNER "+(String)iter.next());
				}

		        board[xBomb][yBomb] = '-';
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
	    		remove(flameRight);
	    		remove(flameLeft);
	    		remove(flameCenter);
				hasBomb = false;
				send("BOMB "+name+" "+hasBomb);
	            revalidate();
	            repaint();
	            c.revalidate();
	            c.repaint();
	        }
        }, 
        4000 
		);
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
				connected=true;

				String tokens[] = serverData.split(" ");
				String pname = tokens[1];
				//DO UI SA TAAS use tokens[2] para sa image

			}else if (!connected){
				System.out.println("Connecting..");				
				send("CONNECT "+name+" "+x+" "+y);
			}else if (connected){
				// System.out.println("serverdata: "+serverData);
				if(serverData.startsWith("RESET")){
					if(level > 3){
						//end game
						System.out.println("END GAME");
					}else{
						//reset game
						level+=1;
						resetBoard();
					}
					
				}else if (serverData.startsWith("PLAYER")){
					String[] playersInfo = serverData.split(":");
					for (int i=0;i<playersInfo.length;i++){
						String[] playerInfo = playersInfo[i].split(" ");
						String pname =playerInfo[1];
						int x = Integer.parseInt(playerInfo[2]);
						int y = Integer.parseInt(playerInfo[3]);
						int xPos = Integer.parseInt(playerInfo[4]);
						int yPos = Integer.parseInt(playerInfo[5]);
						boolean hasBomb = Boolean.parseBoolean(playerInfo[6].trim());

						if(!playerList.keySet().contains(pname) || playerList.isEmpty()){ //if player not in the list yet

							board[x][y] = 'X';
							JLabel player = new JLabel();
							Point prev = new Point(x,y);
							if(i == 0){
								player.setIcon(new ImageIcon("img/lisa.png"));
								name1.setText(pname);
							}else if(i == 1){
								player.setIcon(new ImageIcon("img/rosee.png"));
								name2.setText(pname);
							}else if(i == 2){
								player.setIcon(new ImageIcon("img/jisoo.png"));
								name3.setText(pname);
							}else if(i == 3){
								player.setIcon(new ImageIcon("img/jennie.png"));
								name4.setText(pname);
							}
							playerPos.put(pname, new Point(x, y));
							player.setBounds(yPos, xPos, DIMENSION, DIMENSION);
							playerList.put(pname, player);
							prevPosition.add(prev);
							this.add(player,0);
						}else{
							if(hasBomb){
								boom(pname, x, y);
							}else{
								//update the player UI position
								playerList.get(pname).setBounds(yPos, xPos, DIMENSION, DIMENSION);

								//update the configuration: change the previous position
								int prevX = (int)prevPosition.get(i).getX();
								int prevY = (int)prevPosition.get(i).getY();
								if(prevX != x || prevY != y){
									if(board[prevX][prevY] == 'V'){
										board[prevX][prevY] = 'B';
									}else{
										board[prevX][prevY] = '-';
									}
									board[x][y] = 'X';
								}
							}
							prevPosition.set(i, new Point(x, y));
							
						}

						

						this.revalidate();
						this.repaint();
						c.revalidate();
						c.repaint();
					}
				}			
			}			
		}
	}

	class KeyHandler extends KeyAdapter{
		public void keyPressed(KeyEvent ke){
			if(ke.getKeyCode()==KeyEvent.VK_RIGHT){
				if(yPos%DIMENSION == 0 &&  board[x][y+1] != '-'){
				}else{
					if(xPos%DIMENSION <=4 || xPos%DIMENSION >= 46){
						yPos += 2;
						if(yPos%DIMENSION == 24){
							y = (yPos-50)/DIMENSION+1;
						}
					}
				}
			}
			if(ke.getKeyCode()==KeyEvent.VK_LEFT){
				if(yPos%DIMENSION == 0 &&  board[x][y-1] != '-'){
				}else{
					if(xPos % DIMENSION <= 4 || xPos % DIMENSION >= 46){
						yPos -= 2;
						if(yPos % DIMENSION == 24){
							y = (yPos-50)/DIMENSION;
						}
					}
				}
			}
			if(ke.getKeyCode()==KeyEvent.VK_UP){
				if(xPos%DIMENSION == 0 &&  board[x-1][y] != '-'){
				}else{
					if(yPos % DIMENSION <= 4 || yPos % DIMENSION >= 46){
						xPos -= 2;
						if(xPos % DIMENSION == 24){
							x = (xPos-100)/DIMENSION;
						}
					}
				}
			}
			if(ke.getKeyCode()==KeyEvent.VK_DOWN){
				if(xPos%DIMENSION == 0 &&  board[x+1][y] != '-'){
				}else{
					if(yPos % DIMENSION <= 4 || yPos % DIMENSION >= 46){
						xPos += 2;
						if(xPos % DIMENSION == 24){
							x = (xPos-100)/DIMENSION+1;
						}
					}
				}
			}
			if(ke.getKeyCode()==KeyEvent.VK_SPACE){
				if(!hasBomb){
					hasBomb = true;
				}
				// send("BOMB "+name+" "+x+" "+y);
			}

			send("PLAYER "+name
				+" "+x
				+" "+y
				+" "+xPos
				+" "+yPos
				+" "+hasBomb
				);
		}
		
	}

}