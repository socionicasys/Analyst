package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.socionicasys.analyst.undo.ActiveUndoManager;
import ru.socionicasys.analyst.undo.ActiveUndoManagerListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Виктор
 */
public class ControlsPane extends JToolBar implements ActiveUndoManagerListener {
	private final AspectPanel aspectPanel;
	private final SignPanel signPanel;
	private final MVPanel mvPanel;
	private final DimensionPanel dimensionPanel;
	private final JTextArea commentField;
	private final DocumentHolder documentHolder;
	private ASection currentASection;
	private static final Logger logger = LoggerFactory.getLogger(ControlsPane.class);

	public ControlsPane(DocumentHolder documentHolder, JTextArea commentField, DocumentSelectionModel selectionModel) {
		super("Панель разметки", JToolBar.VERTICAL);

		this.documentHolder = documentHolder;
		this.commentField = commentField;

		signPanel = new SignPanel(selectionModel);
		mvPanel = new MVPanel(selectionModel);
		dimensionPanel = new DimensionPanel(selectionModel);
		aspectPanel = new AspectPanel(selectionModel);

		JPanel container = new JPanel();
		container.setMinimumSize(new Dimension(200, 500));
		JScrollPane scrl = new JScrollPane(container);

		scrl.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		container.add(aspectPanel);
		container.add(signPanel);
		container.add(dimensionPanel);
		container.add(mvPanel);
		add(container);
	}

	protected void setContols(AData data) {
		if (data != null) {
			aspectPanel.setAspect(data);
			dimensionPanel.setDimension(data.getDimension());
			mvPanel.setMV(data.getMV());
			signPanel.setSign(data.getSign());

			//need this not to receive notification
			commentField.setText(data.getComment());
		} else {
			aspectPanel.setAspect(null);
			dimensionPanel.setDimension(null);
			mvPanel.setMV(null);
			signPanel.setSign(null);
			commentField.setText(null);
		}
	}

	@Override
	public void undoStateChanged(ActiveUndoManager undoManager) {
		if (currentASection != null) {
			AData data = documentHolder.getModel().getAData(currentASection);
			setContols(data);
		}
	}
}
