package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;

import java.util.Arrays;

/**
 * Предикат исполняется, если заданный аспект находится в Суперэго.
 */
public class SuperegoPredicate extends PositionPredicate {
	public SuperegoPredicate(Aspect aspect) {
		super(aspect, Arrays.asList(3, 4));
	}

	@Override
	public String toString() {
		return "Суперэго";
	}
}
