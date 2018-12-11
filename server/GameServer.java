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


	
	/**
	 * Simple constructor
	 */
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
	
	/**
	 * Helper method for broadcasting data to all players
	 * @param msg
	 */
	public void broadcast(String msg){
		Iterator ite = game.getPlayers().keySet().iterator();
    	while (ite.hasNext()){
      		String name = (String)ite.next();
      		NetPlayer player = (NetPlayer)game.getPlayers().get(name);
      		send(player, msg);
    	}
	}


	/**
	 * Send a message to a player
	 * @param player
	 * @param msg
	 */
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
					//System.out.println("Game State: Waiting for players...");
				  	//while(playerCount<=numPlayers){
						if (playerData.startsWith("CONNECT")){
							String tokens[] = playerData.split(" ");
							NetPlayer player=new NetPlayer(tokens[1],packet.getAddress(),packet.getPort());
							System.out.println("Player connected: "+tokens[1]);
							game.update(tokens[1].trim(),player);
							broadcast("CONNECTED "+tokens[1]);
							playerCount+=1;
							if (playerCount==numPlayers){
								gameStage=GAME_START;
							}
						}
						//System.out.println("Player count: "+playerCount);
						
					  break;	
				  case GAME_START:
					  System.out.println("Game State: START");
					  broadcast("START");
					  gameStage=IN_PROGRESS;
					  break;
				  case IN_PROGRESS:
					  //System.out.println("Game State: IN_PROGRESS");
					  System.out.println("Player data: "+ playerData);
					  //Player data was received!
					  if (playerData.startsWith("PLAYER")){
						  String[] playerInfo = playerData.split(" ");					  
						  String pname =playerInfo[1];
						  int x = Integer.parseInt(playerInfo[2].trim());
						  int y = Integer.parseInt(playerInfo[3].trim());
						  //Get the player from the game state
						  NetPlayer player=(NetPlayer)game.getPlayers().get(pname);					  
						  player.setX(x);
						  player.setY(y);
						  //Update the game state
						  game.update(pname, player);
						  //Send to all the updated game state
						  broadcast(game.toString());
					  }
					  break;
			}				  
		}
	}



	public static void main(String args[]){
		new GameServer(Integer.parseInt(args[0]));
		
	}
}

