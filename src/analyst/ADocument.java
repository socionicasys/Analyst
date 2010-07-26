package analyst;

import analyst.StyledText;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.*;
import javax.swing.text.*;


public class ADocument extends DefaultStyledDocument implements DocumentListener  
																	  {
	public static final String ENCODING = "UTF-8";
	public static final String DEFAULT_TITLE = "Новый документ";
	// document's properties names
	public static final String TitleProperty1 	= "Документ:";
	public static final String ExpertProperty 	= "Эксперт:";
	public static final String ClientProperty	= "Типируемый:";
	public static final String DateProperty 	= "Дата:";
	public static final String CommentProperty 	= "Комментарий:";

	
		
	protected HashMap <ASection, AData> aDataMap;
	protected Vector <ADocumentChangeListener> listeners; 
	public SimpleAttributeSet defaultStyle;
	public SimpleAttributeSet defaultSectionAttributes;
	public SimpleAttributeSet defaultSearchHighlightAttributes;
	
	private int progress = 0;

	
	private class DocumentFlowEvent implements Comparable<DocumentFlowEvent> {
	 protected 	int type, offset, sectionNo; 
	 protected String style;
	 protected String comment;
	 public static final int LINE_BREAK 	= 1;
	 public static final int SECTION_START 	= 2;
	 public static final int SECTION_END 	= 3;
	 public static final int NEW_ROW		= 4;
	 public static final int BOLD_START		= 5;
	 public static final int BOLD_END		= 6;
	 public static final int ITALIC_START	= 7;
	 public static final int ITALIC_END		= 8;
	 
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

	@Override
	public int compareTo(DocumentFlowEvent o) {
		// Реализация интерфейса java.lang.Comparable<T>
		// Делает возможной сортировку массива из DocumentFlowEvent-ов
		// Сравнение происходит только по позиции (offset)
		return this.offset - o.offset;
	}	
}//class DocumentFlowEvent
	
	public class RawAData{
		protected int handle = -1, beg = -1, end = -1; 
		
		String aData, comment;
		
		public RawAData(int handle){this.handle = handle;}
	
		public void setID (int handle){this.handle=handle;}
		public void setBegin (int beg){this.beg=beg;}
		public void setEnd (int end){this.end=end;}
		public void setAData (String aData){this.aData=aData;}
		public void setComment (String com){this.comment=com;}

		public int getID (){return handle;}
		public int getBegin (){return beg;}
		public int getEnd (){return end;}
		public String getAData (){return aData;}
		public String getComment (){return this.comment;}
		
		
		
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
		
		putProperty((Object)TitleProperty,  (Object)DEFAULT_TITLE);
		putProperty((Object)ExpertProperty, (Object)"");
		putProperty((Object)ClientProperty, (Object)"");
		Date date = new Date();
		putProperty((Object)DateProperty, 	(Object) date.toLocaleString());
		putProperty((Object)CommentProperty, "");

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

public void fireADocumentChanged(){
	if (listeners == null) return;
	for(int i=0; i<listeners.size(); i++){
	listeners.get(i).aDocumentChanged(this);	
	}
}



public void save(FileOutputStream fos, IOWorker iow) throws Exception {
	
	int headerSaveProgress = 20;
	int writePreparationProgress = 20;
	int textWriteProgress= 40;
	int reportWriteProgress= 20;
	
	iow.firePropertyChange("progress", null, new Integer(0));
	
if (fos == null){System.out.println("Error attempting to save file: FileOutputStream is null"); return;}	

//writing the header
String text = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"> \n";
text += "<meta http-equiv=\"Content-Type\" content=\"text/html charset=" + ENCODING + "\"/>";
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

	fos.write(text.getBytes(ENCODING));
	


//document title
	
text = "\n<h1>" + getProperty(TitleProperty) + "</h1>\n";

//saved with version
String comm =(String)getProperty(CommentProperty);
if (comm != null){ 
	
	int ind = comm.indexOf("Сохранено версией:");
	if (ind >=0){
		comm = comm.substring(0,ind);
	}
		else if (comm.length()>0) comm = comm + "<br/>";
	comm += "Сохранено версией:"  + Analyst.version;
}

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
				"	<td>" + comm +" </td>"		+ "\n" +
				"</tr>"												+ "\n" +
				"</table >"											+ "\n";
	
fos.write(text.getBytes(ENCODING));

iow.firePropertyChange("progress", null, new Integer(headerSaveProgress));

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
		
fos.write(text.getBytes(ENCODING));
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

  Set<ASection> keySet= aDataMap.keySet();
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
  
  Vector<ASection> temp = new Vector<ASection>();
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
  
  temp = new Vector<ASection>();

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
	for (int ssNo = 1; ssNo <= sectionStart.size(); ssNo++) {
		ASection ss = sectionStart.get(ssNo - 1);
		int ssOffset = ss.getStartOffset();
		flowEvents.add(new DocumentFlowEvent(
				DocumentFlowEvent.SECTION_START,
				ssOffset,
				getHTMLStyleForAData(aDataMap.get(ss)),
				"{" + ssNo + ": " + aDataMap.get(ss).toString() + "} " + aDataMap.get(ss).getComment() + "\n",
				ssNo)
		);
	}
	for (int seNo = 1; seNo <= sectionEnd.size(); seNo++) {
		ASection se = sectionEnd.get(seNo - 1);
		int seOffset = se.getEndOffset();
		flowEvents.add(new DocumentFlowEvent(
				DocumentFlowEvent.SECTION_END,
				seOffset,
				getHTMLStyleForAData(aDataMap.get(se)),
				aDataMap.get(se).getComment(),
				seNo)
		);
	}
	Element rootElem = this.getDefaultRootElement();
	SimpleAttributeSet boldAttribute = new SimpleAttributeSet();
	StyleConstants.setBold(boldAttribute, true);
	SimpleAttributeSet italicAttribute = new SimpleAttributeSet();
	StyleConstants.setItalic(italicAttribute, true);
	for (int parIndex = 0; parIndex < rootElem.getElementCount(); parIndex++) {
		Element parElem = rootElem.getElement(parIndex);
		for (int i = 0; i < parElem.getElementCount(); i++) {
			Element e = parElem.getElement(i);
			int elemStart = e.getStartOffset();
			int elemEnd = e.getEndOffset();
			AttributeSet attrs = e.getAttributes();
			if (attrs.containsAttributes(boldAttribute)) {
				flowEvents.add(new DocumentFlowEvent(DocumentFlowEvent.BOLD_START,
						elemStart, null, null, 0));
				flowEvents.add(new DocumentFlowEvent(DocumentFlowEvent.BOLD_END,
						elemEnd, null, null, 0));
			}
			if (attrs.containsAttributes(italicAttribute)) {
				flowEvents.add(new DocumentFlowEvent(DocumentFlowEvent.ITALIC_START,
						elemStart, null, null, 0));
				flowEvents.add(new DocumentFlowEvent(DocumentFlowEvent.ITALIC_END,
						elemEnd, null, null, 0));
			}
		}
	}
	for (Position position : lineBreaks) {
		int lb = position.getOffset();
		boolean replaceBreak = false;
		if (!flowEvents.isEmpty()) {
			DocumentFlowEvent prevEvent = flowEvents.lastElement();
			if (prevEvent.type == DocumentFlowEvent.LINE_BREAK &&
				prevEvent.offset == lb - 1) {
				// Заменяем два идущих подряд LINE_BREAK на NEW_ROW
				replaceBreak = true;
			}
		}
		if (replaceBreak) {
			flowEvents.set(flowEvents.size() - 1,
				new DocumentFlowEvent(DocumentFlowEvent.NEW_ROW,
					lb - 1, null, null, 0));
		}
		else {
			flowEvents.add(new DocumentFlowEvent(
					DocumentFlowEvent.LINE_BREAK, lb, null, null, 0));
		}
	}
	Collections.sort(flowEvents);
	
	if (!flowEvents.isEmpty() && (flowEvents.lastElement().getType() != DocumentFlowEvent.NEW_ROW)) {
		flowEvents.add(new DocumentFlowEvent(
				DocumentFlowEvent.NEW_ROW, this.getEndPosition().getOffset() - 1,
				null, null, 0));
	}

	iow.firePropertyChange("progress", null, new Integer(headerSaveProgress + 
						writePreparationProgress));
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
		

		iow.firePropertyChange("progress", null, new Integer(headerSaveProgress + 
								writePreparationProgress+
								textWriteProgress*z/flowEvents.size()));
		
		//writing text 

			String t = this.getText(pos0,pos1-pos0);
			text += t;

		
		// writing text remainder from last event to the end of the document
		if (z == flowEvents.size()-1){
					int finish = this. getLength();
					if (finish>pos1) text += this.getText(pos1, finish-pos1);
		
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
		else if (eventType == DocumentFlowEvent.BOLD_START) {
			text += "<b>";
		}
		else if (eventType == DocumentFlowEvent.BOLD_END) {
			text += "</b>";
		}
		else if (eventType == DocumentFlowEvent.ITALIC_START) {
			text += "<i>";
		}
		else if (eventType == DocumentFlowEvent.ITALIC_END) {
			text += "</i>";
		}
		else if (eventType == DocumentFlowEvent.NEW_ROW || z == flowEvents.size() - 1) {
			if (!stack.isEmpty()) {
				text += "</span>";
			}
			text += "</td>\n";
			text += "<td>" + analisys;
			text += "</td>";
			analisys = "";
			if (z != flowEvents.size() - 1) {
				text += "\n</tr>\n<tr>\n<td>";
			}
			if (!stack.isEmpty()) {
				text += "<span style=" + stack.getCurrentStyle() + ">";
			}
		} // eventType == DocumentFlowEvent.NEW_ROW
		else if (eventType == DocumentFlowEvent.LINE_BREAK)
		{
			text += "<br/>";
		} // eventType == DocumentFlowEvent.LINE_BREAK
	}//for
}//if
	// если в документе нет разметки - просто пишем текст в левый столбец таблицы
	else{
		text += getText(0, getLength());
		text += "</td><td></td>";
	}

text += //"		</td>" 														+ "\n"  +
		"</tr>" 															+ "\n" +		
		"</table>"															+ "\n";
//if not generating report
Analyst an = iow.getProgressWindow().getAnalyst();
		
//fos.write(text.getBytes(ENCODING));

iow.firePropertyChange("progress", null, new Integer(headerSaveProgress + 
		writePreparationProgress+
		textWriteProgress+
		reportWriteProgress));

// if generating report
if (an.getGenerateReport()){
	
	
	text += "<br/>" +	
			"<h1> Анализ типирования </h1>" +
			"<br/>" ;

	text += an.getNavigeTree().getReport();
	text += an.getAnalisysTree().getReport();	
}

    text += 
    		"<br/>"+
    		"Протокол типирования создан программой \"Информационный анализ\"<br/>"+
    		"© Школа системной соционики, Киев.<br/>"+
			"http://www.socionicasys.ru\n";
	
	
	text +=
		"</body >"															+ "\n" +
		"</html >"															+ "\n";
		
fos.write(text.getBytes(ENCODING));

fos.flush();
fos.close();

iow.firePropertyChange("progress", null, new Integer(100));
	
}//save()



public void load(FileInputStream fis, boolean append, IOWorker iow) throws Exception
															{
//	
	
	InputStreamReader isr = new InputStreamReader(fis, Charset.forName(ENCODING));
	String leftColumn="";
    String rightColumn ="";
    String allText ="";
    
    int fileLoadProgress = 20;
    int leftColumnParseProgress = 50;
    int rightColumnParseProgress = 25;
    int textAddProgress = 5;
    int aDataCteationProgress = 0;
    
    
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
    
		int offset = 0;
		int length = fis.available();
		char[] buf = new char[length];
		int bytesRead;
		
		iow.firePropertyChange("progress", null, new Integer(fileLoadProgress/2));	
		
			while(!finished){
				    
					bytesRead = isr.read(buf, 0, length);
					if (bytesRead > 0) allText +=  new String(buf, 0, bytesRead);
					//offset += bytesRead;
					  else {finished = true;
							isr.close();
							fis.close();
					  }
					}
	iow.firePropertyChange("progress", null, new Integer(fileLoadProgress));		
		
	
	//  PARSING THE INPUT DATA
	finished = false;
	offset =0;
	
	// looking for the table "header"
	int searchIndex =allText.indexOf("title=\"header\"", 0);
	
	String colStartToken = "<td>";
	String colEndToken = "</td>";
	String result=null;
	String headerResult=null, leftHeaderColumn = null, rightHeaderColumn = null;
	
	String leftHeaderText  = allText.substring(searchIndex, allText.indexOf("</table", searchIndex));
	
	// looking through columns of table "header" and retreiving text of the left and right columns

	Dictionary<Object, Object> properties = getDocumentProperties();
	

	searchIndex=leftHeaderText.indexOf("<tr>", 0);
	while(searchIndex>0){
		
	
		
		searchIndex=leftHeaderText.indexOf("<tr>", searchIndex);
		if (searchIndex>0) headerResult = findTagContent(leftHeaderText, colStartToken,colEndToken,searchIndex); 
			else break;
		if (headerResult !=null){ 
			leftHeaderColumn =headerResult.trim();
			searchIndex = leftHeaderText.indexOf(colEndToken, searchIndex)+colEndToken.length();
		}
		
		if (searchIndex>0) headerResult = findTagContent(leftHeaderText, colStartToken,colEndToken,searchIndex);
			else break;
		if (headerResult !=null){ 
			rightHeaderColumn =headerResult.trim();
			searchIndex = leftHeaderText.indexOf(colEndToken, searchIndex)+colEndToken.length();
		}
		
		//обработка заголовка
		leftHeaderColumn.replaceAll("\t", "");
		rightHeaderColumn.replaceAll("\t", "");
		rightHeaderColumn = rightHeaderColumn.replaceAll("<br/>", "\n");
		
		
		if(!append){
			if (leftHeaderColumn.equals(TitleProperty1)) {
				iow.firePropertyChange("DocumentProperty", TitleProperty,  new String(rightHeaderColumn));
			}
			if (leftHeaderColumn.equals(ExpertProperty)){
				iow.firePropertyChange("DocumentProperty", ExpertProperty,  new String(rightHeaderColumn));
			} 
			if (leftHeaderColumn.equals(ClientProperty)){
				iow.firePropertyChange("DocumentProperty", ClientProperty,  new String(rightHeaderColumn));	
			} 
			if (leftHeaderColumn.equals(ClientProperty)){
				iow.firePropertyChange("DocumentProperty", ClientProperty,  new String(rightHeaderColumn));
			} 
			if (leftHeaderColumn.equals(CommentProperty)){				
				iow.firePropertyChange("DocumentProperty", CommentProperty,  new String(rightHeaderColumn));
			} 
		} else{
			if (leftHeaderColumn.equals(ExpertProperty)){
				String expert = (String)properties.get(ExpertProperty);
				if (!expert.contains(rightHeaderColumn)) expert += "; " + rightHeaderColumn;
					iow.firePropertyChange("DocumentProperty", ExpertProperty,  new String(expert));
				
			} 
		}
		 
		
	}
	

	// looking for the table "protocol"
	searchIndex = allText.indexOf("title=\"protocol\"", 0);
	
	//limiting ourselves only to the Protocol table
	int tableProtocolEndIndex = allText.indexOf("</table", searchIndex);
	allText = allText.substring(0, tableProtocolEndIndex);
	
	// looking through columns of table "protocol" and retreiving text of the left and right columns
	while(searchIndex>0){
		
		searchIndex=allText.indexOf("<tr>", searchIndex);
		if (searchIndex>0) result = findTagContent(allText, colStartToken,colEndToken,searchIndex); 
			else break;
		if (result !=null) {leftColumn +=result;
							leftColumn +="<br/><br/>";//adding breaks because there are no breaks on row boundaries
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
	leftColumn = leftColumn.trim();
	
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
		iow.firePropertyChange("progress", null, new Integer(fileLoadProgress/2));
		// processing the left column's content
		while (leftColumn.indexOf("[", 0)>=0 || leftColumn.indexOf("]", 0)>=0){
			iow.firePropertyChange("progress", null, new Integer( 	fileLoadProgress+
										leftColumnParseProgress* posBeg/leftColumn.length()));
			int handle = -1;
			int a = 0 ;
			RawAData data = null;
			String handleNo = null;
			//if we met the opening tag
            if ((leftColumn.indexOf("[", 0) >=0) && (leftColumn.indexOf("[", 0) <= leftColumn.indexOf("]", 0) )){
            	posBeg = leftColumn.indexOf("[");
            	handleNo = findTagContent(leftColumn, "[", "|", 0);
				handle =Integer.parseInt(handleNo);
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

		iow.firePropertyChange("progress", null, new Integer(	
												  				fileLoadProgress+
																leftColumnParseProgress));
		
		posBeg = rightColumn.indexOf("{");
		posEnd = -1;
		
		// processing the right column's content
		while (posBeg>=0){
			iow.firePropertyChange("progress", null, new Integer(	
									fileLoadProgress+
									leftColumnParseProgress+
									rightColumnParseProgress*(posBeg/rightColumn.length())));
			
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
			iow.firePropertyChange("progress", null, new Integer(	
																	fileLoadProgress+
																	leftColumnParseProgress+
																	rightColumnParseProgress));
		
		// Обрабатываем стили в уже прочитанном тексте
		SimpleAttributeSet currentStyle = new SimpleAttributeSet(this.defaultStyle);
		Pattern styleTag = Pattern.compile("</?[bi]>");
		String sourceText = leftColumn;
		Matcher styleMatcher = styleTag.matcher(sourceText);
		int sourcePosition = 0;
		int sourceOffset = 0;
		int docPosition = appendOffset;
		Vector<StyledText> styledTextBlocks = new Vector<StyledText>();
		while (styleMatcher.find()) {
			String currentTag = styleMatcher.group();
			int tagLenth = currentTag.length();
			int tagStart = styleMatcher.start();
			int tagEnd = styleMatcher.end();
			String textBlock = sourceText.substring(sourcePosition, tagStart);
			
			// Добавляем в документ текст перед текущим тегом
			styledTextBlocks.add(new StyledText(textBlock, currentStyle));
			docPosition += textBlock.length();
			sourcePosition = tagEnd;
		
			// Так как мы удаляем теги из основного текста, необходимо сместить
			// пометки типировщика, находящиеся после тега
			for (RawAData rd : rawData.values()) {
				if (rd.beg >= tagEnd - sourceOffset) {
					rd.beg -= tagLenth;
				}
				if (rd.end >= tagEnd - sourceOffset) {
					rd.end -= tagLenth;
				}
			}
			sourceOffset += tagLenth;
			
			// Стиль следующего текста в зависимости от текущего тега
			if (currentTag.equals("<b>")) {
				StyleConstants.setBold(currentStyle, true);
			}
			else if (currentTag.equals("</b>")) {
				StyleConstants.setBold(currentStyle, false);
			}
			else if (currentTag.equals("<i>")) {
				StyleConstants.setItalic(currentStyle, true);
			}
			else if (currentTag.equals("</i>")) {
				StyleConstants.setItalic(currentStyle, false);
			}
		}
		// Добавляем в документ текст за последним тегом
		styledTextBlocks.add(new StyledText(sourceText.substring(sourcePosition),
				currentStyle));
		iow.firePropertyChange("AppendStyledText", null, styledTextBlocks);
		
		/// adding plain text to the document
			iow.firePropertyChange("RawData", null, rawData);
			iow.firePropertyChange("progress", null, new Integer(	
																	fileLoadProgress+
																	leftColumnParseProgress+
																	rightColumnParseProgress+
																	textAddProgress));		
		
		//creating and adding the segments AData info
	//	HashMap <ASection, AData> tempADataMap = new HashMap <ASection, AData>();
	

		
		//iow.firePropertyChange("AData", getADataMap(), tempADataMap);
	
		iow.firePropertyChange("progress", null, new Integer(100));	
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
		    dim.equals(AData.ODNOMERNOST)||
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

public int getProgress(){return progress;}

public void load(FileInputStream fis, ProgressWindow pw) throws Exception {
	IOWorker iow = new IOWorker(pw, this, fis);
	iow.setAppend(false);
	iow.execute();
	//while(!iow.isDone());
	if (iow.getException()!=null) throw new Exception(iow.getException());
}

public void append(FileInputStream fis, ProgressWindow pw) throws Exception {
	IOWorker iow = new IOWorker(pw, this, fis);
	iow.setAppend(true);
	iow.execute();
	//while(!iow.isDone())Thread.currentThread().yield();
	if (iow.getException()!=null) throw new Exception(iow.getException());
}

public void save(FileOutputStream fos, ProgressWindow pw, boolean append) {
	IOWorker iow = new IOWorker(pw, this, fos);
	iow.execute();
	//while(!iow.isDone())Thread.currentThread().yield();
	
}

public HashMap <ASection, AData> getADataMap(){return aDataMap;}

public ASection createASection(int beg, int end) throws BadLocationException{return new ASection(createPosition(beg), createPosition(end));}
																	  
} // class ADocument




