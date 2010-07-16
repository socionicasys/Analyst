/**
 * 
 */
package analyst;

/**
 * @author Виктор
 *
 */
public class EndNodeObject extends Object {
	protected String string;
	protected int offset;
	

	public EndNodeObject (int offset, String str) {
		this.string=str;
		this.offset=offset;
	}
	
	public int getOffset(){return offset;}
	
	public String getString(){return string;}
	
	public void setString(String str){this.string = str;}
	public void setOffset(int offset){this.offset = offset;}
	
	public String toString(){
		if(string == null)return "";
			return string.toString();
	}
	
	
}
