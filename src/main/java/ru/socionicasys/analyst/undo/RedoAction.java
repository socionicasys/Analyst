package ru.socionicasys.analyst.undo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class RedoAction extends AbstractAction implements ActiveUndoManagerListener {
	private static final Logger logger = LoggerFactory.getLogger(RedoAction.class);
	private final ActiveUndoManager undoManager;

	/**
	 * Инициализирует redo-действие, привязывая его к данному undo-менеджеру.
	 * @param undoManager undo-менеджер, с которым будет связано действие
	 */
	public RedoAction(ActiveUndoManager undoManager) {
		this.undoManager = undoManager;
		this.undoManager.addActiveUndoManagerListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			undoManager.redo();
		} catch (CannotRedoException exception) {
			logger.error("Unable to redo", exception);
		}
	}

	@Override
	public void undoStateChanged(ActiveUndoManager undoManager) {
		//noinspection ObjectEquality
		assert undoManager == this.undoManager :
				"RedoAction can only be used with UndoManager it was created for";
		setEnabled(undoManager.canRedo());
		putValue(Action.NAME, undoManager.getRedoPresentationName());
	}
}
