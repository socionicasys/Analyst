package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.*;
import javax.swing.undo.*;

public class ADocument extends DefaultStyledDocument implements DocumentListener {
	public static final String DEFAULT_TITLE = "Новый документ";
	// document's properties names
	public static final String TitleProperty1 = "Документ:";
	public static final String ExpertProperty = "Эксперт:";
	public static final String ClientProperty = "Типируемый:";
	public static final String DateProperty = "Дата:";
	public static final String CommentProperty = "Комментарий:";
	private static final long serialVersionUID = 4600082566231722109L;

	private Map<ASection, AData> aDataMap;
	private Collection<ADocumentChangeListener> listeners;
	public final SimpleAttributeSet defaultStyle;
	public final SimpleAttributeSet defaultSectionAttributes;
	public final SimpleAttributeSet defaultSearchHighlightAttributes;

	private CompoundEdit currentCompoundEdit;
	private String keyword;

	private static final Logger logger = LoggerFactory.getLogger(ADocument.class);

	ADocument() {
		super();

		addDocumentListener(this);

		//style of general text
		defaultStyle = new SimpleAttributeSet();
		defaultStyle.addAttribute(StyleConstants.FontSize, Integer.valueOf(16));
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
		if (aDataMap == null) {
			aDataMap = new HashMap<ASection, AData>();
		} else {
			aDataMap.clear();
		}
		try {
			this.replace(0, getLength(), "", defaultStyle);
		} catch (BadLocationException e) {
			logger.error("Invalid document replace() in initNew()", e);
		}

		putProperty(TitleProperty, DEFAULT_TITLE);
		putProperty(ExpertProperty, "");
		putProperty(ClientProperty, "");
		Date date = new Date();
		putProperty(DateProperty, date.toLocaleString());
		putProperty(CommentProperty, "");

		setCharacterAttributes(0, 1, defaultStyle, true);
		fireADocumentChanged();
	}

	/**
	 * Находит блок (ASection), который содержит заданную позицию. Если таких блоков несколько, выбирается тот,
	 * центральная часть которого лежит ближе всего к этой позиции. Среди блоков, центры которых лежат на одном
	 * расстоянии, выбирается блок максимальной вложенности.
	 * @param pos позиция в документе, для которой нужно найти блок
	 * @return блок, содержащий заданную позицию, или null, если такого нет
	 */
	public ASection getASection(final int pos) {
		Collection<ASection> results = new ArrayList<ASection>();
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

	public ASection getASectionThatStartsAt(int startOffset) {
		for (ASection section : aDataMap.keySet()) {
			if (section.getStartOffset() == startOffset) {
				return section;
			}
		}
		return null;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
		//if insert is on the section end - do not extend the section to the inserted text
		int offset = chng.getOffset();
		int length = chng.getLength();

		Iterator<ASection> sectionIterator = aDataMap.keySet().iterator();
		Map<ASection, AData> tempMap = null;
		while (sectionIterator.hasNext()) {
			ASection sect = sectionIterator.next();
			if (sect.getEndOffset() == offset + length) {
				if (tempMap == null) {
					tempMap = new HashMap<ASection, AData>();
				}
				int start = sect.getStartOffset();
				AData aData = aDataMap.get(sect);
				sectionIterator.remove();
				tempMap.put(new ASection(start, offset), aData);
			}
		}

		if (tempMap != null) {
			aDataMap.putAll(tempMap);
		}

		super.insertUpdate(chng, defaultStyle);
	}

	@Override
	protected void removeUpdate(DefaultDocumentEvent chng) {
		startCompoundEdit();

		int offset = chng.getOffset();
		removeCleanup(offset, offset + chng.getLength());
		super.removeUpdate(chng);

		fireADocumentChanged();
		endCompoundEdit(null);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}

	public void removeCleanup(int start, int end) {
		// проверяет не нужно ли удалить схлопнувшиеся сегменты
		boolean foundCollapsed = false;
		Collection<ASection> toRemove = new ArrayList<ASection>();
		for (ASection sect : aDataMap.keySet()) {
			if (sect.getStartOffset() > start && sect.getStartOffset() <= end &&
				sect.getEndOffset() > start && sect.getEndOffset() <= end) {
				toRemove.add(sect);
				foundCollapsed = true;
			}
		}

		for (ASection section : toRemove) {
			removeASection(section);
		}
		if (foundCollapsed) {
			fireADocumentChanged();
		}
	}

	public AData getAData(ASection section) {
		return aDataMap.get(section);
	}

	public void removeASection(ASection section) {
		if (section == null) {
			return;
		}
		AData data = aDataMap.get(section);
		aDataMap.remove(section);

		int startOffset = section.getStartOffset();
		int endOffset = section.getEndOffset();
		setCharacterAttributes(startOffset, endOffset - startOffset, defaultStyle, false);

		fireUndoableEditUpdate(new UndoableEditEvent(this, new ASectionDeletionEdit(section, data)));
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
		int startOffset = aSection.getStartOffset();
		int endOffset = aSection.getEndOffset();
		int begin = Math.min(startOffset, endOffset);
		int length = Math.abs(startOffset - endOffset);

		// удаляет сегменты с такими же границами
		Set<ASection> sectionSet = aDataMap.keySet();
		Iterator<ASection> it = sectionSet.iterator();
		while (it.hasNext()) {
			ASection section = it.next();
			if (section.getStartOffset() == begin && section.getEndOffset() == begin + length) {
				it.remove();
			}
		}

		setCharacterAttributes(begin, length, aSection.getAttributes(), false);
		aDataMap.put(aSection, data);

		fireUndoableEditUpdate(new UndoableEditEvent(this, new ASectionAdditionEdit(aSection, data)));
		fireADocumentChanged();
	}

	public void addADocumentChangeListener(ADocumentChangeListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<ADocumentChangeListener>();
		}
		listeners.add(listener);
	}

	public void removeADocumentChangeListener(ADocumentChangeListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	public void fireADocumentChanged() {
		if (listeners == null) {
			return;
		}
		for (ADocumentChangeListener listener : listeners) {
			listener.aDocumentChanged(this);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
	}

	public void loadDocument(File sourceFile, final ProgressWindow pw, boolean append) throws Exception {
		LegacyHtmlReader worker = new LegacyHtmlReader(pw, this, sourceFile, append);
		worker.getPropertyChangeSupport().addPropertyChangeListener("state", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				StateValue state = (StateValue) evt.getNewValue();
				if (state == StateValue.DONE) {
					pw.getAnalyst().initUndoManager();
				}
			}
		});
		worker.execute();
		if (worker.getException() != null) {
			throw worker.getException();
		}
	}

	public Map<ASection, AData> getADataMap() {
		return aDataMap;
	}

	public void startCompoundEdit() {
		if (currentCompoundEdit != null) {
			endCompoundEdit(null);
		}
		currentCompoundEdit = new CompoundEdit();
		keyword = null;
	}

	public void endCompoundEdit(String keyword) {
		if (currentCompoundEdit == null) {
			return;
		}
		if (keyword == null) {
			currentCompoundEdit.end();
			fireUndoableEditUpdate(new UndoableEditEvent(this, currentCompoundEdit));
			currentCompoundEdit = null;
		} else {
			this.keyword = keyword;
		}
	}

	@Override
	protected void fireUndoableEditUpdate(UndoableEditEvent e) {
		String s = e.getEdit().getPresentationName();
		if (keyword != null && s.contains(keyword)) {
			currentCompoundEdit.addEdit(e.getEdit());
			currentCompoundEdit.end();
			keyword = null;
		}

		if (currentCompoundEdit != null && currentCompoundEdit.isInProgress()) {
			currentCompoundEdit.addEdit(e.getEdit());
		} else {
			super.fireUndoableEditUpdate(e);
		}
	}

	//===================================================================
	private class ASectionAdditionEdit extends AbstractUndoableEdit {
		private final ASection section;
		private final AData data;

		public ASectionAdditionEdit(ASection section, AData data) {
			this.section = section;
			this.data = data;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			aDataMap.remove(section);
			fireADocumentChanged();
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			aDataMap.put(section, data);
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
	}

	//===================================================================
	private class ASectionDeletionEdit extends AbstractUndoableEdit {
		private final ASection section;
		private final AData data;

		public ASectionDeletionEdit(ASection section, AData data) {
			this.section = section;
			this.data = data;
		}
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			aDataMap.put(section, data);
			fireADocumentChanged();
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			aDataMap.remove(section);
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
	}

	//===================================================================
	private class ASectionChangeEdit extends AbstractUndoableEdit {
		private final ASection section;
		private final AData oldData;
		private AData newData;

		public ASectionChangeEdit(ASection section, AData oldData, AData newData) {
			this.section = section;
			this.oldData = oldData;
			this.newData = newData;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			aDataMap.remove(section);
			aDataMap.put(section, oldData);
			fireADocumentChanged();
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			aDataMap.remove(section);
			aDataMap.put(section, newData);
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
		public boolean addEdit(UndoableEdit anEdit) {
			if ((anEdit instanceof ASectionChangeEdit) && ((ASectionChangeEdit) anEdit).getSection().equals(section)) {
				newData = ((ASectionChangeEdit) anEdit).getNewData();
				return true;
			} else {
				return super.addEdit(anEdit);
			}
		}

		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			if ((anEdit instanceof ASectionChangeEdit) && ((ASectionChangeEdit) anEdit).getSection().equals(section)) {
				newData = ((ASectionChangeEdit) anEdit).getNewData();
				return true;
			} else {
				return super.replaceEdit(anEdit);
			}
		}

		public AData getNewData() {
			return newData;
		}

		public ASection getSection() {
			return section;
		}
	}

	public ADocumentFragment getADocFragment(int offset, int length) {
		int selectionEnd = offset + length;
		String text;
		Map<DocSection, AttributeSet> styleMap = new HashMap<DocSection, AttributeSet>();
		Map<DocSection, AData> docMap = new HashMap<DocSection, AData>();

		try {
			text = getText(offset, length);

			//putting styles to a HashMap
			int styleRunStart = offset;
			AttributeSet currentSet = null;
			for (int i = offset; i <= offset + length; i++) {
				Element element = getCharacterElement(i);
				AttributeSet attributeSet = element.getAttributes();
				if (currentSet == null) {
					currentSet = attributeSet;
				}
				if (!attributeSet.isEqual(currentSet) || i == selectionEnd) {
					styleMap.put(new DocSection(styleRunStart - offset, i - offset),
						new SimpleAttributeSet(currentSet));
					currentSet = attributeSet;
					styleRunStart = i;
				}
			}

			//putting AData to a HashMap
			if (aDataMap != null) {
				for (Entry<ASection, AData> dataEntry : aDataMap.entrySet()) {
					int secSt = dataEntry.getKey().getStartOffset();
					int secEnd = dataEntry.getKey().getEndOffset();

					if (secSt >= offset && secEnd <= selectionEnd) {
						docMap.put(new DocSection(secSt - offset, secEnd - offset), dataEntry.getValue());
					}
					if (secSt < offset && secEnd > selectionEnd) {
						docMap.put(new DocSection(0, length), dataEntry.getValue());
					}
					if (secSt < offset && secEnd < selectionEnd && secEnd > offset) {
						docMap.put(new DocSection(0, secEnd - offset), dataEntry.getValue());
					}
					if (secSt > offset && secSt < selectionEnd && secEnd > selectionEnd) {
						docMap.put(new DocSection(secSt - offset, length), dataEntry.getValue());
					}
				}
			}
		} catch (BadLocationException e) {
			logger.error("Error in getADocFragment()", e);
			return null;
		}

		return new ADocumentFragment(text, styleMap, docMap);
	}

	public void pasteADocFragment(int position, ADocumentFragment fragment) {
		// inserting plain text
		try {
			String text = fragment.getText();
			insertString(position, text, defaultStyle);
		} catch (BadLocationException e) {
			logger.error("Invalid document position {} for pasting text", position, e);
			return;
		}

		// inserting styles
		Map<DocSection, AttributeSet> styleMap = fragment.getStyleMap();
		for (Entry<DocSection, AttributeSet> entry : styleMap.entrySet()) {
			DocSection section = entry.getKey();
			AttributeSet style = entry.getValue();
			setCharacterAttributes(position + section.getStart(), section.getLength(), style, true);
		}

		// inserting AData
		Map<DocSection, AData> fragMap = fragment.getADataMap();
		for (Entry<DocSection, AData> entry : fragMap.entrySet()) {
			DocSection section = entry.getKey();
			AData data = entry.getValue();
			aDataMap.put(new ASection(position + section.getStart(), position + section.getEnd()), data);
		}
	}
}
