package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sociotype;

import java.util.ArrayList;
import java.util.List;

/**
 * Позиционный предикат.
 * ТИМ удовлетворяет предикату, если заданный аспект находится в нем на заданных местах.
 */
public class PositionPredicate implements Predicate {
	/**
	 * Аспект, связанный с предикатом.
	 */
	private Aspect aspect;

	/**
	 * Места в модели (1-8), на которых может стоять аспект.
	 */
	private ArrayList<Integer> positions;

	public PositionPredicate(Aspect aspect, List<Integer> positions) {
		this.aspect = aspect;
		this.positions.addAll(positions);
	}

	public Aspect getAspect() {
		return aspect;
	}

	public List<Integer> getPositions() {
		return positions;
	}

	@Override
	public boolean check(Sociotype sociotype) {
		int functionPosition = sociotype.getFunctionByAspect(aspect).getPosition();
		return positions.contains(functionPosition);
	}
}
