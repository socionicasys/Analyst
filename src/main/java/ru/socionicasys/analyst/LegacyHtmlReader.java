package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.text.*;

public class LegacyHtmlReader extends SwingWorker<ADocument, Void> {
	private static final Logger logger = LoggerFactory.getLogger(LegacyHtmlReader.class);

	private static final int FILE_LOAD_PROGRESS = 20;
	private static final int LEFT_COLUMN_PROGRESS = 50;
	private static final int RIGHT_COLUMN_PROGRESS = 25;

	private static final Pattern HTML_BR_PATTERN = Pattern.compile("<br/>", Pattern.LITERAL);
	private static final Pattern LINEBREAK_PATTERN = Pattern.compile("\n", Pattern.LITERAL);

	// Строка таблицы со свойствами документа или таблицы протокола
	private static final Pattern TABLE_ROW_PATTERN =
			Pattern.compile("<tr>\\s*<td>\\s*(.*?)\\s*</td>\\s*<td>\\s*(.*?)\\s*</td>\\s*</tr>", Pattern.DOTALL);
	private static final int TABLE_ROW_CELL_1_GROUP = 1;
	private static final int TABLE_ROW_CELL_2_GROUP = 2;
	
	private static final Map<String, String> PROPERTY_LABEL_2_KEY_MAP = buildPropertyLabel2KeyMap();

	// Регулярное выражение для поиска тегов вида [n| и |n]
	private static final Pattern LEFT_COLUMN_TAG_PATTERN = Pattern.compile("(?:\\[(\\d+)\\|)|(?:\\|(\\d+)\\])");
	private static final int LEFT_COLUMN_TAG_OPENING_GROUP = 1;
	private static final int LEFT_COLUMN_TAG_CLOSING_GROUP = 2;

	// Регулярное выражение для поиска тегов вида {n:пометки типировщика}комментарий типиврощика
	private static final Pattern RIGHT_COLUMN_TAG_PATTERN = Pattern.compile("\\{(\\d+):([^}]*)\\}([^{]*)");
	private static final int RIGHT_COLUMN_TAG_ID_GROUP = 1;
	private static final int RIGHT_COLUMN_TAG_MARKUP_GROUP = 2;
	private static final int RIGHT_COLUMN_TAG_COMMENT_GROUP = 3;

	private static final int BUFFER_SIZE = 1024;

	private final File sourceFile;

	private final Map<Integer, RawAData> rawData = new HashMap<Integer, RawAData>();

	public LegacyHtmlReader(File sourceFile) {
		logger.trace("LegacyHtmlReader({}): entering", sourceFile);
		this.sourceFile = sourceFile;
		logger.trace("LegacyHtmlReader({}): leaving", sourceFile);
	}

	@Override
	protected ADocument doInBackground() throws IOException {
		logger.trace("doInBackground(): entering");
		try {
			logger.debug("doInBackground(): loading file {}", sourceFile);
			return readDocument();
		} catch (IOException e) {
			logger.error("IO error while loading document", e);
			throw e;
		} finally {
			logger.debug("doInBackground(): loading finished");
			logger.trace("doInBackground(): leaving");
		}
	}

	private ADocument readDocument() throws IOException {
		setProgress(0);

		ADocument document = new ADocument();
		document.setAssociatedFile(sourceFile);
		
		String text = readFromStream();
		setProgress(FILE_LOAD_PROGRESS);

		Dictionary<Object, Object> documentProperties = document.getDocumentProperties();
		for (Map.Entry<String, String> entry : parseDocumentProperties(text).entrySet()) {
			documentProperties.put(entry.getKey(), entry.getValue());
		}

		StringBuilder leftColumnBuilder = new StringBuilder();
		StringBuilder rightColumnBuilder = new StringBuilder();
		splitTextColumns(text, leftColumnBuilder, rightColumnBuilder);

		String leftColumn = LINEBREAK_PATTERN.matcher(leftColumnBuilder.toString()).replaceAll("");
		leftColumn = HTML_BR_PATTERN.matcher(leftColumn).replaceAll("\n");
		leftColumn = leftColumn.trim();

		String rightColumn = LINEBREAK_PATTERN.matcher(rightColumnBuilder.toString()).replaceAll("");
		rightColumn = HTML_BR_PATTERN.matcher(rightColumn).replaceAll("\n");

		// Убираем все лишние теги
		Pattern tagPattern = Pattern.compile("</?(span|small)[^>]*>");
		leftColumn = tagPattern.matcher(leftColumn).replaceAll("");

		// processing the left column's content
		leftColumn = parseLeftColumn(leftColumn);
		setProgress(FILE_LOAD_PROGRESS + LEFT_COLUMN_PROGRESS);

		parseRightColumn(rightColumn);
		setProgress(FILE_LOAD_PROGRESS + LEFT_COLUMN_PROGRESS + RIGHT_COLUMN_PROGRESS);

		// Обрабатываем стили в уже прочитанном тексте
		SimpleAttributeSet currentStyle = new SimpleAttributeSet(ADocument.DEFAULT_STYLE);
		Pattern styleTag = Pattern.compile("</?[bi]>");
		String sourceText = leftColumn;
		Matcher styleMatcher = styleTag.matcher(sourceText);
		int sourcePosition = 0;
		int sourceOffset = 0;
		Collection<StyledText> styledTextBlocks = new ArrayList<StyledText>();
		while (styleMatcher.find()) {
			String currentTag = styleMatcher.group();
			int tagLength = currentTag.length();
			int tagStart = styleMatcher.start();
			int tagEnd = styleMatcher.end();
			String textBlock = sourceText.substring(sourcePosition, tagStart);

			// Добавляем в документ текст перед текущим тегом
			styledTextBlocks.add(new StyledText(textBlock, currentStyle));
			sourcePosition = tagEnd;

			// Так как мы удаляем теги из основного текста, необходимо сместить
			// пометки типировщика, находящиеся после тега
			for (RawAData rd : rawData.values()) {
				if (rd.getBegin() >= tagEnd - sourceOffset) {
					rd.setBegin(rd.getBegin() - tagLength);
				}
				if (rd.getEnd() >= tagEnd - sourceOffset) {
					rd.setEnd(rd.getEnd() - tagLength);
				}
			}
			sourceOffset += tagLength;

			// Стиль следующего текста в зависимости от текущего тега
			if ("<b>".equals(currentTag)) {
				StyleConstants.setBold(currentStyle, true);
			} else if ("</b>".equals(currentTag)) {
				StyleConstants.setBold(currentStyle, false);
			} else if ("<i>".equals(currentTag)) {
				StyleConstants.setItalic(currentStyle, true);
			} else if ("</i>".equals(currentTag)) {
				StyleConstants.setItalic(currentStyle, false);
			}
		}
		// Добавляем в документ текст за последним тегом
		styledTextBlocks.add(new StyledText(sourceText.substring(sourcePosition), currentStyle));

		setProgress(100);

		// Применяем к документу блоки текста со стилями из styledTextBlocks
		for (StyledText styledText : styledTextBlocks) {
			String textBlock = styledText.getText();
			AttributeSet textStyle = styledText.getStyle();
			try {
				int docPosition = document.getEndPosition().getOffset() - 1;
				document.insertString(docPosition, textBlock, textStyle);
				// Исправляем ошибку insertString: текст вставляется без стилей
				document.setCharacterAttributes(docPosition, textBlock.length(), textStyle, true);
			} catch (BadLocationException e) {
				logger.error("Illegal document location applying styles to document", e);
			}
		}

		for (RawAData rawAData : rawData.values()) {
			AData data = AData.parseAData(rawAData.getAData());
			if (data == null) {
				continue;
			}
			data.setComment(rawAData.getComment());
			int begin = rawAData.getBegin();
			int end = rawAData.getEnd();
			try {
				DocumentSection section = new DocumentSection(document, begin, end);
				document.getADataMap().put(section, data);
				document.setCharacterAttributes(begin, end - begin, ADocument.DEFAULT_SECTION_STYLE, false);
			} catch (BadLocationException e) {
				logger.error("Invalid position for DocumentSection", e);
			}
		}
		document.fireADocumentChanged();

		return document;
	}

	@SuppressWarnings("OverlyBroadThrowsClause")
	private String readFromStream() throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile),
				LegacyHtmlFormat.FILE_ENCODING));
		try {
			boolean finished = false;
			char[] buf = new char[BUFFER_SIZE];
			StringBuilder textBuilder = new StringBuilder(BUFFER_SIZE);
			while (!finished) {
				int bytesRead = reader.read(buf, 0, BUFFER_SIZE);
				if (bytesRead > 0) {
					textBuilder.append(buf, 0, bytesRead);
				} else {
					finished = true;
				}
			}
			return textBuilder.toString();
		} finally {
			reader.close();
		}
	}

	/**
	 * @return карту, сопоставляющую метки свойсвт документа в htm-файле именам свойств в {@link ADocument}.
	 */
	private static Map<String, String> buildPropertyLabel2KeyMap() {
		Map<String, String> label2KeyMap = new HashMap<String, String>();
		label2KeyMap.put(LegacyHtmlFormat.TITLE_PROPERTY_LABEL, Document.TitleProperty);
		label2KeyMap.put(LegacyHtmlFormat.EXPERT_PROPERTY_LABEL, ADocument.EXPERT_PROPERTY);
		label2KeyMap.put(LegacyHtmlFormat.CLIENT_PROPERTY_LABEL, ADocument.CLIENT_PROPERTY);
		label2KeyMap.put(LegacyHtmlFormat.DATE_PROPERTY_LABEL, ADocument.DATE_PROPERTY);
		label2KeyMap.put(LegacyHtmlFormat.COMMENT_PROPERTY_LABEL, ADocument.COMMENT_PROPERTY);
		return Collections.unmodifiableMap(label2KeyMap);
	}

	/**
	 * Разбирает таблицу со свойствами документа, возвращает карту свойств.
	 *
	 * @param text текст документа
	 * @return карта свойств документа
	 */
	private static Map<String, String> parseDocumentProperties(String text) {
		// looking for the table "header"
		int tableStart = text.indexOf("title=\"header\"", 0);
		String leftHeaderText = text.substring(tableStart, text.indexOf("</table", tableStart));

		// looking through columns of table "header" and retreiving text of the left and right columns
		Map<String, String> documentProperties = new HashMap<String, String>();
		Matcher matcher = TABLE_ROW_PATTERN.matcher(leftHeaderText);
		while (matcher.find()) {
			String propertyLabel = matcher.group(TABLE_ROW_CELL_1_GROUP);
			String propertyValue = matcher.group(TABLE_ROW_CELL_2_GROUP);

			//обработка заголовка
			propertyValue = HTML_BR_PATTERN.matcher(propertyValue).replaceAll("\n");

			if (PROPERTY_LABEL_2_KEY_MAP.containsKey(propertyLabel)) {
				String propertyName = PROPERTY_LABEL_2_KEY_MAP.get(propertyLabel);
				documentProperties.put(propertyName, propertyValue);
			} else {
				logger.warn("Unknown document property '{}'='{}'", propertyLabel, propertyValue);
			}
		}
		return documentProperties;
	}

	/**
	 * Разбирает текст основной таблицы протокола, выделяет из нее тексты левой и правой колонок.
	 *
	 * @param text исходный текст документа
	 * @param leftColumnBuilder буфер для текста левой колонки
	 * @param rightColumnBuilder буфер для текста правой колонки
	 */
	private static void splitTextColumns(String text, StringBuilder leftColumnBuilder, StringBuilder rightColumnBuilder) {
		// looking for the table "protocol"
		int searchIndex = text.indexOf("title=\"protocol\"", 0);

		//limiting ourselves only to the Protocol table
		String contentText = text.substring(searchIndex, text.indexOf("</table", searchIndex));

		// looking through columns of table "protocol" and retrieving text of the left and right columns
		Matcher matcher = TABLE_ROW_PATTERN.matcher(contentText);
		while (matcher.find()) {
			String leftText = matcher.group(TABLE_ROW_CELL_1_GROUP);
			leftColumnBuilder.append(leftText);
			leftColumnBuilder.append("<br/><br/>"); //adding breaks because there are no breaks on row boundaries
			
			String rightText = matcher.group(TABLE_ROW_CELL_2_GROUP);
			rightColumnBuilder.append(rightText);
		}
	}

	/**
	 * Разбирает текст левой колонки в таблице протокола. Заносит найденные теги в карту
	 * {@link #rawData}, возвращает текст левой колонки с удаленными тегами.
	 *
	 * @param rawColumnText исходный текст колонки
	 * @return текст без тегов
	 */
	private String parseLeftColumn(String rawColumnText) {
		Matcher tagMatcher = LEFT_COLUMN_TAG_PATTERN.matcher(rawColumnText);
		int offset = 0;
		while (tagMatcher.find()) {
			setProgress(FILE_LOAD_PROGRESS + LEFT_COLUMN_PROGRESS * tagMatcher.start() / rawColumnText.length());
			String openingTagNumber = tagMatcher.group(LEFT_COLUMN_TAG_OPENING_GROUP);
			String closingTagNumber = tagMatcher.group(LEFT_COLUMN_TAG_CLOSING_GROUP);
			if (openingTagNumber != null) {
				// Открывающий тег [n|
				int tagNumber = Integer.parseInt(openingTagNumber);
				logger.trace("parseLeftColumn(): opening tag [{}| found", tagNumber);
				RawAData data = new RawAData();
				data.setBegin(tagMatcher.start() - offset);
				rawData.put(tagNumber, data);
			} else if (closingTagNumber != null) {
				// Закрывающий тег |n]
				int tagNumber = Integer.parseInt(closingTagNumber);
				logger.trace("parseLeftColumn(): closing tag |{}] found", tagNumber);
				RawAData data = rawData.get(tagNumber);
				if (data != null) {
					data.setEnd(tagMatcher.start() - offset);
				} else {
					logger.warn("Closing tag |{}] without corresponding opening tag", tagNumber);
				}
			}
			offset += tagMatcher.end() - tagMatcher.start();
		}
		return tagMatcher.replaceAll("");
	}

	/**
	 * Разбирает текст правой колонки протокола. Найденные пометки и комментарии
	 * собирает в карте {@link #rawData}.
	 *
	 * @param text текст правой колонки
	 */
	private void parseRightColumn(String text) {
		Matcher tagMatcher = RIGHT_COLUMN_TAG_PATTERN.matcher(text);
		while (tagMatcher.find()) {
			// Обрабатываем теги вида:
			// {n:пометки типировщика} комментарий
			setProgress(FILE_LOAD_PROGRESS + LEFT_COLUMN_PROGRESS +
				RIGHT_COLUMN_PROGRESS * tagMatcher.start() / text.length());

			String id = tagMatcher.group(RIGHT_COLUMN_TAG_ID_GROUP);

			int tagNumber;
			try {
				tagNumber = Integer.parseInt(id);
			} catch (NumberFormatException e) {
				logger.warn("Incorrect right column tag format, missing mark number", e);
				continue;
			}

			RawAData data = rawData.get(tagNumber);
			if (data == null) {
				logger.warn("Incorrect mark number in right column tag: {}", tagNumber);
				continue;
			}

			String markup = tagMatcher.group(RIGHT_COLUMN_TAG_MARKUP_GROUP);
			data.setAData(markup);

			String comment = tagMatcher.group(RIGHT_COLUMN_TAG_COMMENT_GROUP).trim();
			// removing last line break which was added when saving
			while (comment.endsWith("\n")) {
				comment = comment.substring(0, comment.length() - 1);
			}
			data.setComment(comment);
		}
	}
}
