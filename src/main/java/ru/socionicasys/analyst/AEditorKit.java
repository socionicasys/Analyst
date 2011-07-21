package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;

public class AEditorKit extends StyledEditorKit {
	public static class CutAction extends TextAction {
		private static final Logger logger = LoggerFactory.getLogger(CutAction.class);
		private final JTextPane textPane;

		public CutAction(JTextPane textPane) {
			super("cut-to-clipboard");
			this.textPane = textPane;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			ADocument aDoc = (ADocument) textPane.getDocument();
			Caret caret = textPane.getCaret();
			int dot = caret.getDot();
			int mark = caret.getMark();
			//if there is no selection - do nothing
			if (dot == mark) {
				return;
			}
			int selectionStart = Math.min(dot, mark);
			int selectionEnd = Math.max(dot, mark);

			copyToClipboard(textPane);

			try {
				aDoc.remove(selectionStart, selectionEnd - selectionStart);
				aDoc.removeCleanup(selectionStart, selectionEnd);
			} catch (BadLocationException e) {
				logger.error("Error in actionPerformed()", e);
			}

			aDoc.fireADocumentChanged();
		}
	}

	public static class CopyAction extends TextAction {
		private final JTextPane textPane;

		public CopyAction(JTextPane textPane) {
			super("copy-to-clipboard");
			this.textPane = textPane;
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			copyToClipboard(textPane);
		}
	}

	public static class PasteAction extends TextAction {
		private static final Logger logger = LoggerFactory.getLogger(PasteAction.class);
		private final JTextPane textPane;

		public PasteAction(JTextPane textPane) {
			super("paste-from-clipboard");
			this.textPane = textPane;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			Caret caret = textPane.getCaret();
			int dot = caret.getDot();
			int mark = caret.getMark();

			//if there is something selected - do nothing
			if (dot != mark) {
				return;
			}

			try {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable clipboardContents = clipboard.getContents(this);
				DataFlavor nativeFlavor = new DataFlavor(ADocumentFragment.MIME_TYPE);
				ADocumentFragment fragment;
				if (clipboardContents.isDataFlavorSupported(nativeFlavor)) {
					fragment = (ADocumentFragment) clipboardContents.getTransferData(nativeFlavor);
				} else if (clipboardContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					String clipboardText = (String) clipboardContents.getTransferData(DataFlavor.stringFlavor);
					fragment = new ADocumentFragment(clipboardText);
				} else {
					return;
				}

				ADocument document = (ADocument) textPane.getDocument();
				document.startCompoundEdit();
				document.pasteADocFragment(dot, fragment);
				document.endCompoundEdit(null);
				document.fireADocumentChanged();
			} catch (UnsupportedFlavorException e) {
				logger.error("Unable to get clipboard contents", e);
			} catch (IOException e) {
				logger.error("Unable to get clipboard contents", e);
			} catch (ClassNotFoundException e) {
				logger.error("Unable to create ADocumentFragment data flavor", e);
			}
		}
	}

	private static void copyToClipboard(JTextPane textPane) {
		Caret caret = textPane.getCaret();
		int dot = caret.getDot();
		int mark = caret.getMark();
		//if there is no selection - do nothing
		if (dot == mark) {
			return;
		}
		int selectionStart = Math.min(dot, mark);
		int selectionEnd = Math.max(dot, mark);

		ADocument document = (ADocument) textPane.getDocument();
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		// putting data to clipboard
		clipboard.setContents(document.getADocFragment(selectionStart, selectionEnd - selectionStart), null);
	}
}
