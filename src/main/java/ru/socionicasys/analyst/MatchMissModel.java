package ru.socionicasys.analyst;

import ru.socionicasys.analyst.model.AData;
import ru.socionicasys.analyst.predicates.Predicate;
import ru.socionicasys.analyst.types.Sociotype;

import java.util.Collection;
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
		scaleMatchCoefficients();
	}

	@Override
	public void aDocumentChanged(ADocument document) {
		for (MatchMissItem matchMissItem : matchMissMap.values()) {
			matchMissItem.reset();
		}

		for (AData data : document.getADataMap().values()) {
			String aspect = data.getAspect();

			if (aspect == null || AData.DOUBT.equals(aspect)) {
				continue;
			}

			Collection<Predicate> predicates = SocionicsType.createPredicates(data);
			if (predicates.isEmpty()) {
				continue;
			}

			for (MatchMissItem matchMissItem : matchMissMap.values()) {
				matchMissItem.addData(predicates);
			}
		}

		scaleMatchCoefficients();
	}

	/**
	 * Масштабирует коеффициенты соответствия каждого ТИМа так, чтобы максимальный был равен 1.
	 */
	private void scaleMatchCoefficients() {
		float maxCoefficient = 0.0f;
		for (MatchMissItem matchMissItem : matchMissMap.values()) {
			if (maxCoefficient < matchMissItem.getRawCoefficient()) {
				maxCoefficient = matchMissItem.getRawCoefficient();
			}
		}

		if (maxCoefficient == 0.0f) {
			return;
		}

		float scale = Float.isInfinite(maxCoefficient) ? 0f : 1f / maxCoefficient;
		for (MatchMissItem matchMissItem : matchMissMap.values()) {
			matchMissItem.setScale(scale);
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
