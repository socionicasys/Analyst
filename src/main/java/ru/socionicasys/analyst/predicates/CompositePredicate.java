package ru.socionicasys.analyst.predicates;

import java.util.Arrays;
import java.util.List;

/**
 * Абстрактный составной предикат.
 */
public abstract class CompositePredicate implements Predicate {
	/**
	 * Список дочерних предикатов.
	 */
	private final List<Predicate> children;

	protected CompositePredicate(Predicate... predicates) {
		children = Arrays.asList(predicates);
	}

	/**
	 * @return список дочерних предикатов
	 */
	public List<Predicate> getChildren() {
		return children;
	}
}
