package ru.socionicasys.analyst.panel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.socionicasys.analyst.AData;
import ru.socionicasys.analyst.DocumentSelectionModel;
import ru.socionicasys.analyst.predicates.DimensionPredicate;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Панель размерностей (Ex/Nr/St/Tm/«одно-»/«мало-»/«многомерность»/«индивидуальность»
 */
public final class DimensionPanel extends ActivePanel {
	private final Map<String, JRadioButton> buttons;
	private final ButtonGroup buttonGroup;
	private final JButton clearButton;
	private static final Logger logger = LoggerFactory.getLogger(DimensionPanel.class);

	public DimensionPanel(DocumentSelectionModel selectionModel) {
		super(selectionModel);

		buttons = new HashMap<String, JRadioButton>(8);
		buttons.put(AData.D1, new JRadioButton(DimensionPredicate.getDimensionName(1)));
		buttons.put(AData.D2, new JRadioButton(DimensionPredicate.getDimensionName(2)));
		buttons.put(AData.D3, new JRadioButton(DimensionPredicate.getDimensionName(3)));
		buttons.put(AData.D4, new JRadioButton(DimensionPredicate.getDimensionName(4)));
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

	/**
	 * Обновляет элементы управления панели в соответствии со связанными данными из модели выделения.
	 */
	@Override
	protected void updateView() {
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
	 * Обновляет модель в соответствии с измененными в панели данными.
	 */
	@Override
	protected void updateModel() {
		ButtonModel selectedButtonModel = buttonGroup.getSelection();
		if (selectedButtonModel == null) {
			selectionModel.setDimension(null);
		} else {
			selectionModel.setDimension(selectedButtonModel.getActionCommand());
		}
	}
}
