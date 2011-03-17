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
	private final Aspect aspect;

	/**
	 * Минимальная размерность предиката.
	 */
	private final int dimension;

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

	@Override
	public String toString() {
		switch (dimension) {
		case 1:
			return "Ex";
		case 2:
			return "Nr";
		case 3:
			return "St";
		case 4:
			return "Tm";
		default:
			return "";
		}
	}
}
