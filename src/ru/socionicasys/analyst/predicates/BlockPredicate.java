package ru.socionicasys.analyst.predicates;

import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sociotype;

/**
 * Предикат исполняется, если функции с заданными аспектами находятся в одном блоке.
 */
public class BlockPredicate implements Predicate {
	/**
	 * Первый аспект блока.
	 */
	private Aspect sourceAspect;

	/**
	 * Второй аспект блока.
	 */
	private Aspect destinationAspect;

	public BlockPredicate(Aspect sourceAspect, Aspect destinationAspect) {
		this.sourceAspect = sourceAspect;
		this.destinationAspect = destinationAspect;
	}

	@Override
	public boolean check(Sociotype sociotype) {
		int sourcePosition = sociotype.getFunctionByAspect(sourceAspect).getPosition();
		int destinationPosition = sociotype.getFunctionByAspect(destinationAspect).getPosition();
		int minPosition = Math.min(sourcePosition, destinationPosition);
		// Функции находятся в одном блоке, если их индексы отличаются на 1 и меньший из них — нечетный
		return Math.abs(sourcePosition - destinationPosition) == 1 && minPosition % 2 == 1;
	}
}
