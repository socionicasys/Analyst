package analyst;

import java.util.Hashtable;
import java.util.Vector;


public class JumpCounter extends Hashtable <String, Hashtable <String, Integer>> {
	
	public JumpCounter(){
		super();
		
	}
	
	public void addJump(String to, String from){
		Hashtable <String, Integer> t = null;
		
		if (!this.containsKey(to))t = new Hashtable <String, Integer>();
			else t = get(to);
		if (t.containsKey(from)){ 
			Integer  count = t.get(from);
			t.remove(from);
			count = new Integer(count.intValue()+1);
			t.put(from, count);
		}
			else t.put(from, new Integer(1));
		put(to, t);
	}			
		
	
	public int getJumpCount(String to, String from){
		Hashtable <String, Integer> t = null;
		
		if (this.containsKey(to)) t= get(to);
			else return 0;
		if (! t.containsKey(from)) return 0;
		return t.get(from).intValue();
	}
	
	
}
