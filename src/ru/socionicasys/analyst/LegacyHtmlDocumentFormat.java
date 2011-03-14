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

	private static enum EventType {
		LINE_BREAK,
		SECTION_START,
		SECTION_END,
		NEW_ROW,
		BOLD_START,
		BOLD_END,
		ITALIC_START,
		ITALIC_END
	}

	private static class DocumentFlowEvent implements Comparable<DocumentFlowEvent> {
		private final EventType type;
		private final int offset;
		private final int sectionNo;
		private final String style;
		private final String comment;

		public DocumentFlowEvent(EventType type, int offset, String style, String comment, int sectionNo) {
			this.offset = offset;
			this.type = type;
			this.style = style;
			this.comment = comment == null ? null : comment.replaceAll("\n", "<br/>");
			this.sectionNo = sectionNo;
		}

		public int getOffset() {
			return offset;
		}

		public EventType getType() {
			return type;
		}

		public String getStyle() {
			return style;
		}

		public String getComment() {
			return comment;
		}

		public int getSectionNo() {
			return sectionNo;
		}

		@Override
		public int compareTo(DocumentFlowEvent o) {
			// Реализация интерфейса java.lang.Comparable<T>
			// Делает возможной сортировку массива из DocumentFlowEvent-ов
			// Сравнение происходит только по позиции (offset)
			return offset - o.getOffset();
		}
	}

	private static class RDStack {
		private final List<String> styleStack;
		private final Map<Integer, Integer> positionMap;

		public RDStack() {
			styleStack = new ArrayList<String>();
			positionMap = new HashMap<Integer, Integer>();
		}

		public void push(final int handle, final String element) {
			styleStack.add(element);
			positionMap.put(handle, styleStack.size() - 1);
		}

		public void delete(final int handle) {
			int position = positionMap.get(handle);
			styleStack.remove(position);
			positionMap.remove(handle);
			for (Map.Entry<Integer, Integer> entry : positionMap.entrySet()) {
				int key = entry.getKey();
				int value = entry.getValue();
				if (value > position) {
					positionMap.put(key, value - 1);
				}
			}
		}

		public String getCurrentStyle() {
			if (isEmpty()) {
				return null;
			}
			return styleStack.get(styleStack.size() - 1);
		}

		public boolean isEmpty() {
			return styleStack.isEmpty();
		}
	}

	public void writeDocument(ADocument document, OutputStream outputStream, IOWorker iow) throws Exception {
		if (outputStream == null) {
			logger.error("Error attempting to save file: FileOutputStream is null");
			return;
		}

		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, encoding));
		final int headerSaveProgress = 20;
		final int writePreparationProgress = 20;
		final int textWriteProgress = 40;
		final int reportWriteProgress = 20;

		iow.firePropertyChange("progress", null, 0);

		//writing the header
		writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"> \n");
		writer.write(String.format("<meta http-equiv=\"Content-Type\" content=\"text/html charset=%s\"/>", encoding));
		writer.write("<html>\n<head>\n");
		writer.write(String.format("<title>%s</title>\n", document.getProperty(Document.TitleProperty)));
		writer.write("	<style>");
		writer.write("			body 	{font-size:14px;color:black}\n");
		writer.write("			h1		{}\n");
		writer.write("			h2		{}\n");
		writer.write("			th		{font-size:18px;font-weight:bold}\n");
		writer.write("			small	{font-size:9px;color:darkgray}\n");
		writer.write("	</style>\n");
		writer.write("</head> \n");
		writer.write("<body> \n");

		//document title
		writer.write(String.format("\n<h1>%s</h1>\n", document.getProperty(Document.TitleProperty)));

		//saved with version
		String comm = (String) document.getProperty(ADocument.CommentProperty);

		//document header
		writer.write("<br/>\n<br/>");
		writer.write("\n <table title=\"header\" border=1 width=\"40%\"> 	\n");
		writer.write("<tr>\n");
		writer.write(String.format("	<td>      %s     </td>\n", ADocument.TitleProperty1));
		writer.write(String.format("	<td>%s	</td>\n", document.getProperty(Document.TitleProperty)));
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write(String.format("	<td>      %s     </td>\n", ADocument.ClientProperty));
		writer.write(String.format("	<td>%s 	</td>\n", document.getProperty(ADocument.ClientProperty)));
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write(String.format("	<td>      %s     </td>\n", ADocument.ExpertProperty));
		writer.write(String.format("	<td>%s	</td>\n", document.getProperty(ADocument.ExpertProperty)));
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write(String.format("	<td>      %s     </td>\n", ADocument.DateProperty));
		writer.write(String.format("	<td>%s </td>\n", document.getProperty(ADocument.DateProperty)));
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write(String.format("	<td>      %s     </td>\n", ADocument.CommentProperty));
		writer.write(String.format("	<td>%s </td>\n", comm));
		writer.write("</tr>\n");
		writer.write("</table >\n");

		//  writing the color legend
		writer.write("<br/>\n<br/>");
		writer.write("<h3> Расшифровка цветовых обозначений: </h3>");
		writer.write("\n <table title=\"legend\" border=0 width=\"40%\"> 	\n");
		writer.write("<tr>\n");
		writer.write("	<td style=\"background-color:#EAEAEA\">Непонятное место</td>\n");
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write("	<td style=\"background-color:#AAEEEE;\">      Маломерность     </td>\n");
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write("	<td style=\"background-color:#AAEEAA;\">      Многомерность     </td>\n");
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write("	<td  style=\"color:#FF0000;\">      		  Фрагмент содержит информацию о знаке     </td>\n");
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write("	<td style=\"background-color:#FFFFCC;\">      Фрагмент содержит информацию о ментале или витале     </td>\n");
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write("	<td style=\"text-decoration:underline\">      Прочий выделенный фрагмент анализа     </td>\n");
		writer.write("</tr>\n");
		writer.write("</table >\n");

		iow.firePropertyChange("progress", null, headerSaveProgress);

		//document content
		writer.write("<br/>\n");
		writer.write("\n<h2>  АНАЛИЗ </h2>\n");
		writer.write("\n <table title=\"protocol\" border=2 width=\"100%\"> 	\n");
		writer.write("<tr>\n");
		writer.write("	<th width=\"60%\"> ВОПРОСЫ И ОТВЕТЫ </th>\n");
		writer.write("	<th width=\"40%\"> АНАЛИЗ ЭКСПЕРТА</th>\n");
		writer.write("</tr>\n");
		writer.write("<tr>\n");
		writer.write("	<td>");

		// PREPARING
		Vector<DocumentFlowEvent> flowEvents = new Vector<DocumentFlowEvent>();
		ArrayList<Position> lineBreaks = new ArrayList<Position>();

		for (int i = 0; i < document.getLength(); i++) {
			if (document.getText(i, 1).equals("\n")) {
				lineBreaks.add(document.createPosition(i));
			}
		}

		ArrayList<ASection> sections = new ArrayList<ASection>(document.getADataMap().keySet());
		Collections.sort(sections);
		for (int i = 0; i < sections.size(); i++) {
			ASection section = sections.get(i);
			AData data = document.getADataMap().get(section);
			flowEvents.add(new DocumentFlowEvent(
				EventType.SECTION_START,
				section.getStartOffset(),
				getHTMLStyleForAData(data),
				String.format("{%d: %s} %s\n", i + 1, data.toString(), data.getComment()),
				i + 1)
			);
			flowEvents.add(new DocumentFlowEvent(
				EventType.SECTION_END,
				section.getEndOffset(),
				getHTMLStyleForAData(data),
				data.getComment(),
				i + 1)
			);
		}
		Element rootElem = document.getDefaultRootElement();
		SimpleAttributeSet boldAttribute = new SimpleAttributeSet();
		StyleConstants.setBold(boldAttribute, true);
		SimpleAttributeSet italicAttribute = new SimpleAttributeSet();
		StyleConstants.setItalic(italicAttribute, true);
		for (int parIndex = 0; parIndex < rootElem.getElementCount(); parIndex++) {
			Element parElem = rootElem.getElement(parIndex);
			for (int i = 0; i < parElem.getElementCount(); i++) {
				Element e = parElem.getElement(i);
				int elemStart = e.getStartOffset();
				int elemEnd = e.getEndOffset();
				AttributeSet attrs = e.getAttributes();
				if (attrs.containsAttributes(boldAttribute)) {
					flowEvents.add(new DocumentFlowEvent(EventType.BOLD_START,
						elemStart, null, null, 0));
					flowEvents.add(new DocumentFlowEvent(EventType.BOLD_END,
						elemEnd, null, null, 0));
				}
				if (attrs.containsAttributes(italicAttribute)) {
					flowEvents.add(new DocumentFlowEvent(EventType.ITALIC_START,
						elemStart, null, null, 0));
					flowEvents.add(new DocumentFlowEvent(EventType.ITALIC_END,
						elemEnd, null, null, 0));
				}
			}
		}
		for (Position position : lineBreaks) {
			int lb = position.getOffset();
			boolean replaceBreak = false;
			if (!flowEvents.isEmpty()) {
				DocumentFlowEvent prevEvent = flowEvents.lastElement();
				if (prevEvent.getType() == EventType.LINE_BREAK &&
					prevEvent.getOffset() == lb - 1) {
					// Заменяем два идущих подряд LINE_BREAK на NEW_ROW
					replaceBreak = true;
				}
			}
			if (replaceBreak) {
				flowEvents.set(flowEvents.size() - 1,
					new DocumentFlowEvent(EventType.NEW_ROW,
						lb - 1, null, null, 0));
			} else {
				flowEvents.add(new DocumentFlowEvent(
					EventType.LINE_BREAK, lb, null, null, 0));
			}
		}
		Collections.sort(flowEvents);

		if (!flowEvents.isEmpty() && (flowEvents.lastElement().getType() != EventType.NEW_ROW)) {
			flowEvents.add(new DocumentFlowEvent(
				EventType.NEW_ROW, document.getEndPosition().getOffset() - 1,
				null, null, 0));
		}

		iow.firePropertyChange("progress", null, headerSaveProgress + writePreparationProgress);

		// write contents
		RDStack stack = new RDStack();
		if (flowEvents != null && !flowEvents.isEmpty()) {
			int pos1 = 0;
			StringBuilder analysis = new StringBuilder();
			for (int z = 0; z < flowEvents.size(); z++) {
				DocumentFlowEvent event = flowEvents.get(z);
				EventType eventType = event.getType();
				int pos0 = pos1;
				pos1 = event.getOffset();

				iow.firePropertyChange("progress", null, headerSaveProgress + writePreparationProgress +
					textWriteProgress * z / flowEvents.size());

				//writing text
				writer.write(document.getText(pos0, pos1 - pos0));

				// writing text remainder from last event to the end of the document
				if (z == flowEvents.size() - 1) {
					int finish = document.getLength();
					if (finish > pos1) {
						writer.write(document.getText(pos1, finish - pos1));
					}
					eventType = EventType.NEW_ROW;
				}

				//analyzing event and generating  mark-up
				int sectionNo;
				switch (eventType) {
				case SECTION_START:
					sectionNo = event.getSectionNo();
					if (!stack.isEmpty()) {
						writer.write(" </span>");
					}
					writer.write(String.format("<small>[%d|</small><span style=%s>", sectionNo, event.getStyle()));
					stack.push(sectionNo, event.getStyle());
					analysis.append(event.getComment());
					break;

				case SECTION_END:
					if (!stack.isEmpty()) {
						sectionNo = event.getSectionNo();
						writer.write(String.format("</span><small>|%d]</small>", sectionNo));
						stack.delete(sectionNo);
						if (!stack.isEmpty()) {
							writer.write(String.format("<span style=%s>", stack.getCurrentStyle()));
						}
					}
					break;
				
				case BOLD_START:
					writer.write("<b>");
					break;

				case BOLD_END:
					writer.write("</b>");
					break;

				case ITALIC_START:
					writer.write("<i>");
					break;

				case ITALIC_END:
					writer.write("</i>");
					break;

				case NEW_ROW:
					if (!stack.isEmpty()) {
						writer.write("</span>");
					}
					writer.write(String.format("</td>\n<td>%s</td>", analysis));
					analysis = new StringBuilder();
					if (z != flowEvents.size() - 1) {
						writer.write("\n</tr>\n<tr>\n<td>");
					}
					if (!stack.isEmpty()) {
						writer.write(String.format("<span style=%s>", stack.getCurrentStyle()));
					}
					break;

				case LINE_BREAK:
					writer.write("<br/>");
					break;
				}
			}
		}
		// если в документе нет разметки - просто пишем текст в левый столбец таблицы
		else {
			writer.write(document.getText(0, document.getLength()));
			writer.write("</td><td></td>");
		}

		writer.write("</tr>\n</table>\n");
		//if not generating report
		AnalystWindow an = iow.getProgressWindow().getAnalyst();

		iow.firePropertyChange("progress", null, headerSaveProgress + writePreparationProgress +
			textWriteProgress + reportWriteProgress);

		// if generating report
		if (an.getGenerateReport()) {
			writer.write("<br/>");
			writer.write("<h1> Определение ТИМа </h1>");
			writer.write("<br/>");
			writer.write(an.getNavigeTree().getReport());
			writer.write(an.getAnalysisTree().getReport());
		}

		writer.write("<br/>");
		writer.write(String.format("Протокол определения ТИМа создан программой \"Информационный анализ\", верисия: %s <br/>",
			AnalystWindow.version));
		writer.write("© Школа системной соционики, Киев.<br/>");
		writer.write("http://www.socionicasys.ru\n");
		writer.write("</body>\n</html>\n");

		writer.flush();
		writer.close();

		iow.firePropertyChange("progress", null, 100);
	}

	private static String getHTMLStyleForAData(AData data) {
		if (data.getAspect().equals(AData.DOUBT)) {
			return "background-color:#EAEAEA";
		}
		StringBuilder res = new StringBuilder("\"");
		boolean unstyled = true;
		String dimension = data.getDimension();
		if (Arrays.asList(AData.D1, AData.D2, AData.ODNOMERNOST, AData.MALOMERNOST).contains(dimension)) {
			res.append("background-color:#AAEEEE;");
			unstyled = false;
		} else if (Arrays.asList(AData.D3, AData.D4, AData.MNOGOMERNOST).contains(dimension)) {
			// противный зеленый
			res.append("background-color:#AAEEAA;");
			unstyled = false;
		}
		if (data.getSign() != null) {
			res.append("color:#FF0000;");
			unstyled = false;
		}
		if (data.getMV() != null) {
			res.append("background-color:#FFFFCC;");
			unstyled = false;
		}
		//Если не задан другой стиль, то будет этот стиль
		if (unstyled) {
			res.append("text-decoration:underline");
		}
		res.append('\"');
		return res.toString();
	}

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
