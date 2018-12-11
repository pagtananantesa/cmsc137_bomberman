//package ph.edu.uplb.ics.cmsc137;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class GameServer implements Runnable, Constants{
 
	String playerData; //Placeholder for the data received from the player
	int playerCount=0; //The number of currently connected player
    DatagramSocket serverSocket = null; //the socket
	GameState game; //the current game state
	int gameStage=WAITING_FOR_PLAYERS; //current game stage
	int numPlayers; //number of players
	Thread t = new Thread(this); //main thread


	
	public GameServer(int numPlayers){
		this.numPlayers = numPlayers;
		try {
            serverSocket = new DatagramSocket(PORT);
			serverSocket.setSoTimeout(100);
		} catch (IOException e) {
            System.err.println("Could not listen on port: "+PORT);
            System.exit(-1);
		}catch(Exception e){}
		
		game = new GameState(); //Create the game state
		
		System.out.println("Game created...");
		
		t.start(); //start the game thread
	}
	
	public void broadcast(String msg){
		Iterator ite = game.getPlayers().keySet().iterator();
    	while (ite.hasNext()){
      		String name = (String)ite.next();
      		NetPlayer player = (NetPlayer)game.getPlayers().get(name);
      		send(player, msg);
    	}
	}

	public void send(NetPlayer player, String msg){
		byte[] arrayOfByte = msg.getBytes();
    	DatagramPacket packet = new DatagramPacket(arrayOfByte, arrayOfByte.length, player.getAddress(), player.getPort());
    	try{
      		serverSocket.send(packet);
    	}catch (IOException ioe){
      		ioe.printStackTrace();
    	}
	}
	
	public void run(){
		while(true){
						
			// Get the data from players
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try{
     			serverSocket.receive(packet);
			}catch(Exception ioe){}
			
			playerData=new String(buf); //Convert the array of bytes to string
			
			playerData = playerData.trim(); //remove excess bytes

		
			switch(gameStage){ // process
				case WAITING_FOR_PLAYERS:
					if (playerData.startsWith("CONNECT")){
						System.out.println("-Player Data- "+playerData);
						String tokens[] = playerData.split(" ");
						NetPlayer player=new NetPlayer(tokens[1],packet.getAddress(),packet.getPort(), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]));
						System.out.println("Player connected: "+tokens[1]);
						game.update(tokens[1].trim(),player);
						broadcast("CONNECTED "+tokens[1]+" "+playerCount);
						playerCount+=1;
						if (playerCount>=2){
							gameStage=GAME_START;
						}
					}
					//System.out.println("Player count: "+playerCount);
					break;	
				case GAME_START:
					if(playerCount < 4){
						System.out.println("Game State: START");
						broadcast(game.toString());
						gameStage=IN_PROGRESS;
					}else{
						//max player limit reached
					}
					break;
				case IN_PROGRESS:
					//System.out.println("Game State: IN_PROGRESS");
					// System.out.println("Player data: "+ playerData);
					//Player data was received!
					if (playerData.startsWith("PLAYER")){
						String[] playerInfo = playerData.split(" ");					  
						String pname =playerInfo[1];
						int x = Integer.parseInt(playerInfo[2].trim());
						int y = Integer.parseInt(playerInfo[3].trim());
						int xPos = Integer.parseInt(playerInfo[4].trim());
						int yPos = Integer.parseInt(playerInfo[5].trim());
						boolean hasBomb = Boolean.parseBoolean(playerInfo[6].trim());

						//Get the player from the game state
						NetPlayer player=(NetPlayer)game.getPlayers().get(pname);					  
						player.setX(x);
						player.setY(y);
						player.setXPos(xPos);
						player.setYPos(yPos);
						player.setBomb(hasBomb);
						player.setStatus(true);
						//Update the game state
						game.update(pname, player);
						//Send to all the updated game state
						System.out.println(game.toString());
						broadcast(game.toString());
						// player.setBomb(false);
						// game.update(pname, player);
						// broadcast(game.toString());

					}else if(playerData.startsWith("BOMB")){
						String[] playerInfo = playerData.split(" ");					  
						String pname =playerInfo[1];
						System.out.println("Recieved BOMB");
						NetPlayer player=(NetPlayer)game.getPlayers().get(pname);
						player.setBomb(false);
						
						//Update the game state
						game.update(pname, player);
						//Send to all the updated game state
						broadcast(game.toString());
					}else if(playerData.startsWith("DEAD")){
						System.out.println(playerData);
						String[] playerInfo = playerData.split(" ");					  
						String pname =playerInfo[1];
						NetPlayer player=(NetPlayer)game.getPlayers().get(pname);
						player.setStatus(false);
						game.update(pname, player);
						broadcast(game.toString());
					}else if(playerData.startsWith("WINNER")){
						String[] playerInfo = playerData.split(" ");					  
						String pname =playerInfo[1];
						NetPlayer player=(NetPlayer)game.getPlayers().get(pname);
						player.addWins();
						System.out.println(pname+" wins");
						player.setStatus(false);
						game.update(pname, player);
						broadcast(game.toString());
						//reset: new round
						System.out.println("RESET SENT");
						broadcast("RESET");
						game.resetLife();
					}
					// break;
			}				  
		}
	}


	public static void main(String args[]){
		new GameServer(2);
	}
}

