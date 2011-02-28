package analyst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

public class SearchAction extends AbstractAction {

	private ADocument aDoc;
	private JTextComponent component;
	private JPanel mainPanel;
	private JViewport viewport;
	private JRadioButton forwardDirection, backwardDirection;
	private JButton searchButton;
	private JTextArea searchQuote;
	private JCheckBox caseCheckbox;
	private JLabel status;
	private ButtonGroup bg;
	private int searchOffset = 0;
	private int direction = -1;
	private boolean forward = true;
	private boolean caseSensitive = false;


	// constructor
	public SearchAction(JTextComponent component, ADocument aDoc) {
		super();
		putValue(Action.NAME, "Поиск");
		this.aDoc = aDoc;
		this.component = component;
		viewport = (JViewport) component.getParent();
		forwardDirection = new JRadioButton("Вперед");
		forwardDirection.setActionCommand("f");
		backwardDirection = new JRadioButton("Назад");
		backwardDirection.setActionCommand("b");
		bg = new ButtonGroup();
		bg.add(forwardDirection);
		bg.add(backwardDirection);
		forwardDirection.setSelected(true);
		searchButton = new JButton("Искать");
		searchQuote = new JTextArea(3, 30);
		searchQuote.setMaximumSize(new Dimension(400, 100));
		searchQuote.setMinimumSize(new Dimension(400, 100));
		searchQuote.setLineWrap(true);
		searchQuote.setWrapStyleWord(true);
		JLabel searchLabel = new JLabel("Текст для поиска:");
		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		JPanel p3 = new JPanel();
		mainPanel = new JPanel();
		caseCheckbox = new JCheckBox("С учетом регистра");
		caseCheckbox.setSelected(false);

		//status.setForeground(Color.GRAY);
		status = new JLabel("   "); //("Введите строку поиска и нажмите кнопку \"Искать\"");
		p2.setLayout(new BorderLayout());
		//p1.add(searchLabel, BorderLayout.EAST);
		JScrollPane scrl = new JScrollPane(searchQuote);
		scrl.setBorder(new TitledBorder("Строка для поиска:"));
		p1.add(scrl, BorderLayout.CENTER);

		//p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
		p2.setLayout(new BorderLayout());
		p2.add(forwardDirection, BorderLayout.NORTH);
		p2.add(backwardDirection, BorderLayout.SOUTH);

		p3.setLayout(new BorderLayout());
		p3.add(p2, BorderLayout.EAST);
		p3.add(caseCheckbox, BorderLayout.WEST);
		p3.add(searchButton, BorderLayout.CENTER);
		///p3.add(status, BorderLayout.SOUTH);

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(p1);
		mainPanel.add(p2);
		mainPanel.add(p3);
		mainPanel.add(status);

		//mainPanel.add(status);

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
			caseSensitive = caseCheckbox.isSelected();

			if ((bg.getSelection()).getActionCommand().equals("f")) forward = true;
			if ((bg.getSelection()).getActionCommand().equals("b")) forward = false;

			searchOffset = component.getCaret().getDot();

			int searchResult = -1;
			String text = "";
			try {
				text = aDoc.getText(0, aDoc.getLength());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (forward) searchOffset = Math.min(searchOffset + 1, text.length());
			if (!forward) searchOffset = Math.max(0, searchOffset - 1);

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
			}

			if (!forward) {
				searchResult = (text.substring(0, searchOffset)).lastIndexOf(searchText);
				if (searchResult > 0) {
					searchOffset = searchResult;
					dot = Math.min(searchResult + searchText.length(), text.length());
					mark = searchOffset;
				}
			}

			if (searchResult >= 0) {
				status.setText("Найдено позиция: " + searchResult);
				//searchOffset = searchResult;
				try {
					//aDoc.replace(searchResult, searchText.length(), searchText, aDoc.defaultSearchHighlightAttributes);
					Rectangle rect = component.modelToView(searchResult);
					viewport.scrollRectToVisible(rect);

					component.getCaret().setDot(dot);
					component.getCaret().moveDot(mark);
					component.requestFocus();
					status.setText("");
				} catch (BadLocationException e1) {
					//
					System.out.println("SearchPane: error setting model to view :: bad location");
				}
			} else {
				status.setText("       ...cтрока не найдена...");
			}
			searchQuote.selectAll();
			searchQuote.requestFocus();
		}
	}
}
