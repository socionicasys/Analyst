package ru.socionicasys.analyst.undo;

import java.util.EventListener;

/**
 * Интерфейс подписчика на события {@link ActiveUndoManager}.
 */
public interface ActiveUndoManagerListener extends EventListener {
	/**
	 * Метод вызывается, когда в состоянии {@link ActiveUndoManager} произошли изменения.
	 * @param undoManager экземпляр менеджера, в котором сменилось состояние
	 */
	void undoStateChanged(ActiveUndoManager undoManager);
}
