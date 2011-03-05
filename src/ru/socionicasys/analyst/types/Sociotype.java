package ru.socionicasys.analyst.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Описывает модель А отдельного ТИМа.
 */
public enum Sociotype {
	ILE,
	SEI,
	ESE,
	LII,
	EIE,
	LSI,
	SLE,
	IEI,
	SEE,
	ILI,
	LIE,
	ESI,
	LSE,
	EII,
	IEE,
	SLI;

	/**
	 * Набор функций модели.
	 */
	private final List<Function> functions;

	/**
	 * Аббревиатура типа.
	 */
	private final String abbreviation;

	/**
	 * Псевдоним типа.
	 */
	private final String nickname;

	private Sociotype() {
		functions = new ArrayList<Function>(8);
		switch (this) {
		case ILE:
			abbreviation = "ИЛЭ";
			nickname = "дон кихот";
			functions.add(new Function(Aspect.I, 1, Sign.PLUS));
			functions.add(new Function(Aspect.L, 2, Sign.MINUS));
			functions.add(new Function(Aspect.F, 3, Sign.PLUS));
			functions.add(new Function(Aspect.R, 4, Sign.MINUS));
			functions.add(new Function(Aspect.S, 5, Sign.MINUS));
			functions.add(new Function(Aspect.E, 6, Sign.PLUS));
			functions.add(new Function(Aspect.T, 7, Sign.MINUS));
			functions.add(new Function(Aspect.P, 8, Sign.PLUS));
			break;

		case SEI:
			abbreviation = "СЭИ";
			nickname = "дюма";
			functions.add(new Function(Aspect.S, 1, Sign.PLUS));
			functions.add(new Function(Aspect.E, 2, Sign.MINUS));
			functions.add(new Function(Aspect.T, 3, Sign.PLUS));
			functions.add(new Function(Aspect.P, 4, Sign.MINUS));
			functions.add(new Function(Aspect.I, 5, Sign.MINUS));
			functions.add(new Function(Aspect.L, 6, Sign.PLUS));
			functions.add(new Function(Aspect.F, 7, Sign.MINUS));
			functions.add(new Function(Aspect.R, 8, Sign.PLUS));
			break;

		case ESE:
			abbreviation = "ЭСЭ";
			nickname = "гюго";
			functions.add(new Function(Aspect.E, 1, Sign.MINUS));
			functions.add(new Function(Aspect.S, 2, Sign.PLUS));
			functions.add(new Function(Aspect.P, 3, Sign.MINUS));
			functions.add(new Function(Aspect.T, 4, Sign.PLUS));
			functions.add(new Function(Aspect.L, 5, Sign.PLUS));
			functions.add(new Function(Aspect.I, 6, Sign.MINUS));
			functions.add(new Function(Aspect.R, 7, Sign.PLUS));
			functions.add(new Function(Aspect.F, 8, Sign.MINUS));
			break;

		case LII:
			abbreviation = "ЛИИ";
			nickname = "робеспьер";
			functions.add(new Function(Aspect.L, 1, Sign.MINUS));
			functions.add(new Function(Aspect.I, 2, Sign.PLUS));
			functions.add(new Function(Aspect.R, 3, Sign.MINUS));
			functions.add(new Function(Aspect.F, 4, Sign.PLUS));
			functions.add(new Function(Aspect.E, 5, Sign.PLUS));
			functions.add(new Function(Aspect.S, 6, Sign.MINUS));
			functions.add(new Function(Aspect.P, 7, Sign.PLUS));
			functions.add(new Function(Aspect.T, 8, Sign.MINUS));
			break;

		case EIE:
			abbreviation = "ЭИЭ";
			nickname = "гамлет";
			functions.add(new Function(Aspect.E, 1, Sign.PLUS));
			functions.add(new Function(Aspect.T, 2, Sign.MINUS));
			functions.add(new Function(Aspect.P, 3, Sign.PLUS));
			functions.add(new Function(Aspect.S, 4, Sign.MINUS));
			functions.add(new Function(Aspect.L, 5, Sign.MINUS));
			functions.add(new Function(Aspect.F, 6, Sign.PLUS));
			functions.add(new Function(Aspect.R, 7, Sign.MINUS));
			functions.add(new Function(Aspect.I, 8, Sign.PLUS));
			break;

		case LSI:
			abbreviation = "ЛСИ";
			nickname = "максим горький";
			functions.add(new Function(Aspect.L, 1, Sign.PLUS));
			functions.add(new Function(Aspect.F, 2, Sign.MINUS));
			functions.add(new Function(Aspect.R, 3, Sign.PLUS));
			functions.add(new Function(Aspect.I, 4, Sign.MINUS));
			functions.add(new Function(Aspect.E, 5, Sign.MINUS));
			functions.add(new Function(Aspect.T, 6, Sign.PLUS));
			functions.add(new Function(Aspect.P, 7, Sign.MINUS));
			functions.add(new Function(Aspect.S, 8, Sign.PLUS));
			break;

		case SLE:
			abbreviation = "СЛЭ";
			nickname = "жуков";
			functions.add(new Function(Aspect.F, 1, Sign.MINUS));
			functions.add(new Function(Aspect.L, 2, Sign.PLUS));
			functions.add(new Function(Aspect.I, 3, Sign.MINUS));
			functions.add(new Function(Aspect.R, 4, Sign.PLUS));
			functions.add(new Function(Aspect.T, 5, Sign.PLUS));
			functions.add(new Function(Aspect.E, 6, Sign.MINUS));
			functions.add(new Function(Aspect.S, 7, Sign.PLUS));
			functions.add(new Function(Aspect.P, 8, Sign.MINUS));
			break;

		case IEI:
			abbreviation = "ИЭИ";
			nickname = "есенин";
			functions.add(new Function(Aspect.T, 1, Sign.MINUS));
			functions.add(new Function(Aspect.E, 2, Sign.PLUS));
			functions.add(new Function(Aspect.S, 3, Sign.MINUS));
			functions.add(new Function(Aspect.P, 4, Sign.PLUS));
			functions.add(new Function(Aspect.F, 5, Sign.PLUS));
			functions.add(new Function(Aspect.L, 6, Sign.MINUS));
			functions.add(new Function(Aspect.I, 7, Sign.PLUS));
			functions.add(new Function(Aspect.R, 8, Sign.MINUS));
			break;

		case SEE:
			abbreviation = "СЭЭ";
			nickname = "наполеон";
			functions.add(new Function(Aspect.F, 1, Sign.PLUS));
			functions.add(new Function(Aspect.R, 2, Sign.MINUS));
			functions.add(new Function(Aspect.I, 3, Sign.PLUS));
			functions.add(new Function(Aspect.L, 4, Sign.MINUS));
			functions.add(new Function(Aspect.T, 5, Sign.MINUS));
			functions.add(new Function(Aspect.P, 6, Sign.PLUS));
			functions.add(new Function(Aspect.S, 7, Sign.MINUS));
			functions.add(new Function(Aspect.E, 8, Sign.PLUS));
			break;

		case ILI:
			abbreviation = "ИЛИ";
			nickname = "бальзак";
			functions.add(new Function(Aspect.T, 1, Sign.PLUS));
			functions.add(new Function(Aspect.P, 2, Sign.MINUS));
			functions.add(new Function(Aspect.S, 3, Sign.PLUS));
			functions.add(new Function(Aspect.E, 4, Sign.MINUS));
			functions.add(new Function(Aspect.F, 5, Sign.MINUS));
			functions.add(new Function(Aspect.R, 6, Sign.PLUS));
			functions.add(new Function(Aspect.I, 7, Sign.MINUS));
			functions.add(new Function(Aspect.L, 8, Sign.PLUS));
			break;

		case LIE:
			abbreviation = "ЛИЭ";
			nickname = "джек лондон";
			functions.add(new Function(Aspect.P, 1, Sign.MINUS));
			functions.add(new Function(Aspect.T, 2, Sign.PLUS));
			functions.add(new Function(Aspect.E, 3, Sign.MINUS));
			functions.add(new Function(Aspect.S, 4, Sign.PLUS));
			functions.add(new Function(Aspect.R, 5, Sign.PLUS));
			functions.add(new Function(Aspect.F, 6, Sign.MINUS));
			functions.add(new Function(Aspect.L, 7, Sign.PLUS));
			functions.add(new Function(Aspect.I, 8, Sign.MINUS));
			break;

		case ESI:
			abbreviation = "ЭСИ";
			nickname = "драйзер";
			functions.add(new Function(Aspect.R, 1, Sign.MINUS));
			functions.add(new Function(Aspect.F, 2, Sign.PLUS));
			functions.add(new Function(Aspect.L, 3, Sign.MINUS));
			functions.add(new Function(Aspect.I, 4, Sign.PLUS));
			functions.add(new Function(Aspect.P, 5, Sign.PLUS));
			functions.add(new Function(Aspect.T, 6, Sign.MINUS));
			functions.add(new Function(Aspect.E, 7, Sign.PLUS));
			functions.add(new Function(Aspect.S, 8, Sign.MINUS));
			break;

		case LSE:
			abbreviation = "ЛСЭ";
			nickname = "штирлиц";
			functions.add(new Function(Aspect.P, 1, Sign.PLUS));
			functions.add(new Function(Aspect.S, 2, Sign.MINUS));
			functions.add(new Function(Aspect.E, 3, Sign.PLUS));
			functions.add(new Function(Aspect.T, 4, Sign.MINUS));
			functions.add(new Function(Aspect.R, 5, Sign.MINUS));
			functions.add(new Function(Aspect.I, 6, Sign.PLUS));
			functions.add(new Function(Aspect.L, 7, Sign.MINUS));
			functions.add(new Function(Aspect.F, 8, Sign.PLUS));
			break;

		case EII:
			abbreviation = "ЭИИ";
			nickname = "достоевский";
			functions.add(new Function(Aspect.R, 1, Sign.PLUS));
			functions.add(new Function(Aspect.I, 2, Sign.MINUS));
			functions.add(new Function(Aspect.L, 3, Sign.PLUS));
			functions.add(new Function(Aspect.F, 4, Sign.MINUS));
			functions.add(new Function(Aspect.P, 5, Sign.MINUS));
			functions.add(new Function(Aspect.S, 6, Sign.PLUS));
			functions.add(new Function(Aspect.E, 7, Sign.MINUS));
			functions.add(new Function(Aspect.T, 8, Sign.PLUS));
			break;

		case IEE:
			abbreviation = "ИЭЭ";
			nickname = "гексли";
			functions.add(new Function(Aspect.I, 1, Sign.MINUS));
			functions.add(new Function(Aspect.R, 2, Sign.PLUS));
			functions.add(new Function(Aspect.F, 3, Sign.MINUS));
			functions.add(new Function(Aspect.L, 4, Sign.PLUS));
			functions.add(new Function(Aspect.S, 5, Sign.PLUS));
			functions.add(new Function(Aspect.P, 6, Sign.MINUS));
			functions.add(new Function(Aspect.T, 7, Sign.PLUS));
			functions.add(new Function(Aspect.E, 8, Sign.MINUS));
			break;

		case SLI:
			abbreviation = "СЛИ";
			nickname = "габен";
			functions.add(new Function(Aspect.S, 1, Sign.MINUS));
			functions.add(new Function(Aspect.P, 2, Sign.PLUS));
			functions.add(new Function(Aspect.T, 3, Sign.MINUS));
			functions.add(new Function(Aspect.E, 4, Sign.PLUS));
			functions.add(new Function(Aspect.I, 5, Sign.PLUS));
			functions.add(new Function(Aspect.R, 6, Sign.MINUS));
			functions.add(new Function(Aspect.F, 7, Sign.PLUS));
			functions.add(new Function(Aspect.L, 8, Sign.MINUS));
			break;

		default:
			throw new IllegalArgumentException("Illegal sociotype");
		}
	}

	/**
	 * Возвращает функцию модели ТИМа по ее позиции в модели (1-8).
	 * @param position номер функции
	 * @return объект, описывающий функцию, или null, если позиция задана некорректно
	 */
	public Function getFunctionByPosition(int position) {
		for (Function function : functions) {
			if (function.getPosition() == position) {
				return function;
			}
		}
		return null;
	}

	/**
	 * Возвращает функцию модели ТИМа по ее аспекту.
	 * @param aspect аспект функции
	 * @return объект, описывающий функцию
	 */
	public Function getFunctionByAspect(Aspect aspect) {
		for (Function function : functions) {
			if (function.getAspect() == aspect) {
				return function;
			}
		}
		return null;
	}

	/**
	 * @return аббревиатура типа
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * @return псевдоним типа
	 */
	public String getNickname() {
		return nickname;
	}
}
