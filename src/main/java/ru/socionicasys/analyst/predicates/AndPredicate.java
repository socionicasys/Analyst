package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Sociotype;

/**
 * Составной предикат, который проходит проверку, если все дочерние претикаты ее проходит.
 */
public class AndPredicate extends CompositePredicate {
	public AndPredicate(Predicate... predicates) {
		super(predicates);
	}

	@Override
	public CheckResult check(Sociotype sociotype) {
		boolean successFound = false;
		for (Predicate predicate : getChildren()) {
			switch (predicate.check(sociotype)) {
			case FAIL:
				return CheckResult.FAIL;
			case IGNORE:
				break;
			case SUCCESS:
				successFound = true;
				break;
			}
		}
		return successFound ? CheckResult.SUCCESS : CheckResult.IGNORE;
	}
}
