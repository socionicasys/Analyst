package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Синхронизирует поле комментария и модель выделения в документе.
 */
public class CommentConnector implements DocumentListener, PropertyChangeListener {
	/**
	 * Модель выделения, к которой привязана данная панель.
	 */
	private final DocumentSelectionModel selectionModel;

	/**
	 * Текстовый компонент с комментарием.
	 */
	private final JTextComponent textComponent;

	/**
	 * Служит для синхронизации обновлений модель->представление и представление->модель.
	 * Если это поле равно {@code false}, данные в нем находятся в процессе заполнения, и
	 * не должны синхронизироваться обратно в модель.
	 */
	private boolean viewInitialized;

	private static final Logger logger = LoggerFactory.getLogger(CommentConnector.class);

	/**
	 * Создает коннектор между заданным текстовы полем и моделью выделения.
	 *
	 * @param selectionModel модель выделения
	 * @param textComponent текстовое поле комментария
	 */
	public CommentConnector(DocumentSelectionModel selectionModel, JTextComponent textComponent) {
		this.selectionModel = selectionModel;
		this.selectionModel.addPropertyChangeListener(this);
		this.textComponent = textComponent;
		this.textComponent.getDocument().addDocumentListener(this);
		viewInitialized = true;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateModel();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateModel();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateModel();
	}

	/**
	 * Обновляет модель по данным из текстового поля с комментарием.
	 */
	private void updateModel() {
		if (!viewInitialized) {
			return;
		}

		try {
			Document document = textComponent.getDocument();
			selectionModel.setComment(document.getText(0, document.getLength()));
		} catch (BadLocationException e) {
			logger.error("updateModel(): invalid document localtion", e);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!selectionModel.isInitialized()) {
			return;
		}

		viewInitialized = false;
		boolean commentEnabled = !selectionModel.isEmpty() && !selectionModel.isMarkupEmpty();
		textComponent.setEnabled(commentEnabled);
		if (commentEnabled) {
			textComponent.setText(selectionModel.getComment());
		} else {
			textComponent.setText("");
		}
		viewInitialized = true;
	}
}
