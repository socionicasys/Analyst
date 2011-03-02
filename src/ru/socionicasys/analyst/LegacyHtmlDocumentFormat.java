package ru.socionicasys.analyst;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.*;

public class LegacyHtmlDocumentFormat {
	private static final String encoding = "UTF-8";

	private class DocumentFlowEvent implements Comparable<DocumentFlowEvent> {
		protected int type, offset, sectionNo;
		protected String style;
		protected String comment;
		public static final int LINE_BREAK = 1;
		public static final int SECTION_START = 2;
		public static final int SECTION_END = 3;
		public static final int NEW_ROW = 4;
		public static final int BOLD_START = 5;
		public static final int BOLD_END = 6;
		public static final int ITALIC_START = 7;
		public static final int ITALIC_END = 8;

		public DocumentFlowEvent(int type, int offset, String style, String comment, int sectionNo) {
			this.offset = offset;
			this.type = type;
			this.style = style;
			if (comment != null) comment = comment.replaceAll("\n", "<br/>");
			this.comment = comment;
			this.sectionNo = sectionNo;
		}

		public int getOffset() {
			return offset;
		}

		public int getType() {
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
			return this.offset - o.offset;
		}
	}

	private class RDStack extends Vector<String> {
		private Hashtable<Integer, Integer> positionMap;

		public RDStack() {
			super();
			positionMap = new Hashtable<Integer, Integer>();
		}

		public void push(int handle, String element) {
			add(element);
			int position = size() - 1;
			positionMap.put(new Integer(handle), new Integer(position));
		}

		public int getCurrentSectionNo() {
			if (isEmpty()) return -1;
			int position = size() - 1;
			Enumeration en = positionMap.keys();
			Integer nextKey = null;
			Integer nextValue = null;
			while (en.hasMoreElements()) {
				nextKey = (Integer) en.nextElement();
				nextValue = positionMap.get(nextKey);
				int v = nextValue.intValue();
				if (v == position) {
					return nextKey.intValue();
				}
			}
			return 0;
		}

		public void delete(int handle) {
			Integer h = new Integer(handle);
			int position = positionMap.get(h).intValue();
			this.removeElementAt(position);
			positionMap.remove(h);
			Enumeration en = positionMap.keys();
			Integer nextKey = null;
			Integer nextValue = null;
			while (en.hasMoreElements()) {
				nextKey = (Integer) en.nextElement();
				nextValue = positionMap.get(nextKey);
				int v = nextValue.intValue();
				if (v > position) {
					positionMap.remove(nextKey);
					positionMap.put(nextKey, new Integer(v - 1));
				}
			}
		}

		public String getCurrentStyle() {
			if (isEmpty()) return null;
			return get(size() - 1);
		}
	}

	public void writeDocument(ADocument document, FileOutputStream fos, IOWorker iow) throws Exception {
		int headerSaveProgress = 20;
		int writePreparationProgress = 20;
		int textWriteProgress = 40;
		int reportWriteProgress = 20;

		iow.firePropertyChange("progress", null, 0);

		if (fos == null) {
			System.out.println("Error attempting to save file: FileOutputStream is null");
			return;
		}

		//writing the header
		String text = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"> \n";
		text += "<meta http-equiv=\"Content-Type\" content=\"text/html charset=" + encoding + "\"/>";
		text += "<html> \n<head> \n<title> \n" + document.getProperty(ADocument.TitleProperty) + " \n</title> \n" +
			"	<style>" +
			"			body 	{font-size:14px;color:black}\n" +
			"			h1		{}\n" +
			"			h2		{}\n" +
			"			th		{font-size:18px;font-weight:bold}\n" +
			"			small	{font-size:9px;color:darkgray}\n" +
			"" +
			"	</style>\n" +
			"</head> \n" +
			"<body> \n";

		fos.write(text.getBytes(encoding));

		//document title
		text = "\n<h1>" + document.getProperty(ADocument.TitleProperty) + "</h1>\n";

		//saved with version
		String comm = (String) document.getProperty(ADocument.CommentProperty);

		//document header
		text += "<br/>\n<br/>";
		text += "\n <table title=\"header\" border=1 width=\"40%\"> 	" + "\n" +
			"<tr>" + "\n" +
			"	<td>      " + ADocument.TitleProperty1 + "     </td>" + "\n" +
			"	<td>" + document.getProperty(ADocument.TitleProperty) + "	</td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td>      " + ADocument.ClientProperty + "     </td>" + "\n" +
			"	<td>" + document.getProperty(ADocument.ClientProperty) + " 	</td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td>      " + ADocument.ExpertProperty + "     </td>" + "\n" +
			"	<td>" + document.getProperty(ADocument.ExpertProperty) + "	</td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td>      " + ADocument.DateProperty + "     </td>" + "\n" +
			"	<td>" + document.getProperty(ADocument.DateProperty) + " </td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td>      " + ADocument.CommentProperty + "     </td>" + "\n" +
			"	<td>" + comm + " </td>" + "\n" +
			"</tr>" + "\n" +
			"</table >" + "\n";

		//  writing the color legend
		text += "<br/>\n<br/>";
		text += "<h3> Расшифровка цветовых обозначений: </h3>";

		text += "\n <table title=\"legend\" border=0 width=\"40%\"> 	" + "\n" +
			"<tr>" + "\n" +
			"	<td style=\"background-color:#EAEAEA\">" + "Непонятное место" + "</td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td style=\"background-color:#AAEEEE;\">      " + "Маломерность" + "     </td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td style=\"background-color:#AAEEAA;\">      " + "Многомерность" + "     </td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td  style=\"color:#FF0000;\">      		  " + "Фрагмент содержит информацию о знаке" + "     </td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td style=\"background-color:#FFFFCC;\">      " + "Фрагмент содержит информацию о ментале или витале" + "     </td>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td style=\"text-decoration:underline\">      " + "Прочий выделенный фрагмент анализа" + "     </td>" + "\n" +
			"</tr>" + "\n" +
			"</table >" + "\n";

		fos.write(text.getBytes(encoding));
		iow.firePropertyChange("progress", null, headerSaveProgress);

		//document content
		text = "<br/>\n";
		text += "\n<h2>  АНАЛИЗ </h2>\n";
		text += "\n <table title=\"protocol\" border=2 width=\"100%\"> 	" + "\n" +
			"<tr>" + "\n" +
			"	<th width=\"60%\"> ВОПРОСЫ И ОТВЕТЫ </th>" + "\n" +
			"	<th width=\"40%\"> АНАЛИЗ ЭКСПЕРТА</th>" + "\n" +
			"</tr>" + "\n" +
			"<tr>" + "\n" +
			"	<td>"
		;

		fos.write(text.getBytes(encoding));
		text = "";

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
				DocumentFlowEvent.SECTION_START,
				section.getStartOffset(),
				getHTMLStyleForAData(data),
				String.format("{%d: %s} %s\n", i + 1, data.toString(), data.getComment()),
				i + 1)
			);
			flowEvents.add(new DocumentFlowEvent(
				DocumentFlowEvent.SECTION_END,
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
					flowEvents.add(new DocumentFlowEvent(DocumentFlowEvent.BOLD_START,
						elemStart, null, null, 0));
					flowEvents.add(new DocumentFlowEvent(DocumentFlowEvent.BOLD_END,
						elemEnd, null, null, 0));
				}
				if (attrs.containsAttributes(italicAttribute)) {
					flowEvents.add(new DocumentFlowEvent(DocumentFlowEvent.ITALIC_START,
						elemStart, null, null, 0));
					flowEvents.add(new DocumentFlowEvent(DocumentFlowEvent.ITALIC_END,
						elemEnd, null, null, 0));
				}
			}
		}
		for (Position position : lineBreaks) {
			int lb = position.getOffset();
			boolean replaceBreak = false;
			if (!flowEvents.isEmpty()) {
				DocumentFlowEvent prevEvent = flowEvents.lastElement();
				if (prevEvent.type == DocumentFlowEvent.LINE_BREAK &&
					prevEvent.offset == lb - 1) {
					// Заменяем два идущих подряд LINE_BREAK на NEW_ROW
					replaceBreak = true;
				}
			}
			if (replaceBreak) {
				flowEvents.set(flowEvents.size() - 1,
					new DocumentFlowEvent(DocumentFlowEvent.NEW_ROW,
						lb - 1, null, null, 0));
			} else {
				flowEvents.add(new DocumentFlowEvent(
					DocumentFlowEvent.LINE_BREAK, lb, null, null, 0));
			}
		}
		Collections.sort(flowEvents);

		if (!flowEvents.isEmpty() && (flowEvents.lastElement().getType() != DocumentFlowEvent.NEW_ROW)) {
			flowEvents.add(new DocumentFlowEvent(
				DocumentFlowEvent.NEW_ROW, document.getEndPosition().getOffset() - 1,
				null, null, 0));
		}

		iow.firePropertyChange("progress", null, headerSaveProgress + writePreparationProgress);

		// write contents
		int pos0 = 0;
		int pos1 = 0;
		int k = 0;
		String analisys = "";
		DocumentFlowEvent event = null;
		int eventType = -1;
		RDStack stack = new RDStack();

		if (flowEvents != null && !flowEvents.isEmpty()) {
			for (int z = 0; z < flowEvents.size(); z++) {
				event = flowEvents.get(z);
				pos0 = pos1;
				pos1 = event.getOffset();
				eventType = event.getType();

				iow.firePropertyChange("progress", null, headerSaveProgress + writePreparationProgress +
					textWriteProgress * z / flowEvents.size());

				//writing text
				String t = document.getText(pos0, pos1 - pos0);
				text += t;

				// writing text remainder from last event to the end of the document
				if (z == flowEvents.size() - 1) {
					int finish = document.getLength();
					if (finish > pos1) {
						text += document.getText(pos1, finish - pos1);
					}
				}

				//analyzing event and generating  mark-up
				if (eventType == DocumentFlowEvent.SECTION_START) {
					k = event.getSectionNo();
					if (!stack.isEmpty()) {
						text += " </span>";
					}
					text += "<small>[" + k + "|</small>";
					text += "<span style=" + event.getStyle() + ">";
					stack.push(k, event.getStyle());
					analisys += event.getComment();
				} else if (eventType == DocumentFlowEvent.SECTION_END) {
					k = event.getSectionNo();
					if (!stack.isEmpty()) {
						text += "</span>";
						text += "<small>|" + k + "]</small>";
						stack.delete(k);
						if (!stack.isEmpty()) {
							text += "<span style=" + stack.getCurrentStyle() + ">";
						}
					}
				} else if (eventType == DocumentFlowEvent.BOLD_START) {
					text += "<b>";
				} else if (eventType == DocumentFlowEvent.BOLD_END) {
					text += "</b>";
				} else if (eventType == DocumentFlowEvent.ITALIC_START) {
					text += "<i>";
				} else if (eventType == DocumentFlowEvent.ITALIC_END) {
					text += "</i>";
				} else if (eventType == DocumentFlowEvent.NEW_ROW || z == flowEvents.size() - 1) {
					if (!stack.isEmpty()) {
						text += "</span>";
					}
					text += "</td>\n";
					text += "<td>" + analisys;
					text += "</td>";
					analisys = "";
					if (z != flowEvents.size() - 1) {
						text += "\n</tr>\n<tr>\n<td>";
					}
					if (!stack.isEmpty()) {
						text += "<span style=" + stack.getCurrentStyle() + ">";
					}
				} else if (eventType == DocumentFlowEvent.LINE_BREAK) {
					text += "<br/>";
				}
			}
		}
		// если в документе нет разметки - просто пишем текст в левый столбец таблицы
		else {
			text += document.getText(0, document.getLength());
			text += "</td><td></td>";
		}

		text +=	"</tr>" + "\n" + "</table>" + "\n";
		//if not generating report
		AnalystWindow an = iow.getProgressWindow().getAnalyst();

		iow.firePropertyChange("progress", null, headerSaveProgress + writePreparationProgress +
			textWriteProgress + reportWriteProgress);

		// if generating report
		if (an.getGenerateReport()) {
			text += "<br/>" +
				"<h1> Определение ТИМа </h1>" +
				"<br/>";

			text += an.getNavigeTree().getReport();
			text += an.getAnalysisTree().getReport();
		}

		text +=
			"<br/>" +
				"Протокол определения ТИМа создан программой \"Информационный анализ\", верисия: " + AnalystWindow.version + " <br/>" +
				"© Школа системной соционики, Киев.<br/>" +
				"http://www.socionicasys.ru\n";

		text +=
			"</body >" + "\n" +
				"</html >" + "\n";

		fos.write(text.getBytes(encoding));

		fos.flush();
		fos.close();

		iow.firePropertyChange("progress", null, 100);
	}

	private String getHTMLStyleForAData(AData data) {
		if (data.getAspect().equals(AData.DOUBT)) {
			return "background-color:#EAEAEA";
		}
		String res = "\"";
		String dim = data.getDimension();
		String mv = data.getMV();
		String sign = data.getSign();
		if (dim != null &&
			(dim.equals(AData.D1) ||
				dim.equals(AData.D2) ||
				dim.equals(AData.ODNOMERNOST) ||
				dim.equals(AData.MALOMERNOST))) {
			res += "background-color:#AAEEEE;";
		} else if (dim != null &&
			(dim.equals(AData.D3) ||
				dim.equals(AData.D4) ||
				dim.equals(AData.MNOGOMERNOST))) {
			// противный зеленый
			res += "background-color:#AAEEAA;";
		}
		if (sign != null) {
			res += "color:#FF0000;";
		}
		if (mv != null) {
			res += "background-color:#FFFFCC;";
		}
		//Если не задан другой стиль, то будет этот стиль
		if (res.equals("\"")) {
			res += "text-decoration:underline";
		}
		res += "\"";
		return res;
	}

	public void readDocument(ADocument document, FileInputStream fis, boolean append, IOWorker iow) throws Exception {
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName(encoding));
		String leftColumn = "";
		String rightColumn = "";
		String allText = "";

		int fileLoadProgress = 20;
		int leftColumnParseProgress = 50;
		int rightColumnParseProgress = 25;
		int textAddProgress = 5;
		int aDataCteationProgress = 0;

		int appendOffset = 0;
		if (append) appendOffset = document.getLength();

		final class SectorData {
			int handle;
			int startPos;
			int endPos;
			String dataString;

			public SectorData(int handle, int startPos, int endPos) {
				this.handle = handle;
				this.startPos = startPos;
				this.endPos = endPos;
			}

			public SectorData(int handle, int startPos, int endPos, String dataString) {
				this.handle = handle;
				this.startPos = startPos;
				this.endPos = endPos;
				this.dataString = dataString;
			}

			public void setDataString(String dataString) {
				this.dataString = dataString;
			}

			public int getHandle() {
				return handle;
			}

			public int getstartPos() {
				return startPos;
			}

			public int getendPos() {
				return endPos;
			}
		}// class SectorData

		Vector<SectorData> sectorData = new Vector<SectorData>();
		boolean finished = false;
		// reading the file

		int offset = 0;
		int length = fis.available();
		char[] buf = new char[length];
		int bytesRead;

		iow.firePropertyChange("progress", null, new Integer(fileLoadProgress / 2));

		while (!finished) {

			bytesRead = isr.read(buf, 0, length);
			if (bytesRead > 0) allText += new String(buf, 0, bytesRead);
				//offset += bytesRead;
			else {
				finished = true;
				isr.close();
				fis.close();
			}
		}
		iow.firePropertyChange("progress", null, new Integer(fileLoadProgress));

		//  PARSING THE INPUT DATA
		finished = false;
		offset = 0;

		// looking for the table "header"
		int searchIndex = allText.indexOf("title=\"header\"", 0);

		String colStartToken = "<td>";
		String colEndToken = "</td>";
		String result = null;
		String headerResult = null, leftHeaderColumn = null, rightHeaderColumn = null;

		String leftHeaderText = allText.substring(searchIndex, allText.indexOf("</table", searchIndex));

		// looking through columns of table "header" and retreiving text of the left and right columns

		Dictionary<Object, Object> properties = document.getDocumentProperties();

		searchIndex = leftHeaderText.indexOf("<tr>", 0);
		while (searchIndex > 0) {

			searchIndex = leftHeaderText.indexOf("<tr>", searchIndex);
			if (searchIndex > 0) headerResult = findTagContent(leftHeaderText, colStartToken, colEndToken, searchIndex);
			else break;
			if (headerResult != null) {
				leftHeaderColumn = headerResult.trim();
				searchIndex = leftHeaderText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			if (searchIndex > 0) headerResult = findTagContent(leftHeaderText, colStartToken, colEndToken, searchIndex);
			else break;
			if (headerResult != null) {
				rightHeaderColumn = headerResult.trim();
				searchIndex = leftHeaderText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			//обработка заголовка
			leftHeaderColumn.replaceAll("\t", "");
			rightHeaderColumn.replaceAll("\t", "");
			rightHeaderColumn = rightHeaderColumn.replaceAll("<br/>", "\n");

			if (!append) {
				if (leftHeaderColumn.equals(ADocument.TitleProperty1)) {
					iow.firePropertyChange("DocumentProperty", ADocument.TitleProperty, new String(rightHeaderColumn));
				}
				if (leftHeaderColumn.equals(ADocument.ExpertProperty)) {
					iow.firePropertyChange("DocumentProperty", ADocument.ExpertProperty, new String(rightHeaderColumn));
				}
				if (leftHeaderColumn.equals(ADocument.ClientProperty)) {
					iow.firePropertyChange("DocumentProperty", ADocument.ClientProperty, new String(rightHeaderColumn));
				}
				if (leftHeaderColumn.equals(ADocument.DateProperty)) {
					iow.firePropertyChange("DocumentProperty", ADocument.DateProperty, new String(rightHeaderColumn));
				}
				if (leftHeaderColumn.equals(ADocument.CommentProperty)) {
					iow.firePropertyChange("DocumentProperty", ADocument.CommentProperty, new String(rightHeaderColumn));
				}
			} else {
				if (leftHeaderColumn.equals(ADocument.ExpertProperty)) {
					String expert = (String) properties.get(ADocument.ExpertProperty);
					if (!expert.contains(rightHeaderColumn)) expert += "; " + rightHeaderColumn;
					iow.firePropertyChange("DocumentProperty", ADocument.ExpertProperty, new String(expert));
				}
			}
		}

		// looking for the table "protocol"
		searchIndex = allText.indexOf("title=\"protocol\"", 0);

		//limiting ourselves only to the Protocol table
		int tableProtocolEndIndex = allText.indexOf("</table", searchIndex);
		allText = allText.substring(0, tableProtocolEndIndex);

		// looking through columns of table "protocol" and retreiving text of the left and right columns
		while (searchIndex > 0) {

			searchIndex = allText.indexOf("<tr>", searchIndex);
			if (searchIndex > 0) result = findTagContent(allText, colStartToken, colEndToken, searchIndex);
			else break;
			if (result != null) {
				leftColumn += result;
				leftColumn += "<br/><br/>";//adding breaks because there are no breaks on row boundaries
				searchIndex = allText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}

			if (searchIndex > 0) result = findTagContent(allText, colStartToken, colEndToken, searchIndex);
			else break;
			if (result != null) {
				rightColumn += result;
				//rightColumn +="<br/>"; //there are breaks on row boundaries for the right column
				searchIndex = allText.indexOf(colEndToken, searchIndex) + colEndToken.length();
			}
		}

		//remove all tabs
		//leftColumn = leftColumn.replaceAll("\t", "");
		leftColumn = leftColumn.replaceAll("\n", "");
		leftColumn = leftColumn.replace("<br/>", "\n");
		leftColumn = leftColumn.trim();

		rightColumn = rightColumn.replaceAll("\n", "");
		//rightColumn= rightColumn.replaceAll("\t", "");
		rightColumn = rightColumn.replace("<br/>", "\n");

		// Убираем все лишние теги

		leftColumn = removeTag(leftColumn, "<span", ">");
		leftColumn = removeTag(leftColumn, "</span", ">");
		leftColumn = removeTag(leftColumn, "<small", ">");
		leftColumn = removeTag(leftColumn, "</small", ">");

		Hashtable<Integer, RawAData> rawData = new Hashtable<Integer, RawAData>();

		int posBeg = leftColumn.indexOf("[");
		int posEnd = -1;
		iow.firePropertyChange("progress", null, new Integer(fileLoadProgress / 2));
		// processing the left column's content
		while (leftColumn.indexOf("[", 0) >= 0 || leftColumn.indexOf("]", 0) >= 0) {
			iow.firePropertyChange("progress", null, new Integer(fileLoadProgress +
				leftColumnParseProgress * posBeg / leftColumn.length()));
			int handle = -1;
			int a = 0;
			RawAData data;
			String handleNo = null;
			//if we met the opening tag
			if ((leftColumn.indexOf("[", 0) >= 0) && (leftColumn.indexOf("[", 0) <= leftColumn.indexOf("]", 0))) {
				posBeg = leftColumn.indexOf("[");
				handleNo = findTagContent(leftColumn, "[", "|", 0);
				handle = Integer.parseInt(handleNo);
				leftColumn = leftColumn.replace(findTag(leftColumn, "[", "|", 0), "");
				data = new RawAData();
				data.setBegin(posBeg);
				rawData.put(new Integer(handle), data);
				//if we met the closing tag
			} else if (leftColumn.indexOf("]", 0) >= 0) {
				posEnd = leftColumn.indexOf("|");
				handleNo = findTagContent(leftColumn, "|", "]", 0);
				handle = Integer.parseInt(handleNo);
				leftColumn = leftColumn.replace(findTag(leftColumn, "|", "]", 0), "");
				data = rawData.get(new Integer(handle));
				if (data != null) data.setEnd(posEnd);
			}
		}

		iow.firePropertyChange("progress", null, new Integer(
			fileLoadProgress +
				leftColumnParseProgress));

		posBeg = rightColumn.indexOf("{");
		posEnd = -1;

		// processing the right column's content
		while (posBeg >= 0) {
			iow.firePropertyChange("progress", null, new Integer(
				fileLoadProgress +
					leftColumnParseProgress +
					rightColumnParseProgress * (posBeg / rightColumn.length())));

			String handleNo = findTagContent(rightColumn, "{", ":", posBeg);
			int handle = Integer.parseInt(handleNo);
			RawAData data = rawData.get(new Integer(handle));
			if (data != null) {
				String aDataString = findTagContent(rightColumn, ":", "}", posBeg);
				data.setAData(aDataString);
				posEnd = rightColumn.indexOf("{", posBeg + 1);
				if (posEnd < 0) posEnd = rightColumn.length() - 1;
				int posBeg1 = rightColumn.indexOf("}", posBeg) + 1;
				String com = null;
				if (posBeg1 > 0) com = new String(rightColumn.substring(posBeg1, posEnd));

				if (com != null) com = com.trim();
				com = " " + com;
				//removing last line brake which was added when saving
				while (com != null && (com.lastIndexOf("\n") == (com.length() - 1)))
					com = com.substring(0, com.length() - 1);
/*
				while (com != null  &&  com.startsWith(" "))
						 	com  = com.substring(1, com.length()-1);
*/
				if (com == null) com = "";
				data.setComment(com);
			}

			posBeg = rightColumn.indexOf("{", posBeg + 1);
		}
		iow.firePropertyChange("progress", null, new Integer(
			fileLoadProgress +
				leftColumnParseProgress +
				rightColumnParseProgress));

		// Обрабатываем стили в уже прочитанном тексте
		SimpleAttributeSet currentStyle = new SimpleAttributeSet(document.defaultStyle);
		Pattern styleTag = Pattern.compile("</?[bi]>");
		String sourceText = leftColumn;
		Matcher styleMatcher = styleTag.matcher(sourceText);
		int sourcePosition = 0;
		int sourceOffset = 0;
		int docPosition = appendOffset;
		Vector<StyledText> styledTextBlocks = new Vector<StyledText>();
		while (styleMatcher.find()) {
			String currentTag = styleMatcher.group();
			int tagLenth = currentTag.length();
			int tagStart = styleMatcher.start();
			int tagEnd = styleMatcher.end();
			String textBlock = sourceText.substring(sourcePosition, tagStart);

			// Добавляем в документ текст перед текущим тегом
			styledTextBlocks.add(new StyledText(textBlock, currentStyle));
			docPosition += textBlock.length();
			sourcePosition = tagEnd;

			// Так как мы удаляем теги из основного текста, необходимо сместить
			// пометки типировщика, находящиеся после тега
			for (RawAData rd : rawData.values()) {
				if (rd.getBegin() >= tagEnd - sourceOffset) {
					rd.setBegin(rd.getBegin() - tagLenth);
				}
				if (rd.getEnd() >= tagEnd - sourceOffset) {
					rd.setEnd(rd.getEnd() - tagLenth);
				}
			}
			sourceOffset += tagLenth;

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
		styledTextBlocks.add(new StyledText(sourceText.substring(sourcePosition),
			currentStyle));
		iow.firePropertyChange("AppendStyledText", null, styledTextBlocks);

		/// adding plain text to the document
		iow.firePropertyChange("RawData", null, rawData);
		iow.firePropertyChange("progress", null, new Integer(
			fileLoadProgress +
				leftColumnParseProgress +
				rightColumnParseProgress +
				textAddProgress));

		//creating and adding the segments AData info
		//	HashMap <ASection, AData> tempADataMap = new HashMap <ASection, AData>();

		//iow.firePropertyChange("AData", getADataMap(), tempADataMap);

		iow.firePropertyChange("progress", null, new Integer(100));
	}

	private String removeTag(String source, String startToken, String endToken) {
		String tag = null;
		tag = findTag(source, startToken, endToken, 0);
		while (tag != null) {
			source = source.replace((CharSequence) tag, (CharSequence) "");
			tag = findTag(source, startToken, endToken, 0);
		}
		return source;
	}

	private String findTagContent(String text, String startToken, String endToken, int fromIndex) {
		int startIndex = text.indexOf(startToken, fromIndex);
		int endIndex = text.indexOf(endToken, startIndex);

		if (startIndex >= 0 && endIndex > 0 && endIndex > startIndex) {
			return text.substring(startIndex + startToken.length(), endIndex);
		}
		return null;
	}

	private String findTag(String text, String startToken, String endToken, int fromIndex) {
		int startIndex = text.indexOf(startToken, fromIndex);
		int endIndex = text.indexOf(endToken, startIndex);

		if (startIndex >= 0 && endIndex > 0 && endIndex > startIndex) {
			return text.substring(startIndex, endIndex + endToken.length());
		}
		return null;
	}
}
