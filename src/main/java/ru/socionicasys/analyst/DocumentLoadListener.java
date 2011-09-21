package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingWorker.StateValue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;

/**
 * Класс, слушающий состояние загрузки документа. Помещает документ в главное окно по окончанию загрузки.
 */
public final class DocumentLoadListener implements PropertyChangeListener {
	/**
	 * Добавлять ли документ после загрузки к уже существующему.
	 */
	private final boolean append;

	/**
	 * Контейнер с документом, в который нужно поместить новый после окончания загрузки.
	 */
	private final DocumentHolder documentHolder;
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentLoadListener.class);

	/**
	 * Создает объект, который может наблюдать за процессом загрузки документа фоновым потоком
	 * {@link LegacyHtmlReader} и добавит загруженный документ в заданный контейнер по окончанию.
	 *
	 * @param append добавлять ли новый документ в конец уже существующего вместо полной замены
	 * @param documentHolder контейнер для загруженного документа
	 */
	public DocumentLoadListener(boolean append, DocumentHolder documentHolder) {
		this.append = append;
		this.documentHolder = documentHolder;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		StateValue state = (StateValue) evt.getNewValue();
		if (state == StateValue.DONE) {
			try {
				LegacyHtmlReader worker = (LegacyHtmlReader) evt.getSource();
				ADocument document = worker.get();
				if (append) {
					documentHolder.getModel().appendDocument(document);
				} else {
					documentHolder.setModel(document);
				}
			} catch (InterruptedException e) {
				logger.info("Document loading interrupted", e);
			} catch (ExecutionException e) {
				logger.error("Error while loading document", e.getCause());
			}
		}
	}
}
