package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

public class IOWorker extends SwingWorker implements PropertyChangeListener {
	private InputStream inputStream;
	private OutputStream outputStream;
	private final boolean append;
	private boolean firstWrite = true;
	private final ADocument document;
	private final AnalystWindow frame;
	private final ProgressWindow progressWindow;
	private Exception exception = null;
	private int appendOffset = 0;
	private static final Logger logger = LoggerFactory.getLogger(IOWorker.class);

	IOWorker(ProgressWindow progressWindow, ADocument document, InputStream inputStream, boolean append) {
		this.inputStream = inputStream;
		this.document = document;
		this.frame = progressWindow.getAnalyst();
		this.progressWindow = progressWindow;
		this.append = append;

		addPropertyChangeListener(progressWindow);
	}

	@Override
	protected Object doInBackground() throws Exception {
		addPropertyChangeListener(this);
		try {
			LegacyHtmlDocumentFormat documentFormat = new LegacyHtmlDocumentFormat();
			documentFormat.readDocument(document, inputStream, append, this);
		} catch (Exception e) {
			progressWindow.close();
			this.exception = e;
			logger.error("IO error in doInBackground()", e);
		}

		return null;
	}

	@Override
	protected void done() {
		super.done();
		progressWindow.close();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		Object oldValue = evt.getOldValue();

		// if we are loading file
			//updating Document Properties
			if (name.equals("DocumentProperty")) {
				String docPropName = (String) oldValue;
				if (docPropName != null) {
					document.putProperty(docPropName, newValue);
				}
			}
			if (name.equals("AppendStyledText")) {
				@SuppressWarnings("unchecked")
				Iterable<StyledText> styledTextBlocks = (Iterable<StyledText>) newValue;
				for (StyledText styledText : styledTextBlocks) {
					String textBlock = styledText.getText();
					AttributeSet textStyle = styledText.getStyle();
					try {
						if (firstWrite) {
							firstWrite = false;
							if (append) {
								appendOffset = document.getLength();
							} else {
								// Если мы не добавляем в старый документ, то перед
								// первой записью его нужно очистить
								appendOffset = 0;
								document.getADataMap().clear();
								document.remove(0, document.getEndPosition().getOffset() - 1);
							}
						}
						int docPosition = document.getEndPosition().getOffset() - 1;
						document.insertString(docPosition, textBlock, textStyle);
						// Исправляем ошибку insertString: текст вставляется без стилей
						document.setCharacterAttributes(docPosition, textBlock.length(),
							textStyle, true);
					} catch (BadLocationException e) {
						logger.error("Illegal document location while working on AppendStyleText in propertyChange()", e);
					}
				}
			}
			if (name.equals("RawData")) {
				//getting AData
				Map<Integer, RawAData> rawData = (Map<Integer, RawAData>) newValue;
				try {
					for (RawAData rawAData : rawData.values()) {
						AData data = AData.parseAData(rawAData.getAData());
						data.setComment(rawAData.getComment());
						int begin = rawAData.getBegin();
						int end = rawAData.getEnd();
						ASection section = new ASection(begin + appendOffset, end + appendOffset,
							document.defaultSectionAttributes);
						document.getADataMap().put(section, data);
						document.setCharacterAttributes(begin + appendOffset, end - begin,
							document.defaultSectionAttributes, false);
					}
					document.fireADocumentChanged();
				} catch (Exception e) {
					logger.error("Error while working on RawData in propertyChange()", e);
					progressWindow.close();
					this.exception = e;
					this.cancel(true);
				}
			}
			AnalystWindow.initUndoManager();
	}

	public Exception getException() {
		return exception;
	}
}
