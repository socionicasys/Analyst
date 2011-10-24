package ru.socionicasys.analyst.panel;

import ru.socionicasys.analyst.model.AData;
import ru.socionicasys.analyst.model.DocumentSelectionModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Панель знака («Плюс»/«Минус»).
 */
public final class SignPanel extends ActivePanel {
	private final Map<String, JRadioButton> buttons;
	private final ButtonGroup buttonGroup;
	private final JButton clearButton;

	public SignPanel(DocumentSelectionModel selectionModel) {
		super(selectionModel);

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

	/**
	 * Обновляет элементы управления панели в соответствии со связанными данными из модели выделения.
	 */
	@Override
	protected void updateView() {
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
	 * Обновляет модель в соответствии с измененными в панели данными.
	 */
	@Override
	protected void updateModel() {
		ButtonModel selectedButtonModel = buttonGroup.getSelection();
		if (selectedButtonModel == null) {
			selectionModel.setSign(null);
		} else {
			selectionModel.setSign(selectedButtonModel.getActionCommand());
		}
	}
}
