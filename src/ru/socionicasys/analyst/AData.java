package ru.socionicasys.analyst;

import java.io.Serializable;

public class AData implements Serializable {
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

	public AData(String aspect, String sign, String dimension, String mv, String comment) throws ADataException {
		setAspect(aspect);
		setSign(sign);
		setDimension(dimension);
		setMV(mv);
		this.comment = comment;
	}

	public void setAspect(String s) throws ADataException {
		if (s == null) {
			throw new ADataException();
		}
		if (!(isValidAspect(s) || s.equals(DOUBT))) {
			throw new ADataException();
		}
		this.aspect = s;
	}

	public static boolean isValidAspect(String s) {
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

	public String getModifier() {
		return modifier;
	}

	public String getSecondAspect() {
		return secondAspect;
	}

	public void setSign(String s) throws ADataException {
		if (s == null) {
			sign = null;
			return;
		}
		if (!(s.equals(PLUS) || s.equals(MINUS))) {
			throw new ADataException();
		}
		this.sign = s;
	}

	public void setDimension(String s) throws ADataException {
		if (s == null) {
			dimension = null;
			return;
		}
		if (!(s.equals(D1)
			|| s.equals(D2)
			|| s.equals(D3)
			|| s.equals(D4)
			|| s.equals(MALOMERNOST)
			|| s.equals(MNOGOMERNOST)
			|| s.equals(ODNOMERNOST)
			|| s.equals(INDIVIDUALNOST)
		)) {
			throw new ADataException();
		}
		this.dimension = s;
	}

	public void setMV(String s) throws ADataException {
		if (s == null) {
			mv = null;
			return;
		}
		if (!(s.equals(MENTAL)
			|| s.equals(VITAL)
			|| s.equals(SUPEREGO)
			|| s.equals(SUPERID)
		)) {
			throw new ADataException();
		}
		this.mv = s;
	}

	public void setComment(String s) {
		this.comment = s;
	}

	public String getAspect() {
		return aspect;
	}

	public String getSign() {
		return sign;
	}

	public String getMV() {
		return mv;
	}

	public String getDimension() {
		return dimension;
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

	public static AData parseAData(String s) throws ADataException {
		if (s == null) {
			return null;
		}

		String aspect = null;
		String sign = null;
		String mv = null;
		String dimension = null;
		String comment = null;
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
		if (s.contains(PLUS)) {
			sign = PLUS;
		} else if (s.contains(MINUS)) {
			sign = MINUS;
		}

		AData data = new AData(aspect, sign, dimension, mv, comment);

		if (mod != null && AData.isValidAspect(sa)) {
			data.setModifier(mod);
			data.secondAspect = sa;
		}

		return data;
	}

	private void setModifier(String mod) {
		if (mod == null) {
			return;
		}
		if (!(mod.equals(BLOCK) || mod.equals(JUMP))) {
			return;
		}
		this.modifier = mod;
	}

	public class ADataException extends Exception implements Serializable {
		ADataException() {
			super("Invalid argument");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AData)) return false;
		AData d = (AData) obj;
		if ((aspect != null && aspect.equals(d.getAspect()) || (aspect == null && d.getAspect() == null)) &&
			(secondAspect != null && secondAspect.equals(d.getSecondAspect()) || (secondAspect == null && d.getSecondAspect() == null)) &&
			(modifier != null && modifier.equals(d.getModifier()) || (modifier == null && d.getModifier() == null)) &&
			(dimension != null && dimension.equals(d.getDimension()) || (dimension == null && d.getDimension() == null)) &&
			(sign != null && sign.equals(d.getSign()) || (sign == null && d.getSign() == null)) &&
			(mv != null && mv.equals(d.getMV()) || (mv == null && d.getMV() == null)) &&
			(comment != null && comment.equals(d.getComment()) || (comment == null && d.getComment() == null))
			) return true;
		return false;
	}
} //class AData
