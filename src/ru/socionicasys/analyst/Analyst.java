package ru.socionicasys.analyst;

import javax.swing.*;

public class Analyst {
	public static void main(String[] args) {
		final String startupFilename;
		if (args != null && args.length > 0) {
			startupFilename = args[0];
		} else {
			startupFilename = null;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI(startupFilename);
			}
		});
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
		final AnalystWindow frame = new AnalystWindow(startupFilename);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
}
