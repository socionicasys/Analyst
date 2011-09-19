package ru.socionicasys.analyst;

import javax.swing.*;
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
}
