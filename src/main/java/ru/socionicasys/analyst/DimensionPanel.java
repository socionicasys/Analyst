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

/**
 * Панель размерностей (Ex/Nm/St/Tm/«одно-»/«мало-»/«многомерность»/«индивидуальность»
 */
public class DimensionPanel extends JPanel implements PropertyChangeListener, ItemListener {
	private final DocumentSelectionModel selectionModel;
	private JRadioButton d1, d2, d3, d4, malo, mnogo, odno, indi;
	private ButtonGroup dimensionGroup;
	private JButton clearDimensionSelection;
	private final Logger logger = LoggerFactory.getLogger(DimensionPanel.class);

	public DimensionPanel(DocumentSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
		this.selectionModel.addPropertyChangeListener(this);

		d1 = new JRadioButton("Ex");
		d1.setActionCommand(AData.D1);
		d2 = new JRadioButton("Nm");
		d2.setActionCommand(AData.D2);
		d3 = new JRadioButton("St");
		d3.setActionCommand(AData.D3);
		d4 = new JRadioButton("Tm");
		d4.setActionCommand(AData.D4);
		odno = new JRadioButton("Одномерность");
		odno.setActionCommand(AData.ODNOMERNOST);
		malo = new JRadioButton("Маломерность");
		malo.setActionCommand(AData.MALOMERNOST);
		mnogo = new JRadioButton("Многомерность");
		mnogo.setActionCommand(AData.MNOGOMERNOST);
		indi = new JRadioButton("Индивидуальность");
		indi.setActionCommand(AData.INDIVIDUALNOST);

		dimensionGroup = new ButtonGroup();
		dimensionGroup.add(d1);
		dimensionGroup.add(d2);
		dimensionGroup.add(d3);
		dimensionGroup.add(d4);
		dimensionGroup.add(odno);
		dimensionGroup.add(malo);
		dimensionGroup.add(mnogo);
		dimensionGroup.add(indi);
		dimensionGroup.clearSelection();
		clearDimensionSelection = new JButton("Очистить");
		clearDimensionSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dimensionGroup.clearSelection();
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

		p1.add(d1);
		d1.addItemListener(this);
		p1.add(d2);
		d2.addItemListener(this);
		p1.add(d3);
		d3.addItemListener(this);
		p1.add(d4);
		d4.addItemListener(this);
		p2.add(indi);
		indi.addItemListener(this);
		p2.add(odno);
		odno.addItemListener(this);
		p2.add(malo);
		malo.addItemListener(this);
		p2.add(mnogo);
		mnogo.addItemListener(this);

		p.add(p1);
		p.add(p2);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(p);
		add(clearDimensionSelection);
		setBorder(new TitledBorder("Размерность"));

		updateView();
	}

	@Deprecated
	public String getDimensionSelection() {
		ButtonModel bm = dimensionGroup.getSelection();
		if (bm == null) {
			return null;
		}
		return bm.getActionCommand();
	}

	@Deprecated
	public void setDimension(String dimension) {
		if (dimension == null) {
			dimensionGroup.clearSelection();
		} else if (dimension.equals(AData.D1)) {
			d1.setSelected(true);
		} else if (dimension.equals(AData.D2)) {
			d2.setSelected(true);
		} else if (dimension.equals(AData.D3)) {
			d3.setSelected(true);
		} else if (dimension.equals(AData.D4)) {
			d4.setSelected(true);
		} else if (dimension.equals(AData.ODNOMERNOST)) {
			odno.setSelected(true);
		} else if (dimension.equals(AData.MALOMERNOST)) {
			malo.setSelected(true);
		} else if (dimension.equals(AData.MNOGOMERNOST)) {
			mnogo.setSelected(true);
		} else if (dimension.equals(AData.INDIVIDUALNOST)) {
			indi.setSelected(true);
		}

		if (dimensionGroup.getSelection() != null) {
			clearDimensionSelection.setEnabled(true);
		} else {
			clearDimensionSelection.setEnabled(false);
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

		d1.setEnabled(panelEnabled);
		d2.setEnabled(panelEnabled);
		d3.setEnabled(panelEnabled);
		d4.setEnabled(panelEnabled);
		odno.setEnabled(panelEnabled);
		mnogo.setEnabled(panelEnabled);
		malo.setEnabled(panelEnabled);
		indi.setEnabled(panelEnabled);
		clearDimensionSelection.setEnabled(selectionEnabled);

		if (selectionEnabled) {
			if (dimension.equals(AData.D1)) {
				dimensionGroup.setSelected(d1.getModel(), true);
			} else if (dimension.equals(AData.D2)) {
				dimensionGroup.setSelected(d2.getModel(), true);
			} else if (dimension.equals(AData.D3)) {
				dimensionGroup.setSelected(d3.getModel(), true);
			} else if (dimension.equals(AData.D4)) {
				dimensionGroup.setSelected(d4.getModel(), true);
			} else if (dimension.equals(AData.ODNOMERNOST)) {
				dimensionGroup.setSelected(odno.getModel(), true);
			} else if (dimension.equals(AData.MALOMERNOST)) {
				dimensionGroup.setSelected(malo.getModel(), true);
			} else if (dimension.equals(AData.MNOGOMERNOST)) {
				dimensionGroup.setSelected(mnogo.getModel(), true);
			} else if (dimension.equals(AData.INDIVIDUALNOST)) {
				dimensionGroup.setSelected(indi.getModel(), true);
			}
		} else {
			dimensionGroup.clearSelection();
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
		ButtonModel selectedButtonModel = dimensionGroup.getSelection();
		if (selectedButtonModel == null) {
			selectionModel.setDimension(null);
		} else {
			selectionModel.setDimension(selectedButtonModel.getActionCommand());
		}
	}
}
