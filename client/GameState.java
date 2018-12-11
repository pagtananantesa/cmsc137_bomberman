import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class GameState{
	
	private Map players=new HashMap();
	
	public GameState(){}
	
	
	public void update(String name, NetPlayer player){
		players.put(name,player);
	}
	
	
	public String toString(){
		String retval="";
		for(Iterator ite=players.keySet().iterator();ite.hasNext();){
			String name=(String)ite.next();
			NetPlayer player=(NetPlayer)players.get(name);
			retval+=player.toString()+":";
		}
		return retval;
	}
	
	/
	public Map getPlayers(){
		return players;
	}
}
