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
public class ControlsPane extends JToolBar implements ADataChangeListener, ChangeListener,
		TreeSelectionListener, ActiveUndoManagerListener {
	private final AspectPanel aspectPanel;
	private final SignPanel signPanel;
	private final MVPanel mvPanel;
	private final DimensionPanel dimensionPanel;
	private final JTextPane textPane;
	private final List<ADataChangeListener> aDataListeners;
	private final JTextArea commentField;
	private final DocumentSelectionModel selectionModel;
	private final DocumentHolder documentHolder;
	private ASection currentASection;
	private Object oldTreeObject;
	private static final Logger logger = LoggerFactory.getLogger(ControlsPane.class);

	public ControlsPane(JTextPane textPane, DocumentHolder documentHolder, JTextArea commentField, DocumentSelectionModel selectionModel) {
		super("Панель разметки", JToolBar.VERTICAL);

		this.textPane = textPane;
		this.documentHolder = documentHolder;
		this.commentField = commentField;
		this.selectionModel = selectionModel;

		aDataListeners = new ArrayList<ADataChangeListener>();
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

	public AData getAData() {
		AData adata = null;
		try {
			adata = AData.parseAData(aspectPanel.getAspectSelection() + AData.SEPARATOR +
				signPanel.getSignSelection() + AData.SEPARATOR +
				dimensionPanel.getDimensionSelection() + AData.SEPARATOR +
				mvPanel.getMVSelection() + AData.SEPARATOR
			);
			if (adata != null) {
				adata.setComment(commentField.getText());
			}
		} catch (IllegalArgumentException e) {
			logger.error("Error in getAData()", e);
		}
		return adata;
	}


	protected void setContols(AData data) {
		removeADataListener(this);

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

		addADataListener(this);
	}

	protected void addADataListener(ADataChangeListener l) {
		aDataListeners.add(l);
	}

	protected void removeADataListener(ADataChangeListener l) {
		aDataListeners.remove(l);
	}

	private void fireADataChanged() {
		if (aDataListeners.isEmpty()) {
			return;
		}

		Caret caret = textPane.getCaret();

		int dot = caret.getDot();
		int mark = caret.getMark();
		int begin = Math.min(dot, mark);
		int end = Math.max(dot, mark);

		AData data = getAData();

		for (int i = aDataListeners.size() - 1; i >= 0; i--) {
			aDataListeners.get(i).aDataChanged(begin, end, data);
		}
	}

	@Override
	public void aDataChanged(int start, int end, AData data) {
		ADocument document = documentHolder.getModel();
		if (data == null && currentASection != null) {
			document.removeASection(currentASection);
			currentASection = null;
		} else if (data != null && currentASection != null) {
			document.updateASection(currentASection, data);
		} else if (data != null) {
			try {
				currentASection = new ASection(document, start, end);
				document.addASection(currentASection, data);
			} catch (BadLocationException e) {
				logger.error("Invalid position for ASection", e);
				currentASection = null;
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// Event from the comment field - just update changes to the document
		if (aspectPanel.isAspectSelected()) {
			fireADataChanged();
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode leafNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
		Object leafObject = leafNode.getUserObject();
		if (leafObject.equals(oldTreeObject)) {
			return;
		}

		oldTreeObject = leafObject;

		int index = 0;

		if (leafObject instanceof EndNodeObject) {
			index = ((EndNodeObject) leafObject).getOffset();
		} else {
			String quote = leafObject.toString();
			if (quote != null && quote.startsWith("#")) {
				String indexStr = quote.substring(1, quote.indexOf("::"));
				index = Integer.parseInt(indexStr);
			}
		}
		//////////////test for text positioning in scroll pane////////////////////////
		JViewport viewport = (JViewport) textPane.getParent();

		ADocument document = documentHolder.getModel();
		currentASection = document.getASectionThatStartsAt(index);
		if (currentASection != null) {
			int offset = currentASection.getMiddleOffset();
			int start = currentASection.getStartOffset();
			int end = currentASection.getEndOffset();

			textPane.getCaret().setDot(end);
			textPane.getCaret().moveDot(start);

			commentField.getCaret().removeChangeListener(this);

			AData data = document.getAData(currentASection);
			setContols(data);

			commentField.getCaret().addChangeListener(this);

			try {
				viewport.scrollRectToVisible(textPane.modelToView(offset));
				textPane.grabFocus();
			} catch (BadLocationException e1) {
				logger.error("Error setting model to view :: bad location", e1);
			}
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
