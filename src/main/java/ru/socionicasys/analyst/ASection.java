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
public class ASection implements Comparable<ASection> {
	private final Position start;
	private final Position end;

	/**
	 * Создает интервал внутри документа.
	 * 
	 * @param sourceDocument документ, в котором нужно создать интервал
	 * @param startOffset начальное смещение интервала
	 * @param endOffset конечное смещение интервала
	 * @throws BadLocationException когда начальное/конечное смещения находятся вне границ документа
	 */
	public ASection(Document sourceDocument, int startOffset, int endOffset)
			throws BadLocationException {
		start = sourceDocument.createPosition(startOffset);
		end = sourceDocument.createPosition(endOffset);
	}

	/**
	 * @return текущее смещение начала интервала в документе
	 */
	public int getStartOffset() {
		return start.getOffset();
	}

	/**
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
		return startOffset < endOffset && offset >= startOffset && offset < endOffset;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ASection)) {
			return false;
		}
		ASection otherSection = (ASection) obj;
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
	public int compareTo(ASection o) {
		return getStartOffset() - o.getStartOffset();
	}

	@Override
	public String toString() {
		return String.format("ASection{start=%s, end=%s}", start, end);
	}
}
