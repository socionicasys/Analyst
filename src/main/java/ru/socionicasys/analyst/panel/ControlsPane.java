package ru.socionicasys.analyst.panel;

import ru.socionicasys.analyst.model.DocumentSelectionModel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Виктор
 */
public class ControlsPane extends JToolBar {
	public ControlsPane(DocumentSelectionModel selectionModel) {
		super("Панель разметки", JToolBar.VERTICAL);

		SignPanel signPanel = new SignPanel(selectionModel);
		MVPanel mvPanel = new MVPanel(selectionModel);
		DimensionPanel dimensionPanel = new DimensionPanel(selectionModel);
		AspectPanel aspectPanel = new AspectPanel(selectionModel);

		JPanel container = new JPanel();
		//container.setMinimumSize(new Dimension(200, 500));
		JScrollPane scrl = new JScrollPane(container);

		scrl.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		container.add(aspectPanel);
		container.add(signPanel);
		container.add(dimensionPanel);
		container.add(mvPanel);
		container.add(Box.createVerticalGlue());
		add(container);
		add(Box.createVerticalGlue());
	}
}
