package ru.socionicasys.analyst;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

/**
 * @author Виктор
 */
public class ProgressWindow implements PropertyChangeListener {
	private static final int MIN_VALUE = 0;
	private static final int MAX_VALUE = 100;

	private static final int PROGRESS_WIDTH = 300;
	private static final int PROGRESS_HEIGHT = 30;
	private static final int DIALOG_WIDTH = 500;
	private static final int DIALOG_HEIGHT = 100;

	private final JDialog dialog;
	private final JLabel label;
	private final JProgressBar progressBar;
	private final ADocument document;

	public ProgressWindow(AnalystWindow analystWindow, ADocument document, String message) {
		this.document = document;

		progressBar = new JProgressBar(SwingConstants.HORIZONTAL, MIN_VALUE, MAX_VALUE);
		progressBar.setMaximumSize(new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));
		progressBar.setPreferredSize(new Dimension(PROGRESS_WIDTH, PROGRESS_HEIGHT));

		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.add(new JPanel());
		innerPanel.add(progressBar);
		innerPanel.add(new JPanel());

		JPanel outerPanel = new JPanel(new BorderLayout());
		label = new JLabel(message);
		outerPanel.add(label, BorderLayout.WEST);
		outerPanel.add(innerPanel, BorderLayout.CENTER);

		dialog = new JDialog(analystWindow, "Подождите пожалуйста, выполняется операция...", false);
		dialog.setContentPane(outerPanel);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		dialog.setLocationRelativeTo(analystWindow);
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.setVisible(true);
	}

	public void close() {
		dialog.setVisible(false);
		dialog.dispose();
		document.fireADocumentChanged();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		if ("progress".equals(propertyName)) {
			int value = (Integer) evt.getNewValue();
			if (value >= MIN_VALUE && value <= MAX_VALUE) {
				progressBar.setValue(value);
				label.setText(String.format("      Выполнено :%d%%", value));
			}
		} else if ("state".equals(propertyName)) {
			SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
			if (state == SwingWorker.StateValue.DONE) {
				close();
			}
		}
	}
}
