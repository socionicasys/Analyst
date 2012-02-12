package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

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
		logger.trace("DocumentTransferHandler({}): entering", parentTransferHandler);
		this.parentTransferHandler = parentTransferHandler;
		logger.trace("DocumentTransferHandler({}): leaving", parentTransferHandler);
	}

	@Override
	public boolean canImport(TransferSupport support) {
		logger.trace("canImport({}): entering", support);
		boolean canImport = support.isDataFlavorSupported(ADocumentFragment.getNativeFlavor()) ||
				parentTransferHandler != null && parentTransferHandler.canImport(support);
		logger.trace("canImport({}): leaving, result = {}", support, canImport);
		return canImport;
	}

	@Override
	public boolean importData(TransferSupport support) {
		logger.trace("importData({}): entering", support);
		if (!canImport(support)) {
			logger.debug("Cannot import data");
			logger.trace("importData({}): leaving, result = false", support);
			return false;
		}

		DataFlavor nativeFlavor = ADocumentFragment.getNativeFlavor();
		if (!support.isDataFlavorSupported(nativeFlavor)) {
			logger.debug("ADocumentFragment flavor not supported, delegating to parent");
			boolean parentCanImport = parentTransferHandler != null && parentTransferHandler.importData(support);
			logger.trace("importData({}): leaving, result = {}", support, parentCanImport);
			return parentCanImport;
		}

		logger.debug("Inserting ADocumentFragment");
		ADocumentFragment fragment;
		try {
			fragment = (ADocumentFragment) support.getTransferable().getTransferData(nativeFlavor);
			logger.debug("Obtained document fragment {}", fragment);
		} catch (UnsupportedFlavorException e) {
			logger.error("Unexpectedly unsupported native data flavour", e);
			logger.trace("importData({}): leaving, result = false", support);
			return false;
		} catch (IOException e) {
			logger.error("I/O error while making data transfer");
			logger.trace("importData({}): leaving, result = false", support);
			return false;
		}

		Component sourceComponent = support.getComponent();
		assert sourceComponent instanceof TextPane :
				"ADocumentFragment insertion is only supported for TextPane instances";
		TextPane textPane = (TextPane) sourceComponent;
		ADocument document = textPane.getDocument();

		int insertPosition;
		if (support.isDrop()) {
			JTextComponent.DropLocation dropLocation = (JTextComponent.DropLocation) support.getDropLocation();
			insertPosition = dropLocation.getIndex();
		} else {
			insertPosition = textPane.getCaretPosition();
		}

		logger.debug("Inserting document fragment at position {} (isDrop() = {})", insertPosition, support.isDrop());
		document.pasteADocFragment(insertPosition, fragment);
		logger.trace("importData({}): leaving, result = true", support);
		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	@Override
	@SuppressWarnings("ReturnOfNull")
	protected Transferable createTransferable(JComponent c) {
		logger.trace("createTransferable(): entering");
		assert c instanceof TextPane : "DocumentTransferHandler is only usable with TextPane instance";
		TextPane textPane = (TextPane) c;

		int selectionStart = textPane.getSelectionStart();
		int selectionEnd = textPane.getSelectionEnd();
		if (selectionStart == selectionEnd) {
			logger.debug("Empty selection, no fragment created");
			return null;
		}

		logger.debug("Creating new ADocumentFragment");
		ADocument document = textPane.getDocument();
		ADocumentFragment documentFragment = document.getADocFragment(selectionStart, selectionEnd - selectionStart);
		logger.trace("createTransferable(): leaving");
		return documentFragment;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		logger.trace("exportDone(source, {}, {}): entering", data, action);
		if (action != MOVE) {
			logger.debug("exportDone(): nothing to be done for this action");
			logger.trace("exportDone(source, {}, {}): leaving", data, action);
			return;
		}

		assert source instanceof TextPane : "DocumentTransferHandler is only usable with TextPane instance";
		TextPane textPane = (TextPane) source;

		int selectionStart = textPane.getSelectionStart();
		int selectionEnd = textPane.getSelectionEnd();

		ADocument document = textPane.getDocument();
		try {
			logger.debug("exportDone(): removing document fragment ({}, {}) after MOVE action",
					selectionStart, selectionEnd);
			document.remove(selectionStart, selectionEnd - selectionStart);
		} catch (BadLocationException e) {
			logger.error("exportDone(): invalid document position", e);
		} finally {
			logger.trace("exportDone(source, {}, {}): leaving", data, action);
		}
	}
}
