package ru.socionicasys.analyst;

/**
 * Информация о имени/версии/билде.
 */
public class VersionInfo {
	private static final String APPLICATION_NAME = "Информационный анализ";
	private static String specificationVersion = null;

	/**
	 * @return Имя приложения
	 */
	public static String getApplicationName() {
		return APPLICATION_NAME;
	}

	/**
	 * @return Версия приложения
	 */
	public static String getVersion() {
		return getSpecificationVersion();
	}

	private static String getSpecificationVersion() {
		if (specificationVersion == null) {
			specificationVersion = VersionInfo.class.getPackage().getSpecificationVersion();
			if (specificationVersion == null) {
				specificationVersion = "";
			}
		}
		return specificationVersion;
	}
}
