package ru.socionicasys.analyst;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;


public class ADocument extends DefaultStyledDocument implements DocumentListener {
	public static final String ENCODING = "UTF-8";
	public static final String DEFAULT_TITLE = "Новый документ";
	// document's properties names
	public static final String TitleProperty1 = "Документ:";
	public static final String ExpertProperty = "Эксперт:";
	public static final String ClientProperty = "Типируемый:";
	public static final String DateProperty = "Дата:";
	public static final String CommentProperty = "Комментарий:";


	protected HashMap<ASection, AData> aDataMap;
	protected Vector<ADocumentChangeListener> listeners;
	public SimpleAttributeSet defaultStyle;
	public SimpleAttributeSet defaultSectionAttributes;
	public SimpleAttributeSet defaultSearchHighlightAttributes;

	private int progress = 0;
	private CompoundEdit currentCompoundEdit = null;
	private boolean blockUndoEvents = false;
	//	private boolean blockremoveUpdate = false;
	private boolean blockRemoveUpdate;
	private String keyword;

	ADocument() {
		super();

		addDocumentListener(this);

		//style of general text
		defaultStyle = new SimpleAttributeSet();
		defaultStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
		defaultStyle.addAttribute(StyleConstants.Background, Color.white);
		//style of a section with mark-up
		defaultSectionAttributes = new SimpleAttributeSet();
		defaultSectionAttributes.addAttribute(StyleConstants.Background, Color.decode("#E0ffff"));
		defaultSearchHighlightAttributes = new SimpleAttributeSet();
		defaultSearchHighlightAttributes.addAttribute(StyleConstants.Background, Color.decode("#ff0000"));

		//init new Document
		initNew();
	}

	public void initNew() {
		if (aDataMap == null) aDataMap = new HashMap<ASection, AData>();
		else aDataMap.clear();
		try {
			this.replace(0, getLength(), "", defaultStyle);
		} catch (BadLocationException e) {
			System.out.println("Error in ADocument.initNew() :\n");
			e.printStackTrace();
		}

		putProperty((Object) TitleProperty, (Object) DEFAULT_TITLE);
		putProperty((Object) ExpertProperty, (Object) "");
		putProperty((Object) ClientProperty, (Object) "");
		Date date = new Date();
		putProperty((Object) DateProperty, (Object) date.toLocaleString());
		putProperty((Object) CommentProperty, "");

		setCharacterAttributes(0, 1, defaultStyle, true);
		fireADocumentChanged();
		AnalystWindow.initUndoManager();
	}

	/**
	 * Находит блок (ASection), который содержит заданную позицию. Если таких блоков несколько, выбирается тот,
	 * центральная часть которого лежит ближе всего к этой позиции. Среди блоков, центры которых лежат на одном
	 * расстоянии, выбирается блок максимальной вложенности.
	 * @param pos позиция в документе, для которой нужно найти блок
	 * @return блок, содержащий заданную позицию, или null, если такого нет
	 */
	public ASection getASection(final int pos) {
		ArrayList<ASection> results = new ArrayList<ASection>();
		for (ASection as : aDataMap.keySet()) {
			if (as.containsOffset(pos)) {
				results.add(as);
			}
		}
		if (results.isEmpty()) {
			return null;
		}

		return Collections.min(results, new Comparator<ASection>() {
			@Override
			public int compare(ASection o1, ASection o2) {
				int midDistance1 = Math.abs(pos - o1.getMiddleOffset());
				int midDistance2 = Math.abs(pos - o2.getMiddleOffset());
				if (midDistance1 != midDistance2) {
					return midDistance1 - midDistance2;
				}
				return -(o1.getStartOffset() - o2.getStartOffset());
			}
		});
	}

	public ASection getASectionThatStartsAt(int pos1) {
		Set<ASection> set = aDataMap.keySet();
//	Vector <ASection> results = new Vector <ASection>();
		ASection r = null;

		Iterator<ASection> it = set.iterator();
		while (it.hasNext()) {
			r = it.next();
			if (r.getStartOffset() == pos1) return r;
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
	public void changedUpdate(DocumentEvent e) {

	}//changedUpdate(); 	


	@Override
	protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet set) {

		//if insert is on the section end - do not extend the section to the inserted text

		int offset = chng.getOffset();
		int length = chng.getLength();

		Set<ASection> s = aDataMap.keySet();
		Iterator<ASection> it = s.iterator();
		int start = -1;
		AData aData = null;
		HashMap<ASection, AData> tempMap = null;

		while (it.hasNext()) {
			ASection sect = it.next();
			if (sect.getEndOffset() == offset + length) {
				if (tempMap == null) tempMap = new HashMap<ASection, AData>();
				start = sect.getStartOffset();
				aData = aDataMap.get(sect);
				it.remove();
				try {
					sect = new ASection(createPosition(start), createPosition(offset));
				} catch (BadLocationException e) {

					e.printStackTrace();
				}
				tempMap.put(sect, aData);
			}
		}

		if (tempMap != null) aDataMap.putAll(tempMap);

		set = defaultStyle;

		super.insertUpdate(chng, set);

//insertCleanup();

	}


	protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {

		if (blockRemoveUpdate) {

			return;
		}
		startCompoundEdit();

		//blockUndoEvents(true);
		int offset = chng.getOffset();
		int length = chng.getLength();

		//ADocumentFragment fragment = getADocFragment((ADocument)chng.getDocument(),offset, length);
		//UndoableEdit edit = new ADocDeleteEdit(offset, fragment);

/*	
	try {
		blockRemoveUpdate = true;
		this.remove(offset, length);
		blockRemoveUpdate = false;
	} catch (BadLocationException e) {
		e.printStackTrace();
	}
		
*/

		//blockUndoEvents(false);

		removeCleanup(offset, offset + length);
		super.removeUpdate(chng);

		//fireUndoableEditUpdate(new UndoableEditEvent(this, edit));
		//AnalystWindow.undo.addEdit((UndoableEdit) new UndoableEditEvent(this, edit));

		fireADocumentChanged();
		endCompoundEdit(null);

//	fireUndoableEditUpdate(new UndoableEditEvent(this, new ADocDeleteEdit(chng.getOffset(), getADocFragment(this, chng.getOffset(),chng.getLength(),false))));

	}

	@Override
	public void removeUpdate(DocumentEvent e) {

	} //removeUpdate()

	public void removeCleanup(int start, int end) {

		// проверяет не нужно ли удалить схлопнувшиеся сегменты
		Set<ASection> s = aDataMap.keySet();
		Iterator<ASection> it = s.iterator();
		boolean foundCollapsed = false;
		Vector<ASection> toRemove = new Vector<ASection>();
		while (it.hasNext()) {
			ASection sect = it.next();
			if (sect.getStartOffset() > start &&
				sect.getStartOffset() <= end &&
				sect.getEndOffset() > start &&
				sect.getEndOffset() <= end
				) { //it.remove();
				toRemove.add(sect);
				foundCollapsed = true;
			}
		}

		for (int i = 0; i < toRemove.size(); i++) {
			removeASection(toRemove.get(i));
		}
		if (foundCollapsed) fireADocumentChanged();
	}


	public void insertCleanup() {
/*	
	if (aDataMap == null ) return;
	// проверяет не нужно ли объединить пересекающиеся сегменты с одинаковыми данными
	Set<ASection> s = aDataMap.keySet();
	Iterator<ASection> it = s.iterator ();
	ASection[] sections = s.toArray(new ASection[]{});
	boolean foundCollapsed = false;
	while(it.hasNext()){
		ASection sect = it.next(); 
		AData dat = this.getAData(sect);
		for (int i = 0; i< sections.length; i++){
			ASection curSect = sections[i];
			AData curData = this.getAData(curSect);
			if (dat.equals(curData)&& sect!= curSect){
				if (sect.getStartOffset()>= curSect.getStartOffset()   &&
					sect.getStartOffset()<= curSect.getEndOffset()){
					(sections[i]).setEndOffset(Math.max(sect.getEndOffset(), curSect.getEndOffset()));
					it.remove();
					foundCollapsed = true;
				}	 
				if (sect.getEndOffset()  >= curSect.getStartOffset()   &&
					sect.getEndOffset()  <= curSect.getEndOffset()){ 
					(sections[i]).setStartOffset(Math.min(sect.getEndOffset(), curSect.getEndOffset()));
					it.remove();
					foundCollapsed = true;
				}
			}	
		}
	}
	if (foundCollapsed) fireADocumentChanged();
*/
	}


	public AData getAData(ASection section) {

		return aDataMap.get(section);
	}


	public void removeASection(ASection aSection) {
		if (aSection == null) return;
		AData data = aDataMap.get(aSection);
		aDataMap.remove(aSection);

		int st = aSection.getStartOffset();
		int en = aSection.getEndOffset();

		//startCompoundEdit();
		setCharacterAttributes(st, en - st, defaultStyle, false);
		fireUndoableEditUpdate(new UndoableEditEvent(this, new ASectionDeletionEdit(aSection, data)));
		//endCompoundEdit();
		fireADocumentChanged();
	}


	public void updateASection(ASection aSection, AData data) {
		AData oldData = aDataMap.get(aSection);
		aDataMap.remove(aSection);
		aDataMap.put(aSection, data);
		fireUndoableEditUpdate(new UndoableEditEvent(this, new ASectionChangeEdit(aSection, oldData, data)));

		fireADocumentChanged();
	}


	public void addASection(ASection aSection, AData data) {

		int st = aSection.getStartOffset();
		int en = aSection.getEndOffset();
		int beg = Math.min(st, en);
		int len = Math.abs(st - en);

		// удаляет сегменты с такими же границами
		Set<ASection> s = aDataMap.keySet();
		Iterator<ASection> it = s.iterator();

		while (it.hasNext()) {
			ASection sect = it.next();
			if (sect.getStartOffset() == beg && sect.getEndOffset() == beg + len) it.remove();
		}

		//startCompoundEdit();

		setCharacterAttributes(beg, len, aSection.getAttributes(), false);
		aDataMap.put(aSection, data);
		fireUndoableEditUpdate(new UndoableEditEvent(this, new ASectionAdditionEdit(aSection, data)));
		//endCompoundEdit();

		fireADocumentChanged();
	}

	public void addADocumentChangeListener(ADocumentChangeListener l) {
		if (listeners == null) listeners = new Vector<ADocumentChangeListener>();
		listeners.add(l);
	}

	public void fireADocumentChanged() {
		if (listeners == null) return;
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).aDocumentChanged(this);
		}
	}

	public void load(FileInputStream fis, boolean append, IOWorker iow) throws Exception {
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName(ENCODING));
		String leftColumn = "";
		String rightColumn = "";
		String allText = "";

		int fileLoadProgress = 20;
		int leftColumnParseProgress = 50;
		int rightColumnParseProgress = 25;
		int textAddProgress = 5;
		int aDataCteationProgress = 0;

		int appendOffset = 0;
		if (append) appendOffset = getLength();

		final class SectorData {
			int handle;
			int startPos;
			int endPos;
			String dataString;

			public SectorData(int handle, int startPos, int endPos) {
				this.handle = handle;
				this.startPos = startPos;
				this.endPos = endPos;
			}

			public SectorData(int handle, int startPos, int endPos, String dataString) {
				this.handle = handle;
				this.startPos = startPos;
				this.endPos = endPos;
				this.dataString = dataString;
			}

			public void setDataString(String dataString) {
				this.dataString = dataString;
			}

			public int getHandle() {
				return handle;
			}

			public int getstartPos() {
				return startPos;
			}

			public int getendPos() {
				return endPos;
			}
		}// class SectorData

		Vector<SectorData> sectorData = new Vector<SectorData>();
		boolean finished = false;
		// reading the file

		int offset = 0;
		int length = fis.available();
		char[] buf = new char[length];
		int bytesRead;

		iow.firePropertyChange("progress", null, new Integer(fileLoadProgress / 2));

		while (!finished) {

			bytesRead = isr.read(buf, 0, length);
			if (bytesRead > 0) allText += new String(buf, 0, bytesRead);
				//offset += bytesRead;
			else {
				finished = true;
				isr.close();
				fis.close();
			}
		}
		iow.firePropertyChange("progress", null, new Integer(fileLoadProgress));

		//  PARSING THE INPUT DATA
		finished = false;
		offset = 0;

		// looking for the table "header"
		int searchIndex = allText.indexOf("title=\"header\"", 0);

		String colStartToken = "<td>";
		String colEndToken = "</td>";
		String result = null;
		String headerResult = null, leftHeaderColumn = null, rightHeaderColumn = null;

		String leftHeaderText = allText.substring(searchIndex, allText.indexOf("</table", searchIndex));

		// looking through columns of table "header" and retreiving text of the left and right columns

		Dictionary<Object, Object> properties = getDocumentProperties();

		searchIndex = leftHeaderText.indexOf("<tr>", 0);
		while (searchIndex > 0) {

			searchIndex = leftHeaderText.indexOf("<tr>", searchIndex);
			if (searchIndex > 0) headerResult = findTagContent(leftHeaderText, colStartToken, colEndToken, searchIndex);
			else break;
			if (headerResult != null) {
				leftHeaderColumn = headerResult.trim();
				searchIndex = leftHeaderText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			if (searchIndex > 0) headerResult = findTagContent(leftHeaderText, colStartToken, colEndToken, searchIndex);
			else break;
			if (headerResult != null) {
				rightHeaderColumn = headerResult.trim();
				searchIndex = leftHeaderText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			//обработка заголовка
			leftHeaderColumn.replaceAll("\t", "");
			rightHeaderColumn.replaceAll("\t", "");
			rightHeaderColumn = rightHeaderColumn.replaceAll("<br/>", "\n");

			if (!append) {
				if (leftHeaderColumn.equals(TitleProperty1)) {
					iow.firePropertyChange("DocumentProperty", TitleProperty, new String(rightHeaderColumn));
				}
				if (leftHeaderColumn.equals(ExpertProperty)) {
					iow.firePropertyChange("DocumentProperty", ExpertProperty, new String(rightHeaderColumn));
				}
				if (leftHeaderColumn.equals(ClientProperty)) {
					iow.firePropertyChange("DocumentProperty", ClientProperty, new String(rightHeaderColumn));
				}
				if (leftHeaderColumn.equals(DateProperty)) {
					iow.firePropertyChange("DocumentProperty", DateProperty, new String(rightHeaderColumn));
				}
				if (leftHeaderColumn.equals(CommentProperty)) {
					iow.firePropertyChange("DocumentProperty", CommentProperty, new String(rightHeaderColumn));
				}
			} else {
				if (leftHeaderColumn.equals(ExpertProperty)) {
					String expert = (String) properties.get(ExpertProperty);
					if (!expert.contains(rightHeaderColumn)) expert += "; " + rightHeaderColumn;
					iow.firePropertyChange("DocumentProperty", ExpertProperty, new String(expert));
				}
			}
		}

		// looking for the table "protocol"
		searchIndex = allText.indexOf("title=\"protocol\"", 0);

		//limiting ourselves only to the Protocol table
		int tableProtocolEndIndex = allText.indexOf("</table", searchIndex);
		allText = allText.substring(0, tableProtocolEndIndex);

		// looking through columns of table "protocol" and retreiving text of the left and right columns
		while (searchIndex > 0) {

			searchIndex = allText.indexOf("<tr>", searchIndex);
			if (searchIndex > 0) result = findTagContent(allText, colStartToken, colEndToken, searchIndex);
			else break;
			if (result != null) {
				leftColumn += result;
				leftColumn += "<br/><br/>";//adding breaks because there are no breaks on row boundaries
				searchIndex = allText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			if (searchIndex > 0) result = findTagContent(allText, colStartToken, colEndToken, searchIndex);
			else break;
			if (result != null) {
				rightColumn += result;
				//rightColumn +="<br/>"; //there are breaks on row boundaries for the right column
				searchIndex = allText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}
		}

		//remove all tabs
		//leftColumn = leftColumn.replaceAll("\t", "");
		leftColumn = leftColumn.replaceAll("\n", "");
		leftColumn = leftColumn.replace("<br/>", "\n");
		leftColumn = leftColumn.trim();

		rightColumn = rightColumn.replaceAll("\n", "");
		//rightColumn= rightColumn.replaceAll("\t", "");
		rightColumn = rightColumn.replace("<br/>", "\n");

		// Убираем все лишние теги

		leftColumn = removeTag(leftColumn, "<span", ">");
		leftColumn = removeTag(leftColumn, "</span", ">");
		leftColumn = removeTag(leftColumn, "<small", ">");
		leftColumn = removeTag(leftColumn, "</small", ">");

		Hashtable<Integer, RawAData> rawData = new Hashtable<Integer, RawAData>();

		int posBeg = leftColumn.indexOf("[");
		int posEnd = -1;
		iow.firePropertyChange("progress", null, new Integer(fileLoadProgress / 2));
		// processing the left column's content
		while (leftColumn.indexOf("[", 0) >= 0 || leftColumn.indexOf("]", 0) >= 0) {
			iow.firePropertyChange("progress", null, new Integer(fileLoadProgress +
				leftColumnParseProgress * posBeg / leftColumn.length()));
			int handle = -1;
			int a = 0;
			RawAData data;
			String handleNo = null;
			//if we met the opening tag
			if ((leftColumn.indexOf("[", 0) >= 0) && (leftColumn.indexOf("[", 0) <= leftColumn.indexOf("]", 0))) {
				posBeg = leftColumn.indexOf("[");
				handleNo = findTagContent(leftColumn, "[", "|", 0);
				handle = Integer.parseInt(handleNo);
				leftColumn = leftColumn.replace(findTag(leftColumn, "[", "|", 0), "");
				data = new RawAData();
				data.setBegin(posBeg);
				rawData.put(new Integer(handle), data);
				//if we met the closing tag
			} else if (leftColumn.indexOf("]", 0) >= 0) {
				posEnd = leftColumn.indexOf("|");
				handleNo = findTagContent(leftColumn, "|", "]", 0);
				handle = Integer.parseInt(handleNo);
				leftColumn = leftColumn.replace(findTag(leftColumn, "|", "]", 0), "");
				data = rawData.get(new Integer(handle));
				if (data != null) data.setEnd(posEnd);
			}
		}

		iow.firePropertyChange("progress", null, new Integer(
			fileLoadProgress +
				leftColumnParseProgress));

		posBeg = rightColumn.indexOf("{");
		posEnd = -1;

		// processing the right column's content
		while (posBeg >= 0) {
			iow.firePropertyChange("progress", null, new Integer(
				fileLoadProgress +
					leftColumnParseProgress +
					rightColumnParseProgress * (posBeg / rightColumn.length())));

			String handleNo = findTagContent(rightColumn, "{", ":", posBeg);
			int handle = Integer.parseInt(handleNo);
			RawAData data = rawData.get(new Integer(handle));
			if (data != null) {
				String aDataString = findTagContent(rightColumn, ":", "}", posBeg);
				data.setAData(aDataString);
				posEnd = rightColumn.indexOf("{", posBeg + 1);
				if (posEnd < 0) posEnd = rightColumn.length() - 1;
				int posBeg1 = rightColumn.indexOf("}", posBeg) + 1;
				String com = null;
				if (posBeg1 > 0) com = new String(rightColumn.substring(posBeg1, posEnd));

				if (com != null) com = com.trim();
				com = " " + com;
				//removing last line brake which was added when saving
				while (com != null && (com.lastIndexOf("\n") == (com.length() - 1)))
					com = com.substring(0, com.length() - 1);
/*				
				while (com != null  &&  com.startsWith(" "))
						 	com  = com.substring(1, com.length()-1);
*/
				if (com == null) com = "";
				data.setComment(com);
			}

			posBeg = rightColumn.indexOf("{", posBeg + 1);
		}
		iow.firePropertyChange("progress", null, new Integer(
			fileLoadProgress +
				leftColumnParseProgress +
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
				if (rd.getBegin() >= tagEnd - sourceOffset) {
					rd.setBegin(rd.getBegin() - tagLenth);
				}
				if (rd.getEnd() >= tagEnd - sourceOffset) {
					rd.setEnd(rd.getEnd() - tagLenth);
				}
			}
			sourceOffset += tagLenth;

			// Стиль следующего текста в зависимости от текущего тега
			if (currentTag.equals("<b>")) {
				StyleConstants.setBold(currentStyle, true);
			} else if (currentTag.equals("</b>")) {
				StyleConstants.setBold(currentStyle, false);
			} else if (currentTag.equals("<i>")) {
				StyleConstants.setItalic(currentStyle, true);
			} else if (currentTag.equals("</i>")) {
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
			fileLoadProgress +
				leftColumnParseProgress +
				rightColumnParseProgress +
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
		while (tag != null) {
			source = source.replace((CharSequence) tag, (CharSequence) "");
			tag = this.findTag(source, startToken, endToken, 0);
		}
		return source;
	}

	private String findTagContent(String text, String startToken, String endToken,
								  int fromIndex) {

		int startIndex = text.indexOf(startToken, fromIndex);
		int endIndex = text.indexOf(endToken, startIndex);

		if (startIndex >= 0 && endIndex > 0 && endIndex > startIndex)
			return text.substring(startIndex + startToken.length(), endIndex);
		return null;
	}

	private String findTag(String text, String startToken, String endToken,
						   int fromIndex) {

		int startIndex = text.indexOf(startToken, fromIndex);
		int endIndex = text.indexOf(endToken, startIndex);

		if (startIndex >= 0 && endIndex > 0 && endIndex > startIndex)
			return text.substring(startIndex, endIndex + endToken.length());
		return null;
	}

	public String getHTMLStyleForAData(AData data) {

		if (data.getAspect().equals(AData.DOUBT)) {
			return "background-color:#EAEAEA";
		}
		String res = "\"";
		String dim = data.getDimension();
		String mv = data.getMV();
		String sign = data.getSign();
		if (dim != null &&
			(dim.equals(AData.D1) ||
				dim.equals(AData.D2) ||
				dim.equals(AData.ODNOMERNOST) ||
				dim.equals(AData.MALOMERNOST))) {
			res += "background-color:#AAEEEE;";
		} else if (dim != null &&
			(dim.equals(AData.D3) ||
				dim.equals(AData.D4) ||
				dim.equals(AData.MNOGOMERNOST))) {
			// противный зеленый
			res += "background-color:#AAEEAA;";
		}
		if (sign != null) {
			res += "color:#FF0000;";
		}
		if (mv != null) {
			res += "background-color:#FFFFCC;";
		}
		//Если не задан другой стиль, то будет этот стиль
		if (res.equals("\"")) res += "text-decoration:underline";

		res += "\"";
		return res;
	}//getStyleForAData()

	@Override
	public void insertUpdate(DocumentEvent e) {

	}

	public int getProgress() {
		return progress;
	}

	public void load(FileInputStream fis, ProgressWindow pw) throws Exception {
		IOWorker iow = new IOWorker(pw, this, fis);
		iow.setAppend(false);
		iow.execute();
		//while(!iow.isDone());
		if (iow.getException() != null) throw new Exception(iow.getException());
	}

	public void append(FileInputStream fis, ProgressWindow pw) throws Exception {
		IOWorker iow = new IOWorker(pw, this, fis);
		iow.setAppend(true);
		iow.execute();
		//while(!iow.isDone())Thread.currentThread().yield();
		if (iow.getException() != null) throw new Exception(iow.getException());
	}

	public void save(FileOutputStream fos, ProgressWindow pw, boolean append) {
		IOWorker iow = new IOWorker(pw, this, fos);
		iow.execute();
		//while(!iow.isDone())Thread.currentThread().yield();

	}

	public HashMap<ASection, AData> getADataMap() {
		return aDataMap;
	}

	public ASection createASection(int beg, int end) throws BadLocationException {
		return new ASection(createPosition(beg), createPosition(end));
	}

	public void startCompoundEdit() {
		if (currentCompoundEdit != null) endCompoundEdit(null);
		currentCompoundEdit = new CompoundEdit();
		keyword = null;
	}

	public void endCompoundEdit(String s) {

		if (currentCompoundEdit == null) return;
/*	if (!currentCompoundEdit.canUndo() && !currentCompoundEdit.canRedo()) {
		currentCompoundEdit =null;
		return;
	}
*/
		if (s == null) {
			currentCompoundEdit.end();
			fireUndoableEditUpdate(new UndoableEditEvent(this, (UndoableEdit) currentCompoundEdit));
			currentCompoundEdit = null;
		} else keyword = s;
	}


	//@override
	protected void fireUndoableEditUpdate(UndoableEditEvent e) {
		String s = e.getEdit().getPresentationName();
		if (keyword != null && s.contains(keyword)) {
			currentCompoundEdit.addEdit(e.getEdit());
			currentCompoundEdit.end();
			keyword = null;
			//fireUndoableEditUpdate(new UndoableEditEvent(this, (UndoableEdit)currentCompoundEdit));
		}

		if (currentCompoundEdit != null && currentCompoundEdit.isInProgress()) {
			currentCompoundEdit.addEdit(e.getEdit());
		} else super.fireUndoableEditUpdate(e);
	}


	//===================================================================
	private class ASectionAdditionEdit extends AbstractUndoableEdit {

		boolean canUndo, canRedo;
		ASection section;
		AData data;

		public ASectionAdditionEdit(ASection section, AData data) {
			this.section = section;
			this.data = data;
			canUndo = true;
			canRedo = false;
		}

		public boolean canUndo() {
			return canUndo;
		}

		public boolean canRedo() {
			return canRedo;
		}

		public void undo() throws CannotUndoException {
			if (!canUndo) throw new CannotUndoException();

			//blockUndoEvents(true);
			/* setCharacterAttributes(	section.getStartOffset(),
								 Math.abs(section.getEndOffset()-section.getStartOffset()),
								 defaultStyle, false);
		 */
			//blockUndoEvents(false);
			aDataMap.remove(section);
			canUndo = false;
			canRedo = true;
			fireADocumentChanged();
		}

		public void redo() throws CannotRedoException {
			if (!canRedo) throw new CannotRedoException();
			//blockUndoEvents(true);
			/*setCharacterAttributes(	section.getStartOffset(),
								 section.getEndOffset()-section.getStartOffset(),
								 section.getAttributes(), false);
		 */
			//blockUndoEvents(false);
			aDataMap.put(section, data);
			canUndo = true;
			canRedo = false;
			fireADocumentChanged();
		}

		public String getUndoPresentationName() {
			return "Отменить вставку сегмента анализа";
		}

		public String getPresentationName() {
			return "Вставка сегмента анализа";
		}

		public String getRedoPresentationName() {
			return "Вернуть вставку сегмента анализа";
		}

		public boolean isSignificant() {
			return true;
		}

		public boolean addEdit(UndoableEdit e) {
			return false;
		}

		public boolean replaceEdit(UndoableEdit e) {
			return false;
		}
	} // end class ASectionAdditionEdit


	//===================================================================
	private class ASectionDeletionEdit extends AbstractUndoableEdit {

		boolean canUndo, canRedo;
		ASection section;
		AData data;


		public ASectionDeletionEdit(ASection section, AData data) {
			this.section = section;
			this.data = data;

			canUndo = true;
			canRedo = false;
		}

		public boolean canUndo() {
			return canUndo;
		}

		public boolean canRedo() {
			return canRedo;
		}

		public void undo() throws CannotUndoException {
			if (!canUndo) throw new CannotUndoException();
			//blockUndoEvents(true);
			aDataMap.put(section, data);
			/*
		 setCharacterAttributes(	section.getStartOffset(),
				 section.getEndOffset()-section.getStartOffset(),
				 section.getAttributes(), false);
		 */
			//blockUndoEvents(false);

			canUndo = false;
			canRedo = true;
			fireADocumentChanged();
		}

		public void redo() throws CannotRedoException {
			if (!canRedo) throw new CannotRedoException();
			//blockUndoEvents(true);
			aDataMap.remove(section);
			/*
		 setCharacterAttributes(	section.getStartOffset(),
				 section.getEndOffset()-section.getStartOffset(),
				 defaultStyle, false);
		 */
			//blockUndoEvents(false);

			canUndo = true;
			canRedo = false;
			fireADocumentChanged();
		}

		public String getUndoPresentationName() {
			return "Отменить очистку сегмента анализа";
		}

		public String getPresentationName() {
			return "Очистка сегмента анализа";
		}

		public String getRedoPresentationName() {
			return "Вернуть очитску сегмента анализа";
		}

		public boolean isSignificant() {
			return true;
		}

		public boolean addEdit(UndoableEdit e) {
			return false;
		}

		public boolean replaceEdit(UndoableEdit e) {
			return false;
		}
	} // end class ASectionDeletionEdit


	//===================================================================
	private class ASectionChangeEdit extends AbstractUndoableEdit {

		boolean canUndo, canRedo;
		ASection section;
		AData oldData, newData;


		public ASectionChangeEdit(ASection section, AData oldData, AData newData) {
			this.section = section;
			this.oldData = oldData;
			this.newData = newData;

			canUndo = true;
			canRedo = false;
		}

		public boolean canUndo() {
			return canUndo;
		}

		public boolean canRedo() {
			return canRedo;
		}

		public void undo() throws CannotUndoException {
			if (!canUndo) throw new CannotUndoException();

			aDataMap.remove(section);
			aDataMap.put(section, oldData);

			canUndo = false;
			canRedo = true;
			fireADocumentChanged();
		}

		public void redo() throws CannotRedoException {
			if (!canRedo) throw new CannotRedoException();

			aDataMap.remove(section);
			aDataMap.put(section, newData);

			canUndo = true;
			canRedo = false;
			fireADocumentChanged();
		}

		public String getUndoPresentationName() {
			return "Отменить редактирование сегмента анализа";
		}

		public String getPresentationName() {
			return "Редактирование сегмента анализа";
		}

		public String getRedoPresentationName() {
			return "Вернуть редактирование сегмента анализа";
		}

		public boolean isSignificant() {
			return true;
		}

		public boolean addEdit(UndoableEdit e) {
			if ((e instanceof ASectionChangeEdit) && ((ASectionChangeEdit) e).getSection().equals(section)) {
				newData = ((ASectionChangeEdit) e).getNewData();
				return true;
			} else
				return false;
		}

		public boolean replaceEdit(UndoableEdit e) {
			if ((e instanceof ASectionChangeEdit) && ((ASectionChangeEdit) e).getSection().equals(section)) {
				newData = ((ASectionChangeEdit) e).getNewData();
				return true;
			} else
				return false;
		}

		public AData getNewData() {
			return newData;
		}

		public ASection getSection() {
			return section;
		}
	} // end class ASectionChangeEdit

	//===================================================================
	private class ADocDeleteEdit extends AbstractUndoableEdit {

		int position;
		ADocumentFragment fragment;
		boolean canUndo;
		boolean canRedo;
		private boolean blockRemoveEvents;

		public ADocDeleteEdit(int position, ADocumentFragment fragment) {
			this.position = position;
			this.fragment = fragment;
			canUndo = true;
			canRedo = false;
		}

		public boolean canUndo() {
			return canUndo;
		}

		public boolean canRedo() {
			return canRedo;
		}

		public void undo() throws CannotUndoException {
			if (!canUndo) throw new CannotUndoException();
			blockUndoEvents(true);
			pasteADocFragment(ADocument.this, position, fragment);

			fireADocumentChanged();
			canUndo = false;
			canRedo = true;
			blockUndoEvents(false);
		}

		public void redo() throws CannotRedoException {
			if (!canRedo) throw new CannotRedoException();
			blockUndoEvents(true);
			try {
				blockRemoveUpdate = true;
				ADocument.this.remove(position, fragment.getText().length());
				removeCleanup(position, position + fragment.getText().length());
				blockRemoveUpdate = false;
			} catch (BadLocationException e) {

				e.printStackTrace();
			}
			fireADocumentChanged();
			canUndo = true;
			canRedo = false;
			blockUndoEvents(false);
		}

		public String getUndoPresentationName() {
			return "Отменить удаление";
		}

		public String getPresentationName() {
			return "Удаление";
		}

		public String getRedoPresentationName() {
			return "Вернуть удаление";
		}

		public boolean isSignificant() {
			return true;
		}

		public boolean addEdit(UndoableEdit e) {
			return false;
		}

		public boolean replaceEdit(UndoableEdit e) {
			return false;
		}
	} // end class ADocDeleteEdit

/////////////////////////////////////////////////////////////////////////////////////////

	//===================================================================
	private class ADocFragmentPasteEdit extends AbstractUndoableEdit {

		int position;
		ADocument aDoc;
		ADocumentFragment fragment;
		boolean canUndo;
		boolean canRedo;
		private boolean blockRemoveEvents;

		public ADocFragmentPasteEdit(int position, ADocument aDoc, ADocumentFragment fragment) {
			this.position = position;
			this.aDoc = aDoc;
			this.fragment = fragment;
			canUndo = true;
			canRedo = false;
		}

		public boolean canUndo() {
			return canUndo;
		}

		public boolean canRedo() {
			return canRedo;
		}

		public void undo() throws CannotUndoException {
			if (!canUndo) throw new CannotUndoException();
			blockUndoEvents(true);

			try {
				blockRemoveUpdate = true;
				aDoc.remove(position, fragment.getText().length());
				removeCleanup(position, fragment.getText().length());
				blockRemoveUpdate = false;
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			fireADocumentChanged();
			canUndo = false;
			canRedo = true;
			blockUndoEvents(false);
		}

		public void redo() throws CannotRedoException {
			if (!canRedo) throw new CannotRedoException();
			blockUndoEvents(true);
			pasteADocFragment(ADocument.this, position, fragment);

			fireADocumentChanged();
			canUndo = true;
			canRedo = false;
			blockUndoEvents(false);
		}

		public String getUndoPresentationName() {
			return "Отменить вставку";
		}

		public String getPresentationName() {
			return "Вставка";
		}

		public String getRedoPresentationName() {
			return "Вернуть вставку";
		}

		public boolean isSignificant() {
			return true;
		}

		public boolean addEdit(UndoableEdit e) {
			return false;
		}

		public boolean replaceEdit(UndoableEdit e) {
			return false;
		}
	} // end class ADocFragmentPasteEdit

/////////////////////////////////////////////////////////////////////////////////////////


	public void blockUndoEvents(boolean block) {
		blockUndoEvents = block;
	}

	public static ADocumentFragment getADocFragment(ADocument aDoc, int offset, int length) {

		int selectionEnd = offset + length;
		String text = null;
		HashMap<DocSection, AttributeSet> styleMap = new HashMap<DocSection, AttributeSet>();
		HashMap<DocSection, AData> docMap = null;

		try {
			text = aDoc.getText(offset, length);

			//putting styles to a HashMap
			Element e = null;
			AttributeSet as = null;
			int styleRunStart = offset;
			AttributeSet currentSet = null;
			for (int i = offset; i <= offset + length; i++) {
				e = aDoc.getCharacterElement(i);
				as = e.getAttributes();
				if (currentSet == null) currentSet = as;
				if (!as.isEqual(currentSet) || i == (selectionEnd)) {

					styleMap.put(new DocSection(styleRunStart - offset, i - offset), new SimpleAttributeSet(currentSet));
					currentSet = as;
					styleRunStart = i;
				}
/*				if (i == (selectionEnd)){
					styleMap.put(new DocSection(styleRunStart-selectionStart, i-selectionStart), new SimpleAttributeSet(currentSet));
				} */
			}

			//putting AData to a HashMap
			HashMap<ASection, AData> aDataMap = aDoc.getADataMap();

			if (aDataMap != null) {
				docMap = new HashMap<DocSection, AData>();
				Set<ASection> keys = aDataMap.keySet();
				Iterator<ASection> it = keys.iterator();
				ASection section = null;
				int secSt;
				int secEnd;
				while (it.hasNext()) {
					section = it.next();
					secSt = section.getStartOffset();
					secEnd = section.getEndOffset();

					if (secSt >= offset && secEnd <= selectionEnd) {
						docMap.put(new DocSection(secSt - offset, secEnd - offset),
							aDataMap.get(section));
					}

					if (secSt < offset && secEnd > selectionEnd) {
						docMap.put(new DocSection(0, length),
							aDataMap.get(section));
					}
					if (secSt < offset && secEnd < selectionEnd && secEnd > offset) {
						docMap.put(new DocSection(0, secEnd - offset),
							aDataMap.get(section));
					}
					if (secSt > offset && secSt < selectionEnd && secEnd > selectionEnd) {
						docMap.put(new DocSection(secSt - offset, length),
							aDataMap.get(section));
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		return new ADocumentFragment(text, styleMap, docMap);
	}

	public static void pasteADocFragment(ADocument aDoc, int position, ADocumentFragment fragment) {
		String text = fragment.getText();
		HashMap<DocSection, AttributeSet> styleMap = fragment.getStyleMap();
		HashMap<DocSection, AData> fragMap = fragment.getaDataMap();

		try {
			// inserting plain text
			//aDoc.blockUndoEvents(true);
			if (text != null) aDoc.insertString(position, text, aDoc.defaultStyle);

			//  inserting styles
			if (styleMap != null) {
				Set<DocSection> keys = styleMap.keySet();
				Iterator<DocSection> it = keys.iterator();
				DocSection section = null;

				while (it.hasNext()) {
					section = it.next();
					aDoc.setCharacterAttributes(position + section.getStart(),
						section.getLength(),
						styleMap.get(section),
						true);
				}
			}

			//  inserting AData

			if (fragMap != null) {
				HashMap<ASection, AData> aDataMap = aDoc.getADataMap();

				Set<DocSection> keys = fragMap.keySet();
				Iterator<DocSection> it = keys.iterator();
				DocSection section = null;

				while (it.hasNext()) {
					section = it.next();
					aDataMap.put(new ASection(aDoc.createPosition(position + section.getStart()), aDoc.createPosition(position + section.getEnd())),
						fragMap.get(section));
				}
			}
			//aDoc.blockUndoEvents(false);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public boolean containsAnyASection(int min, int max) {

		Set<ASection> s = aDataMap.keySet();
		Iterator<ASection> it = s.iterator();
		boolean found = false;

		while (it.hasNext()) {
			ASection sect = it.next();
			if ((sect.getStartOffset() > min &&
				sect.getStartOffset() <= max) ||
				(sect.getEndOffset() > min &&
					sect.getEndOffset() <= max)
				) {
				found = true;
			}
		}
		return found;
	}
} // class ADocument




