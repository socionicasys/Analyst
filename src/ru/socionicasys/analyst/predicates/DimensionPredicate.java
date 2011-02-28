package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sociotype;

/**
 * Предикат исполняется, если функция с заданным аспектом имеет размерность не ниже указанной.
 */
public class DimensionPredicate implements Predicate {
	/**
	 * Аспект предиката.
	 */
	private Aspect aspect;

	/**
	 * Минимальная размерность предиката.
	 */
	private int dimension;

	public DimensionPredicate(Aspect aspect, int dimension) {
		if (dimension >= 1 && dimension <= 4) {
			this.dimension = dimension;
		} else {
			throw new IllegalArgumentException("Illegal dimension for DimensionPredicate");
		}
		this.aspect = aspect;
	}

	@Override
	public boolean check(Sociotype sociotype) {
		return sociotype.getFunctionByAspect(aspect).getDimension() >= dimension;
	}
}
