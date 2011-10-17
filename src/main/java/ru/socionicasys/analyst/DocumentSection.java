package ru.socionicasys.analyst;

import ru.socionicasys.analyst.util.HashUtil;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * Представляет собой интервал в документе и стиль выделения этого интервала.
 * Автоматически отслеживает изменения в документе и корректирует положение
 * своей начальной и конечной позиции.
 */
public class DocumentSection implements Comparable<DocumentSection> {
	/**
	 * Позиция начала интервала. Привязана к документу.
	 */
	private final Position start;

	/**
	 * Позиция конца интервала. Привязана к документу.
	 */
	private final Position end;

	/**
	 * Создает интервал внутри документа.
	 * 
	 * @param sourceDocument документ, в котором нужно создать интервал
	 * @param startOffset начальное смещение интервала
	 * @param endOffset конечное смещение интервала
	 * @throws BadLocationException когда начальное/конечное смещения находятся вне границ документа
	 */
	public DocumentSection(Document sourceDocument, int startOffset, int endOffset)
			throws BadLocationException {
		start = sourceDocument.createPosition(Math.min(startOffset, endOffset));
		end = sourceDocument.createPosition(Math.max(startOffset, endOffset));
	}

	/**
	 * Возвращает текущее смещение начала интервала в документе.
	 * Гарантированно {@code getStartOffset()} <= {@link #getEndOffset()}.
	 *
	 * @return текущее смещение начала интервала в документе
	 */
	public int getStartOffset() {
		return start.getOffset();
	}

	/**
	 * Возвращает текущее смещение конца интервала в документе.
	 * Гарантированно {@link #getStartOffset()} <= {@code getEndOffset()}.
	 *
	 * @return текущее смещение конца интервала в документе
	 */
	public int getEndOffset() {
		return end.getOffset();
	}

	/**
	 * @return текущее смещение средины интервала в документе
	 */
	public int getMiddleOffset() {
		return (end.getOffset() + start.getOffset()) / 2;
	}

	/**
	 * Провряет, содержит ли интервал данную позицию в документе
	 * @param offset позиция в документе для проверки
	 * @return содержит ли интервал данную позицию
	 */
	public boolean containsOffset(int offset) {
		int startOffset = start.getOffset();
		int endOffset = end.getOffset();
		return offset >= startOffset && offset < endOffset;
	}

	/**
	 * Возвращает {@code true} если интервал пересекается с заданным в параметре интервалом
	 *
	 * @param otherSection интервал, пересечение с которым нужно проверить
	 * @return пересекаются ли интервалы
	 */
	public boolean intersects(DocumentSection otherSection) {
		return getStartOffset() <= otherSection.getEndOffset() && otherSection.getStartOffset() <= getEndOffset();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DocumentSection)) {
			return false;
		}
		DocumentSection otherSection = (DocumentSection) obj;
		return getStartOffset() == otherSection.getStartOffset()
				&& getEndOffset() == otherSection.getEndOffset();
	}

	@Override
	public int hashCode() {
		HashUtil hashUtil = new HashUtil();
		hashUtil.hash(start);
		hashUtil.hash(end);
		return hashUtil.getComputedHash();
	}

	@Override
	public int compareTo(DocumentSection o) {
		return getStartOffset() - o.getStartOffset();
	}

	@Override
	public String toString() {
		return String.format("DocumentSection{start=%s, end=%s}", start, end);
	}
}
