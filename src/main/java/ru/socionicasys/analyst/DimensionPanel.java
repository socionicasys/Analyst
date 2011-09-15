package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Панель размерностей (Ex/Nm/St/Tm/«одно-»/«мало-»/«многомерность»/«индивидуальность»
 */
public class DimensionPanel extends JPanel implements PropertyChangeListener, ItemListener {
	private final DocumentSelectionModel selectionModel;
	private final Map<String, JRadioButton> buttons;
	private final ButtonGroup buttonGroup;
	private final JButton clearButton;
	private static final Logger logger = LoggerFactory.getLogger(DimensionPanel.class);

	public DimensionPanel(DocumentSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
		this.selectionModel.addPropertyChangeListener(this);

		buttons = new HashMap<String, JRadioButton>(8);
		buttons.put(AData.D1, new JRadioButton("Ex"));
		buttons.put(AData.D2, new JRadioButton("Nm"));
		buttons.put(AData.D3, new JRadioButton("St"));
		buttons.put(AData.D4, new JRadioButton("Tm"));
		buttons.put(AData.ODNOMERNOST, new JRadioButton("Одномерность"));
		buttons.put(AData.MALOMERNOST, new JRadioButton("Маломерность"));
		buttons.put(AData.MNOGOMERNOST, new JRadioButton("Многомерность"));
		buttons.put(AData.INDIVIDUALNOST, new JRadioButton("Индивидуальность"));

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

		Panel p = new Panel();
		Panel p1 = new Panel();
		Panel p2 = new Panel();

		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

		setMinimumSize(new Dimension(200, 170));
		setMaximumSize(new Dimension(200, 170));

		p1.add(buttons.get(AData.D1));
		p1.add(buttons.get(AData.D2));
		p1.add(buttons.get(AData.D3));
		p1.add(buttons.get(AData.D4));
		p2.add(buttons.get(AData.INDIVIDUALNOST));
		p2.add(buttons.get(AData.ODNOMERNOST));
		p2.add(buttons.get(AData.MALOMERNOST));
		p2.add(buttons.get(AData.MNOGOMERNOST));

		p.add(p1);
		p.add(p2);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(p);
		add(clearButton);
		setBorder(new TitledBorder("Размерность"));

		updateView();
	}

	@Deprecated
	public String getDimensionSelection() {
		ButtonModel bm = buttonGroup.getSelection();
		if (bm == null) {
			return null;
		}
		return bm.getActionCommand();
	}

	@Deprecated
	public void setDimension(String dimension) {
		if (dimension == null) {
			buttonGroup.clearSelection();
		} else if (buttons.containsKey(dimension)) {
			buttonGroup.setSelected(buttons.get(dimension).getModel(), true);
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
		String dimension = selectionModel.getDimension();
		boolean panelEnabled = !selectionModel.isEmpty() && !selectionModel.isMarkupEmpty();
		boolean selectionEnabled = panelEnabled && dimension != null;

		for (JRadioButton button : buttons.values()) {
			button.setEnabled(panelEnabled);
		}
		clearButton.setEnabled(selectionEnabled);

		if (selectionEnabled) {
			JRadioButton selectedButton = buttons.get(dimension);
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
			selectionModel.setDimension(null);
		} else {
			selectionModel.setDimension(selectedButtonModel.getActionCommand());
		}
	}
}
