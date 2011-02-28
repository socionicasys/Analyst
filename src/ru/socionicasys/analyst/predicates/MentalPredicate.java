package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;

import java.util.Arrays;

/**
 * Предикат исполняется, если заданный аспект находится в ментальном кольце.
 */
public class MentalPredicate extends PositionPredicate {
	public MentalPredicate(Aspect aspect) {
		super(aspect, Arrays.asList(1, 2, 3, 4));
	}
}
