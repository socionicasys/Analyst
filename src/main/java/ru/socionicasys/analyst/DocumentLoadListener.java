package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutionException;

/**
 * Класс, слушающий состояние загрузки документа. Помещает документ в главное окно по окончанию загрузки.
 */
public final class DocumentLoadListener extends SwingWorkerDoneListener {
	/**
	 * Добавлять ли документ после загрузки к уже существующему.
	 */
	private final boolean append;

	/**
	 * Смещение, по которому нужно добавить текст в существующий документ в случае {@code append = true}.
	 */
	private final int appendOffset;

	/**
	 * Контейнер с документом, в который нужно поместить новый после окончания загрузки.
	 */
	private final DocumentHolder documentHolder;
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentLoadListener.class);

	/**
	 * Создает объект, который может наблюдать за процессом загрузки документа фоновым потоком
	 * {@link LegacyHtmlReader} и добавит загруженный документ в заданный контейнер по окончанию.
	 *
	 * @param documentHolder контейнер для загруженного документа
	 * @param append добавлять ли новый документ в конец уже существующего вместо полной замены
	 * @param appendOffset Смещение, по которому нужно добавить текст в существующий документ
	 */
	public DocumentLoadListener(DocumentHolder documentHolder, boolean append, int appendOffset) {
		this.documentHolder = documentHolder;
		this.append = append;
		this.appendOffset = appendOffset;
	}

	@Override
	protected void swingWorkerDone(PropertyChangeEvent evt) {
		try {
			LegacyHtmlReader worker = (LegacyHtmlReader) evt.getSource();
			ADocument document = worker.get();
			if (append) {
				documentHolder.getModel().appendDocument(document, appendOffset);
			} else {
				documentHolder.setModel(document);
			}
		} catch (InterruptedException e) {
			logger.info("Document loading interrupted", e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			logger.error("Error while loading document", cause);
			JOptionPane.showOptionDialog(null,
					String.format("Ошибка открытия файла:\n%s", cause.getMessage()),
					"Ошибка открытия файла",
					JOptionPane.OK_OPTION,
					JOptionPane.ERROR_MESSAGE,
					null,
					new Object[]{"Закрыть"},
					null);
		}
	}
}
