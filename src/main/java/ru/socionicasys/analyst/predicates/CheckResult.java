package ru.socionicasys.analyst.predicates;

/**
 * Результат проверки соответствия социотипа предикату.
 */
public enum CheckResult {
	/**
	 * Соответствие.
	 */
	SUCCESS,

	/**
	 * Несоответствие.
	 */
	FAIL,

	/**
	 * Проверка пропущена — результат не должен влиять ни на соответствия, ни на несоответствия.
	 */
	IGNORE;

	public static CheckResult fromBoolean(boolean checkResult) {
		return checkResult ? SUCCESS : FAIL;
	}
}
