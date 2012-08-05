package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sociotype;
import ru.socionicasys.analyst.util.HashUtil;

/**
 * Перевод из одного аспекта в другой. Не учитывается при подсчете соответствий.
 */
public class JumpPredicate implements Predicate {
	/**
	 * Аспект, из которого делается перевод.
	 */
	private final Aspect fromAspect;

	/**
	 * Аспект, в который делается перевод.
	 */
	private final Aspect toAspect;

	public JumpPredicate(Aspect fromAspect, Aspect toAspect) {
		this.fromAspect = fromAspect;
		this.toAspect = toAspect;
	}

	@Override
	public CheckResult check(Sociotype sociotype) {
		return CheckResult.IGNORE;
	}

	@Override
	public String toString() {
		return String.format("%s->%s", fromAspect, toAspect);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JumpPredicate that = (JumpPredicate) o;
		return fromAspect == that.fromAspect && toAspect == that.toAspect;
	}

	@Override
	public int hashCode() {
		HashUtil hashUtil = new HashUtil();
		hashUtil.hash(fromAspect);
		hashUtil.hash(toAspect);
		return hashUtil.getComputedHash();
	}
}
