package ru.socionicasys.analyst.types;

/**
 * Описывает отдельную функцию в модели А.
 */
public class Function {
	/**
	 * Аспект функции.
	 */
	private Aspect aspect;

	/**
	 * Расположении функции в модели: 1-8.
	 */
	private int position;

	/**
	 * Знак функции.
	 */
	private Sign sign;

	public Function(Aspect aspect, int position, Sign sign)
	{
		this.aspect = aspect;
		if (position >= 1 && position <= 8) {
			this.position = position;
		} else {
			throw new IllegalArgumentException("Illegation position for socionics function.");
		}
		this.sign = sign;
	}

	/**
	 * @return аспект функции
	 */
	public Aspect getAspect() {
		return aspect;
	}

	/**
	 * @return позиция функции в модели А (1-8)
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @return знак функции
	 */
	public Sign getSign() {
		return sign;
	}

	/**
	 * Рассчитывает размерность функции.
	 * 1-я и 8-я функции 4-х мерные, 2-я и 7-я — 3-х мерные, 3-я и 6-я — 2-х мерные, 4-я и 5-я — одномерные.
	 * @return размерность функции
	 */
	public int getDimension() {
		if (isMental()) {
			return 5 - position;
		} else {
			return position - 4;
		}
	}

	/**
	 * Проверка функции на ментальность (1-4) или витальность (5-8).
	 * @return является ли функция ментальной
	 */
	public boolean isMental() {
		return (position <= 4);
	}
}
