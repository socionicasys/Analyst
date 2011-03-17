package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;

import java.util.Arrays;

/**
 * Предикат исполняется, если функция с заданным аспектом является многомерной.
 */
public class HighDimensionPredicate extends PositionPredicate {
	public HighDimensionPredicate(Aspect aspect) {
		super(aspect, Arrays.asList(1, 2, 7, 8));
	}

	@Override
	public String toString() {
		return "Многомерность";
	}
}
