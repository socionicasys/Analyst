package ru.socionicasys.analyst;

import ru.socionicasys.analyst.predicates.Predicate;
import ru.socionicasys.analyst.types.Sociotype;

import java.util.Collection;

/**
 * Совпадения/несовпадения отдельного ТИМа.
 */
public class MatchMissItem {
	/**
	 * ТИМ, с которым связан пункт совпадений-несовпадений
	 */
	private final Sociotype sociotype;

	/**
	 * Количество отметок, совпадающих с данным ТИМом
	 */
	private int matchCount;

	/**
	 * Количество отметок, не совпадающих с данным ТИМом 
	 */
	private int missCount;

	/**
	 * Коеффициент масштабирования, на который множится коеффициент соответствия
	 */
	private float scale;

	/**
	 * Создает счетчик совпадений-несовпадений для заданного ТИМа.
	 *
	 * @param sociotype ТИМ, с которым связана модель (не)совпадений
	 */
	public MatchMissItem(Sociotype sociotype) {
		this.sociotype = sociotype;
		reset();
	}

	/**
	 * Сбрасывает счетчики.
	 */
	public void reset() {
		matchCount = 0;
		missCount = 0;
		scale = 1f;
	}

	/**
	 * Обновляет счетчики соответствиями из нового блока.
	 *
	 * @param predicates список предикатов из одного блока текстовых отметок
	 */
	public void addData(Collection<Predicate> predicates) {
		if (SocionicsType.matches(sociotype, predicates)) {
			matchCount++;
		} else {
			missCount++;
		}
	}

	/**
	 * @return число совпадений
	 */
	public int getMatchCount() {
		return matchCount;
	}

	/**
	 * @return число несовпадений
	 */
	public int getMissCount() {
		return missCount;
	}

	/**
	 * Возвращает коеффициент соответствия, равный {@link #matchCount} / {@link #missCount}.
	 *
	 * @return коеффициент соответствия до нормализации
	 */
	public float getRawCoefficient() {
		float matchCoefficient;
		if (missCount == 0) {
			matchCoefficient = Float.POSITIVE_INFINITY;
		} else {
			matchCoefficient = (float) matchCount / missCount;
		}
		return matchCoefficient;
	}

	/**
	 * Возвращает нормализованный коеффициент соответствия, полученный умножением стандартного коеффициента
	 * {@link #getRawCoefficient()} на масштабный коеффициент {@link #scale}.
	 *
	 * @return нормализованный коэффициент совпадения
	 */
	public float getScaledCoefficient() {
		// Считаем matchCoefficient по matchCount и missCount
		float matchCoefficient = getRawCoefficient();

		// Масштабируем matchCoefficient
		if (scale == 0f) {
			matchCoefficient = Float.isInfinite(matchCoefficient)? 1f : 0f;
		} else {
			matchCoefficient *= scale;
		}
		return matchCoefficient;
	}

	/**
	 * Задает масштаб для коеффициента соответствия
	 *
	 * @param scale новое значение масштаба
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}
}
