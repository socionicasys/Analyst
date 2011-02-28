package ru.socionicasys.analyst;

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


	public AEditorKit() {
		super();
	}

	//@override
	public static class CutAction extends TextAction {

		private JTextPane textPane;

		public CutAction(JTextPane textPane) {
			super("cut-to-clipboard");
			this.textPane = textPane;
		}

		public void actionPerformed(ActionEvent ae) {
			ADocument aDoc = (ADocument) textPane.getDocument();
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Caret caret = textPane.getCaret();
			int dot = caret.getDot();
			int mark = caret.getMark();
			//if there is no selection - do nothing
			if (dot == mark) return;
			int selectionStart = Math.min(dot, mark);
			int selectionEnd = Math.max(dot, mark);

			copyToClipboard(textPane);

			try {
				aDoc.remove(selectionStart, selectionEnd - selectionStart);
				aDoc.removeCleanup(selectionStart, selectionEnd);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			aDoc.fireADocumentChanged();
		}
	}//end class CutAction

	//
	public static class CopyAction extends TextAction {

		private JTextPane textPane;

		public CopyAction(JTextPane textPane) {
			super("copy-to-clipboard");
			this.textPane = textPane;
		}


		public void actionPerformed(ActionEvent ae) {
			copyToClipboard(textPane);
		}
	}//end class CopyAction


	//
	public static class PasteAction extends TextAction {

		private JTextPane textPane;

		public PasteAction(JTextPane textPane) {
			super("paste-from-clipboard");
			this.textPane = textPane;
		}


		public void actionPerformed(ActionEvent ae) {
			ADocument aDoc = (ADocument) textPane.getDocument();
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Caret caret = textPane.getCaret();
			int dot = caret.getDot();
			int mark = caret.getMark();
			//if there is something selected - do nothing
			if (dot != mark) return;

			ADocumentFragment fragment = null;
			/*
		 DataFlavor [] f = clipboard.getAvailableDataFlavors();
		 for (int i = 0 ; i<f.length; i++){
			 System.out.println(f[i].getMimeType());
		 }
		 */
			try {
				Transferable tr = clipboard.getContents(this);

				DataFlavor flavor = new DataFlavor(ADocumentFragment.MIME_TYPE);

				if (tr.isDataFlavorSupported(flavor))
					fragment = (ADocumentFragment) tr.getTransferData(flavor);
				else {
					flavor = new DataFlavor("application/x-java-serialized-object; class=java.lang.String");
					if (tr.isDataFlavorSupported(flavor)) {
						String s = new String((((String) tr.getTransferData(flavor))));
						fragment = new ADocumentFragment(s, null, null);
					}
				}
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			if (fragment == null) return;

			aDoc.startCompoundEdit();
			ADocument.pasteADocFragment(aDoc, dot, fragment);
			aDoc.endCompoundEdit(null);
			//aDoc.fireUndoableEditUpdate(new UndoableEditEvent(this, aDoc.new ADocFragmentPasteEdit(dot, aDoc, fragment)));
			//Analyst.undo.addEdit((UndoableEdit)  aDoc.new ADocFragmentPasteEdit(dot, aDoc, fragment));

			aDoc.fireADocumentChanged();
		}
	}//end class PasteAction

	private static void copyToClipboard(JTextPane textPane) {
		ADocument aDoc = (ADocument) textPane.getDocument();
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		Caret caret = textPane.getCaret();
		int dot = caret.getDot();
		int mark = caret.getMark();
		//if there is no selection - do nothing
		if (dot == mark) return;
		int selectionStart = Math.min(dot, mark);
		int selectionEnd = Math.max(dot, mark);

		// putting data to clipboard

		clipboard.setContents(ADocument.getADocFragment(aDoc, selectionStart, selectionEnd - selectionStart), null);
	}
}
