package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
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

	/**
	 * Информация о соответствиях/несоответствиях ТИМам
	 */
	private final MatchMissModel matchMissModel;

	private static final Logger logger = LoggerFactory.getLogger(ADocument.class);

	/**
	 * Сравнивает две ASection исходя из их близости к определенному положению в документе.
	 */
	private static final class SectionDistanceComparator implements Comparator<ASection> {
		private final int targetPosition;

		/**
		 * @param targetPosition позиция в документе. Секции, центры которых близки к этой позиции
		 * будут выше при сортировке.
		 */
		private SectionDistanceComparator(int targetPosition) {
			this.targetPosition = targetPosition;
		}

		@Override
		public int compare(ASection o1, ASection o2) {
			int midDistance1 = Math.abs(targetPosition - o1.getMiddleOffset());
			int midDistance2 = Math.abs(targetPosition - o2.getMiddleOffset());
			if (midDistance1 != midDistance2) {
				return midDistance1 - midDistance2;
			}
			return -(o1.getStartOffset() - o2.getStartOffset());
		}
	}

	ADocument() {
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

		matchMissModel = new MatchMissModel();
		addADocumentChangeListener(matchMissModel);

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
			replace(0, getLength(), "", defaultStyle);
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
	public ASection getASection(int pos) {
		Collection<ASection> results = new ArrayList<ASection>();
		for (ASection as : aDataMap.keySet()) {
			if (as.containsOffset(pos)) {
				results.add(as);
			}
		}
		if (results.isEmpty()) {
			return null;
		}

		return Collections.min(results, new SectionDistanceComparator(pos));
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

	/**
	 * Добавляет содержимое из документа anotherDocument в конец данного документа.
	 * @param anotherDocument документ, содержимое которого нужно добавить.
	 */
	public void appendDocument(ADocument anotherDocument) {
		List<ElementSpec> specs = new ArrayList<ElementSpec>();
		ElementSpec spec = new ElementSpec(new SimpleAttributeSet(), ElementSpec.StartTagType);
		specs.add(spec);
		visitElements(anotherDocument.getDefaultRootElement(), specs, false);
		spec = new ElementSpec(new SimpleAttributeSet(), ElementSpec.EndTagType);
		specs.add(spec);

		ElementSpec[] arr = new ElementSpec[specs.size()];
		specs.toArray(arr);
		int documentLength = getLength();
		try {
			insert(documentLength, arr);
		} catch (BadLocationException e) {
			logger.error("Error while appending to document");
		}

		for (Entry<ASection, AData> entry : anotherDocument.aDataMap.entrySet()) {
			ASection sourceSection = entry.getKey();
			ASection destinationSection = new ASection(sourceSection.getStartOffset() + documentLength,
					sourceSection.getEndOffset() + documentLength);
			aDataMap.put(destinationSection, entry.getValue());
		}

		StringBuilder builder = new StringBuilder(getProperty(ExpertProperty).toString());
		builder.append("; ");
		builder.append(anotherDocument.getProperty(ExpertProperty));
		getDocumentProperties().put(ExpertProperty, builder.toString());

		fireADocumentChanged();
	}

	/**
	 * Проходится по элементу и всем его дочерним элементам, собирая всю информацию в список ElementSpec-ов.
	 * @param element элемент, с которого начинается обход
	 * @param specs список, в который будут добавлены описания элементов
	 * @param includeRoot добавлять ли в описание теги открытия/закрытия начального элемента
	 */
	private static void visitElements(Element element, List<ElementSpec> specs, boolean includeRoot) {
		if (element.isLeaf()) {
			try {
				String elementText = element.getDocument().getText(element.getStartOffset(),
						element.getEndOffset() - element.getStartOffset());
				specs.add(new ElementSpec(element.getAttributes(), ElementSpec.ContentType,
						elementText.toCharArray(), 0, elementText.length()));
			} catch (BadLocationException e) {
				logger.error("Error while traversing document");
			}
		}
		else {
			if (includeRoot) {
				specs.add(new ElementSpec(element.getAttributes(), ElementSpec.StartTagType));
			}
			for (int i = 0; i < element.getElementCount(); i++) {
				visitElements(element.getElement(i), specs, true);
			}

			if (includeRoot) {
				specs.add(new ElementSpec(element.getAttributes(), ElementSpec.EndTagType));
			}
		}
	}

	/**
	 * @return объект, описывающий (не)соответствия всем ТИМамы
	 */
	public MatchMissModel getMatchMissModel() {
		return matchMissModel;
	}
}
