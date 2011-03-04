package ru.socionicasys.analyst.types;

import java.util.Arrays;
import java.util.List;

/**
 * Описывает информацию об отдельном аспекте.
 */
public class Aspect {
	/**
	 * Название (аббревиатура) аспекта.
	 */
	private final String name;

	public final static Aspect P = new Aspect("ЧЛ");
	public final static Aspect L = new Aspect("БЛ");
	public final static Aspect F = new Aspect("ЧС");
	public final static Aspect S = new Aspect("БС");
	public final static Aspect E = new Aspect("ЧЭ");
	public final static Aspect R = new Aspect("БЭ");
	public final static Aspect I = new Aspect("ЧИ");
	public final static Aspect T = new Aspect("БИ");

	public final static List<Aspect> aspects = Arrays.asList(P, L, F, S, E, R, I, T);

	private Aspect(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
