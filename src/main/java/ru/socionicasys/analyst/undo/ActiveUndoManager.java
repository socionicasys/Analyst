package ru.socionicasys.analyst.undo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.util.ArrayList;
import java.util.List;

/**
 * Наследник {@link UndoManager}, оповещающий слушателей об изменениях своего состояния.
 */
@SuppressWarnings("serial")
public class ActiveUndoManager extends UndoManager {
	private static final Logger logger = LoggerFactory.getLogger(ActiveUndoManager.class);
	@SuppressWarnings("NonSerializableFieldInSerializableClass")
	private final List<ActiveUndoManagerListener> undoManagerListeners;

	public ActiveUndoManager() {
		undoManagerListeners = new ArrayList<ActiveUndoManagerListener>();
	}

	@Override
	public void undo() throws CannotUndoException {
		logger.trace("undo(): entering");
		super.undo();
		fireStateChanged();
		logger.trace("undo(): leaving");
	}

	@Override
	public void redo() throws CannotRedoException {
		logger.trace("redo(): entering");
		super.redo();
		fireStateChanged();
		logger.trace("redo(): leaving");
	}

	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		logger.trace("addEdit(): entering");
		boolean canEdit = super.addEdit(anEdit);
		fireStateChanged();
		logger.trace("addEdit(): leaving");
		return canEdit;
	}

	@Override
	public void discardAllEdits() {
		logger.trace("discardAllEdits(): entering");
		super.discardAllEdits();
		fireStateChanged();
		logger.trace("discardAllEdits(): leaving");
	}

	@Override
	public void setLimit(int l) {
		logger.trace("setLimit(): entering, limit={}", l);
		super.setLimit(l);
		fireStateChanged();
		logger.trace("setLimit(): leaving");
	}

	@Override
	public void end() {
		logger.trace("end(): entering");
		super.end();
		fireStateChanged();
		logger.trace("end(): leaving");
	}

	/**
	 * Оповещает слушателей об изменеиях в состоянии класса (новых UndoableEdit-ах,
	 * выполненных {@link #undo()}, {@link #redo()}, и т. п.
	 */
	private void fireStateChanged() {
		for (int i = undoManagerListeners.size() - 1; i >= 0; i--) {
			ActiveUndoManagerListener listener = undoManagerListeners.get(i);
			listener.undoStateChanged(this);
		}
	}

	/**
	 * Добавляет слушателя состояния данного менеджера.
	 * @param listener слушатель состояния {@code ActiveUndoManager}
	 */
	public void addActiveUndoManagerListener(ActiveUndoManagerListener listener) {
		undoManagerListeners.add(listener);
		listener.undoStateChanged(this);
	}

	/**
	 * Удаляет слушателя состояния данного менеджера.
	 * @param listener слушатель состояния {@code ActiveUndoManager}
	 */
	public void removeActiveUndoManagerListener(ActiveUndoManagerListener listener) {
		undoManagerListeners.remove(listener);
	}
}
