package ru.socionicasys.analyst;

import ru.socionicasys.analyst.types.Aspect;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map;

/**
 * Панель выбора аспекта/блока/перевода.
 */
public final class AspectPanel extends ActivePanel {
	private final Map<Aspect, JRadioButton> primaryAspectButtons;
	private final Map<Aspect, JRadioButton> secondaryAspectButtons;
	private final JRadioButton doubt;
	private final JRadioButton aspect;
	private final JRadioButton block;
	private final JRadioButton jump;
	private final ButtonGroup primaryAspectGroup;
	private final ButtonGroup secondaryAspectGroup;
	private final ButtonGroup controlGroup;
	private final JButton clearButton;

	public AspectPanel(DocumentSelectionModel selectionModel) {
		super(selectionModel);

		setMinimumSize(new Dimension(200, 270));
		setMaximumSize(new Dimension(200, 270));

		Panel pAspect = new Panel();
		pAspect.setLayout(new BoxLayout(pAspect, BoxLayout.Y_AXIS));
		pAspect.setPreferredSize(new Dimension(50, 200));
		pAspect.setMinimumSize(new Dimension(50, 200));

		primaryAspectGroup = new ButtonGroup();
		primaryAspectButtons = new EnumMap<Aspect, JRadioButton>(Aspect.class);
		for (Aspect aspect : Aspect.values()) {
			JRadioButton button = new JRadioButton(aspect.getAbbreviation());
			button.setActionCommand(aspect.getAbbreviation());
			button.addItemListener(this);
			primaryAspectGroup.add(button);
			pAspect.add(button);
			primaryAspectButtons.put(aspect, button);
		}

		Panel pAspect2 = new Panel();
		pAspect2.setLayout(new BoxLayout(pAspect2, BoxLayout.Y_AXIS));
		pAspect2.setPreferredSize(new Dimension(50, 200));
		pAspect2.setMinimumSize(new Dimension(50, 200));

		secondaryAspectGroup = new ButtonGroup();
		secondaryAspectButtons = new EnumMap<Aspect, JRadioButton>(Aspect.class);
		for (Aspect aspect : Aspect.values()) {
			JRadioButton button = new JRadioButton(aspect.getAbbreviation());
			button.setActionCommand(aspect.getAbbreviation());
			button.addItemListener(this);
			secondaryAspectGroup.add(button);
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

		doubt = new JRadioButton("???");
		doubt.getModel().addItemListener(this);
		doubt.setActionCommand(AData.DOUBT);
		primaryAspectGroup.add(doubt);

		controlGroup = new ButtonGroup();
		controlGroup.add(aspect);
		controlGroup.add(block);
		controlGroup.add(jump);

		clearButton = new JButton("Очистить");

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
		pB.add(clearButton);
		pB.add(new Panel());
		pB.add(doubt);
		pB.add(new Panel());

		setLayout(new BorderLayout());
		add(pControl, BorderLayout.NORTH);
		add(pA, BorderLayout.WEST);
		add(pB, BorderLayout.EAST);

		setBorder(new TitledBorder("Аспект/Блок"));

		primaryAspectGroup.clearSelection();
		secondaryAspectGroup.clearSelection();
		controlGroup.clearSelection();

		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				primaryAspectGroup.clearSelection();
				secondaryAspectGroup.clearSelection();
				controlGroup.setSelected(aspect.getModel(), true);
			}
		});

		updateView();
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
		boolean enableSecondAspect = firstAspect != null && !AData.DOUBT.equals(firstAspect);
		for (Map.Entry<Aspect, JRadioButton> entry : secondaryAspectButtons.entrySet()) {
			JRadioButton button = entry.getValue();
			if (enableSecondAspect) {
				String buttonAspect = entry.getKey().getAbbreviation();
				button.setEnabled(!buttonAspect.equals(firstAspect));
			} else {
				button.setEnabled(false);
			}
		}
	}

	/**
	 * Обновляет элементы управления панели в соответствии со связанными данными из модели выделения.
	 */
	@Override
	protected void updateView() {
		boolean panelEnable = !selectionModel.isEmpty();

		for (Aspect buttonAspect : Aspect.values()) {
			primaryAspectButtons.get(buttonAspect).setEnabled(panelEnable);
			secondaryAspectButtons.get(buttonAspect).setEnabled(panelEnable);
		}

		doubt.setEnabled(panelEnable);
		aspect.setEnabled(panelEnable);
		block.setEnabled(panelEnable);
		jump.setEnabled(panelEnable);

		boolean markupEnable = panelEnable && !selectionModel.isMarkupEmpty();
		clearButton.setEnabled(markupEnable);
		
		if (!panelEnable) {
			primaryAspectGroup.clearSelection();
			secondaryAspectGroup.clearSelection();
			controlGroup.clearSelection();
			return;
		}

		String firstAspect = selectionModel.getAspect();
		if (firstAspect == null) {
			primaryAspectGroup.clearSelection();
			aspect.getModel().setSelected(true);
		} else if (AData.DOUBT.equals(firstAspect)) {
			doubt.getModel().setSelected(true);
			aspect.getModel().setSelected(true);
		} else {
			JRadioButton selectedButton = primaryAspectButtons.get(Aspect.byAbbreviation(firstAspect));
			selectedButton.getModel().setSelected(true);
		}

		String modifier = selectionModel.getModifier();
		if (AData.BLOCK.equals(modifier)) {
			block.getModel().setSelected(true);
			setSecondAspectForBlock(firstAspect);
		} else if (AData.JUMP.equals(modifier)) {
			jump.getModel().setSelected(true);
			setSecondAspectForJump(firstAspect);
		} else {
			aspect.getModel().setSelected(true);
			for (JRadioButton button : secondaryAspectButtons.values()) {
				button.setEnabled(false);
			}
		}

		String secondAspect = selectionModel.getSecondAspect();
		if (secondAspect == null) {
			secondaryAspectGroup.clearSelection();
		} else {
			secondaryAspectButtons.get(Aspect.byAbbreviation(secondAspect)).getModel().setSelected(true);
		}
	}

	/**
	 * Обновляет модель в соответствии с измененными в панели данными.
	 */
	@Override
	protected void updateModel() {
		ButtonModel aspectGroupSelection = primaryAspectGroup.getSelection();
		if (aspectGroupSelection == null) {
			selectionModel.setAspect(null);
		} else {
			String firstAspect = aspectGroupSelection.getActionCommand();
			selectionModel.setAspect(firstAspect);
		}

		ButtonModel secondAspectGroupSelection = secondaryAspectGroup.getSelection();
		if (secondAspectGroupSelection == null) {
			selectionModel.setSecondAspect(null);
		} else {
			String secondAspect = secondAspectGroupSelection.getActionCommand();
			selectionModel.setSecondAspect(secondAspect);
		}

		ButtonModel controlGroupSelection = controlGroup.getSelection();
		String modifier;
		if (controlGroupSelection == null) {
			modifier = null;
		} else {
			String controlCommand = controlGroupSelection.getActionCommand();
			if ("block".equals(controlCommand)) {
				modifier = AData.BLOCK;
			} else if ("jump".equals(controlCommand)) {
				modifier = AData.JUMP;
			} else {
				modifier = null;
			}
		}
		selectionModel.setModifier(modifier);
	}
}
