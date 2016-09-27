package ru.socionicasys.analyst;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.CannotUndoException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class NiceTextArea extends JTextArea {

	private UndoManager undoManager = new UndoManager();

	public NiceTextArea(int rows, int columns) {
		super(rows, columns);
		setLineWrap(true);
		setWrapStyleWord(true);
		getDocument().addUndoableEditListener(undoManager);
		bindListeners(this);
	}

	static private void bindListeners(final NiceTextArea ta) {
		ta.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent key) {

				final int m_shift = KeyEvent.SHIFT_DOWN_MASK;
				final int m_ctrl = KeyEvent.CTRL_DOWN_MASK;
				final int m_ctrl_shift = m_ctrl | m_shift;

				final int modifiers = key.getModifiersEx();
				final int keyCode = key.getKeyCode();

				boolean keyCatched = true;

				if (modifiers == m_ctrl && keyCode == KeyEvent.VK_V)
					ta.paste();
				else if (modifiers == m_ctrl && keyCode == KeyEvent.VK_C)
					ta.copy();
				else if (modifiers == m_ctrl && keyCode == KeyEvent.VK_X)
					ta.cut();
				else if (modifiers == m_shift && keyCode == KeyEvent.VK_INSERT)
					ta.paste();
				else if (modifiers == m_ctrl && keyCode == KeyEvent.VK_INSERT)
					ta.copy();
				else if (modifiers == m_shift && keyCode == KeyEvent.VK_DELETE)
					ta.cut();
				else if (modifiers == m_ctrl_shift && keyCode == KeyEvent.VK_Z)
					ta.redo();
				else if (modifiers == m_ctrl && keyCode == KeyEvent.VK_Z)
					ta.undo();
				else if (modifiers == m_ctrl && keyCode == KeyEvent.VK_Y)
					ta.redo();
				else
					keyCatched = false;

				if (keyCatched)
					key.consume();
			}
		});
	}

	public void undo() {
		try {
			if (undoManager.canUndo())
				undoManager.undo();
		} catch (CannotUndoException exp) {
			exp.printStackTrace();
		}
	}

	public void redo() {
		try {
			if (undoManager.canRedo())
				undoManager.redo();
		} catch (CannotUndoException exp) {
			exp.printStackTrace();
		}
	}

	public void setTextAndDiscardAllEdits(String t) {
		setText(t);
		undoManager.discardAllEdits();
	}

}
