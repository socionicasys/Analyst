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
	public boolean check(Sociotype sociotype) {
		for (Predicate predicate : getChildren()) {
			if (predicate.check(sociotype)) {
				return true;
			}
		}
		return false;
	}
}
