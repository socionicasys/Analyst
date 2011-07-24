package ru.socionicasys.analyst;

import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

/**
 * Класс-обертка для документа, позволяющая менять документ на новый, не регистрируя заново
 * всех слушателей событий, связанных с документом.
 */
public final class DocumentHolder implements ModelHolder<ADocument>, ADocumentChangeListener, UndoableEditListener {
	private ADocument model;
	private final EventListenerList listenerList;

	public DocumentHolder(ADocument model) {
		listenerList = new EventListenerList();
		setModel(model);
	}

	@Override
	public ADocument getModel() {
		return model;
	}

	@Override
	public void setModel(ADocument model) {
		if (this.model != model) {
			if (this.model != null) {
				this.model.removeADocumentChangeListener(this);
			}
			this.model = model;
			this.model.addADocumentChangeListener(this);
			fireDocumentChanged();
		}
	}

	@Override
	public void aDocumentChanged(ADocument doc) {
		assert doc == model : "aDocumentChanged event from unknown document";
		fireDocumentChanged();
	}

	public void addADocumentChangeListener(ADocumentChangeListener listener) {
		listenerList.add(ADocumentChangeListener.class, listener);
	}

	public void removeADocumentChangeListener(ADocumentChangeListener listener) {
		listenerList.remove(ADocumentChangeListener.class, listener);
	}

	private void fireDocumentChanged() {
		for (ADocumentChangeListener listener : listenerList.getListeners(ADocumentChangeListener.class)) {
			listener.aDocumentChanged(model);
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		fireUndoableEditUpdate(e);
	}

	public void addUndoableEditListener(UndoableEditListener listener) {
		listenerList.add(UndoableEditListener.class, listener);
	}

	public void removeUndoableEditListener(UndoableEditListener listener) {
		listenerList.remove(UndoableEditListener.class, listener);
	}

	private void fireUndoableEditUpdate(UndoableEditEvent e) {
		for (UndoableEditListener listener : listenerList.getListeners(UndoableEditListener.class)) {
			listener.undoableEditHappened(e);
		}
	}
}
