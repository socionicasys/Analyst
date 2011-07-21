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

public class ADocumentFragment implements Transferable, Serializable {
	private static final Logger logger = LoggerFactory.getLogger(ADocumentFragment.class);
	private static final long serialVersionUID = -5267960680238237369L;

	public static final String MIME_TYPE =
		"application/x-java-serialized-object; class=ru.socionicasys.analyst.ADocumentFragment";

	private final String text;
	private final Map<DocSection, AttributeSet> styleMap;
	private final Map<DocSection, AData> aDataMap;

	public ADocumentFragment(String text) {
		this.text = text;
		styleMap = new HashMap<DocSection, AttributeSet>();
		aDataMap = new HashMap<DocSection, AData>();
	}

	public ADocumentFragment(String text, Map<DocSection, AttributeSet> styleMap, Map<DocSection, AData> aDataMap) {
		this(text);
		this.styleMap.putAll(styleMap);
		this.aDataMap.putAll(aDataMap);
	}

	public String getText() {
		return text;
	}

	public Map<DocSection, AttributeSet> getStyleMap() {
		return Collections.unmodifiableMap(styleMap);
	}

	public Map<DocSection, AData> getADataMap() {
		return Collections.unmodifiableMap(aDataMap);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.getMimeType().equals(MIME_TYPE)) {
			return this;
		}
		if (flavor.getMimeType().equals(DataFlavor.stringFlavor.getMimeType())) {
			return text;
		}
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		try {
			DataFlavor nativeFlavor = new DataFlavor(MIME_TYPE);
			return new DataFlavor[]{nativeFlavor, DataFlavor.stringFlavor};
		} catch (ClassNotFoundException e) {
			logger.error("Unable to load class in getTransferDataFlavors()", e);
		}
		return null;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		String flavorMimeType = flavor.getMimeType();
		return flavorMimeType.equals(MIME_TYPE) || flavorMimeType.equals(DataFlavor.stringFlavor.getMimeType());
	}
}
