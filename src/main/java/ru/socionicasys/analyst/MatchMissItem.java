package ru.socionicasys.analyst;

import ru.socionicasys.analyst.types.Sociotype;

/**
 * Совпадения/несовпадения отдельного ТИМа.
 */
public class MatchMissItem {
	private final Sociotype sociotype;
	
	private int matchCount;
	private int missCount;
	private float matchCoefficient;

	/**
	 * @param sociotype ТИМ, с которым связана модель (не)совпадений
	 */
	public MatchMissItem(Sociotype sociotype) {
		this.sociotype = sociotype;
	}

	/**
	 * Сбрасывает счетчики
	 */
	public void reset() {
		matchCount = 0;
		missCount = 0;
		matchCoefficient = 0.0f;
	}

	/**
	 * Обновляет счетчики соответствиями из нового блока
	 * @param data блок соционических пометок
	 */
	public void addData(AData data) {
		if (SocionicsType.matches(sociotype, data)) {
			matchCount++;
		} else {
			missCount++;
		}

		if (missCount == 0) {
			matchCoefficient = Float.POSITIVE_INFINITY;
		} else {
			matchCoefficient = (float) matchCount / missCount;
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
	 * @return (нормализованный) коэффициент совпадения
	 */
	public float getMatchCoefficient() {
		return matchCoefficient;
	}

	public void setMatchCoefficient(float matchCoefficient) {
		this.matchCoefficient = matchCoefficient;
	}
}
