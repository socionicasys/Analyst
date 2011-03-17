package ru.socionicasys.analyst.types;

/**
 * Знак функции.
 */
public enum Sign {
	PLUS ("+"),
	MINUS ("-");

	/**
	 * Текстовое представление знака
	 */
	private final String representation;

	private Sign(String representation) {
		this.representation = representation;
	}

	public Sign	inverse() {
		switch (this) {
		case PLUS:
			return MINUS;

		case MINUS:
			return PLUS;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public String toString() {
		return representation;
	}
}
