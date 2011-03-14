package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.*;

public class LegacyHtmlDocumentFormat {
	private static final String encoding = "UTF-8";
	private static final Logger logger = LoggerFactory.getLogger(LegacyHtmlDocumentFormat.class);

	public void readDocument(ADocument document, InputStream inputStream, boolean append, IOWorker iow) throws Exception {
		final int fileLoadProgress = 20;
		final int leftColumnParseProgress = 50;
		final int rightColumnParseProgress = 25;

		String allText;
		Reader isr = new BufferedReader(new InputStreamReader(inputStream, encoding));
		try {
			// reading the file
			iow.firePropertyChange("progress", null, 0);
			int length = inputStream.available();
			char[] buf = new char[length];
			boolean finished = false;
			StringBuilder textBuilder = new StringBuilder();
			while (!finished) {
				int bytesRead = isr.read(buf, 0, length);
				if (bytesRead > 0) {
					textBuilder.append(buf, 0, bytesRead);
				} else {
					finished = true;
				}
			}
			iow.firePropertyChange("progress", null, fileLoadProgress);
			allText = textBuilder.toString();
		} finally {
			inputStream.close();
			isr.close();
		}

		// looking for the table "header"
		int searchIndex = allText.indexOf("title=\"header\"", 0);
		String leftHeaderText = allText.substring(searchIndex, allText.indexOf("</table", searchIndex));

		// looking through columns of table "header" and retreiving text of the left and right columns
		searchIndex = leftHeaderText.indexOf("<tr>", 0);
		String colStartToken = "<td>";
		String colEndToken = "</td>";
		String leftHeaderColumn = null;
		String rightHeaderColumn = null;
		while (searchIndex > 0) {
			searchIndex = leftHeaderText.indexOf("<tr>", searchIndex);
			String headerResult;
			if (searchIndex > 0) {
				headerResult = findTagContent(leftHeaderText, colStartToken, colEndToken, searchIndex);
			} else {
				break;
			}
			if (headerResult != null) {
				leftHeaderColumn = headerResult.trim();
				searchIndex = leftHeaderText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			if (searchIndex > 0) {
				headerResult = findTagContent(leftHeaderText, colStartToken, colEndToken, searchIndex);
			} else {
				break;
			}
			if (headerResult != null) {
				rightHeaderColumn = headerResult.trim();
				searchIndex = leftHeaderText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			//обработка заголовка
			rightHeaderColumn = rightHeaderColumn.replaceAll("<br/>", "\n");

			if (!append) {
				if (leftHeaderColumn.equals(ADocument.TitleProperty1)) {
					iow.firePropertyChange("DocumentProperty", Document.TitleProperty, rightHeaderColumn);
				}
				if (leftHeaderColumn.equals(ADocument.ExpertProperty)) {
					iow.firePropertyChange("DocumentProperty", ADocument.ExpertProperty, rightHeaderColumn);
				}
				if (leftHeaderColumn.equals(ADocument.ClientProperty)) {
					iow.firePropertyChange("DocumentProperty", ADocument.ClientProperty, rightHeaderColumn);
				}
				if (leftHeaderColumn.equals(ADocument.DateProperty)) {
					iow.firePropertyChange("DocumentProperty", ADocument.DateProperty, rightHeaderColumn);
				}
				if (leftHeaderColumn.equals(ADocument.CommentProperty)) {
					iow.firePropertyChange("DocumentProperty", ADocument.CommentProperty, rightHeaderColumn);
				}
			} else {
				if (leftHeaderColumn.equals(ADocument.ExpertProperty)) {
					String expert = (String) document.getDocumentProperties().get(ADocument.ExpertProperty);
					if (!expert.contains(rightHeaderColumn)) {
						expert += "; " + rightHeaderColumn;
					}
					iow.firePropertyChange("DocumentProperty", ADocument.ExpertProperty, expert);
				}
			}
		}

		// looking for the table "protocol"
		searchIndex = allText.indexOf("title=\"protocol\"", 0);

		//limiting ourselves only to the Protocol table
		int tableProtocolEndIndex = allText.indexOf("</table", searchIndex);
		allText = allText.substring(0, tableProtocolEndIndex);

		// looking through columns of table "protocol" and retreiving text of the left and right columns
		StringBuilder leftColumnBuilder = new StringBuilder();
		StringBuilder rightColumnBuilder = new StringBuilder();
		while (searchIndex > 0) {
			searchIndex = allText.indexOf("<tr>", searchIndex);
			String result;
			if (searchIndex > 0) {
				result = findTagContent(allText, colStartToken, colEndToken, searchIndex);
			} else {
				break;
			}
			if (result != null) {
				leftColumnBuilder.append(result);
				leftColumnBuilder.append("<br/><br/>"); //adding breaks because there are no breaks on row boundaries
				searchIndex = allText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			if (searchIndex > 0) {
				result = findTagContent(allText, colStartToken, colEndToken, searchIndex);
			} else {
				break;
			}
			if (result != null) {
				rightColumnBuilder.append(result);
				searchIndex = allText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}
		}

		String leftColumn = leftColumnBuilder.toString().replaceAll("\n", "");
		leftColumn = leftColumn.replace("<br/>", "\n");
		leftColumn = leftColumn.trim();

		String rightColumn = rightColumnBuilder.toString().replaceAll("\n", "");
		rightColumn = rightColumn.replace("<br/>", "\n");

		// Убираем все лишние теги
		leftColumn = removeTag(leftColumn, "<span", ">");
		leftColumn = removeTag(leftColumn, "</span", ">");
		leftColumn = removeTag(leftColumn, "<small", ">");
		leftColumn = removeTag(leftColumn, "</small", ">");

		HashMap<Integer, RawAData> rawData = new HashMap<Integer, RawAData>();

		int posBeg = leftColumn.indexOf('[');
		iow.firePropertyChange("progress", null, fileLoadProgress / 2);
		// processing the left column's content
		while (leftColumn.indexOf('[', 0) >= 0 || leftColumn.indexOf(']', 0) >= 0) {
			iow.firePropertyChange("progress", null, fileLoadProgress +
				leftColumnParseProgress * posBeg / leftColumn.length());
			int handle;
			RawAData data;
			String handleNo;
			//if we met the opening tag
			if ((leftColumn.indexOf('[', 0) >= 0) && (leftColumn.indexOf('[', 0) <= leftColumn.indexOf(']', 0))) {
				posBeg = leftColumn.indexOf('[');
				handleNo = findTagContent(leftColumn, "[", "|", 0);
				handle = Integer.parseInt(handleNo);
				leftColumn = leftColumn.replace(findTag(leftColumn, "[", "|", 0), "");
				data = new RawAData();
				data.setBegin(posBeg);
				rawData.put(handle, data);
				//if we met the closing tag
			} else if (leftColumn.indexOf(']', 0) >= 0) {
				int posEnd = leftColumn.indexOf('|');
				handleNo = findTagContent(leftColumn, "|", "]", 0);
				handle = Integer.parseInt(handleNo);
				leftColumn = leftColumn.replace(findTag(leftColumn, "|", "]", 0), "");
				data = rawData.get(handle);
				if (data != null) {
					data.setEnd(posEnd);
				}
			}
		}

		iow.firePropertyChange("progress", null, fileLoadProgress + leftColumnParseProgress);

		posBeg = rightColumn.indexOf('{');

		// processing the right column's content
		while (posBeg >= 0) {
			iow.firePropertyChange("progress", null, fileLoadProgress + leftColumnParseProgress +
				rightColumnParseProgress * (posBeg / rightColumn.length()));
			String handleNo = findTagContent(rightColumn, "{", ":", posBeg);
			int handle = Integer.parseInt(handleNo);
			RawAData data = rawData.get(handle);
			if (data != null) {
				String aDataString = findTagContent(rightColumn, ":", "}", posBeg);
				data.setAData(aDataString);
				int posEnd = rightColumn.indexOf('{', posBeg + 1);
				if (posEnd < 0) {
					posEnd = rightColumn.length() - 1;
				}
				int posBeg1 = rightColumn.indexOf('}', posBeg) + 1;
				String comment = null;
				if (posBeg1 > 0) {
					comment = rightColumn.substring(posBeg1, posEnd).trim();
				}
				comment = ' ' + comment;
				//removing last line brake which was added when saving
				while (comment != null && (comment.lastIndexOf('\n') == (comment.length() - 1))) {
					comment = comment.substring(0, comment.length() - 1);
				}
				if (comment == null) {
					comment = "";
				}
				data.setComment(comment);
			}
			posBeg = rightColumn.indexOf('{', posBeg + 1);
		}
		iow.firePropertyChange("progress", null, fileLoadProgress + leftColumnParseProgress +
			rightColumnParseProgress);

		// Обрабатываем стили в уже прочитанном тексте
		SimpleAttributeSet currentStyle = new SimpleAttributeSet(document.defaultStyle);
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
		iow.firePropertyChange("AppendStyledText", null, styledTextBlocks);

		/// adding plain text to the document
		iow.firePropertyChange("RawData", null, rawData);
		iow.firePropertyChange("progress", null, 100);
	}

	private static String removeTag(final String source, final String startToken, final String endToken) {
		String buffer = source;
		String tag = findTag(buffer, startToken, endToken, 0);
		while (tag != null) {
			buffer = buffer.replace(tag, "");
			tag = findTag(buffer, startToken, endToken, 0);
		}
		return buffer;
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

	private static String findTag(final String text, final String startToken, final String endToken,
			final int fromIndex) {
		int startIndex = text.indexOf(startToken, fromIndex);
		int endIndex = text.indexOf(endToken, startIndex);

		if (startIndex >= 0 && endIndex > 0 && endIndex > startIndex) {
			return text.substring(startIndex, endIndex + endToken.length());
		}
		return null;
	}
}
