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

	@Override
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

	@Override
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


	@Override
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

		@Override
		public boolean canUndo() {
			return canUndo;
		}

		@Override
		public boolean canRedo() {
			return canRedo;
		}

		@Override
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

		@Override
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

		@Override
		public String getUndoPresentationName() {
			return "Отменить вставку сегмента анализа";
		}

		@Override
		public String getPresentationName() {
			return "Вставка сегмента анализа";
		}

		@Override
		public String getRedoPresentationName() {
			return "Вернуть вставку сегмента анализа";
		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public boolean addEdit(UndoableEdit e) {
			return false;
		}

		@Override
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

		@Override
		public boolean canUndo() {
			return canUndo;
		}

		@Override
		public boolean canRedo() {
			return canRedo;
		}

		@Override
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

		@Override
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

		@Override
		public String getUndoPresentationName() {
			return "Отменить очистку сегмента анализа";
		}

		@Override
		public String getPresentationName() {
			return "Очистка сегмента анализа";
		}

		@Override
		public String getRedoPresentationName() {
			return "Вернуть очитску сегмента анализа";
		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public boolean addEdit(UndoableEdit e) {
			return false;
		}

		@Override
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

		@Override
		public boolean canUndo() {
			return canUndo;
		}

		@Override
		public boolean canRedo() {
			return canRedo;
		}

		@Override
		public void undo() throws CannotUndoException {
			if (!canUndo) throw new CannotUndoException();

			aDataMap.remove(section);
			aDataMap.put(section, oldData);

			canUndo = false;
			canRedo = true;
			fireADocumentChanged();
		}

		@Override
		public void redo() throws CannotRedoException {
			if (!canRedo) throw new CannotRedoException();

			aDataMap.remove(section);
			aDataMap.put(section, newData);

			canUndo = true;
			canRedo = false;
			fireADocumentChanged();
		}

		@Override
		public String getUndoPresentationName() {
			return "Отменить редактирование сегмента анализа";
		}

		@Override
		public String getPresentationName() {
			return "Редактирование сегмента анализа";
		}

		@Override
		public String getRedoPresentationName() {
			return "Вернуть редактирование сегмента анализа";
		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public boolean addEdit(UndoableEdit e) {
			if ((e instanceof ASectionChangeEdit) && ((ASectionChangeEdit) e).getSection().equals(section)) {
				newData = ((ASectionChangeEdit) e).getNewData();
				return true;
			} else
				return false;
		}

		@Override
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

		@Override
		public boolean canUndo() {
			return canUndo;
		}

		@Override
		public boolean canRedo() {
			return canRedo;
		}

		@Override
		public void undo() throws CannotUndoException {
			if (!canUndo) throw new CannotUndoException();
			blockUndoEvents(true);
			pasteADocFragment(ADocument.this, position, fragment);

			fireADocumentChanged();
			canUndo = false;
			canRedo = true;
			blockUndoEvents(false);
		}

		@Override
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

		@Override
		public String getUndoPresentationName() {
			return "Отменить удаление";
		}

		@Override
		public String getPresentationName() {
			return "Удаление";
		}

		@Override
		public String getRedoPresentationName() {
			return "Вернуть удаление";
		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public boolean addEdit(UndoableEdit e) {
			return false;
		}

		@Override
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

		@Override
		public boolean canUndo() {
			return canUndo;
		}

		@Override
		public boolean canRedo() {
			return canRedo;
		}

		@Override
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

		@Override
		public void redo() throws CannotRedoException {
			if (!canRedo) throw new CannotRedoException();
			blockUndoEvents(true);
			pasteADocFragment(ADocument.this, position, fragment);

			fireADocumentChanged();
			canUndo = true;
			canRedo = false;
			blockUndoEvents(false);
		}

		@Override
		public String getUndoPresentationName() {
			return "Отменить вставку";
		}

		@Override
		public String getPresentationName() {
			return "Вставка";
		}

		@Override
		public String getRedoPresentationName() {
			return "Вернуть вставку";
		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public boolean addEdit(UndoableEdit e) {
			return false;
		}

		@Override
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




