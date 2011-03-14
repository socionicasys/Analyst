package ru.socionicasys.analyst.types;

import java.util.ArrayList;
import java.util.List;

import static ru.socionicasys.analyst.types.Aspect.P;
import static ru.socionicasys.analyst.types.Aspect.L;
import static ru.socionicasys.analyst.types.Aspect.F;
import static ru.socionicasys.analyst.types.Aspect.S;
import static ru.socionicasys.analyst.types.Aspect.E;
import static ru.socionicasys.analyst.types.Aspect.R;
import static ru.socionicasys.analyst.types.Aspect.I;
import static ru.socionicasys.analyst.types.Aspect.T;

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
		functions = new ArrayList<Function>();
		switch (this) {
		case ILE:
			abbreviation = "ИЛЭ";
			nickname = "дон кихот";
			initializeFunctions(Sign.PLUS, I, L, F, R, S, E, T, P);
			break;

		case SEI:
			abbreviation = "СЭИ";
			nickname = "дюма";
			initializeFunctions(Sign.PLUS, S, E, T, P, I, L, F, R);
			break;

		case ESE:
			abbreviation = "ЭСЭ";
			nickname = "гюго";
			initializeFunctions(Sign.MINUS, E, S, P, T, L, I, R, F);
			break;

		case LII:
			abbreviation = "ЛИИ";
			nickname = "робеспьер";
			initializeFunctions(Sign.MINUS, L, I, R, F, E, S, P, T);
			break;

		case EIE:
			abbreviation = "ЭИЭ";
			nickname = "гамлет";
			initializeFunctions(Sign.PLUS, E, T, P, S, L, F, R, I);
			break;

		case LSI:
			abbreviation = "ЛСИ";
			nickname = "максим горький";
			initializeFunctions(Sign.PLUS, L, F, R, I, E, T, P, S);
			break;

		case SLE:
			abbreviation = "СЛЭ";
			nickname = "жуков";
			initializeFunctions(Sign.MINUS, F, L, I, R, T, E, S, P);
			break;

		case IEI:
			abbreviation = "ИЭИ";
			nickname = "есенин";
			initializeFunctions(Sign.MINUS, T, E, S, P, F, L, I, R);
			break;

		case SEE:
			abbreviation = "СЭЭ";
			nickname = "наполеон";
			initializeFunctions(Sign.PLUS, F, R, I, L, T, P, S, E);
			break;

		case ILI:
			abbreviation = "ИЛИ";
			nickname = "бальзак";
			initializeFunctions(Sign.PLUS, T, P, S, E, F, R, I, L);
			break;

		case LIE:
			abbreviation = "ЛИЭ";
			nickname = "джек лондон";
			initializeFunctions(Sign.MINUS, P, T, E, S, R, F, L, I);
			break;

		case ESI:
			abbreviation = "ЭСИ";
			nickname = "драйзер";
			initializeFunctions(Sign.MINUS, R, F, L, I, P, T, E, S);
			break;

		case LSE:
			abbreviation = "ЛСЭ";
			nickname = "штирлиц";
			initializeFunctions(Sign.PLUS, P, S, E, T, R, I, L, F);
			break;

		case EII:
			abbreviation = "ЭИИ";
			nickname = "достоевский";
			initializeFunctions(Sign.PLUS, R, I, L, F, P, S, E, T);
			break;

		case IEE:
			abbreviation = "ИЭЭ";
			nickname = "гексли";
			initializeFunctions(Sign.MINUS, I, R, F, L, S, P, T, E);
			break;

		case SLI:
			abbreviation = "СЛИ";
			nickname = "габен";
			initializeFunctions(Sign.MINUS, S, P, T, E, I, R, F, L);
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

	private void initializeFunctions(Sign firstSign, Aspect... aspects) {
		Sign currentSign = firstSign;
		for (int i = 0; i < aspects.length; ++i) {
			Aspect aspect = aspects[i];
			functions.add(new Function(aspect, i + 1, currentSign));
			// Знаки в модели чередуются по номеру функции,
			// за исключением знаков функций 4 и 5, которые равны
			if (i != 3) {
				currentSign = currentSign.inverse();
			}
		}
	}
}
