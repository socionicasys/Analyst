package ru.socionicasys.analyst.types;

/**
 * Знак функции.
 */
public enum Sign {
	PLUS,
	MINUS;

	public Sign	inverse() {
		switch (this) {
		case PLUS:
			return MINUS;

		case MINUS:
			return PLUS;
		}
		throw new IllegalArgumentException();
	}
}
