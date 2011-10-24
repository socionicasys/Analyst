package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.socionicasys.analyst.model.AData;
import ru.socionicasys.analyst.model.DocumentSelectionModel;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.tree.DefaultMutableTreeNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Отслеживает и синхронизирует изменения в модели выделения для документа, самом документе,
 * и в положении курсора в текстовом поле.
 */
public class DocumentSelectionConnector implements PropertyChangeListener, CaretListener, TreeSelectionListener {
	/**
	 * Текстовое поле, с которым связан коннектор.
	 */
	private final TextPane textPane;

	/**
	 * Модель выделения, с которой связан коннектор.
	 */
	private final DocumentSelectionModel selectionModel;

	private static final Logger logger = LoggerFactory.getLogger(DocumentSelectionConnector.class);

	/**
	 * Создает объект-коннектор, связывающий текстовое поле и модель выделения в документе.
	 * 
	 * @param textPane текстовое поле, выделение в котором отображается в модель
	 * @param selectionModel модель выделения, изменения из которой будут транслироваться в документ,
	 * связанный с текстовым полем
	 */
	public DocumentSelectionConnector(TextPane textPane, DocumentSelectionModel selectionModel) {
		this.textPane = textPane;
		this.textPane.addCaretListener(this);
		this.selectionModel = selectionModel;
		this.selectionModel.addPropertyChangeListener(this);
	}

	/**
	 * Метод вызывается при изменениях в модели текущего выделения.
	 *
	 * @param evt парметр игнорируется, обновленные данные берутся непосредственно из модели
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!selectionModel.isInitialized()) {
			return;
		}

		DocumentSection currentSection =
				getCurrentSection(selectionModel.getStartOffset(), selectionModel.getEndOffset());
		if (currentSection == null) {
			return;
		}

		ADocument document = textPane.getDocument();
		AData newMarkup = selectionModel.getMarkupData();
		AData oldMarkup = document.getAData(currentSection);
		if (oldMarkup == null) {
			if (newMarkup != null) {
				// Новая отметка в документе
				document.addSection(currentSection, newMarkup);
			}
		} else if (newMarkup == null) {
			// Удаление старой отметки
			document.removeSection(currentSection);
		} else if (!newMarkup.equals(oldMarkup)) {
			// Обновление данных в существующей отметке
			document.updateSection(currentSection, newMarkup);
		}
	}

	/**
	 * Метод вызывается при перемещении курсора или смене выделения в текстовом поле.
	 * 
	 * @param e текущее положение курсора и выделения в текстовом поле
	 */
	@Override
	public void caretUpdate(CaretEvent e) {
		int dot = e.getDot();
		int mark = e.getMark();
		logger.trace("caretUpdate: {}, {}", dot, mark);

		selectionModel.setInitialized(false);

		int startOffset = Math.min(dot, mark);
		int endOffset = Math.max(dot, mark);
		selectionModel.setStartOffset(startOffset);
		selectionModel.setEndOffset(endOffset);

		DocumentSection currentSection = getCurrentSection(startOffset, endOffset);
		if (currentSection == null) {
			selectionModel.setMarkupData(null);
		} else {
			ADocument document = textPane.getDocument();
			AData currentMarkupData = document.getAData(currentSection);
			selectionModel.setMarkupData(currentMarkupData);
		}

		selectionModel.setInitialized(true);
	}

	/**
	 * Возвращает интервал в документе, выделенный в данный момент курсором,
	 * {@code null} если в документе сейчас нет активного выделения.
	 *
	 * @param startOffset позиция начала выделения
	 * @param endOffset позиция конца выделения
	 * @return выделенный в документе интервал
	 */
	private DocumentSection getCurrentSection(int startOffset, int endOffset) {
		if (startOffset == endOffset) {
			return null;
		}
		
		ADocument document = textPane.getDocument();
		try {
			return new DocumentSection(document, startOffset, endOffset);
		} catch (BadLocationException e) {
			logger.error("Invalid document positions: {}, {}", startOffset, endOffset);
			logger.error("Exception thrown: ", e);
			return null;
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		logger.trace("valueChanged({}): entering", e);

		Object leaf = e.getPath().getLastPathComponent();
		if (!(leaf instanceof DefaultMutableTreeNode)) {
			logger.warn("Unexpected node type detected, {}", leaf);
			logger.trace("valueChanged({}): leaving", e);
			return;
		}
		DefaultMutableTreeNode leafNode = (DefaultMutableTreeNode) leaf;

		Object leafObject = leafNode.getUserObject();
		if (!(leafObject instanceof EndNodeObject)) {
			logger.debug("Leaf object {} is not an EndNodeObject, skipping navigation", leafObject);
			logger.trace("valueChanged({}): leaving", e);
			return;
		}
		EndNodeObject endNodeObject = (EndNodeObject) leafObject;
		int index = endNodeObject.getOffset();
		logger.trace("Leaf object {} initiated navigation to offset {}", endNodeObject, index);

		ADocument document = textPane.getDocument();
		DocumentSection currentDocumentSection = document.getSectionThatStartsAt(index);
		if (currentDocumentSection == null) {
			logger.warn("No section at offset {}, but it was supposed to be there, skipping navigation", index);
		} else {
			Caret caret = textPane.getCaret();
			caret.setDot(currentDocumentSection.getStartOffset());
			caret.moveDot(currentDocumentSection.getEndOffset());
		}
		logger.trace("valueChanged({}): leaving", e);
	}
}
