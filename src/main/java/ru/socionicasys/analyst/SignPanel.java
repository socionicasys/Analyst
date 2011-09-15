package ru.socionicasys.analyst;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Панель знака («Плюс»/«Минус»).
 */
public class SignPanel extends JPanel implements PropertyChangeListener, ItemListener {
	private final DocumentSelectionModel selectionModel;
	private final Map<String, JRadioButton> buttons;
	private final ButtonGroup buttonGroup;
	private final JButton clearButton;

	public SignPanel(DocumentSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
		this.selectionModel.addPropertyChangeListener(this);

		buttons = new HashMap<String, JRadioButton>(2);
		buttons.put(AData.PLUS, new JRadioButton("+"));
		buttons.put(AData.MINUS, new JRadioButton("-"));

		buttonGroup = new ButtonGroup();
		for (Map.Entry<String, JRadioButton> entry : buttons.entrySet()) {
			String buttonKey = entry.getKey();
			JRadioButton button = entry.getValue();
			button.addItemListener(this);
			button.setActionCommand(buttonKey);
			buttonGroup.add(button);
		}

		clearButton = new JButton("Очистить");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonGroup.clearSelection();
			}
		});

		Panel pp = new Panel();
		pp.setMaximumSize(new Dimension(100, 50));
		pp.setPreferredSize(new Dimension(100, 50));

		setMinimumSize(new Dimension(200, 80));
		setPreferredSize(new Dimension(200, 80));
		setMaximumSize(new Dimension(200, 80));

		pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
		pp.add(buttons.get(AData.PLUS));
		pp.add(buttons.get(AData.MINUS));

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		add(pp);
		add(clearButton);
		setBorder(new TitledBorder("Знак"));

		updateView();
	}

	@Deprecated
	public String getSignSelection() {
		ButtonModel bm = buttonGroup.getSelection();
		if (bm == null) {
			return null;
		}
		return bm.getActionCommand();
	}

	@Deprecated
	public void setSign(String sign) {
		if (sign == null) {
			buttonGroup.clearSelection();
		} else if (buttons.containsKey(sign)) {
			buttonGroup.setSelected(buttons.get(sign).getModel(), true);
		}

		if (buttonGroup.getSelection() != null) {
			clearButton.setEnabled(true);
		} else {
			clearButton.setEnabled(false);
		}
	}

	/**
	 * Обрабатывает изменение свойств в модели выделения, к которой привязана эта панель.
	 * Включает/отключает панель и меняет состояние элементов при изменениях в выделении.
	 *
	 * @param evt параметр не используется
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		updateView();
	}

	/**
	 * Обновляет элементы управления панели в соответствии со связанными данными из модели выделения.
	 */
	private void updateView() {
		String sign = selectionModel.getSign();
		boolean panelEnabled = !selectionModel.isEmpty() && !selectionModel.isMarkupEmpty();
		boolean selectionEnabled = panelEnabled && sign != null;

		for (JRadioButton button : buttons.values()) {
			button.setEnabled(panelEnabled);
		}
		clearButton.setEnabled(selectionEnabled);

		if (selectionEnabled) {
			JRadioButton selectedButton = buttons.get(sign);
			buttonGroup.setSelected(selectedButton.getModel(), true);
		} else {
			buttonGroup.clearSelection();
		}

	}

	/**
	 * Обрабатывает изменение в состоянии этой панели и отображает их на модель выделения,
	 * к которой панель привязана.
	 *
	 * @param e параметр не используется
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		ButtonModel selectedButtonModel = buttonGroup.getSelection();
		if (selectedButtonModel == null) {
			selectionModel.setSign(null);
		} else {
			selectionModel.setSign(selectedButtonModel.getActionCommand());
		}
	}
}
