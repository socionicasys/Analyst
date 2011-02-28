package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;

import java.util.Arrays;

/**
 * Предикат исполняется, если функция с заданным аспектом является одномерной.
 */
public class Dimension1Predicate extends PositionPredicate {
	public Dimension1Predicate(Aspect aspect) {
		super(aspect, Arrays.asList(4, 5));
	}
}
