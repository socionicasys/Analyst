package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import javax.swing.*;

/**
 * Главный класс, точка входа в «Информационный аналитик».
 */
public class Analyst implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Analyst.class);
	private final String startupFilename;

	/**
	 * Создает экземпляр приложения
	 * @param startupFilename имя файла для начальной загрузки
	 */
	public Analyst(String startupFilename) {
		this.startupFilename = startupFilename;
	}

	public static void main(String[] args) {
		logger.trace("> main()");
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
		String startupFilename;
		if (args != null && args.length > 0) {
			startupFilename = args[0];
		} else {
			startupFilename = null;
		}
		SwingUtilities.invokeLater(new Analyst(startupFilename));
		logger.trace("< main()");
	}

	@Override
	public void run() {
		logger.trace("> run(), startupFilename={}", startupFilename);

		// Язык приложения: русский
		// TODO: возможность менять язык
		Locale defaultLocale = new Locale("ru");
		Locale.setDefault(defaultLocale);

		UIDefaults uiDefaults = UIManager.getDefaults();
		uiDefaults.setDefaultLocale(defaultLocale);
		uiDefaults.addResourceBundle("ru.socionicasys.analyst.messages");
		uiDefaults.put("swing.boldMetal", false);

		final AnalystWindow analystWindow = new AnalystWindow();
		if (startupFilename != null) {
			analystWindow.openFile(startupFilename, false);
		}

		//Display the window.
		analystWindow.setVisible(true);
		logger.trace("< run()");
	}
}
