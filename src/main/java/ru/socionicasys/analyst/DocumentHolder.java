package ru.socionicasys.analyst;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

/**
 * Класс-обертка для документа, позволяющая менять документ на новый, не регистрируя заново
 * всех слушателей событий, связанных с документом.
 */
public final class DocumentHolder extends GenericModelHolder<ADocument> implements ADocumentChangeListener, UndoableEditListener {
	/**
	 * Создает контейнер и инициализирует его заданным документом.
	 * @param model хранимый в контейнере документ
	 */
	public DocumentHolder(ADocument model) {
		setModel(model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setModel(ADocument model) {
		if (this.model == model) {
			return;
		}
		if (this.model != null) {
			this.model.removeADocumentChangeListener(this);
			this.model.removeUndoableEditListener(this);
		}
		super.setModel(model);
		this.model.addADocumentChangeListener(this);
		this.model.addUndoableEditListener(this);
		fireDocumentChanged();
	}

	/**
	 * Вызывается, когда происходят изменения в хранимом ADocument
	 * @param document экземпляр документа, в котором произошли изменения
	 */
	@Override
	public void aDocumentChanged(ADocument document) {
		assert document == model : "aDocumentChanged event from unknown document";
		fireDocumentChanged();
	}

	/**
	 * Добавляет слушателя для событий обновления документа. Эти события транслируются от экземпляра ADocument,
	 * хранимого в контейнере.
	 * @param listener слушатель
	 */
	public void addADocumentChangeListener(ADocumentChangeListener listener) {
		listenerList.add(ADocumentChangeListener.class, listener);
	}

	/**
	 * Удаляет слушателя для событий обновления документа.
	 * @param listener слушатель
	 */
	public void removeADocumentChangeListener(ADocumentChangeListener listener) {
		listenerList.remove(ADocumentChangeListener.class, listener);
	}

	/**
	 * Оповещает слушателей об изменениях в хранимом документе ADocument.
	 */
	private void fireDocumentChanged() {
		for (ADocumentChangeListener listener : listenerList.getListeners(ADocumentChangeListener.class)) {
			listener.aDocumentChanged(model);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		fireUndoableEditUpdate(e);
	}

	/**
	 * Добавляет слушателя для {@link UndoableEditEvent}. Эти события транслируются от экземпляра ADocument,
	 * хранимого к контейнере.
	 * @param listener слушатель
	 */
	public void addUndoableEditListener(UndoableEditListener listener) {
		listenerList.add(UndoableEditListener.class, listener);
	}

	/**
	 * Добавляет слушателя для {@link UndoableEditEvent}.
	 * @param listener слушатель
	 */
	public void removeUndoableEditListener(UndoableEditListener listener) {
		listenerList.remove(UndoableEditListener.class, listener);
	}

	/**
	 * Оповещает слушателей об {@link UndoableEditEvent}, произошедших в хранимом ADocument.
	 * @param e событие
	 */
	private void fireUndoableEditUpdate(UndoableEditEvent e) {
		for (UndoableEditListener listener : listenerList.getListeners(UndoableEditListener.class)) {
			listener.undoableEditHappened(e);
		}
	}
}
