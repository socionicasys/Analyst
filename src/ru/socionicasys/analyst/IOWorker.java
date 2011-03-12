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
	private InputStream fis;
	private OutputStream fos;
	boolean append = false;
	private boolean firstWrite = true;
	private ADocument aDoc;
	HashMap<ASection, AData> aData = null;
	AnalystWindow frame;
	private ProgressWindow pw;
	private Operation op;
	private Exception exception = null;
	private int appendOffset = 0;
	private HashMap<Integer, RawAData> rawData = null;
	private static final Logger logger = LoggerFactory.getLogger(IOWorker.class);

	IOWorker(ProgressWindow pw, ADocument aDoc, InputStream fis) {
		this.fis = fis;
		this.aDoc = aDoc;
		this.frame = pw.getAnalyst();
		this.pw = pw;
		this.op = Operation.LOAD;

		this.addPropertyChangeListener(pw);
	}

	IOWorker(ProgressWindow pw, ADocument aDoc, OutputStream fos) {
		this.fos = fos;
		this.aDoc = aDoc;
		this.frame = pw.getAnalyst();
		this.pw = pw;
		this.op = Operation.SAVE;

		addPropertyChangeListener(pw);
		addPropertyChangeListener(frame);
		addPropertyChangeListener(this);
	}


	@Override
	protected Object doInBackground() throws Exception {
		addPropertyChangeListener(this);
		try {
			LegacyHtmlDocumentFormat documentFormat = new LegacyHtmlDocumentFormat();
			if (op.equals(Operation.LOAD)) {
				documentFormat.readDocument(aDoc, fis, append, this);
			} else if (op.equals(Operation.SAVE)) {
				documentFormat.writeDocument(aDoc, fos, this);
			}
		} catch (Exception e) {
			pw.close();
			this.exception = e;
			logger.error("IO error in doInBackground()", e);
		}

		return null;
	}

	protected final void setProgressValue(int p) {
		setProgress(p);
	}

	@Override
	protected void done() {
		super.done();
		pw.close();
	}

	protected void setAppend(boolean append) {
		this.append = append;
	}


	public static final class Operation {
		public static Operation LOAD = new Operation();
		public static Operation SAVE = new Operation();
	}

	public ProgressWindow getProgressWindow() {
		return pw;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		String name = evt.getPropertyName();
		Object newValue = evt.getNewValue();
		Object oldValue = evt.getOldValue();

		// if we are loading file
		if (op.equals(Operation.LOAD)) {
			//updating Document Properties
			if (name.equals("DocumentProperty")) {
				Dictionary<Object, Object> props = aDoc.getDocumentProperties();
				String docPropName = (String) oldValue;
				if (docPropName != null) {
					props.remove(docPropName);
					props.put(new String(docPropName), new String((String) newValue));
				}
			}
			if (name.equals("AppendStyledText")) {
				@SuppressWarnings("unchecked")
				ArrayList<StyledText> styledTextBlocks = (ArrayList<StyledText>) newValue;
				for (StyledText styledText : styledTextBlocks) {
					String textBlock = styledText.getText();
					AttributeSet textStyle = styledText.getStyle();
					try {
						if (firstWrite) {
							firstWrite = false;
							if (append) {
								appendOffset = aDoc.getLength();
							} else {
								// Если мы не добавляем в старый документ, то перед
								// первой записью его нужно очистить
								appendOffset = 0;
								aDoc.getADataMap().clear();
								aDoc.remove(0, aDoc.getEndPosition().getOffset() - 1);
							}
						}
						int docPosition = aDoc.getEndPosition().getOffset() - 1;
						aDoc.insertString(docPosition, textBlock, textStyle);
						// Исправляем ошибку insertString: текст вставляется без стилей
						aDoc.setCharacterAttributes(docPosition, textBlock.length(),
							textStyle, true);
					} catch (BadLocationException e) {
						logger.error("Illegal document location while working on AppendStyleText in propertyChange()", e);
					}
				}
			}
			if (name.equals("RawData")) {
				//getting AData
				rawData = (HashMap<Integer, RawAData>) newValue;

				try {
					Iterator<RawAData> it = (rawData.values()).iterator();
					RawAData temp = null;
					while (it.hasNext()) {
						temp = it.next();
						AData ad = null;

						ad = AData.parseAData(temp.getAData());
						ad.setComment(temp.getComment());
						int beg = temp.getBegin();
						int end = temp.getEnd();

						ASection section = new ASection(beg + appendOffset, end + appendOffset, aDoc.defaultSectionAttributes);
						aDoc.getADataMap().put(section, ad);
						aDoc.setCharacterAttributes(beg + appendOffset, end - beg, aDoc.defaultSectionAttributes, false);
					}
					aDoc.fireADocumentChanged();
				} catch (Exception e) {
					logger.error("Error while working on RawData in propertyChange()", e);
					pw.close();
					this.exception = e;
					this.cancel(true);
				}
			}
			AnalystWindow.initUndoManager();
		}//if load

		// if we are saving file
		if (op.equals(Operation.SAVE)) {

			// so far nothing to do on the dispatch thread
		}
	}

	public Exception getException() {
		return exception;
	}
}
