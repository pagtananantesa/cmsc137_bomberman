import java.net.InetAddress;

public class NetPlayer {
	private InetAddress address; //network address of the player
	private int port; //port number
	private String name; //name of player
	private int x,y; //The position of player
	private boolean isAlive;

	public NetPlayer(String name,InetAddress address, int port){
		this.address = address;
		this.port = port;
		this.name = name;
		this.isAlive = false;
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

	
	public String toString(){
		String retval="";
		retval+="PLAYER ";
		retval+=name+" ";
		retval+=x+" ";
		retval+=y;
		return retval;
	}	
}
