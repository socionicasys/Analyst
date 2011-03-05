package ru.socionicasys.analyst.types;

/**
 * Описывает информацию об отдельном аспекте.
 */
public enum Aspect {
	P ("ЧЛ"),
	L ("БЛ"),
	F ("ЧС"),
	S ("БС"),
	E ("ЧЭ"),
	R ("БЭ"),
	I ("ЧИ"),
	T ("БИ");

	/**
	 * Название (аббревиатура) аспекта.
	 */
	private final String abbreviation;

	private Aspect(String abbreviation)
	{
		this.abbreviation = abbreviation;
	}

	public String getAbbreviation()
	{
		return abbreviation;
	}
}
