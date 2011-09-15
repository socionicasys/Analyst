package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.undo.ActiveUndoManager;
import ru.socionicasys.analyst.undo.ActiveUndoManagerListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Виктор
 */
public class ControlsPane extends JToolBar implements CaretListener, ADataChangeListener, ChangeListener,
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

		aspectPanel.addAspectSelectionListener(signPanel);
		aspectPanel.addAspectSelectionListener(dimensionPanel);

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

		dimensionPanel.setPanelEnabled(false);
		signPanel.setPanelEnabled(false);
		aspectPanel.setPanelEnabled(false);
	}

	private interface AspectSelectionListener {
		void setPanelEnabled(boolean enabled);
	}

	private class SignPanel extends JPanel implements ActionListener, AspectSelectionListener {
		private JRadioButton plusButton;
		private JRadioButton minusButton;
		private ButtonGroup signButtonGroup;
		private JButton clearSignSelection;
		private final DocumentSelectionModel selectionModel;

		private SignPanel(DocumentSelectionModel selectionModel) {
			this.selectionModel = selectionModel;

			plusButton = new JRadioButton("+");
			minusButton = new JRadioButton("-");
			plusButton.addActionListener(this);
			plusButton.setActionCommand(AData.PLUS);
			minusButton.addActionListener(this);
			minusButton.setActionCommand(AData.MINUS);
			signButtonGroup = new ButtonGroup();
			signButtonGroup.clearSelection();
			clearSignSelection = new JButton("Очистить");

			signButtonGroup.add(plusButton);
			signButtonGroup.add(minusButton);
			signButtonGroup.clearSelection();
			clearSignSelection.addActionListener(this);

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

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(clearSignSelection)) {
				signButtonGroup.clearSelection();
				clearSignSelection.setEnabled(false);
			} else {
				clearSignSelection.setEnabled(true);
			}
			fireADataChanged();
		}

		@Override
		public void setPanelEnabled(boolean enabled) {
			if (!enabled) {
				signButtonGroup.clearSelection();
				clearSignSelection.setEnabled(false);
			}
			plusButton.setEnabled(enabled);
			minusButton.setEnabled(enabled);
		}

		public String getSignSelection() {
			ButtonModel bm = signButtonGroup.getSelection();
			if (bm == null) {
				return null;
			}
			return bm.getActionCommand();
		}

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
	}

	private class AspectPanel extends JPanel implements ItemListener {
		private Map<Aspect, JRadioButton> primaryAspectButtons;
		private Map<Aspect, JRadioButton> secondaryAspectButtons;
		private JRadioButton d;
		private JRadioButton aspect, block, jump;
		private ButtonGroup aspectGroup, secondAspectGroup, controlGroup;
		private JButton clearAspectSelection;
		private List<AspectSelectionListener> actionListeners = new ArrayList<AspectSelectionListener>();
		private final DocumentSelectionModel selectionModel;

		private AspectPanel(DocumentSelectionModel selectionModel) {
			this.selectionModel = selectionModel;

			setMinimumSize(new Dimension(200, 270));
			setMaximumSize(new Dimension(200, 270));

			Panel pAspect = new Panel();
			pAspect.setLayout(new BoxLayout(pAspect, BoxLayout.Y_AXIS));
			pAspect.setPreferredSize(new Dimension(50, 200));
			pAspect.setMinimumSize(new Dimension(50, 200));

			aspectGroup = new ButtonGroup();
			primaryAspectButtons = new HashMap<Aspect, JRadioButton>();
			for (Aspect aspect : Aspect.values()) {
				JRadioButton button = new JRadioButton(aspect.getAbbreviation());
				button.setActionCommand(aspect.getAbbreviation());
				button.addItemListener(this);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						secondAspectGroup.clearSelection();
						clearAspectSelection.setEnabled(true);
						updateSelections();
					}
				});
				aspectGroup.add(button);
				pAspect.add(button);
				primaryAspectButtons.put(aspect, button);
			}

			Panel pAspect2 = new Panel();
			pAspect2.setLayout(new BoxLayout(pAspect2, BoxLayout.Y_AXIS));
			pAspect2.setPreferredSize(new Dimension(50, 200));
			pAspect2.setMinimumSize(new Dimension(50, 200));

			secondAspectGroup = new ButtonGroup();
			secondaryAspectButtons = new HashMap<Aspect, JRadioButton>();
			for (Aspect aspect : Aspect.values()) {
				JRadioButton button = new JRadioButton(aspect.getAbbreviation());
				button.setActionCommand(aspect.getAbbreviation());
				button.addItemListener(this);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						clearAspectSelection.setEnabled(true);
						updateSelections();
					}
				});
				secondAspectGroup.add(button);
				pAspect2.add(button);
				secondaryAspectButtons.put(aspect, button);
			}

			aspect = new JRadioButton("Аспект");
			aspect.addItemListener(this);
			aspect.setActionCommand("aspect");
			block = new JRadioButton("Блок");
			block.addItemListener(this);
			block.setActionCommand("block");
			jump = new JRadioButton("Перевод");
			jump.addItemListener(this);
			jump.setActionCommand("jump");

			d = new JRadioButton("???");
			d.getModel().addItemListener(this);
			d.setActionCommand(AData.DOUBT);
			aspectGroup.add(d);
			d.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					secondAspectGroup.clearSelection();
					clearAspectSelection.setEnabled(true);
					updateSelections();
				}
			});

			controlGroup = new ButtonGroup();
			controlGroup.add(aspect);
			aspect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					secondAspectGroup.clearSelection();
					setSecondAspectGroupEnabled(false);
					setAspectGroupEnabled(true);
					updateSelections();
				}
			});
			controlGroup.add(block);
			block.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					secondAspectGroup.clearSelection();
					setSecondAspectGroupEnabled(false);
					updateSelections();
				}
			});
			controlGroup.add(jump);
			jump.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					secondAspectGroup.clearSelection();
					setSecondAspectGroupEnabled(false);
					updateSelections();
				}
			});

			clearAspectSelection = new JButton("Очистить");

			Panel pControl = new Panel();
			pControl.setLayout(new BoxLayout(pControl, BoxLayout.X_AXIS));
			pControl.setPreferredSize(new Dimension(50, 40));
			pControl.setMinimumSize(new Dimension(50, 40));
			pControl.add(aspect);
			pControl.add(block);
			pControl.add(jump);

			Panel pA = new Panel();
			pA.setLayout(new BoxLayout(pA, BoxLayout.X_AXIS));

			pA.add(pAspect);
			pA.add(pAspect2);

			Panel pB = new Panel();
			pB.setLayout(new BoxLayout(pB, BoxLayout.Y_AXIS));

			pB.add(new Panel());
			pB.add(clearAspectSelection);
			pB.add(new Panel());
			pB.add(d);
			pB.add(new Panel());

			setLayout(new BorderLayout());
			add(pControl, BorderLayout.NORTH);
			add(pA, BorderLayout.WEST);
			add(pB, BorderLayout.EAST);

			setBorder(new TitledBorder("Аспект/Блок"));

			aspectGroup.clearSelection();
			secondAspectGroup.clearSelection();
			controlGroup.clearSelection();

			clearAspectSelection.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					aspectGroup.clearSelection();
					secondAspectGroup.clearSelection();
					setSecondAspectGroupEnabled(false);
					controlGroup.setSelected(aspect.getModel(), true);
					clearAspectSelection.setEnabled(false);
					informListeners(false);
					updateSelections();
				}
			});
			clearAspectSelection.setEnabled(false);
		}

		public void addAspectSelectionListener(AspectSelectionListener asl) {
			actionListeners.add(asl);
			informListeners(isAspectSelected());
		}

		private void informListeners(boolean selected) {
			for (AspectSelectionListener actionListener : actionListeners) {
				actionListener.setPanelEnabled(selected);
			}

			if (selected) {
				if (!commentField.isEditable()) {
					commentField.setEditable(true);
				}
			} else {
				commentField.setText("");
				commentField.setEditable(false);
			}
		}

		private void updateSelections() {
			if (block.isSelected() && block.isEnabled()) {
				ButtonModel bm = aspectGroup.getSelection();
				if (bm != null) {
					setSecondAspectForBlock(bm.getActionCommand());
				}
			}

			if (jump.isSelected() && jump.isEnabled()) {
				ButtonModel bm = aspectGroup.getSelection();
				if (bm != null) {
					setSecondAspectForJump(bm.getActionCommand());
				}
			}
			fireADataChanged();
		}

		private void setSecondAspectGroupEnabled(boolean enabled) {
			for (JRadioButton button : secondaryAspectButtons.values()) {
				button.setEnabled(enabled);
			}
		}

		private void setSecondAspectForBlock(String firstAspectName) {
			if (firstAspectName != null) {
				Aspect firstAspect = Aspect.byAbbreviation(firstAspectName);
				for (Map.Entry<Aspect, JRadioButton> entry : secondaryAspectButtons.entrySet()) {
					JRadioButton button = entry.getValue();
					Aspect buttonAspect = entry.getKey();
					button.setEnabled(firstAspect.isBlockWith(buttonAspect));
				}
			}
		}

		private void setSecondAspectForJump(String firstAspect) {
			if (firstAspect != null) {
				for (Map.Entry<Aspect, JRadioButton> entry : secondaryAspectButtons.entrySet()) {
					JRadioButton button = entry.getValue();
					String buttonAspect = entry.getKey().getAbbreviation();
					button.setEnabled(!buttonAspect.equals(firstAspect));
				}
			}
		}

		public boolean isAspectSelected() {
			return aspectGroup.getSelection() != null;
		}

		public void setPanelEnabled(boolean enabled) {
			if (!enabled) {
				aspectGroup.clearSelection();
				secondAspectGroup.clearSelection();
				controlGroup.clearSelection();
				setAspectGroupEnabled(false);
				setSecondAspectGroupEnabled(false);
				setControlGroupEnabled(false);
				clearAspectSelection.setEnabled(false);
				informListeners(enabled);
			} else {
				setControlGroupEnabled(true);
				controlGroup.setSelected(aspect.getModel(), true);
				aspectGroup.clearSelection();
				setAspectGroupEnabled(true);
			}
		}

		private void setAspectGroupEnabled(boolean enabled) {
			for (JRadioButton button : primaryAspectButtons.values()) {
				button.setEnabled(enabled);
			}
			d.setEnabled(enabled);
		}

		private void setControlGroupEnabled(boolean enabled) {
			aspect.setEnabled(enabled);
			block.setEnabled(enabled);
			jump.setEnabled(enabled);
		}

		public String getAspectSelection() {
			String res = "";
			ButtonModel bma = aspectGroup.getSelection();
			ButtonModel bma2 = secondAspectGroup.getSelection();

			if (bma == null) {
				return null;
			}

			res += bma.getActionCommand();

			if (bma2 != null) {
				if (block.isSelected()) {
					res += AData.BLOCK_TOKEN + bma2.getActionCommand();
				}
				else if (jump.isSelected()) {
					res += AData.JUMP_TOKEN + bma2.getActionCommand();
				}
			}
			return res;
		}

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			//извещение от поля комментария
			informListeners(isAspectSelected());
		}

		public void setAspect(AData data) {
			if (data == null) {
				return;
			}
			String aspect = data.getAspect();

			if (aspect == null) {
				aspectGroup.clearSelection();
				secondAspectGroup.clearSelection();
				this.aspect.getModel().setSelected(true);
			} else if (aspect.equals(AData.DOUBT)) {
				d.getModel().setSelected(true);
			} else {
				for (Map.Entry<Aspect, JRadioButton> entry : primaryAspectButtons.entrySet()) {
					if (entry.getKey().getAbbreviation().equals(aspect)) {
						entry.getValue().getModel().setSelected(true);
					}
				}
			}

			String modifier = data.getModifier();
			String secondAspect = data.getSecondAspect();

			if (modifier != null) {
				if (modifier.equals(AData.BLOCK)) {
					block.getModel().setSelected(true);
					setSecondAspectForBlock(aspect);
				} else if (modifier.equals(AData.JUMP)) {
					jump.getModel().setSelected(true);
					setSecondAspectForJump(aspect);
				}

				if (secondAspect == null) {
					secondAspectGroup.clearSelection();
				} else {
					for (Map.Entry<Aspect, JRadioButton> entry : secondaryAspectButtons.entrySet()) {
						String buttonAspect = entry.getKey().getAbbreviation();
						if (buttonAspect.equals(secondAspect)) {
							JRadioButton button = entry.getValue();
							button.getModel().setSelected(true);
						}
					}
				}
			}

			informListeners(isAspectSelected());
			clearAspectSelection.setEnabled(isAspectSelected());
		}
	}

	private class DimensionPanel extends JPanel implements ActionListener, AspectSelectionListener {
		private JRadioButton d1, d2, d3, d4, malo, mnogo, odno, indi;
		private ButtonGroup dimensionGroup;
		private JButton clearDimensionSelection;
		private final Logger logger = LoggerFactory.getLogger(DimensionPanel.class);
		private final DocumentSelectionModel selectionModel;

		private DimensionPanel(DocumentSelectionModel selectionModel) {
			this.selectionModel = selectionModel;

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
			clearDimensionSelection.addActionListener(this);

			Panel p = new Panel();
			Panel p1 = new Panel();
			Panel p2 = new Panel();

			p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
			p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
			p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

			setMinimumSize(new Dimension(200, 170));
			setMaximumSize(new Dimension(200, 170));

			p1.add(d1);
			d1.addActionListener(this);
			p1.add(d2);
			d2.addActionListener(this);
			p1.add(d3);
			d3.addActionListener(this);
			p1.add(d4);
			d4.addActionListener(this);
			p2.add(indi);
			indi.addActionListener(this);
			p2.add(odno);
			odno.addActionListener(this);
			p2.add(malo);
			malo.addActionListener(this);
			p2.add(mnogo);
			mnogo.addActionListener(this);

			p.add(p1);
			p.add(p2);

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(p);
			add(clearDimensionSelection);
			setBorder(new TitledBorder("Размерность"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(clearDimensionSelection)) {
				dimensionGroup.clearSelection();
				clearDimensionSelection.setEnabled(false);
			} else {
				clearDimensionSelection.setEnabled(true);
			}
			fireADataChanged();
		}

		@Override
		public void setPanelEnabled(boolean enabled) {
			if (!enabled) {
				dimensionGroup.clearSelection();
				clearDimensionSelection.setEnabled(false);
			}

			d1.setEnabled(enabled);
			d2.setEnabled(enabled);
			d3.setEnabled(enabled);
			d4.setEnabled(enabled);
			odno.setEnabled(enabled);
			mnogo.setEnabled(enabled);
			malo.setEnabled(enabled);
			indi.setEnabled(enabled);
		}

		public String getDimensionSelection() {
			ButtonModel bm = dimensionGroup.getSelection();
			if (bm == null) {
				return null;
			}
			return bm.getActionCommand();
		}

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


	@Override
	public void caretUpdate(CaretEvent e) {
		int dot = e.getDot();
		int mark = e.getMark();
		logger.debug("caretUpdate: {}, {}", dot, mark);
		int begin = Math.min(dot, mark);

		//try to find current ASection to edit
		ADocument document = documentHolder.getModel();
		currentASection = document.getASectionThatStartsAt(begin);

		// if found mark the section with caret
		if (currentASection != null) {
			logger.debug("currentASection = {}", currentASection);
			commentField.getCaret().removeChangeListener(this);

			aspectPanel.setPanelEnabled(false);
			AData data = document.getAData(currentASection);
			aspectPanel.setPanelEnabled(true);
			setContols(data);

			commentField.getCaret().addChangeListener(this);
		} else if (dot == mark) {
			logger.debug("Empty selection, disable aspect panel");
			aspectPanel.setPanelEnabled(false);
		} else {
			logger.debug("Non-empty selection, enable aspect panel");
			aspectPanel.setPanelEnabled(true);
		}
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

			textPane.removeCaretListener(this);
			textPane.getCaret().setDot(end);
			textPane.getCaret().moveDot(start);
			textPane.addCaretListener(this);

			commentField.getCaret().removeChangeListener(this);

			aspectPanel.setPanelEnabled(false);
			AData data = document.getAData(currentASection);
			aspectPanel.setPanelEnabled(true);
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
