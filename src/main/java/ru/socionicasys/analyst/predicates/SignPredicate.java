package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sign;
import ru.socionicasys.analyst.types.Sociotype;
import ru.socionicasys.analyst.util.EqualsUtil;
import ru.socionicasys.analyst.util.HashUtil;

/**
 * Предикат исполняется, если заданная функция имеет свойства заданного знака.
 * Поскольку функция со знаком '-' компетентна и в зоне '+', то отметка '+' работает для всех функций.
 */
public class SignPredicate implements Predicate {
	/**
	 * Аспект предиката.
	 */
	private final Aspect aspect;

	/**
	 * Знак функции с заданным аспектом.
	 */
	private final Sign sign;

	public SignPredicate(Aspect aspect, Sign sign) {
		this.aspect = aspect;
		this.sign = sign;
	}

	@Override
	public boolean check(Sociotype sociotype) {
		Sign actualSign = sociotype.getFunctionByAspect(aspect).getSign();
		return sign == Sign.PLUS || actualSign == Sign.MINUS;
	}

	@Override
	public String toString() {
		return String.format(" %s ", sign);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SignPredicate)) {
			return false;
		}

		SignPredicate otherPredicate = (SignPredicate) obj;
		return EqualsUtil.areEqual(aspect, otherPredicate.aspect) && sign == otherPredicate.sign;
	}

	@Override
	public int hashCode() {
		HashUtil hashUtil = new HashUtil();
		hashUtil.hash(aspect);
		hashUtil.hash(sign);
		return hashUtil.getComputedHash();
	}
}
