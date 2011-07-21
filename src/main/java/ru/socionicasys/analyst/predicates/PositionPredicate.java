package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sociotype;
import ru.socionicasys.analyst.util.EqualsUtil;
import ru.socionicasys.analyst.util.HashUtil;

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
	private final Aspect aspect;

	/**
	 * Места в модели (1-8), на которых может стоять аспект.
	 */
	private final List<Integer> positions;

	public PositionPredicate(Aspect aspect, List<Integer> positions) {
		this.aspect = aspect;
		this.positions = new ArrayList<Integer>();
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PositionPredicate)) {
			return false;
		}

		PositionPredicate otherPredicate = (PositionPredicate) obj;
		return EqualsUtil.areEqual(aspect, otherPredicate.aspect) && positions.equals(otherPredicate.positions);
	}

	@Override
	public int hashCode() {
		HashUtil hashUtil = new HashUtil();
		hashUtil.hash(aspect);
		hashUtil.hash(positions);
		return hashUtil.getComputedHash();
	}
}
