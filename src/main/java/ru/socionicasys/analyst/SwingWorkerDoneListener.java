package ru.socionicasys.analyst;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Абстрактный класс, выполняющий действие по окончанию работы {@link SwingWorker}.
 */
public abstract class SwingWorkerDoneListener implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("state".equals(evt.getPropertyName())) {
			SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
			if (state == SwingWorker.StateValue.DONE) {
				swingWorkerDone(evt);
			}
		}
	}

	/**
	 * Метод вызывается по окончанию работы {@link SwingWorker}.
	 *
	 * @param evt событие, связанное с окончанием работы {@code SwingWorker}.
	 * {@code evt.getPropertyName()} при вызове метода равно {@code "state"},
	 * {@code evt.getNewValue()} равно {@code SwingWorker.StateValue.DONE}.
	 */
	protected abstract void swingWorkerDone(PropertyChangeEvent evt);
}
