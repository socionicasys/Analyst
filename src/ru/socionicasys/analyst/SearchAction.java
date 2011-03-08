package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

public class SearchAction extends AbstractAction {
	private ADocument aDoc;
	private JTextComponent component;
	private JPanel mainPanel;
	private JTextArea searchQuote;
	private JCheckBox caseCheckbox;
	private JLabel status;
	private ButtonGroup bg;
	private static final Logger logger = LoggerFactory.getLogger(SearchAction.class);

	public SearchAction(JTextComponent component, ADocument aDoc) {
		super();
		putValue(Action.NAME, "Поиск");
		this.aDoc = aDoc;
		this.component = component;

		JRadioButton forwardDirection = new JRadioButton("Вперед");
		forwardDirection.setActionCommand("f");
		JRadioButton backwardDirection = new JRadioButton("Назад");
		backwardDirection.setActionCommand("b");
		bg = new ButtonGroup();
		bg.add(forwardDirection);
		bg.add(backwardDirection);
		forwardDirection.setSelected(true);
		JButton searchButton = new JButton("Искать");
		searchQuote = new JTextArea(3, 30);
		searchQuote.setMaximumSize(new Dimension(400, 100));
		searchQuote.setMinimumSize(new Dimension(400, 100));
		searchQuote.setLineWrap(true);
		searchQuote.setWrapStyleWord(true);
		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		JPanel p3 = new JPanel();
		mainPanel = new JPanel();
		caseCheckbox = new JCheckBox("С учетом регистра");
		caseCheckbox.setSelected(false);

		status = new JLabel("   "); //("Введите строку поиска и нажмите кнопку \"Искать\"");
		p2.setLayout(new BorderLayout());
		JScrollPane scrl = new JScrollPane(searchQuote);
		scrl.setBorder(new TitledBorder("Строка для поиска:"));
		p1.add(scrl, BorderLayout.CENTER);

		p2.setLayout(new BorderLayout());
		p2.add(forwardDirection, BorderLayout.NORTH);
		p2.add(backwardDirection, BorderLayout.SOUTH);

		p3.setLayout(new BorderLayout());
		p3.add(p2, BorderLayout.EAST);
		p3.add(caseCheckbox, BorderLayout.WEST);
		p3.add(searchButton, BorderLayout.CENTER);

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(p1);
		mainPanel.add(p2);
		mainPanel.add(p3);
		mainPanel.add(status);

		searchButton.addActionListener(this);
	}

	public void showSearchDialog() {
		JOptionPane.showOptionDialog(null,
			mainPanel,
			"Поиск",
			JOptionPane.OK_OPTION,
			JOptionPane.PLAIN_MESSAGE,
			null,
			new Object[]{"Закрыть"},
			null);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("Поиск")) {
			status.setText("");
			showSearchDialog();
		} else {
			boolean caseSensitive = caseCheckbox.isSelected();
			boolean forward = true;
			if ((bg.getSelection()).getActionCommand().equals("f")) {
				forward = true;
			}
			if ((bg.getSelection()).getActionCommand().equals("b")) {
				forward = false;
			}

			int searchOffset = component.getCaret().getDot();

			int searchResult = -1;
			String text = "";
			try {
				text = aDoc.getText(0, aDoc.getLength());
			} catch (BadLocationException e) {
				logger.error("Illegal document position in actionPerformed()", e);
			}

			if (forward) {
				searchOffset = Math.min(searchOffset + 1, text.length());
			} else {
				searchOffset = Math.max(0, searchOffset - 1);
			}

			String searchText = searchQuote.getText();

			if (!caseSensitive) {
				text = text.toLowerCase();
				searchText = searchText.toLowerCase();
			}

			int dot = searchOffset;
			int mark = searchOffset;

			if (forward) {
				searchResult = text.indexOf(searchText, searchOffset);
				if (searchResult > 0) {
					searchOffset = Math.min(searchResult + searchText.length(), text.length());
					dot = searchOffset;
					mark = searchResult;
				}
			} else {
				searchResult = (text.substring(0, searchOffset)).lastIndexOf(searchText);
				if (searchResult > 0) {
					searchOffset = searchResult;
					dot = Math.min(searchResult + searchText.length(), text.length());
					mark = searchOffset;
				}
			}

			if (searchResult >= 0) {
				status.setText("Найдено позиция: " + searchResult);
				try {
					Rectangle rect = component.modelToView(searchResult);
					((JViewport) component.getParent()).scrollRectToVisible(rect);
					component.getCaret().setDot(dot);
					component.getCaret().moveDot(mark);
					component.requestFocus();
					status.setText("");
				} catch (BadLocationException e1) {
					logger.error("SearchPane: error setting model to view :: bad location", e1);
				}
			} else {
				status.setText("       ...cтрока не найдена...");
			}
			searchQuote.selectAll();
			searchQuote.requestFocus();
		}
	}
}
