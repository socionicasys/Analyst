package ru.socionicasys.analyst.undo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.undo.CannotUndoException;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class UndoAction extends AbstractAction implements ActiveUndoManagerListener {
	private static final Logger logger = LoggerFactory.getLogger(UndoAction.class);
	private final ActiveUndoManager undoManager;

	/**
	 * Инициализирует undo-действие, привязывая его к данному undo-менеджеру.
	 * @param undoManager undo-менеджер, с которым будет связано действие
	 */
	public UndoAction(ActiveUndoManager undoManager) {
		this.undoManager = undoManager;
		this.undoManager.addActiveUndoManagerListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			undoManager.undo();
		} catch (CannotUndoException exception) {
			logger.error("Unable to undo", exception);
		}
	}

	@Override
	public void undoStateChanged(ActiveUndoManager undoManager) {
		//noinspection ObjectEquality
		assert undoManager == this.undoManager :
				"UndoAction can only be used with UndoManager it was created for";
		setEnabled(undoManager.canUndo());
		putValue(Action.NAME, undoManager.getUndoPresentationName());
	}
}
