package ru.socionicasys.analyst.undo;

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
	@SuppressWarnings("NonSerializableFieldInSerializableClass")
	private final List<ActiveUndoManagerListener> undoManagerListeners;

	public ActiveUndoManager() {
		undoManagerListeners = new ArrayList<ActiveUndoManagerListener>();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		fireStateChanged();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		fireStateChanged();
	}

	@Override
	public boolean addEdit(UndoableEdit anEdit) {
		boolean canEdit = super.addEdit(anEdit);
		fireStateChanged();
		return canEdit;
	}

	@Override
	public void discardAllEdits() {
		super.discardAllEdits();
		fireStateChanged();
	}

	@Override
	public void setLimit(int l) {
		super.setLimit(l);
		fireStateChanged();
	}

	@Override
	public void end() {
		super.end();
		fireStateChanged();
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
