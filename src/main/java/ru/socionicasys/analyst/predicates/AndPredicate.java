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
	public boolean check(Sociotype sociotype) {
		for (Predicate predicate : getChildren()) {
			if (!predicate.check(sociotype)) {
				return false;
			}
		}
		return true;
	}
}
