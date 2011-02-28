package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;

import java.util.Arrays;

/**
 * Предикат исполняется, если заданный аспект находится в Супериде.
 */
public class SuperidPredicate extends PositionPredicate {
	public SuperidPredicate(Aspect aspect) {
		super(aspect, Arrays.asList(5, 6));
	}
}
