package ru.socionicasys.analyst.types;

import ru.socionicasys.analyst.service.ServiceContainer;

import java.util.ResourceBundle;

/**
 * Описывает информацию об отдельном аспекте.
 */
public enum Aspect {
	P,
	L,
	F,
	S,
	E,
	R,
	I,
	T;

	/**
	 * Название (аббревиатура) аспекта.
	 */
	private final String abbreviation;

	Aspect() {
		ResourceBundle bundle = ServiceContainer.getResourceBundle();
		String aspectKey = String.format("%s.%s", getClass().getName(), name());
		abbreviation = bundle.getString(aspectKey);
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public boolean isBlockWith(final Aspect secondAspect) {
		switch (this) {
		case P:
			return secondAspect == S || secondAspect == T;
		case L:
			return secondAspect == F || secondAspect == I;
		case F:
			return secondAspect == L || secondAspect == R;
		case S:
			return secondAspect == P || secondAspect == E;
		case E:
			return secondAspect == S || secondAspect == T;
		case R:
			return secondAspect == F || secondAspect == I;
		case I:
			return secondAspect == L || secondAspect == R;
		case T:
			return secondAspect == P || secondAspect == E;
		}
		return false;
	}

	public static Aspect byAbbreviation(String abbreviation) {
		for (Aspect aspect : Aspect.values()) {
			if (aspect.getAbbreviation().equals(abbreviation)) {
				return aspect;
			}
		}
		throw new IllegalArgumentException("Illegal aspect abbreviation");
	}

	@Override
	public String toString() {
		return abbreviation;
	}
}
