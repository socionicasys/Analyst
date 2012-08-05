package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Sociotype;

/**
 * Составной предикат, который проходит проверку, если хотя бы один из дочерних претикатов проходит.
 */
public class OrPredicate extends CompositePredicate {
	public OrPredicate(Predicate... predicates) {
		super(predicates);
	}

	@Override
	public CheckResult check(Sociotype sociotype) {
		boolean failFound = false;
		for (Predicate predicate : getChildren()) {
			switch (predicate.check(sociotype)) {
			case FAIL:
				failFound = true;
				break;
			case IGNORE:
				break;
			case SUCCESS:
				return CheckResult.SUCCESS;
			}
		}
		return failFound ? CheckResult.FAIL : CheckResult.IGNORE;
	}
}
