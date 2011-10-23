package ru.socionicasys.analyst;

import ru.socionicasys.analyst.util.EqualsUtil;
import ru.socionicasys.analyst.util.HashUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AData implements Serializable {
	private static final long serialVersionUID = -7524659842673948203L;

	public static final String L = "БЛ";
	public static final String P = "ЧЛ";
	public static final String R = "БЭ";
	public static final String E = "ЧЭ";
	public static final String S = "БС";
	public static final String F = "ЧС";
	public static final String T = "БИ";
	public static final String I = "ЧИ";
	private static final List<String> VALID_ASPECTS = Arrays.asList(L, P, R, E, S, F, T, I);

	public static final String DOUBT = "Фрагмент требует уточнения";

	public static final String BLOCK = "БЛОК";
	public static final String JUMP = "ПЕРЕВОД";
	private static final List<String> VALID_MODIFIERS = Arrays.asList(BLOCK, JUMP);

	private static final String BLOCK_TOKEN = "~";
	private static final String JUMP_TOKEN = ">";
	private static final String SEPARATOR = ";";

	public static final String PLUS = "ПЛЮС";
	public static final String MINUS = "МИНУС";
	private static final List<String> VALID_SIGNS = Arrays.asList(PLUS, MINUS);

	public static final String D1 = "Размерность ОПЫТ";
	public static final String D2 = "Размерность НОРМА";
	public static final String D3 = "Размерность СИТУАЦИЯ";
	public static final String D4 = "Размерность ВРЕМЯ";
	public static final String ODNOMERNOST = "Одномерность";
	public static final String INDIVIDUALNOST = "Индивидуальность";
	public static final String MALOMERNOST = "Маломерность";
	public static final String MNOGOMERNOST = "Многомерность";
	private static final List<String> VALID_DIMENSIONS = Arrays.asList(D1, D2, D3, D4, MALOMERNOST, MNOGOMERNOST, ODNOMERNOST, INDIVIDUALNOST);

	public static final String MENTAL = "Ментал";
	public static final String VITAL = "Витал";
	public static final String SUPERID = "Супер-Ид";
	public static final String SUPEREGO = "Супер-Эго";
	private static final List<String> VALID_MVS = Arrays.asList(MENTAL, VITAL, SUPERID, SUPEREGO);

	private static final Pattern parsePattern = buildParsePattern();
	private static final int ASPECT_GROUP = 1;
	private static final int MODIFIER_GROUP = 2;
	private static final int SECOND_ASPECT_GROUP = 3;
	private static final int SIGN_GROUP = 4;
	private static final int DIMENSION_GROUP = 5;
	private static final int MV_GROUP = 6;

	private final String secondAspect;
	private final String modifier;
	private final String aspect;
	private final String sign;
	private final String mv;
	private final String dimension;
	private String comment;

	public AData(String aspect, String secondAspect, String sign, String dimension, String mv, String modifier, String comment) {
		if (VALID_ASPECTS.contains(aspect) || DOUBT.equals(aspect)) {
			this.aspect = aspect;
		} else {
			throw new IllegalArgumentException(String.format("Invalid aspect value (%s)", aspect));
		}

		if (secondAspect == null || VALID_ASPECTS.contains(secondAspect)) {
			this.secondAspect = secondAspect;
		} else {
			throw new IllegalArgumentException();
		}

		if (sign == null || VALID_SIGNS.contains(sign)) {
			this.sign = sign;
		} else {
			throw new IllegalArgumentException();
		}

		if (dimension == null || VALID_DIMENSIONS.contains(dimension)) {
			this.dimension = dimension;
		} else {
			throw new IllegalArgumentException();
		}

		if (mv == null || VALID_MVS.contains(mv)) {
			this.mv = mv;
		} else {
			throw new IllegalArgumentException();
		}

		if (modifier == null || VALID_MODIFIERS.contains(modifier)) {
			this.modifier = modifier;
		} else {
			throw new IllegalArgumentException();
		}

		setComment(comment);
	}

	public String getAspect() {
		return aspect;
	}

	public String getModifier() {
		return modifier;
	}

	public String getSecondAspect() {
		return secondAspect;
	}

	public String getSign() {
		return sign;
	}

	public String getDimension() {
		return dimension;
	}

	public String getMV() {
		return mv;
	}

	public void setComment(String comment) {
		this.comment = comment == null ? "" : comment;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(aspect);
		if (BLOCK.equals(modifier)) {
			builder.append(BLOCK_TOKEN).append(secondAspect).append(SEPARATOR);
		} else if (JUMP.equals(modifier)) {
			builder.append(JUMP_TOKEN).append(secondAspect).append(SEPARATOR);
		} else {
			builder.append(SEPARATOR);
		}

		if (sign != null) {
			builder.append(sign).append(SEPARATOR);
		}
		if (dimension != null) {
			builder.append(dimension).append(SEPARATOR);
		}
		if (mv != null) {
			builder.append(mv);
		}
		return builder.toString();
	}

	public static AData parseAData(String s) {
		if (s == null) {
			throw new NullPointerException();
		}

		Matcher dataMatcher = parsePattern.matcher(s);
		if (!dataMatcher.matches()) {
			throw new IllegalArgumentException(String.format("Invalid markup data '%s'", s));
		}
		
		String aspect = dataMatcher.group(ASPECT_GROUP);
		String modifierToken = dataMatcher.group(MODIFIER_GROUP);
		String secondAspect = dataMatcher.group(SECOND_ASPECT_GROUP);
		String sign = dataMatcher.group(SIGN_GROUP);
		String dimension = dataMatcher.group(DIMENSION_GROUP);
		String mv = dataMatcher.group(MV_GROUP);

		String modifier = null;
		if (BLOCK_TOKEN.equals(modifierToken)) {
			modifier = BLOCK;
		} else if (JUMP_TOKEN.equals(modifierToken)) {
			modifier = JUMP;
		}

		return new AData(aspect, secondAspect, sign, dimension, mv, modifier, null);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AData)) {
			return false;
		}

		AData data = (AData) obj;
		return EqualsUtil.areEqual(aspect, data.aspect) &&
			EqualsUtil.areEqual(secondAspect, data.secondAspect) &&
			EqualsUtil.areEqual(modifier, data.modifier) &&
			EqualsUtil.areEqual(dimension, data.dimension) &&
			EqualsUtil.areEqual(sign, data.sign) &&
			EqualsUtil.areEqual(mv, data.mv) &&
			EqualsUtil.areEqual(comment, data.comment);
	}

	@Override
	public int hashCode() {
		HashUtil hashUtil = new HashUtil();
		hashUtil.hash(aspect);
		hashUtil.hash(secondAspect);
		hashUtil.hash(modifier);
		hashUtil.hash(dimension);
		hashUtil.hash(sign);
		hashUtil.hash(mv);
		hashUtil.hash(comment);
		return hashUtil.getComputedHash();
	}

	/**
	 * Формирует регулярное выражение для разбора строк в экземпляр {@code AData}.
	 *
	 * @return сформированное регулярное выражение
	 */
	private static Pattern buildParsePattern() {
		StringBuilder patternBuilder = new StringBuilder(" *(");
		patternBuilder.append(joinRegexValues(VALID_ASPECTS));
		patternBuilder.append('|').append(DOUBT).append(')');

		patternBuilder.append("(?:([").append(BLOCK_TOKEN).append(JUMP_TOKEN).append("])");
		patternBuilder.append('(').append(joinRegexValues(VALID_ASPECTS)).append("))?;");

		patternBuilder.append("(?:(").append(joinRegexValues(VALID_SIGNS)).append(");)?");

		patternBuilder.append("(?:(").append(joinRegexValues(VALID_DIMENSIONS)).append(");)?");

		patternBuilder.append('(').append(joinRegexValues(VALID_MVS)).append(")?");

		return Pattern.compile(patternBuilder.toString());
	}

	/**
	 * Объединяет варианты из массива в строку, разделенную символами |
	 * для создания регулярного выражения.
	 *
	 * @param values массив значений
	 * @return строка из значений через |
	 */
	private static String joinRegexValues(List<String> values) {
		StringBuilder join = new StringBuilder();
		for (String value : values) {
			join.append(value).append('|');
		}
		join.deleteCharAt(join.length() - 1);
		return join.toString();
	}
}
