package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.AttributeSet;

/**
 * Представление части документа для буфера обмена.
 */
public class ADocumentFragment implements Transferable, Serializable {
	private static final Logger logger = LoggerFactory.getLogger(ADocumentFragment.class);
	private static final long serialVersionUID = -5267960680238237369L;

	public static final String MIME_TYPE =
		"application/x-java-serialized-object; class=ru.socionicasys.analyst.ADocumentFragment";
	private static DataFlavor nativeFlavor;

	private final String text;
	private final Map<DocSection, AttributeSet> styleMap;
	private final Map<DocSection, AData> aDataMap;

	/**
	 * Создает представление фрагмента документа без пометок.
	 * @param text текст фрагмента
	 */
	public ADocumentFragment(String text) {
		this.text = text;
		styleMap = new HashMap<DocSection, AttributeSet>();
		aDataMap = new HashMap<DocSection, AData>();
	}

	/**
	 * Создает представление фрагмента документа со стилями и пометками пользователя.
	 * @param text текст фрагмента
	 * @param styleMap стили фрагмента
	 * @param aDataMap пометки фрагмента
	 */
	public ADocumentFragment(String text, Map<DocSection, AttributeSet> styleMap, Map<DocSection, AData> aDataMap) {
		this(text);
		this.styleMap.putAll(styleMap);
		this.aDataMap.putAll(aDataMap);
	}

	/**
	 * @return текст фрагмента
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return стили фрагмента
	 */
	public Map<DocSection, AttributeSet> getStyleMap() {
		return Collections.unmodifiableMap(styleMap);
	}

	/**
	 * @return пометки фрагмента
	 */
	public Map<DocSection, AData> getADataMap() {
		return Collections.unmodifiableMap(aDataMap);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		String requestedMimeType = flavor.getMimeType();
		if (requestedMimeType.equals(MIME_TYPE)) {
			return this;
		} else if (requestedMimeType.equals(DataFlavor.stringFlavor.getMimeType())) {
			return text;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		createNativeFlavor();
		if (nativeFlavor != null) {
			return new DataFlavor[]{nativeFlavor, DataFlavor.stringFlavor};
		} else {
			return new DataFlavor[]{DataFlavor.stringFlavor};
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		String flavorMimeType = flavor.getMimeType();
		return flavorMimeType.equals(MIME_TYPE) || flavorMimeType.equals(DataFlavor.stringFlavor.getMimeType());
	}

	/**
	 * Инициализирует DataFlavor, предствляющий ADocumentFragment.
	 */
	private static void createNativeFlavor() {
		if (nativeFlavor == null) {
			try {
				nativeFlavor = new DataFlavor(MIME_TYPE);
			} catch (ClassNotFoundException e) {
				logger.error("Unable to create class for native data flavour", e);
			}
		}
	}
}
