package ru.socionicasys.analyst;

import ru.socionicasys.analyst.predicates.*;
import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sign;
import ru.socionicasys.analyst.types.Sociotype;

import java.util.*;

/**
 * Содержит в себе код сверки социотипа и набора данных из пометок.
 */
public final class SocionicsType {
	private SocionicsType() {
	}

	/**
	 * Проверяет, соответствует ли данный ТИМ конкретным отметкам.
	 *
	 * @param type ТИМ
	 * @param aspect аспект отметки
	 * @param secondAspect второй аспект (для блока)
	 * @param sign знак
	 * @param dimension размерность
	 * @param mv ментальность/витальность
	 * @return соответствует ли ТИМ
	 */
	public static boolean matches(Sociotype type, String aspect, String secondAspect, String sign, String dimension, String mv) {
		if (aspect == null) {
			return false;
		}

		Collection<Predicate> predicates = createPredicates(aspect, secondAspect, sign, dimension, mv);

		for (Predicate predicate : predicates) {
			if (!predicate.check(type)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Преобразует набор отметок в список предикатов.
	 *
	 * @param aspect аспект отметки
	 * @param secondAspect второй аспект (для блока)
	 * @param sign знак
	 * @param dimension размерность
	 * @param mv ментальность/витальность
	 * @return список предикатов
	 */
	public static Collection<Predicate> createPredicates(String aspect, String secondAspect, String sign, String dimension, String mv) {
		if (aspect == null) {
			return Collections.emptyList();
		}

		Aspect baseAspect = Aspect.byAbbreviation(aspect);
		Collection<Predicate> predicates = new ArrayList<Predicate>();

		if (secondAspect != null) {
			predicates.add(new BlockPredicate(baseAspect, Aspect.byAbbreviation(secondAspect)));
		}

		if (sign != null) {
			Sign theSign;
			if (sign.equals(AData.PLUS)) {
				theSign = Sign.PLUS;
			} else if (sign.equals(AData.MINUS)) {
				theSign = Sign.MINUS;
			} else {
				throw new IllegalArgumentException("Illegal sign in SocionicsType.matches()");
			}
			predicates.add(new SignPredicate(baseAspect, theSign));
		}

		if (dimension != null) {
			Predicate dimensionPredicate;
			if (dimension.equals(AData.D1)) {
				dimensionPredicate = new DimensionPredicate(baseAspect, 1);
			} else if (dimension.equals(AData.D2)) {
				dimensionPredicate = new DimensionPredicate(baseAspect, 2);
			} else if (dimension.equals(AData.D3)) {
				dimensionPredicate = new DimensionPredicate(baseAspect, 3);
			} else if (dimension.equals(AData.D4)) {
				dimensionPredicate = new DimensionPredicate(baseAspect, 4);
			} else if (dimension.equals(AData.MALOMERNOST)) {
				dimensionPredicate = new LowDimensionPredicate(baseAspect);
			} else if (dimension.equals(AData.MNOGOMERNOST)) {
				dimensionPredicate = new HighDimensionPredicate(baseAspect);
			} else if (dimension.equals(AData.ODNOMERNOST)) {
				dimensionPredicate = new Dimension1Predicate(baseAspect);
			} else if (dimension.equals(AData.INDIVIDUALNOST)) {
				dimensionPredicate = new IndividualityPredicate(baseAspect);
			} else {
				throw new IllegalArgumentException("Illegal dimension in SocionicsType.matches()");
			}
			predicates.add(dimensionPredicate);
		}

		if (mv != null) {
			Predicate mvPredicate;
			if (mv.equals(AData.MENTAL)) {
				mvPredicate = new MentalPredicate(baseAspect);
			} else if (mv.equals(AData.VITAL)) {
				mvPredicate = new VitalPredicate(baseAspect);
			} else if (mv.equals(AData.SUPEREGO)) {
				mvPredicate = new SuperegoPredicate(baseAspect);
			} else if (mv.equals(AData.SUPERID)) {
				mvPredicate = new SuperidPredicate(baseAspect);
			} else {
				throw new IllegalArgumentException("Illegal mv in SocionicsType.matches()");
			}
			predicates.add(mvPredicate);
		}

		return predicates;
	}
}
