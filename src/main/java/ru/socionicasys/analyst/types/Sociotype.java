package ru.socionicasys.analyst.types;

import ru.socionicasys.analyst.service.ServiceContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
	ILE (Sign.PLUS,  I, L, F, R, S, E, T, P),
	SEI (Sign.PLUS,  S, E, T, P, I, L, F, R),
	ESE (Sign.MINUS, E, S, P, T, L, I, R, F),
	LII (Sign.MINUS, L, I, R, F, E, S, P, T),
	EIE (Sign.PLUS,  E, T, P, S, L, F, R, I),
	LSI (Sign.PLUS,  L, F, R, I, E, T, P, S),
	SLE (Sign.MINUS, F, L, I, R, T, E, S, P),
	IEI (Sign.MINUS, T, E, S, P, F, L, I, R),
	SEE (Sign.PLUS,  F, R, I, L, T, P, S, E),
	ILI (Sign.PLUS,  T, P, S, E, F, R, I, L),
	LIE (Sign.MINUS, P, T, E, S, R, F, L, I),
	ESI (Sign.MINUS, R, F, L, I, P, T, E, S),
	LSE (Sign.PLUS,  P, S, E, T, R, I, L, F),
	EII (Sign.PLUS,  R, I, L, F, P, S, E, T),
	IEE (Sign.MINUS, I, R, F, L, S, P, T, E),
	SLI (Sign.MINUS, S, P, T, E, I, R, F, L);

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

	Sociotype(Sign firstSign, Aspect... aspects) {
		ResourceBundle bundle = ServiceContainer.getResourceBundle();

		String sociotypeKey = String.format("%s.%s", getClass().getName(), name());
		String abbreviationKey = String.format("%s.abbreviation", sociotypeKey);
		String nicknameKey = String.format("%s.nickname", sociotypeKey);

		abbreviation = bundle.getString(abbreviationKey);
		nickname = bundle.getString(nicknameKey);
		functions = new ArrayList<Function>();

		initializeFunctions(firstSign, aspects);
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

	@Override
	public String toString() {
		return String.format("%s (%s)", abbreviation, nickname);
	}
}
