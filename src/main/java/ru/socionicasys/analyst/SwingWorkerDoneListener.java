package ru.socionicasys.analyst;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Абстрактный класс, выполняющий действие по окончанию работы {@link SwingWorker}.
 *
 * @param <T> тип-наследник {@link SwingWorker}, к которому привязан слушатель
 */
public abstract class SwingWorkerDoneListener<T extends SwingWorker<?,?>> implements PropertyChangeListener {

	private static final String STATE_PROPERTY_NAME = "state";

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (STATE_PROPERTY_NAME.equals(evt.getPropertyName()) && evt.getNewValue() == SwingWorker.StateValue.DONE) {
			swingWorkerDone((T) evt.getSource());
		}
	}

	/**
	 * Метод вызывается по окончанию работы {@link SwingWorker}.
	 *
	 * @param worker экземпляр {@link SwingWorker}, который вызывал событие
	 */
	protected abstract void swingWorkerDone(T worker);
}
