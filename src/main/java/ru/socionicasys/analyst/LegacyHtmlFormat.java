package ru.socionicasys.analyst;

/**
 * Содержит константы, общие для {@link LegacyHtmlReader} и {@link LegacyHtmlWriter}.
 */
public interface LegacyHtmlFormat {
	// Кодировка файлов
	String FILE_ENCODING = "UTF-8";

	// Расширение по-умолчанию
	String EXTENSION = "htm";

	// Заголовки полей документа в файле
	String TITLE_PROPERTY_LABEL = "Документ:";
	String EXPERT_PROPERTY_LABEL = "Эксперт:";
	String CLIENT_PROPERTY_LABEL = "Типируемый:";
	String DATE_PROPERTY_LABEL = "Дата:";
	String COMMENT_PROPERTY_LABEL = "Комментарий:";
}
