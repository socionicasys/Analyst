package ru.socionicasys.analyst;

import java.io.Serializable;

/**
 * Представляет собой жестко фиксированный интервал текста, то есть интервал, который,
 * в отличие от {@link ASection}, не привязан к какому-либо документу и не отслеживает
 * сдвиги текста в нем.
 */
public class FixedDocumentSection implements Serializable {
	private static final long serialVersionUID = -4178944250423457610L;

	/**
	 * Начало интервала
	 */
	private final int start;

	/**
	 * Конец интервала
	 */
	private final int end;

	/**
	 * Создает интервал с заданными концами.
	 *
	 * @param start начало нового интервала
	 * @param end конец нового интервала
	 */
	public FixedDocumentSection(int start, int end) {
		this.start = Math.min(start, end);
		this.end = Math.max(start, end);
	}

	/**
	 * @return начало интервала
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @return конец интервала
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * @return длина интервала
	 */
	public int getLength() {
		return end - start;
	}
}
