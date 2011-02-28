package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sign;
import ru.socionicasys.analyst.types.Sociotype;

/**
 * Предикат исполняется, если заданная функция имеет свойства заданного знака.
 * Поскольку функция со знаком '-' компетентна и в зоне '+', то отметка '+' работает для всех функций.
 */
public class SignPredicate implements Predicate {
	private Aspect aspect;
	private Sign sign;

	public SignPredicate(Aspect aspect, Sign sign) {
		this.aspect = aspect;
		this.sign = sign;
	}

	@Override
	public boolean check(Sociotype sociotype) {
		Sign actualSign = sociotype.getFunctionByAspect(aspect).getSign();
		return sign == Sign.PLUS || actualSign == Sign.MINUS;
	}
}
