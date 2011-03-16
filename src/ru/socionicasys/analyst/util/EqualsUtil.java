package ru.socionicasys.analyst.util;

/**
 * Вспомогательный класс для реализации проверки на равенство в объектах.
 */
public final class EqualsUtil {
	/**
	 * Класс только для хранения статических методов.
	 */
	private EqualsUtil() {
	}

	/**
	 * Проверяет на равенство два объекта, возможно равных null
	 */
	public static boolean areEqual(Object first, Object second) {
		return first == null ? second == null : first.equals(second);
	}
}
