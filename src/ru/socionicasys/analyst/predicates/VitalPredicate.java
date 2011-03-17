package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;

import java.util.Arrays;

/**
 * Предикат исполняется, если заданный аспект находится в витальном кольце.
 */
public class VitalPredicate extends PositionPredicate {
	public VitalPredicate(Aspect aspect) {
		super(aspect, Arrays.asList(5, 6, 7, 8));
	}

	@Override
	public String toString() {
		return "Витал";
	}
}
