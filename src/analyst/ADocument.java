package analyst;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.JViewport;
import javax.swing.event.*;
import javax.swing.text.*;

import analyst.AData.ADataException;

import com.sun.org.apache.bcel.internal.generic.NEW;


public class ADocument extends DefaultStyledDocument implements DocumentListener,  Serializable
																	  {
	// document's properties
	public static String TitleProperty1 	= "Документ:";
	public static String ExpertProperty 	= "Эксперт:";
	public static String ClientProperty		= "Типируемый:";
	public static String DateProperty 		= "Дата:";
	public static String CommentProperty 	= "Комментарий:";

	
		
	protected HashMap <ASection, AData> aDataMap;
	protected Vector <ADocumentChangeListener> listeners; 
	public SimpleAttributeSet defaultStyle;
	public SimpleAttributeSet defaultSectionAttributes;
	public SimpleAttributeSet defaultSearchHighlightAttributes;

	
	private class DocumentFlowEvent  {
	 protected 	int type, offset, sectionNo; 
	 protected String style;
	 protected String comment;
	 public static final int LINE_BREAK 	= 1;
	 public static final int SECTION_START 	= 2;
	 public static final int SECTION_END 	= 3;

	 

	 
	public DocumentFlowEvent (int type, int offset, String style, String comment, int sectionNo){
		this.offset=offset;
		this.type=type;
		this.style=style;
		if (comment!=null) comment = comment.replaceAll("\n", "<br/>");
		this.comment=comment;
		this.sectionNo=sectionNo; 
		
	}
	 
	public int getOffset(){
		return offset;
	}
	
	public int getType(){
		return type;
	}	
	
	public String getStyle() {
		return style;
	}
	
	public String getComment() {
		return comment;
	}

	public int getSectionNo() {
		
		return sectionNo;
	}	
}//class DocumentFlowEvent
	
	private class RawAData{
		protected int handle = -1, beg = -1, end = -1; 
		
		String aData, comment;
		
		public RawAData(int handle){this.handle = handle;}
	
		protected void setID (int handle){this.handle=handle;}
		protected void setBegin (int beg){this.beg=beg;}
		protected void setEnd (int end){this.end=end;}
		protected void setAData (String aData){this.aData=aData;}
		protected void setComment (String com){this.comment=com;}

		protected int getID (){return handle;}
		protected int getBegin (){return beg;}
		protected int getEnd (){return end;}
		protected String getAData (){return aData;}
		protected String getComment (){return this.comment;}
		
		
		
	}//class RawAData
	 	
	ADocument(){
	super();
	addDocumentListener(this);

	//style of general text
	defaultStyle = new SimpleAttributeSet();
	defaultStyle. addAttribute(StyleConstants.FontSize, new Integer(16));
	//style of a section with mark-up
	defaultSectionAttributes = new SimpleAttributeSet();
	defaultSectionAttributes.addAttribute(StyleConstants.Background, Color.decode("#E0ffff"));
	defaultSearchHighlightAttributes = new SimpleAttributeSet();
	defaultSearchHighlightAttributes.addAttribute(StyleConstants.Background, Color.decode("#ff0000"));

	
	//init new Document
	initNew();

	}	
	
	public void initNew(){
		
		if (aDataMap == null) aDataMap = new HashMap <ASection, AData>();	
			else aDataMap.clear();
		try {
			this.replace(0, getLength(), "", defaultStyle);
		} catch (BadLocationException e) {
			System.out.println("Error in ADocument.initNew() :\n");
			e.printStackTrace();
		}
		String name = "Новый документ";
		putProperty((Object)Document.TitleProperty, (Object)name);
		fireADocumentChanged();
	}

	public class ASection implements Serializable{
	protected Position start;
	protected Position end;
	protected AttributeSet attributes;
	protected Vector<ADocumentChangeListener> listeners; 
	
	public ASection(Position start, Position end){
		this.start = start;
		this.end = end; 
	}
	
	public ASection(Position start, Position end, AttributeSet as){
		this.start = start;
		this.end = end; 
		this.attributes = as;
	}
	
	public AttributeSet getAttributes(){
		return attributes;
	}
	
	public void setAttributes(AttributeSet as){
		this.attributes = as;	
	}
	// @Override
	public boolean equals(Object o){
		if (! (o instanceof ASection )) return false;
		if ( (start.getOffset() == ((ASection)o).getStartOffset()) 
			 &&	(end.getOffset() == ((ASection)o).getEndOffset())
			) return true;
			return false;
	}
	
	public int getStartOffset(){
	return start.getOffset();	
	}
	
	public int getEndOffset(){
	return end.getOffset();	
	}
	
	public int getMiddleOffset(){
		return ((end.getOffset()+ start.getOffset())/2);	
		}
	
	public boolean isCollapsed(){
		if ( start.getOffset() ==  end.getOffset() ) return true;
		return false;
		}
	
	public boolean containsOffset(int offset){
	int b = start.getOffset();
	int e = end.getOffset();
	if ( b<e && offset >= b  && offset < e ) return true;
	return false;
	}
	
		
}//class ASection
	
private class RDStack extends Vector <String>{
 
private Hashtable <Integer, Integer> positionMap;

public RDStack(){
	super();
	positionMap = new Hashtable <Integer, Integer> ();
}

public void push (int handle, String element){
	add(element); 
	int position = size()-1;
	positionMap.put(new Integer(handle), new Integer (position));	
}
	
public int getCurrentSectionNo(){
	if (isEmpty()) return  -1;
	int position = size()-1; 
	Enumeration en = positionMap.keys();
	Integer nextKey = null;
	Integer nextValue = null;
	while(en.hasMoreElements()){
		nextKey = (Integer)en.nextElement();
		nextValue = positionMap.get(nextKey);
		int v = nextValue.intValue();
		if (v == position){
			return nextKey.intValue();
		}
	}
	return 0;
}

public void delete (int handle){
	Integer h = new Integer(handle);
	int position = positionMap.get(h).intValue();
	this.removeElementAt(position);
	positionMap.remove(h);
	Enumeration en = positionMap.keys();
	Integer nextKey = null;
	Integer nextValue = null;
	while(en.hasMoreElements()){
		nextKey = (Integer)en.nextElement();
		nextValue = positionMap.get(nextKey);
		int v = nextValue.intValue();
		if (v > position){
			positionMap.remove(nextKey);
			positionMap.put(nextKey, new Integer(v-1));
		}
	}
}

public String getCurrentStyle(){
if (isEmpty()) return null;
return get(size()-1);
}

}//class RDStack
	
/*	
public void addASection(int st, int en, AData data) {
	if (en<=st) return;
	try {
		ASection as = new ASection( createPosition(st), createPosition(en));
		as.setAttributes(getASectionAttributes(as));
		aDataMap.put(as, data);
		setCharacterAttributes(st, en-st, as.getAttributes(), false);
	} catch (BadLocationException e) {return;}	

}	
*/

public ASection getASection(int pos) {
	Set <ASection> set =  aDataMap.keySet();
	Vector <ASection> results = new Vector <ASection>();
	ASection r = null;
	
	Iterator <ASection> it = set.iterator();
	while (it.hasNext()){
		ASection as = it.next();
		if (as.containsOffset(pos)) results.add(as);
	}
	if (results.isEmpty())return null;
	
	int distance = Analyst.MAX_CHARACTERS;
	for (int i=0; i<results.size(); i++){
		ASection temp = results.get(i);
			int curdistance = Math.abs(pos - temp.getMiddleOffset());
			if (curdistance < distance) {r = temp; distance = curdistance;}
			if (curdistance == distance){
				if(temp.getStartOffset()>r.getStartOffset()) r = temp;
			}
	}
	return r;
}

public ASection getASectionThatStartsAt(int pos1) {
	Set <ASection> set =  aDataMap.keySet();
//	Vector <ASection> results = new Vector <ASection>();
	ASection r = null;
	
	Iterator <ASection> it = set.iterator();
	while (it.hasNext()){
		r = it.next();
		if (r.getStartOffset()== pos1) return r;
	}
	return null;
}

public AttributeSet getASectionAttributes(ASection as) {
	//default implementation
	SimpleAttributeSet set = new SimpleAttributeSet();
	set.addAttribute(StyleConstants.Background, Color.yellow);
	return set;
}

// @Override	
public	void changedUpdate(DocumentEvent e) {
	

	}//changedUpdate(); 	

@Override
protected void insertUpdate (AbstractDocument.DefaultDocumentEvent chng, AttributeSet set) {

set =  defaultStyle;

super.insertUpdate(chng, set);

}


protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng){
	super.removeUpdate(chng);	
	//int position = chng.getOffset();
	//chng.end();
	//this.setCharacterAttributes(position, 1,  defaultStyle, false);
}

@Override
public void removeUpdate(DocumentEvent e) {

} //removeUpdate()

public void removeCleanup(int offset){
	//int offset = e.getOffset();
	
//	this.setCharacterAttributes(offset, 1, defaultSectionAttributes, true);
	
	// проверяет не нужно ли удалить схлопнувшиеся сегменты
	Set<ASection> s = aDataMap.keySet();
	Iterator<ASection> it = s.iterator ();
	boolean foundCollapsed = false;
	while(it.hasNext()){
		ASection sect = it.next(); 
		if (sect.isCollapsed()){ it.remove(); foundCollapsed = true;}
	
	}
	if (foundCollapsed) fireADocumentChanged();
}


public AData getAData(ASection section) {
	
	return aDataMap.get(section);
}


public void removeASection(ASection aSection) {
	if (aSection==null) return;
	
	aDataMap.remove(aSection);
	
	int st = aSection.getStartOffset();
	int en = aSection.getEndOffset();
	SimpleAttributeSet attr = new SimpleAttributeSet();
	attr.addAttribute(StyleConstants.Background, Color.white);
	setCharacterAttributes(st, en-st, attr, false);	
	fireADocumentChanged();
}


public void updateASection(ASection aSection, AData data) {
	removeASection(aSection);
	addASection(aSection, data);
//	fireADocumentChanged();
}



public void addASection(ASection aSection, AData data) {

	int st = aSection.getStartOffset();
	int en = aSection.getEndOffset();
	int beg = Math.min(st, en);
	int len = Math.abs(st-en);	
	
	// удаляет сегменты с такими же границами
	Set<ASection> s = aDataMap.keySet();
	Iterator<ASection> it = s.iterator ();
	
	while(it.hasNext()){
		ASection sect = it.next(); 
		if (sect.getStartOffset() == beg && sect.getEndOffset() == beg+len) it.remove();
	}
	
	aDataMap.put(aSection, data);
	
	setCharacterAttributes(beg, len, defaultSectionAttributes, false);
	fireADocumentChanged();
}

public void addADocumentChangeListener (ADocumentChangeListener l){
	if (listeners == null) listeners = new Vector <ADocumentChangeListener>();
	listeners.add(l);
}

protected void fireADocumentChanged(){
	if (listeners == null) return;
	for(int i=0; i<listeners.size(); i++){
	listeners.get(i).aDocumentChanged(this);	
	}
}

public ASection getAnyAData() {
	if (aDataMap!=null && !aDataMap.isEmpty()) {
		Set keys = aDataMap.keySet();
		return (ASection)keys.iterator().next();
	}
	return null;
}

public HashMap<ASection, AData> getADataHashMap() {
	return aDataMap;
	
}

public void save(FileOutputStream fos){
if (fos == null){System.out.println("Error attempting to save file: FileOutputStream is null"); return;}	
try {
//writing the header
String text = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"> \n";
text += "<html> \n<head> \n<title> \n" + getProperty(TitleProperty) + " \n</title> \n" +
"	<style>"+
"			body 	{font-size:14px;color:black}\n"+
"			h1		{}\n"+
"			h2		{}\n"+
"			th		{font-size:18px;font-weight:bold}\n"+
"			small	{font-size:9px;color:darkgray}\n"+
""+
"	</style>\n"+
"</head> \n"+
"<body> \n";

	fos.write(text.getBytes());
	
	

//document title
	
text = "\n<h1>" + getProperty(TitleProperty) + "</h1>\n";

//document header
text += "<br/>\n<br/>";
text += "\n <table title=\"header\" border=1 width=\"40%\"> 	" 					+ "\n" +
				"<tr>" 												+ "\n" +
				"	<td>      "+TitleProperty1+"     </td>"			+ "\n" +
				"	<td>"  + this.getProperty(TitleProperty) + "	</td>"	+ "\n" +
				"</tr>"												+ "\n" +
				"<tr>" 												+ "\n" +
				"	<td>      "+ClientProperty+"     </td>" 			+ "\n" +
				"	<td>" + this.getProperty(ClientProperty)+ " 	</td>"	+ "\n" +
				"</tr>"												+ "\n" +
				"<tr>" 												+ "\n" +
				"	<td>      "+ExpertProperty+"     </td>"		+ "\n" +
				"	<td>"  + this.getProperty(ExpertProperty) + "	</td>"	+ "\n" +
				"</tr>"												+ "\n" +
				"<tr>" 												+ "\n" +
				"	<td>      "+DateProperty+"     </td>"		+ "\n" +
				"	<td>" + this.getProperty(DateProperty)+" </td>"		+ "\n" +
				"</tr>"												+ "\n" +
				"<tr>" 												+ "\n" +
				"	<td>      "+CommentProperty+"     </td>"		+ "\n" +
				"	<td>" + this.getProperty(CommentProperty)+" </td>"		+ "\n" +
				"</tr>"												+ "\n" +
				"</table >"											+ "\n";
	
fos.write(text.getBytes());

//document content
		text = "<br/>\n";
		text += "\n<h2>  АНАЛИЗ </h2>\n";		
		text += "\n <table title=\"protocol\" border=2 width=\"100%\"> 	" 			+ "\n" +
		"<tr>" 															+ "\n" +
		"	<th width=\"60%\"> ВОПРОСЫ И ОТВЕТЫ </th>"					+ "\n" +
		"	<th width=\"40%\"> АНАЛИЗ ЭКСПЕРТА</th>"  					+ "\n" +
		"</tr>" 														+ "\n"  +
		"<tr>" 															+ "\n" +		
		"	<td>"																
		;
		
fos.write(text.getBytes());
text = "";
// PREPARING  
	Vector <DocumentFlowEvent> flowEvents = new Vector<DocumentFlowEvent>();
	Vector <Integer> openSections = new Vector <Integer>();
	Vector <Position> lineBreaks = new Vector <Position>();
	


	String qwerty = null;
	for (int i = 0; i< this.getLength(); i++){
		qwerty = getText(i, 1);
		if(qwerty.equals("\n") ){
		  lineBreaks.add(createPosition(i));
		}
	}

  Set keySet= aDataMap.keySet();
  Vector <ASection> sectionStart = new Vector<ASection>();
  Vector <ASection> sectionEnd = new Vector<ASection>();
  if (keySet!=null){
	  Iterator <ASection> it = keySet.iterator();
	  ASection sss=null;
	  while (it.hasNext()){
		  sss=it.next();
		  sectionStart.add(sss);  
		  sectionEnd.add(sss);
	  }
  }
  
  Vector temp = new Vector();
  int index=Analyst.MAX_CHARACTERS;
  
  ASection sec = null;
  while (!sectionStart.isEmpty()){
	   index=Analyst.MAX_CHARACTERS;
	   for (int j = 0; j < sectionStart.size(); j++){
		if (sectionStart.get(j).getStartOffset()<= index){
			sec	= sectionStart.get(j);
			index = sec.getStartOffset();
		} 		
	  }
	   temp.add(sec);
	   sectionStart.removeElement(sec);
	   
 }
  
  sectionStart = temp;
  
  temp = new Vector();

  while (!sectionEnd.isEmpty()){
	   index=Analyst.MAX_CHARACTERS;
	   for (int j = 0; j < sectionEnd.size(); j++){
		if (sectionEnd.get(j).getEndOffset()<= index){
			sec = sectionEnd.get(j);
			index = sec.getEndOffset();
		}  
	   }
	   temp.add(sec);
	   sectionEnd.removeElement(sec);
	   
 }  
 
  sectionEnd = temp; 
  
 
  
/*  int lbIndex = 0;
  int ssIndex = 0;
  int seIndex = 0;
  int mark = 0;
*/
  
  int ssNo = 1;
  int seNo =1;
  
  while (!(  lineBreaks.isEmpty()
		  && sectionStart.isEmpty()
		  && sectionEnd.isEmpty() )){
	  
			  int lb = Analyst.MAX_CHARACTERS;
			  int ssOffset = Analyst.MAX_CHARACTERS;
			  int seOffset = Analyst.MAX_CHARACTERS; 
			  ASection ss = null;
			  ASection se = null;			  
			  
	            if (!sectionStart.isEmpty()){ss = sectionStart.get(0); ssOffset = ss.getStartOffset();}
	            if (!sectionEnd.isEmpty())  {se = sectionEnd.get(0);   seOffset = se.getEndOffset();}
	            if (!lineBreaks.isEmpty())  lb = lineBreaks.get(0).getOffset();
	    
	
		        if ((ssOffset <= lb ) && (ssOffset <= seOffset )){
		        	flowEvents.add( new DocumentFlowEvent(DocumentFlowEvent.SECTION_START, 
		        										 ssOffset, 
		        										 getHTMLStyleForAData(aDataMap.get(ss)),
		        										 "{"+ssNo+ ": " + aDataMap.get(ss).toString()+"} "+ aDataMap.get(ss).getComment()+"\n",
		        										 ssNo));
		        	sectionStart.remove(0);
		        	ssNo++;
		        }
		        else 
		        	
			        if ((seOffset <= lb ) && (seOffset <= ssOffset )){
			        	flowEvents.add( new DocumentFlowEvent(DocumentFlowEvent.SECTION_END, 
			        										 seOffset, 
			        										 getHTMLStyleForAData(aDataMap.get(se)),
			        										 aDataMap.get(se).getComment(),
			        										 seNo));
			        	sectionEnd.remove(0);
			        	seNo++;
			        }
			        else 
			        	
				        if (( lb<=  seOffset) && (lb <= ssOffset )){
				        	flowEvents.add( new DocumentFlowEvent(DocumentFlowEvent.LINE_BREAK, 
				        										 lb, 
				        										 null,
				        										 null,
				        										 0 ));
				        	lineBreaks.remove(0);
				        }		        
  }
  
  
		// write contents

 // flowEvents.capacity();
int pos0 = 0;
int pos1 = 0;
int k = 0;
String analisys = "";
DocumentFlowEvent event = null;
int eventType = -1;
RDStack stack = new RDStack();


if (flowEvents!=null && !flowEvents.isEmpty()){
	for  (int z=0; z<flowEvents.size(); z++){
		event = flowEvents.get(z);
		pos0 = pos1;
		pos1 = event.getOffset();
		eventType = event.getType();	
		
		//writing text 
		try {
			String t = this.getText(pos0,pos1-pos0);
			text += t;
		} catch (BadLocationException e) {
			System.out.println("Error retrieving text from Document when saving document:");
			e.printStackTrace();
		}
		
		// writing text remainder from last event to the end of the document
		if (z == flowEvents.size()-1){
			try { 
				int finish = this. getLength();
				  if (finish>pos1) text += this.getText(pos1, finish-pos1);
			} catch (BadLocationException e) {
				System.out.println("Error retrieving text from Document when saving document:");
				e.printStackTrace();
			}			
		}
		
		//analyzing event and generating  mark-up
		
		if (eventType == DocumentFlowEvent.SECTION_START){
			k = event.getSectionNo();
			if (!stack.isEmpty()) text+=" </span>";
			text += "<small>["+k+"|</small>";
			text += "<span style="+event.getStyle()+">";
			stack.push(k, event.getStyle());
			analisys +=  event.getComment();
			
		} // event == SECTION_START
		else
		if (eventType == DocumentFlowEvent.SECTION_END){
			k = event.getSectionNo();
			if (!stack.isEmpty()){
				text += "</span>";
				text += "<small>|"+k+"]</small>";
				stack.delete(k);
				if (!stack.isEmpty()){
					text += "<span style="+stack.getCurrentStyle()+">";
				}
			}
		} // event == SECTION_END	
		
			if (eventType == DocumentFlowEvent.LINE_BREAK || z == flowEvents.size()-1){
				boolean makeBreak = true;	
				if (z>0 && (flowEvents.get(z-1).getType()==DocumentFlowEvent.SECTION_END)||
						(z<flowEvents.size()-1 && flowEvents.get(z+1).getType()==DocumentFlowEvent.SECTION_START)||
						(z == flowEvents.size()-1)){
				
					if (!stack.isEmpty())text += "</span>";
					text += "</td>\n";
					text += "<td>"+ analisys+"</td>";
					analisys="";
					if (!(z == flowEvents.size()-1)) text += "\n</tr>\n<tr>\n<td>";
					if (!stack.isEmpty())
						text += "<span style="+stack.getCurrentStyle()+">";
					makeBreak = false;
				}
			 if (makeBreak) text += "<br/>";	
			} // event == LINE_BREAK
		

	}//for
}//if
	

text += "		</td>" 														+ "\n"  +
		"</tr>" 															+ "\n" +		
		"</table >"															+ "\n" +
		"</body >"															+ "\n" +
		"</html >"															+ "\n";
fos.write(text.getBytes());
fos.flush();
fos.close();
} catch (IOException e) {
	System.out.println("Ошибка записи файла :");	
	e.printStackTrace();
	} catch (BadLocationException e) {
	
	e.printStackTrace();
}
	
}//save()



public void load(FileInputStream fis, boolean append){
//	
	
	InputStreamReader isr = new InputStreamReader(fis);
	String leftColumn="";
    String rightColumn ="";
    String allText ="";
    int appendOffset=0;
    if (append) appendOffset = getLength();
	
	final class SectorData {
	int handle;
	int startPos;
	int endPos;
	String dataString;
	
	public SectorData(int handle, int startPos, int endPos){
		this.handle=handle;
		this.startPos=startPos;
		this.endPos=endPos;
	}
		
	public SectorData(int handle, int startPos, int endPos, String dataString){
		this.handle=handle;
		this.startPos=startPos;
		this.endPos=endPos;
		this.dataString=dataString;
	}
	
	public void setDataString(String dataString){
		this.dataString=dataString;
	}
	
	public int getHandle()	{return handle;}
	public int getstartPos(){return startPos;}
	public int getendPos()	{return endPos;}
	}// class SectorData

	
	

      
    Vector <SectorData> sectorData = new Vector <SectorData>();
    boolean finished = false;
    // reading the file
    try {
		int offset = 0;
		int length = fis.available();
		char[] buf = new char[length];
		int bytesRead;
		
			while(!finished){
				    
					bytesRead = isr.read(buf, 0, length);
					if (bytesRead > 0) allText += String.valueOf(buf, 0, bytesRead);
					//offset += bytesRead;
					  else {finished = true;
							isr.close();
							fis.close();
					  }
					}
				
		
	} catch (IOException e) {
				System.out.println("Ошибка чтения файла :");	
				e.printStackTrace();
			}
	//  PARSING THE INPUT DATA
	finished = false;
	int offset =0;
	
	// looking for the table "header"
	int searchIndex =allText.indexOf("title=\"header\"", 0);
	
	String colStartToken = "<td>";
	String colEndToken = "</td>";
	String result=null;
	String headerResult=null, leftHeaderColumn = null, rightHeaderColumn = null;
	
	
	// looking through columns of table "header" and retreiving text of the left and right columns
	Dictionary<Object, Object> properties = getDocumentProperties();
	if (!append){
		properties.remove(TitleProperty);
		properties.remove(ExpertProperty);
		properties.remove(ClientProperty);
		properties.remove(DateProperty);
		properties.remove(CommentProperty);
	}

	while(searchIndex>0){
		
		searchIndex=allText.indexOf("<tr>", searchIndex);
		if (searchIndex>0) headerResult = findTagContent(allText, colStartToken,colEndToken,searchIndex); 
			else break;
		if (headerResult !=null){ 
			leftHeaderColumn =headerResult.trim();
			searchIndex = allText.indexOf(colEndToken, searchIndex)+colEndToken.length();
		}
		
		if (searchIndex>0) headerResult = findTagContent(allText, colStartToken,colEndToken,searchIndex);
			else break;
		if (headerResult !=null){ 
			rightHeaderColumn =headerResult.trim();
			searchIndex = allText.indexOf(colEndToken, searchIndex)+colEndToken.length();
		}
		
		//обработка заголовка
		leftHeaderColumn.replaceAll("\t", "");
		rightHeaderColumn.replaceAll("\t", "");
		
		
		
		if(!append){
			if (leftHeaderColumn.equals(TitleProperty1)) {
				properties.put(TitleProperty, rightHeaderColumn);
			}
			if (leftHeaderColumn.equals(ExpertProperty)){
				properties.put(ExpertProperty, rightHeaderColumn);
			} 
			if (leftHeaderColumn.equals(ClientProperty)){
				properties.put(ClientProperty, rightHeaderColumn);	
			} 
			if (leftHeaderColumn.equals(DateProperty)){
				properties.put(DateProperty, rightHeaderColumn);
			} 
			if (leftHeaderColumn.equals(CommentProperty)){				
				properties.put(CommentProperty, rightHeaderColumn);
			} 
		} else{
			if (leftHeaderColumn.equals(ExpertProperty)){
				String expert = (String)properties.get(ExpertProperty);
				if (!expert.equals(rightHeaderColumn)){ 
					properties.remove(ExpertProperty);
					rightHeaderColumn = expert + "; " + rightHeaderColumn;
					properties.put(ExpertProperty, rightHeaderColumn);
				}
			} 
		}
		 this.setDocumentProperties(properties);
		
	}
	
	
	
	// looking for the table "protocol"
	searchIndex =allText.indexOf("title=\"protocol\"", 0);
	
	// looking through columns of table "protocol" and retreiving text of the left and right columns
	while(searchIndex>0){
		
		searchIndex=allText.indexOf("<tr>", searchIndex);
		if (searchIndex>0) result = findTagContent(allText, colStartToken,colEndToken,searchIndex); 
			else break;
		if (result !=null) {leftColumn +=result;
							leftColumn +="<br/>";//adding breaks because there are no breaks on row boundaries
							searchIndex = allText.indexOf(colEndToken, searchIndex)+colEndToken.length();
							 
		}
		
		if (searchIndex>0) result = findTagContent(allText, colStartToken,colEndToken,searchIndex);
			else break;
		if (result !=null) {rightColumn +=result;
							//rightColumn +="<br/>"; //there are breaks on row boundaries for the right column
							searchIndex = allText.indexOf(colEndToken, searchIndex)+colEndToken.length();
		}
	}

	//remove all tabs
	//leftColumn = leftColumn.replaceAll("\t", "");
	leftColumn = leftColumn.replaceAll("\n", "");
	leftColumn = leftColumn.replace("<br/>", "\n");
	
	rightColumn= rightColumn.replaceAll("\n", "");	
	//rightColumn= rightColumn.replaceAll("\t", "");	
	rightColumn= rightColumn.replace("<br/>", "\n");

	// Убираем все лишние теги 
	
	leftColumn = removeTag (leftColumn , "<span", ">");
	leftColumn = removeTag (leftColumn , "</span", ">");
	leftColumn = removeTag (leftColumn , "<small", ">");
	leftColumn = removeTag (leftColumn , "</small", ">");
   
	
	Hashtable <Integer, RawAData> rawData = new Hashtable  <Integer, RawAData>();
	
		int posBeg = leftColumn.indexOf("[");
		int posEnd = -1;
		
		// processing the left column's content
		while (leftColumn.indexOf("[", 0)>=0 || leftColumn.indexOf("]", 0)>=0){
			int handle = -1;
			int a = 0 ;
			RawAData data = null;
			String handleNo = null;
			//if we met the opening tag
            if ((leftColumn.indexOf("[", 0) >=0) && (leftColumn.indexOf("[", 0) <= leftColumn.indexOf("]", 0) )){
            	posBeg = leftColumn.indexOf("[");
            	handleNo = findTagContent(leftColumn, "[", "|", 0);
				handle =Integer.parseInt(handleNo);
				if(handle >127)
					a = handle;
				leftColumn=leftColumn.replace(findTag(leftColumn, "[", "|", 0), "");
				data = new RawAData(handle);
				data.setBegin(posBeg);
				rawData.put(new Integer(handle), data);
				//if we met the closing tag
            }else
                if (leftColumn.indexOf("]", 0) >=0  ){
	            	posEnd = leftColumn.indexOf("|");
	            	handleNo = findTagContent(leftColumn, "|", "]", 0);
					handle =Integer.parseInt(handleNo);
					leftColumn=leftColumn.replace(findTag(leftColumn, "|", "]", 0), "");
					data = rawData.get(new Integer(handle));
					if (data !=null)data.setEnd(posEnd);
            }
		}			

		
		
		posBeg = rightColumn.indexOf("{");
		posEnd = -1;
		
		// processing the right column's content
		while (posBeg>=0){
			String handleNo = findTagContent(rightColumn, "{", ":", posBeg);
			int handle =Integer.parseInt(handleNo);
			RawAData data = rawData.get(new Integer (handle));
			if (data!=null){
				String aDataString = findTagContent(rightColumn, ":", "}", posBeg);
				data.setAData(aDataString);
				posEnd = rightColumn.indexOf("{", posBeg+1);
				if (posEnd<0) posEnd = rightColumn.length()-1;
				int posBeg1 = rightColumn.indexOf("}",posBeg)+1;
				String com = null;
				if (posBeg1>0) com = new String(rightColumn.substring (posBeg1, posEnd)); 
				
				if (com != null) com = com.trim();
				com = " " + com;
				//removing last line brake which was added when saving
				while (com != null &&  (com.lastIndexOf("\n") == (com.length()-1))) 
							com  = com.substring(0, com.length()-1);
/*				
				while (com != null  &&  com.startsWith(" "))
						 	com  = com.substring(1, com.length()-1);
*/				
				if (com == null) com = "";
				data.setComment(com);
			}
						
			posBeg = rightColumn.indexOf("{", posBeg+1);
		}
		/// adding plain text to the document
		if (!append) aDataMap.clear();
		try {
			
			if (!append) this.remove(0, this.getEndPosition().getOffset()-1);
			if (append) leftColumn = leftColumn+ "\n";
			this.insertString(appendOffset, leftColumn, defaultStyle);
		} catch (BadLocationException e) {
			
			e.printStackTrace();
		}
		
		//creating and adding the segments AData info
		
		Iterator <RawAData> it =  rawData.values().iterator();
		RawAData temp = null;
		while(it.hasNext()){
			temp = it.next();
			AData ad=null;
			try {
				ad = AData.parceAData(temp.getAData());
				String ggg = temp.getComment();
				ad.setComment(temp.getComment());
				int beg = temp.getBegin();
				int end = temp.getEnd();
				aDataMap.put(new ASection(createPosition(beg+appendOffset), createPosition(end+appendOffset)), ad);
				setCharacterAttributes(beg+appendOffset, end-beg, defaultSectionAttributes, false);
			} catch (ADataException e) {
				System.out.println("Ошибка разбора строки анализа при загрузке документа: "+ temp.getAData());
				e.printStackTrace();
			} catch (BadLocationException e) {
				System.out.println("Ошибка создания позиций размеченного сегмента при загрузке документа: " +
						"[" + temp.getBegin()+ "..." + temp.getEnd()+ "]");
				e.printStackTrace();
			}	
		
			
			this.fireADocumentChanged();
		}
		
		
	
}//load(FileInputStream fis)

private String removeTag(String source, String startToken,
		String endToken) {
	String tag = null;
	tag = this.findTag(source, startToken, endToken, 0);
	while(tag!= null){
		source = source.replace((CharSequence)tag, (CharSequence)"");	
		tag = this.findTag(source, startToken, endToken, 0);	
	}	
	return source;
}

private String findTagContent(String text, String startToken, String endToken,
		int fromIndex) {
 
     int startIndex = text.indexOf(startToken, fromIndex);
     int endIndex = text.indexOf(endToken, startIndex);
     
     if (startIndex>=0 && endIndex >0 && endIndex>startIndex) 
    	 				return text.substring(startIndex+startToken.length(), endIndex);
     return null;
}

private String findTag(String text, String startToken, String endToken,
		int fromIndex) {
 
     int startIndex = text.indexOf(startToken, fromIndex);
     int endIndex = text.indexOf(endToken, startIndex);
     
     if (startIndex>=0 && endIndex >0 && endIndex>startIndex) 
    	 				return text.substring(startIndex, endIndex+endToken.length());
     return null;
}

public String getHTMLStyleForAData(AData data){
	
	if (data.getAspect().equals(AData.DOUBT)){return "background-color:#EAEAEA";}
	String res = "\"";
	String dim = data.getDimension();
	String mv = data.getMV();
	String sign = data.getSign();
	if (dim !=null &&
		(	dim.equals(AData.D1) ||
		    dim.equals(AData.D2) ||	
		    dim.equals(AData.MALOMERNOST) )												) {
		res += "background-color:#AAEEEE;";	
	} else
		if (dim !=null &&
				(	dim.equals(AData.D3) ||
				    dim.equals(AData.D4) ||	
				    dim.equals(AData.MNOGOMERNOST) )												) {
			  // противный зеленый
		res += "background-color:#AAEEAA;";}			
	if (sign !=null){ res += "color:#FF0000;" ;}
	if (mv   !=null){ res += "background-color:#FFFFCC;" ;}	
	//Если не задан другой стиль, то будет этот стиль
	if (res.equals("\""))res += "text-decoration:underline";
	
	//TODO доделать разметку для случая перевода с одного аспекта на другой
	res += "\"";
	return res;
}//getStyleForAData()

@Override
public void insertUpdate(DocumentEvent e) {
	// TODO Auto-generated method stub
	
}
	

																	  
} // class ADocument
