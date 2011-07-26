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
	private static final String FILE_ENCODING = "UTF-8";
	private static final Logger logger = LoggerFactory.getLogger(LegacyHtmlReader.class);

	private static final int FILE_LOAD_PROGRESS = 20;
	private static final int LEFT_COLUMN_PROGRESS = 50;
	private static final int RIGHT_COLUMN_PROGRESS = 25;

	private static final String HTML_CELL_OPEN = "<td>";
	private static final String HTML_CELL_CLOSE = "</td>";
	private static final String HTML_ROW_OPEN = "<tr>";
	private static final Pattern HTML_BR_PATTERN = Pattern.compile("<br/>", Pattern.LITERAL);
	private static final Pattern LINEBREAK_PATTERN = Pattern.compile("\n", Pattern.LITERAL);

	private static final int BUFFER_SIZE = 1024;

	private final File sourceFile;

	private final Map<Integer, RawAData> rawData = new HashMap<Integer, RawAData>();

	LegacyHtmlReader(File sourceFile) {
		this.sourceFile = sourceFile;
	}

	@Override
	protected ADocument doInBackground() throws Exception {
		try {
			return readDocument();
		} catch (IOException e) {
			logger.error("IO error while loading document", e);
			throw e;
		} catch (Exception e) {
			logger.error("Non-IO exception while loading document", e);
			throw e;
		}
	}

	private ADocument readDocument() throws IOException {
		setProgress(0);

		ADocument document = new ADocument();
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
		SimpleAttributeSet currentStyle = new SimpleAttributeSet(document.defaultStyle);
		Pattern styleTag = Pattern.compile("</?[bi]>");
		String sourceText = leftColumn;
		Matcher styleMatcher = styleTag.matcher(sourceText);
		int sourcePosition = 0;
		int sourceOffset = 0;
		List<StyledText> styledTextBlocks = new ArrayList<StyledText>();
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
			if (currentTag.equals("<b>")) {
				StyleConstants.setBold(currentStyle, true);
			} else if (currentTag.equals("</b>")) {
				StyleConstants.setBold(currentStyle, false);
			} else if (currentTag.equals("<i>")) {
				StyleConstants.setItalic(currentStyle, true);
			} else if (currentTag.equals("</i>")) {
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

		try {
			for (RawAData rawAData : rawData.values()) {
				AData data = AData.parseAData(rawAData.getAData());
				data.setComment(rawAData.getComment());
				int begin = rawAData.getBegin();
				int end = rawAData.getEnd();
				ASection section = new ASection(begin, end, document.defaultSectionAttributes);
				document.getADataMap().put(section, data);
				document.setCharacterAttributes(begin, end - begin, document.defaultSectionAttributes, false);
			}
			document.fireADocumentChanged();
		} catch (Exception e) {
			logger.error("Error while working with RawData", e);
			cancel(true);
		}

		return document;
	}

	@SuppressWarnings("OverlyBroadThrowsClause")
	private String readFromStream() throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), FILE_ENCODING));
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

	private static Map<String, String> parseDocumentProperties(String text) {
		// looking for the table "header"
		int tableStart = text.indexOf("title=\"header\"", 0);
		String leftHeaderText = text.substring(tableStart, text.indexOf("</table", tableStart));

		// looking through columns of table "header" and retreiving text of the left and right columns
		int searchIndex = leftHeaderText.indexOf(HTML_ROW_OPEN, 0);
		Map<String, String> documentProperties = new HashMap<String, String>();
		while (searchIndex > 0) {
			searchIndex = leftHeaderText.indexOf(HTML_ROW_OPEN, searchIndex);
			String headerResult;
			if (searchIndex > 0) {
				headerResult = findTagContent(leftHeaderText, HTML_CELL_OPEN, HTML_CELL_CLOSE, searchIndex);
			} else {
				break;
			}
			String propertyName;
			if (headerResult != null) {
				propertyName = headerResult.trim();
				searchIndex = leftHeaderText.indexOf(HTML_CELL_CLOSE, searchIndex) + HTML_CELL_CLOSE.length();
			} else {
				break;
			}

			if (searchIndex > 0) {
				headerResult = findTagContent(leftHeaderText, HTML_CELL_OPEN, HTML_CELL_CLOSE, searchIndex);
			} else {
				break;
			}
			String propertyValue;
			if (headerResult != null) {
				propertyValue = headerResult.trim();
				searchIndex = leftHeaderText.indexOf(HTML_CELL_CLOSE, searchIndex) + HTML_CELL_CLOSE.length();
			} else {
				break;
			}

			//обработка заголовка
			propertyValue = HTML_BR_PATTERN.matcher(propertyValue).replaceAll("\n");

			if (ADocument.TitleProperty1.equals(propertyName)) {
				documentProperties.put(Document.TitleProperty, propertyValue);
			} else if (ADocument.ExpertProperty.equals(propertyName)) {
				documentProperties.put(ADocument.ExpertProperty, propertyValue);
			} else if (ADocument.ClientProperty.equals(propertyName)) {
				documentProperties.put(ADocument.ClientProperty, propertyValue);
			} else if (ADocument.DateProperty.equals(propertyName)) {
				documentProperties.put(ADocument.DateProperty, propertyValue);
			} else if (ADocument.CommentProperty.equals(propertyName)) {
				documentProperties.put(ADocument.CommentProperty, propertyValue);
			}
		}
		return documentProperties;
	}

	private static void splitTextColumns(String text, StringBuilder leftColumnBuilder, StringBuilder rightColumnBuilder) {
		// looking for the table "protocol"
		int searchIndex = text.indexOf("title=\"protocol\"", 0);

		//limiting ourselves only to the Protocol table
		text = text.substring(0, text.indexOf("</table", searchIndex));

		// looking through columns of table "protocol" and retrieving text of the left and right columns
		while (searchIndex > 0) {
			searchIndex = text.indexOf(HTML_ROW_OPEN, searchIndex);
			String result;
			if (searchIndex > 0) {
				result = findTagContent(text, HTML_CELL_OPEN, HTML_CELL_CLOSE, searchIndex);
			} else {
				break;
			}
			if (result != null) {
				leftColumnBuilder.append(result);
				leftColumnBuilder.append("<br/><br/>"); //adding breaks because there are no breaks on row boundaries
				searchIndex = text.indexOf(HTML_CELL_CLOSE, searchIndex) + HTML_CELL_CLOSE.length();
			}

			if (searchIndex > 0) {
				result = findTagContent(text, HTML_CELL_OPEN, HTML_CELL_CLOSE, searchIndex);
			} else {
				break;
			}
			if (result != null) {
				rightColumnBuilder.append(result);
				searchIndex = text.indexOf(HTML_CELL_CLOSE, searchIndex) + HTML_CELL_CLOSE.length();
			}
		}
	}

	private String parseLeftColumn(String text) {
		int openingBracketPos = text.indexOf('[');
		int closingBracketPos = text.indexOf(']');
		StringBuilder columnBuilder = new StringBuilder(text);
		while (openingBracketPos >= 0 || closingBracketPos >= 0) {
			setProgress(FILE_LOAD_PROGRESS + LEFT_COLUMN_PROGRESS * openingBracketPos / text.length());
			int handle;
			RawAData data;
			int middleBracketPos = text.indexOf('|');
			if (openingBracketPos >= 0 && openingBracketPos <= closingBracketPos) {
				// Открывающий тег [n|
				handle = Integer.parseInt(text.substring(openingBracketPos + 1, middleBracketPos));
				columnBuilder.delete(openingBracketPos, middleBracketPos + 1);
				data = new RawAData();
				data.setBegin(openingBracketPos);
				rawData.put(handle, data);
			} else if (closingBracketPos >= 0) {
				// Закрывающий тег |n]
				handle = Integer.parseInt(text.substring(middleBracketPos + 1, closingBracketPos));
				columnBuilder.delete(middleBracketPos, closingBracketPos + 1);
				data = rawData.get(handle);
				if (data != null) {
					data.setEnd(middleBracketPos);
				} else {
					logger.warn("Closing tag |{}] without corresponding opening tag", handle);
				}
			}
			text = columnBuilder.toString();
			openingBracketPos = text.indexOf('[');
			closingBracketPos = text.indexOf(']');
		}
		return text;
	}

	private void parseRightColumn(String text) {
		int openingBracePos = text.indexOf('{');
		// processing the right column's content
		while (openingBracePos >= 0) {
			setProgress(FILE_LOAD_PROGRESS + LEFT_COLUMN_PROGRESS +
				RIGHT_COLUMN_PROGRESS * openingBracePos / text.length());
			// Обрабатываем теги вида:
			// {n:пометки типировщика} комментарий
			int middleBracePos = text.indexOf(':', openingBracePos);
			if (middleBracePos < 0) {
				logger.warn("Incorrect right column tag format, missing ':'");
				openingBracePos = text.indexOf('{', openingBracePos + 1);
				continue;
			}
			int handle;
			try {
				handle = Integer.parseInt(text.substring(openingBracePos + 1, middleBracePos));
			} catch (NumberFormatException e) {
				logger.warn("Incorrect right column tag format, missing mark number", e);
				openingBracePos = text.indexOf('{', openingBracePos + 1);
				continue;
			}

			int closingBracePos = text.indexOf('}', openingBracePos);
			openingBracePos = text.indexOf('{', openingBracePos + 1);
			if (closingBracePos < 0) {
				logger.warn("Incorrect right column tag format, missing '}'");
				continue;
			}

			RawAData data = rawData.get(handle);
			if (data == null) {
				logger.warn("Incorrect mark number in right column tag: {}", handle);
				continue;
			}

			String aDataString = text.substring(middleBracePos + 1, closingBracePos);
			data.setAData(aDataString);

			// В комментарий попадает текст от конца текущего тега '}' до начала следующего '{'
			String comment = ' ' + text.substring(closingBracePos + 1,
				openingBracePos >= 0 ? openingBracePos : text.length() - 1).trim();

			// removing last line break which was added when saving
			while (comment.lastIndexOf('\n') == comment.length() - 1) {
				comment = comment.substring(0, comment.length() - 1);
			}
			data.setComment(comment);
		}
	}

	private static String findTagContent(final String text, final String startToken, final String endToken,
			final int fromIndex) {
		int startIndex = text.indexOf(startToken, fromIndex);
		int endIndex = text.indexOf(endToken, startIndex);

		if (startIndex >= 0 && endIndex > 0 && endIndex > startIndex) {
			return text.substring(startIndex + startToken.length(), endIndex);
		}
		return null;
	}
}
