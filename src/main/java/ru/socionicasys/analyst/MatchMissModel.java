package ru.socionicasys.analyst;

import ru.socionicasys.analyst.model.AData;
import ru.socionicasys.analyst.types.Sociotype;

import java.util.EnumMap;
import java.util.Map;

/**
 * Модель данных для гистограммы совпадений/несовпадений с ТИМами.
 */
public class MatchMissModel implements ADocumentChangeListener {
	private final Map<Sociotype, MatchMissItem> matchMissMap;

	public MatchMissModel() {
		matchMissMap = new EnumMap<Sociotype, MatchMissItem>(Sociotype.class);
		for (Sociotype sociotype : Sociotype.values()) {
			matchMissMap.put(sociotype, new MatchMissItem(sociotype));
		}
	}

	@Override
	public void aDocumentChanged(ADocument document) {
		for (MatchMissItem matchMissItem : matchMissMap.values()) {
			matchMissItem.reset();
		}

		for (AData data : document.getADataMap().values()) {
			String aspect = data.getAspect();
			String modifier = data.getModifier();

			if (aspect == null || AData.DOUBT.equals(aspect)) {
				continue;
			}
			if (AData.JUMP.equals(modifier)) {
				continue;
			}

			for (MatchMissItem matchMissItem : matchMissMap.values()) {
				matchMissItem.addData(data);
			}
		}

		boolean exactMatchFound = false;
		float maxCoefficient = 0.0f;
		for (MatchMissItem matchMissItem : matchMissMap.values()) {
			if (matchMissItem.getMissCount() == 0) {
				exactMatchFound = true;
				maxCoefficient = Float.POSITIVE_INFINITY;
				break;
			}
			if (maxCoefficient < matchMissItem.getMatchCoefficient()) {
				maxCoefficient = matchMissItem.getMatchCoefficient();
			}
		}

		if (maxCoefficient == 0.0f) {
			return;
		}

		for (MatchMissItem matchMissItem : matchMissMap.values()) {
			float currentCoefficient = matchMissItem.getMatchCoefficient();
			if (exactMatchFound) {
				matchMissItem.setMatchCoefficient(Float.isInfinite(currentCoefficient) ? 1.0f : 0.0f);
			} else {
				matchMissItem.setMatchCoefficient(currentCoefficient / maxCoefficient);
			}
		}
	}

	/**
	 * Возвращает описание (не)совпадений с заданным ТИМом
	 * @param sociotype ТИМ, (не)совпадения которого нужно получить
	 * @return модель (не)совпадений отдельного ТИМа
	 */
	public MatchMissItem get(Sociotype sociotype) {
		return matchMissMap.get(sociotype);
	}
}
