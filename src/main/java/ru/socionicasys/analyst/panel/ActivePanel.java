package ru.socionicasys.analyst.panel;

import ru.socionicasys.analyst.model.DocumentSelectionModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Общий класс для всех панелей, позволяющих изменять отметки
 * (аспект/размерность и т. д.) в текущем выделении.
 */
public abstract class ActivePanel extends JPanel implements PropertyChangeListener, ItemListener {
	/**
	 * Модель выделения, к которой привязана данная панель.
	 */
	protected final DocumentSelectionModel selectionModel;

	/**
	 * Служит для синхронизации обновлений модель->представление и представление->модель.
	 * Если это поле равно {@code false}, данные в нем находятся в процессе заполнения, и
	 * не должны синхронизироваться обратно в модель.
	 */
	protected boolean viewInitialized;

	/**
	 * Создает панель с привязкой к заданной модели выделения.
	 *
	 * @param selectionModel модель выделения, которую будет отображать и изменять панель
	 */
	protected ActivePanel(DocumentSelectionModel selectionModel) {
		this.selectionModel = selectionModel;
		this.selectionModel.addPropertyChangeListener(this);
		viewInitialized = true;
	}

	/**
	 * Обрабатывает изменение свойств в модели выделения, к которой привязана эта панель.
	 * Включает/отключает панель и меняет состояние элементов при изменениях в выделении.
	 *
	 * @param evt параметр не используется
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!selectionModel.isInitialized()) {
			return;
		}

		viewInitialized = false;
		updateView();
		viewInitialized = true;
	}

	/**
	 * Обновляет элементы управления панели в соответствии с данными из модели выделения.
	 */
	protected abstract void updateView();

	/**
	 * Обрабатывает изменение в состоянии этой панели и отображает их на модель выделения,
	 * к которой панель привязана.
	 *
	 * @param e параметр не используется
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (!viewInitialized) {
			return;
		}

		selectionModel.setInitialized(false);
		updateModel();
		selectionModel.setInitialized(true);
	}

	/**
	 * Обновляет модель в соответствии с измененными в панели данными.
	 */
	protected abstract void updateModel();

	/**
	 * Формирует layout следюущего вида:
	 * +-------------+
	 * |+-----+-----+|
	 * || pp1 | pp2 ||
	 * |+-----+-----+|
	 * +-------------+
	 * | clearButton |
	 * +-------------+
	 */
	protected void buildLayoutType1(JPanel pp1, JPanel pp2, JButton clearButton) {
		JPanel contentPanel = new JPanel();
		JPanel clearButtonPanel = new JPanel();

		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		clearButtonPanel.setLayout(new BoxLayout(clearButtonPanel, BoxLayout.X_AXIS));

		if (pp1 != null)
			contentPanel.add(pp1);
		if (pp2 != null)
			contentPanel.add(pp2);
		contentPanel.add(Box.createHorizontalGlue());
		clearButtonPanel.add(Box.createHorizontalGlue());
		clearButtonPanel.add(clearButton);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(contentPanel);
		add(clearButtonPanel);
		//setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

		if (false) {
			pp1.setBorder(BorderFactory.createLineBorder(Color.green));
			pp2.setBorder(BorderFactory.createLineBorder(Color.green));
			contentPanel.setBorder(BorderFactory.createLineBorder(Color.black));
			clearButtonPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
		}
	}

}
