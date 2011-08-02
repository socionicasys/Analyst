package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Transferable;

/**
 * Реализует взаимодействие документа {@link ADocument} в {@link TextPane} с буфером обмена и/или DnD.
 */
public class DocumentTransferHandler extends TransferHandler {
	private static final long serialVersionUID = -2218303045857461200L;
	private static final Logger logger = LoggerFactory.getLogger(DocumentTransferHandler.class);

	private final TransferHandler parentTransferHandler;

	/**
	 * Создает {@code DocumentTransferHandler}, привязывая его к «родительскому» {@link TransferHandler}.
	 * Данные в формате, не поддерживаемом {@code DocumentTransferHandler}, будут переданы родителю.
	 * @param parentTransferHandler родительский {@code TransferHandler}.
	 */
	public DocumentTransferHandler(TransferHandler parentTransferHandler) {
		logger.debug("DocumentTransferHandler(): constructing with parent={}", parentTransferHandler);
		this.parentTransferHandler = parentTransferHandler;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		logger.debug("canImport(): called with support={}", support);
		boolean canImport = support.isDataFlavorSupported(ADocumentFragment.getNativeFlavor()) ||
				parentTransferHandler != null && parentTransferHandler.canImport(support);
		logger.debug("canImport(): result = {}", canImport);
		return canImport;
	}

	@Override
	public boolean importData(TransferSupport support) {
		logger.debug("importData(): called with support={}", support);
		if (!canImport(support)) {
			return false;
		}

		if (!support.isDataFlavorSupported(ADocumentFragment.getNativeFlavor())) {
			logger.debug("importData(): ADocumentFragment flavor not supported, delegating to parent");
			return parentTransferHandler != null && parentTransferHandler.importData(support);
		}

		logger.debug("importData(): inserting ADocumentFragment");
		Component sourceComponent = support.getComponent();
		assert sourceComponent instanceof TextPane :
				"ADocumentFragment insertion is only supported for TextPane instances";
		TextPane textPane = (TextPane) sourceComponent;
		JTextComponent.DropLocation dropLocation = (JTextComponent.DropLocation) support.getDropLocation();
		ADocument document = textPane.getDocument();
		ADocumentFragment fragment = (ADocumentFragment) support.getTransferable();
		document.pasteADocFragment(dropLocation.getIndex(), fragment);
		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	@Override
	@SuppressWarnings("ReturnOfNull")
	protected Transferable createTransferable(JComponent c) {
		logger.debug("createTransferable(): called with c={}", c);
		assert c instanceof TextPane : "DocumentTransferHandler is only usable with TextPane instance";
		TextPane textPane = (TextPane) c;

		int selectionStart = textPane.getSelectionStart();
		int selectionEnd = textPane.getSelectionEnd();
		if (selectionStart == selectionEnd) {
			logger.debug("createTransferable(): empty selection, no fragment created");
			return null;
		}

		logger.debug("createTransferable(): creating new ADocumentFragment");
		ADocument document = textPane.getDocument();
		return document.getADocFragment(selectionStart, selectionEnd - selectionStart);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		logger.debug("exportDone(): called with source={}, data={}, action={}",
				new Object[]{source, data, action});
		if (action != MOVE) {
			logger.debug("exportDone(): nothing to be done for this action");
			return;
		}

		assert source instanceof TextPane : "DocumentTransferHandler is only usable with TextPane instance";
		TextPane textPane = (TextPane) source;

		int selectionStart = textPane.getSelectionStart();
		int selectionEnd = textPane.getSelectionEnd();

		ADocument document = textPane.getDocument();
		try {
			logger.debug("exportDone(): removing document fragment after MOVE action");
			document.remove(selectionStart, selectionEnd - selectionStart);
			document.removeCleanup(selectionStart, selectionEnd);
		} catch (BadLocationException e) {
			logger.error("exportDone(): invalid document position", e);
		}
	}
}
