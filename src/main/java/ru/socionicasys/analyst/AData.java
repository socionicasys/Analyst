package ru.socionicasys.analyst;

import ru.socionicasys.analyst.util.EqualsUtil;
import ru.socionicasys.analyst.util.HashUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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

	public static final String DOUBT = "Фрагмент требует уточнения";

	public static final String BLOCK = "БЛОК";
	public static final String BLOCK_TOKEN = "~";
	public static final String JUMP = "ПЕРЕВОД";
	public static final String JUMP_TOKEN = ">";
	public static final String SEPARATOR = ";";

	public static final String PLUS = "ПЛЮС";
	public static final String MINUS = "МИНУС";
	public static final List<String> VALID_SIGNS = Arrays.asList(PLUS, MINUS);

	public static final String D1 = "Размерность ОПЫТ";
	public static final String D2 = "Размерность НОРМА";
	public static final String D3 = "Размерность СИТУАЦИЯ";
	public static final String D4 = "Размерность ВРЕМЯ";
	public static final String ODNOMERNOST = "Одномерность";
	public static final String INDIVIDUALNOST = "Индивидуальность";
	public static final String MALOMERNOST = "Маломерность";
	public static final String MNOGOMERNOST = "Многомерность";
	public static final List<String> VALID_DIMENSIONS = Arrays.asList(D1, D2, D3, D4, MALOMERNOST, MNOGOMERNOST, ODNOMERNOST, INDIVIDUALNOST);

	public static final String MENTAL = "Ментал";
	public static final String VITAL = "Витал";
	public static final String SUPERID = "Супер-Ид";
	public static final String SUPEREGO = "Супер-Эго";
	public static final List<String> VALID_MVS = Arrays.asList(MENTAL, VITAL, SUPERID, SUPEREGO);

	private String secondAspect;
	private String modifier;
	private final String aspect;
	private final String sign;
	private final String mv;
	private final String dimension;
	private String comment;

	public AData(String aspect, String sign, String dimension, String mv, String comment) {
		if (aspect == null) {
			throw new IllegalArgumentException("Aspect cannot be null");
		}
		if (!(isValidAspect(aspect) || aspect.equals(DOUBT))) {
			throw new IllegalArgumentException(String.format("Invalid aspect value (%s)", aspect));
		}
		this.aspect = aspect;

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

		this.comment = comment;
	}

	private static boolean isValidAspect(String s) {
		if (s == null) {
			return false;
		}
		return s.equals(L)
			|| s.equals(P)
			|| s.equals(R)
			|| s.equals(E)
			|| s.equals(S)
			|| s.equals(F)
			|| s.equals(T)
			|| s.equals(I);
	}

	public String getAspect() {
		return aspect;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getModifier() {
		return modifier;
	}

	public void setSecondAspect(String secondAspect) {
		this.secondAspect = secondAspect;
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
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(aspect);
		if (modifier != null && modifier.equals(BLOCK)) {
			builder.append(BLOCK_TOKEN).append(secondAspect).append(SEPARATOR);
		} else if (modifier != null && modifier.equals(JUMP)) {
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
			return null;
		}

		String sa = null;
		String mod = null;

		//detecting aspect
		if (s.contains(BLOCK_TOKEN)) {
			int index1 = s.indexOf(BLOCK_TOKEN) + BLOCK_TOKEN.length();
			int index2 = s.indexOf(SEPARATOR);
			String a2 = s.substring(index1, index2);
			if (isValidAspect(a2)) {
				sa = a2;
				s = s.replace(BLOCK_TOKEN + a2, "");
				mod = BLOCK;
			}
		} else if (s.contains(JUMP_TOKEN)) {
			int index1 = s.indexOf(JUMP_TOKEN) + JUMP_TOKEN.length();
			int index2 = s.indexOf(SEPARATOR);
			String a2 = s.substring(index1, index2);
			if (isValidAspect(a2)) {
				sa = a2;
				s = s.replace(JUMP_TOKEN + a2, "");
				mod = JUMP;
			}
		}

		String aspect = null;
		if (s.contains(L)) {
			aspect = L;
		} else if (s.contains(P)) {
			aspect = P;
		} else if (s.contains(R)) {
			aspect = R;
		} else if (s.contains(E)) {
			aspect = E;
		} else if (s.contains(S)) {
			aspect = S;
		} else if (s.contains(F)) {
			aspect = F;
		} else if (s.contains(T)) {
			aspect = T;
		} else if (s.contains(I)) {
			aspect = I;
		}

		if (s.contains(DOUBT)) {
			aspect = DOUBT;
		}

		if (aspect == null) {
			return null;
		}

		//detecting mental\vital
		String mv = null;
		if (s.contains(MENTAL)) {
			mv = MENTAL;
		} else if (s.contains(VITAL)) {
			mv = VITAL;
		} else if (s.contains(SUPEREGO)) {
			mv = SUPEREGO;
		} else if (s.contains(SUPERID)) {
			mv = SUPERID;
		}

		//detecting dimension
		String dimension = null;
		if (s.contains(D1)) {
			dimension = D1;
		} else if (s.contains(D2)) {
			dimension = D2;
		} else if (s.contains(D3)) {
			dimension = D3;
		} else if (s.contains(D4)) {
			dimension = D4;
		} else if (s.contains(MALOMERNOST)) {
			dimension = MALOMERNOST;
		} else if (s.contains(MNOGOMERNOST)) {
			dimension = MNOGOMERNOST;
		} else if (s.contains(ODNOMERNOST)) {
			dimension = ODNOMERNOST;
		} else if (s.contains(INDIVIDUALNOST)) {
			dimension = INDIVIDUALNOST;
		}

		//detecting sign
		String sign = null;
		if (s.contains(PLUS)) {
			sign = PLUS;
		} else if (s.contains(MINUS)) {
			sign = MINUS;
		}

		AData data = new AData(aspect, sign, dimension, mv, null);

		if (Arrays.asList(BLOCK, JUMP).contains(mod) && AData.isValidAspect(sa)) {
			data.modifier = mod;
			data.secondAspect = sa;
		}

		return data;
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
}
