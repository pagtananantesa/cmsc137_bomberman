//package ph.edu.uplb.ics.cmsc137;
import java.net.InetAddress;

/**
 * This class encapsulates a network players
 * @author Joseph Anthony C. Hermocilla
 *
 */

public class NetPlayer implements Constants{
	private InetAddress address; //network address of the player
	private int port; //port number
	private String name; //name of player
	private int x,y; //The board position of player
	private int xPos, yPos; //UI position
	private boolean isAlive;
	private boolean hasBomb;
	private int wins;

	public NetPlayer(String name,InetAddress address, int port, int x, int y){
		this.address = address;
		this.port = port;
		this.name = name;
		this.isAlive = false;
		this.x = x;
		this.y = y;
		this.xPos = 100+DIMENSION*x;
		this.yPos = 50+DIMENSION*y;
		this.wins = 0;

	}

	public InetAddress getAddress(){
		return address;
	}

	public int getPort(){
		return port;
	}

	public String getName(){
		return name;
	}
	
	public boolean getStatus(){
		return isAlive;
	}

	public void setStatus(boolean bool){
		this.isAlive = bool;
	}

	public void setX(int x){
		this.x=x;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setY(int y){
		this.y=y;		
	}

	public void setXPos(int xPos){
		this.xPos = xPos;
	}
	public void setYPos(int yPos){
		this.yPos = yPos;
	}

	public int getXPos(){
		return this.xPos;
	}

	public int getYPos(){
		return this.yPos;
	}

	public boolean getBomb(){
		return this.hasBomb;
	}

	public void setBomb(boolean hasBomb){
		this.hasBomb = hasBomb;
	}

	public void addWins(){
		this.wins+=1;
	}

	public int getWins(){
		return this.wins;
	}
	/**
	 * String representation. used for transfer over the network
	 */
	public String toString(){
		String retval="";
		retval+="PLAYER ";
		retval+=name+" ";
		retval+=x+" ";
		retval+=y+" ";
		retval+=xPos+" ";
		retval+=yPos+" ";
		retval+=hasBomb;

		return retval;
	}	
}
