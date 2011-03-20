package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class Analyst {
	private static final Logger logger = LoggerFactory.getLogger(Analyst.class);

	public static void main(String[] args) {
		logger.trace("> main()");
		final String startupFilename;
		if (args != null && args.length > 0) {
			startupFilename = args[0];
		} else {
			startupFilename = null;
		}
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI(startupFilename);
			}
		});
		logger.trace("< main()");
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 *
	 * @param startupFilename
	 */
	private static void createAndShowGUI(String startupFilename) {
		//Create and set up the window.
		logger.trace("> createAndShowGUI(), startupFilename={}", startupFilename);
		final AnalystWindow analystWindow = new AnalystWindow(startupFilename);

		//Display the window.
		analystWindow.setVisible(true);
		logger.trace("< createAndShowGUI()");
	}
}
