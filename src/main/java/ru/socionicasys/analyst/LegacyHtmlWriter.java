package ru.socionicasys.analyst;

import ru.socionicasys.analyst.types.Sociotype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public class LegacyHtmlWriter extends SwingWorker<Void, Void> {
	private static final Logger logger = LoggerFactory.getLogger(LegacyHtmlWriter.class);

	private static final int HEADER_PROGRESS = 20;
	private static final int PREPARATION_PROGRESS = 20;
	private static final int TEXT_PROGRESS = 40;
	private static final int REPORT_PROGRESS = 20;

	private final ADocument document;
	private final File outputFile;
	private final AnalystWindow analystWindow;

	private enum EventType {
		LINE_BREAK,
		SECTION_START,
		SECTION_END,
		NEW_ROW,
		BOLD_START,
		BOLD_END,
		ITALIC_START,
		ITALIC_END
	}

	private static final class DocumentFlowEvent {
		private final EventType type;
		private final int offset;
		private final int sectionNo;
		private final String style;
		private final String comment;

		private DocumentFlowEvent(EventType type, int offset, String style, String comment, int sectionNo) {
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
	}

	/**
	 * Делает возможной сортировку массива из {@link DocumentFlowEvent}-ов.
	 * Сравнение происходит только по позиции (offset).
	 */
	@SuppressWarnings("serial")
	private static final class DocumentFlowEventComparator implements Comparator<DocumentFlowEvent>, Serializable {
		@Override
		public int compare(DocumentFlowEvent o1, DocumentFlowEvent o2) {
			return Integer.valueOf(o1.getOffset()).compareTo(o2.getOffset());
		}
	}

	private static final class RDStack {
		private final List<String> styleStack;
		private final Map<Integer, Integer> positionMap;

		private RDStack() {
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

	public LegacyHtmlWriter(AnalystWindow analystWindow, ADocument document, File outputFile) {
		this.document = document;
		this.outputFile = outputFile;
		this.analystWindow = analystWindow;
	}

	@Override
	protected Void doInBackground() throws BadLocationException, IOException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
				LegacyHtmlFormat.FILE_ENCODING));
		try {
			writeDocument(writer);
			document.setAssociatedFile(outputFile);
		} catch (BadLocationException e) {
			logger.error("Incorrect document position while saving document", e);
			throw e;
		} catch (IOException e) {
			logger.error("IO error while saving document", e);
			throw e;
		} finally {
			writer.close();
		}

		return null;
	}

	private void writeDocument(Writer writer) throws IOException, BadLocationException {
		setProgress(0);

		//writing the header
		writer.write(String.format(
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"> \n" +
			"<meta http-equiv=\"Content-Type\" content=\"text/html charset=%s\"/>" +
			"<html>\n<head>\n" +
			"<title>%s</title>\n" +
			"	<style>" +
			"			body 	{font-size:14px;color:black}\n" +
			"			h1		{}\n" +
			"			h2		{}\n" +
			"			th		{font-size:18px;font-weight:bold}\n" +
			"			small	{font-size:9px;color:darkgray}\n" +
			"	</style>\n" +
			"</head> \n" +
			"<body> \n",
			LegacyHtmlFormat.FILE_ENCODING,
			document.getProperty(Document.TitleProperty)
		));

		//document title
		writer.write(String.format("\n<h1>%s</h1>\n", document.getProperty(Document.TitleProperty)));

		//document header
		writer.write(String.format(
			"<br/>\n<br/>" +
			"\n <table title=\"header\" border=1 width=\"40%%\"> 	\n" +
			"<tr>\n" +
			"	<td>      %s     </td>\n" +
			"	<td>%s	</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td>      %s     </td>\n" +
			"	<td>%s 	</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td>      %s     </td>\n" +
			"	<td>%s	</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td>      %s     </td>\n" +
			"	<td>%s </td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td>      %s     </td>\n" +
			"	<td>%s </td>\n" +
			"</tr>\n" +
			"</table >\n",
			LegacyHtmlFormat.TITLE_PROPERTY_LABEL,
			document.getProperty(Document.TitleProperty),
			LegacyHtmlFormat.CLIENT_PROPERTY_LABEL,
			document.getProperty(ADocument.CLIENT_PROPERTY),
			LegacyHtmlFormat.EXPERT_PROPERTY_LABEL,
			document.getProperty(ADocument.EXPERT_PROPERTY),
			LegacyHtmlFormat.DATE_PROPERTY_LABEL,
			document.getProperty(ADocument.DATE_PROPERTY),
			LegacyHtmlFormat.COMMENT_PROPERTY_LABEL,
			document.getProperty(ADocument.COMMENT_PROPERTY)
		));

		//  writing the color legend
		writer.write(
			"<br/>\n<br/>" +
			"<h3> Расшифровка цветовых обозначений: </h3>" +
			"\n <table title=\"legend\" border=0 width=\"40%\"> 	\n" +
			"<tr>\n" +
			"	<td style=\"background-color:#EAEAEA\">Непонятное место</td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td style=\"background-color:#AAEEEE;\">      Маломерность     </td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td style=\"background-color:#AAEEAA;\">      Многомерность     </td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td  style=\"color:#FF0000;\">      		  Фрагмент содержит информацию о знаке     </td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td style=\"background-color:#FFFFCC;\">      Фрагмент содержит информацию о ментале или витале     </td>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td style=\"text-decoration:underline\">      Прочий выделенный фрагмент анализа     </td>\n" +
			"</tr>\n" +
			"</table >\n"
		);

		setProgress(HEADER_PROGRESS);

		//document content
		writer.write(
			"<br/>\n\n" +
			"<h2>  АНАЛИЗ </h2>\n\n" +
			" <table title=\"protocol\" border=2 width=\"100%\"> 	\n" +
			"<tr>\n" +
			"	<th width=\"60%\"> ВОПРОСЫ И ОТВЕТЫ </th>\n" +
			"	<th width=\"40%\"> АНАЛИЗ ЭКСПЕРТА</th>\n" +
			"</tr>\n" +
			"<tr>\n" +
			"	<td>"
		);

		// PREPARING
		List<DocumentFlowEvent> flowEvents = new ArrayList<DocumentFlowEvent>();
		Collection<Position> lineBreaks = new ArrayList<Position>();
		for (int i = 0; i < document.getLength(); i++) {
			if (document.getText(i, 1).equals("\n")) {
				lineBreaks.add(document.createPosition(i));
			}
		}

		List<ASection> sections = new ArrayList<ASection>(document.getADataMap().keySet());
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
		MutableAttributeSet boldAttribute = new SimpleAttributeSet();
		StyleConstants.setBold(boldAttribute, true);
		MutableAttributeSet italicAttribute = new SimpleAttributeSet();
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
				DocumentFlowEvent prevEvent = flowEvents.get(flowEvents.size() - 1);
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
		Collections.sort(flowEvents, new DocumentFlowEventComparator());

		if (!flowEvents.isEmpty() && (flowEvents.get(flowEvents.size() - 1).getType() != EventType.NEW_ROW)) {
			flowEvents.add(new DocumentFlowEvent(
				EventType.NEW_ROW, document.getEndPosition().getOffset() - 1,
				null, null, 0));
		}

		setProgress(HEADER_PROGRESS + PREPARATION_PROGRESS);

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

				setProgress(HEADER_PROGRESS + PREPARATION_PROGRESS + TEXT_PROGRESS * z / flowEvents.size());

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
		setProgress(HEADER_PROGRESS + PREPARATION_PROGRESS + TEXT_PROGRESS + REPORT_PROGRESS);

		// if generating report
		writer.write(
			"<br/>" +
					"<h1> Определение ТИМа </h1>" +
					"<br/>"
		);
		writer.write(analystWindow.getNavigeTree().getReport());
		writeMissMatchReport(writer);

		writer.write(String.format(
			"<br/>" +
			"Протокол определения ТИМа создан программой &laquo;%s&raquo;, верисия: %s <br/>" +
			"© Школа системной соционики, Киев.<br/>" +
			"http://www.socionicasys.ru\n" +
			"</body>\n" +
			"</html>\n",
			VersionInfo.getApplicationName(),
			VersionInfo.getVersion()
		));

		setProgress(100);
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

	private void writeMissMatchReport(Writer writer) throws IOException {
		if (!document.getADataMap().isEmpty()) {
			writer.write(
				"<br/>" +
				"<h2> Соответствие ТИМу </h2>" +
				"Приведенная ниже таблица позволяет определить наиболее вероятный ТИМ типируемого.<br/>" +
				"Для получения данных таблицы использовался следующий алгоритм. Каждый из отмеченных экспертом фрагментов текста<br/> " +
				"типируемого проверяется на соответствие обработке информации каждым из 16 ТИМов. Если фрагмент соответствует модели <br/>" +
				"обработки информации для данного ТИМа, значение в столбце \"Соответствие\" для данного ТИМа будет увеличено на 1.<br/>" +
				"Если нет, соответственно, увеличивается значение  в столбце \"Несоответствие\" для данного ТИМа.<br/><br/>" +
				"В столбце \"Коэффициент соответствия\" приведен нормализованный расчетный коэффициент, который рассчитывается для каждого ТИМа " +
				"по формуле:<br/>   <code> К.С. = NORM<small style=\"vertical-align:sub;color:black\"> 100</small>( СООТВЕТСТВИЕ / НЕСООТВЕТСТВИЕ )</code><br/>" +
				"Этот коэффициент применяется для выделения наиболее вероятного ТИМа,<br/>" +
				"но не следует рассматривать его как математическую вероятность определения ТИМа. <br/><br/>" +
				"<table title=\"TIM analysis\" border=1 width=\"80%\">" +
				"<tr>\n" +
				"	<th width=\"40%\"> ТИМ </th>\n" +
				"	<th width=\"20%\"> Соответствие </th>\n" +
				"	<th width=\"20%\"> Несоответствие </th>\n" +
				"	<th width=\"20%\"> Коэффициент соответствия </th>\n" +
				"</tr>\n"
			);
			for (Sociotype sociotype : Sociotype.values()) {
				MatchMissItem matchMissItem = document.getMatchMissModel().get(sociotype);
				writer.write(String.format(
					"<tr>\n" +
					"	<td style=\"font-weight:bold\">%s</td>\n" +
					"		<td align=\"center\">%s </td>\n" +
					"		<td align=\"center\">%s </td>\n" +
					"		<td align=\"center\"> %2.0f </td>\n" +
					"</tr>\n",
					sociotype,
					matchMissItem.getMatchCount(),
					matchMissItem.getMissCount(),
					100.0f * matchMissItem.getMatchCoefficient()
				));
			}
			writer.write("</table>");
		} else {
			writer.write("<br/><h2> Невозможно определить ТИМ </h2><br/>");
		}
	}
}
