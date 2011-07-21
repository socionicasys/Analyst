package ru.socionicasys.analyst;

import ru.socionicasys.analyst.util.EqualsUtil;
import ru.socionicasys.analyst.util.HashUtil;

import java.util.Arrays;

public class AData {
	public final static String L = "БЛ";
	public final static String P = "ЧЛ";
	public final static String R = "БЭ";
	public final static String E = "ЧЭ";
	public final static String S = "БС";
	public final static String F = "ЧС";
	public final static String T = "БИ";
	public final static String I = "ЧИ";

	public final static String DOUBT = "Фрагмент требует уточнения";

	public final static String BLOCK = "БЛОК";
	public final static String BLOCK_TOKEN = "~";
	public final static String JUMP = "ПЕРЕВОД";
	public final static String JUMP_TOKEN = ">";
	public final static String SEPARATOR = ";";

	public final static String PLUS = "ПЛЮС";
	public final static String MINUS = "МИНУС";
	public final static String MENTAL = "Ментал";
	public final static String VITAL = "Витал";
	public final static String D1 = "Размерность ОПЫТ";
	public final static String D2 = "Размерность НОРМА";
	public final static String D3 = "Размерность СИТУАЦИЯ";
	public final static String D4 = "Размерность ВРЕМЯ";
	public final static String ODNOMERNOST = "Одномерность";
	public final static String INDIVIDUALNOST = "Индивидуальность";
	public final static String MALOMERNOST = "Маломерность";
	public final static String MNOGOMERNOST = "Многомерность";

	public final static String SUPERID = "Супер-Ид";
	public final static String SUPEREGO = "Супер-Эго";

	private String secondAspect = null;
	private String modifier = null;
	private String aspect;
	private String sign;
	private String mv;
	private String dimension;
	private String comment;

	public AData(String aspect, String sign, String dimension, String mv, String comment) {
		if (aspect == null) {
			throw new IllegalArgumentException("Aspect cannot be null");
		}
		if (!(isValidAspect(aspect) || aspect.equals(DOUBT))) {
			throw new IllegalArgumentException(String.format("Invalid aspect value (%s)", aspect));
		}
		this.aspect = aspect;

		if (sign == null) {
			this.sign = null;
		} else if (!(sign.equals(PLUS) || sign.equals(MINUS))) {
			throw new IllegalArgumentException();
		}
		this.sign = sign;

		if (dimension == null) {
			this.dimension = null;
		} else if (!Arrays.asList(D1, D2, D3, D4, MALOMERNOST, MNOGOMERNOST, ODNOMERNOST, INDIVIDUALNOST).contains(dimension)) {
			throw new IllegalArgumentException();
		}
		this.dimension = dimension;

		if (mv == null) {
			this.mv = null;
		} else if (!Arrays.asList(MENTAL, VITAL, SUPERID, SUPEREGO).contains(mv)) {
			throw new IllegalArgumentException();
		}
		this.mv = mv;

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

	public void setComment(String s) {
		this.comment = s;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		String res = aspect;
		if (modifier != null && modifier.equals(BLOCK)) {
			res += BLOCK_TOKEN + secondAspect + SEPARATOR;
		} else if (modifier != null && modifier.equals(JUMP)) {
			res += JUMP_TOKEN + secondAspect + SEPARATOR;
		} else {
			res += SEPARATOR;
		}

		if (sign != null) {
			res += (sign + SEPARATOR);
		}
		if (dimension != null) {
			res += (dimension + SEPARATOR);
		}
		if (mv != null) {
			res += mv;
		}
		return res;
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
