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

/**
 * Панель знака («Плюс»/«Минус»).
 */
public class SignPanel extends JPanel implements PropertyChangeListener, ItemListener {
	private final DocumentSelectionModel selectionModel;
	private JRadioButton plusButton;
	private JRadioButton minusButton;
	private ButtonGroup signButtonGroup;
	private JButton clearSignSelection;

	public SignPanel(DocumentSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
		this.selectionModel.addPropertyChangeListener(this);

		plusButton = new JRadioButton("+");
		minusButton = new JRadioButton("-");
		plusButton.addItemListener(this);
		plusButton.setActionCommand(AData.PLUS);
		minusButton.addItemListener(this);
		minusButton.setActionCommand(AData.MINUS);
		signButtonGroup = new ButtonGroup();
		signButtonGroup.clearSelection();
		clearSignSelection = new JButton("Очистить");

		signButtonGroup.add(plusButton);
		signButtonGroup.add(minusButton);
		signButtonGroup.clearSelection();
		clearSignSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				signButtonGroup.clearSelection();
			}
		});

		Panel pp = new Panel();
		pp.setMaximumSize(new Dimension(100, 50));
		pp.setPreferredSize(new Dimension(100, 50));

		setMinimumSize(new Dimension(200, 80));
		setPreferredSize(new Dimension(200, 80));
		setMaximumSize(new Dimension(200, 80));

		pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
		pp.add(plusButton);
		pp.add(minusButton);

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		add(pp);
		add(clearSignSelection);
		setBorder(new TitledBorder("Знак"));
	}

	@Deprecated
	public String getSignSelection() {
		ButtonModel bm = signButtonGroup.getSelection();
		if (bm == null) {
			return null;
		}
		return bm.getActionCommand();
	}

	@Deprecated
	public void setSign(String sign) {
		if (sign == null) {
			signButtonGroup.clearSelection();
		} else if (sign.equals(AData.PLUS)) {
			signButtonGroup.setSelected(plusButton.getModel(), true);
		} else if (sign.equals(AData.MINUS)) {
			signButtonGroup.setSelected(minusButton.getModel(), true);
		}

		if (signButtonGroup.getSelection() != null) {
			clearSignSelection.setEnabled(true);
		} else {
			clearSignSelection.setEnabled(false);
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

		plusButton.setEnabled(panelEnabled);
		minusButton.setEnabled(panelEnabled);

		clearSignSelection.setEnabled(selectionEnabled);

		if (selectionEnabled) {
			if (AData.PLUS.equals(sign)) {
				signButtonGroup.setSelected(plusButton.getModel(), true);
			} else if (AData.MINUS.equals(sign)) {
				signButtonGroup.setSelected(minusButton.getModel(), true);
			}
		} else {
			signButtonGroup.clearSelection();
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
		ButtonModel selectedButtonModel = signButtonGroup.getSelection();
		if (selectedButtonModel == null) {
			selectionModel.setSign(null);
		} else {
			selectionModel.setSign(selectedButtonModel.getActionCommand());
		}
	}
}
