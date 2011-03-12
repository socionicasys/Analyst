package ru.socionicasys.analyst;

public final class EqualsUtil {
	/**
	 * Проверяет на равенство два объекта, возможно равных null
	 */
	public static boolean areEqual(Object first, Object second) {
		return first == null ? second == null : first.equals(second);
	}
}
