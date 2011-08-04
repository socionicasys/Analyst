package ru.socionicasys.analyst;

import javax.swing.event.EventListenerList;

/**
 * Обобщенная реализация интерфейса {@link ModelHolder}.
 * @param <T> тип хранимой модели
 */
public class GenericModelHolder<T> implements ModelHolder<T> {
	protected T model;
	protected final EventListenerList listenerList;

	/**
	 * Создает контейнер с пустой (null) моделью.
	 */
	public GenericModelHolder() {
		listenerList = new EventListenerList();
	}

	@Override
	public T getModel() {
		return model;
	}

	@Override
	public void setModel(T model) {
		if (this.model == model) {
			return;
		}
		T oldModel = this.model;
		this.model = model;
		fireModelChanged(oldModel);
	}

	@Override
	public void addModelChangedListener(ModelChangedListener<T> listener) {
		listenerList.add(ModelChangedListener.class, listener);
	}

	@Override
	public void removeModelChangedListener(ModelChangedListener<T> listener) {
		listenerList.remove(ModelChangedListener.class, listener);
	}

	/**
	 * Оповещает всех слушателей об изменении модели
	 * @param oldModel старая модель
	 */
	protected void fireModelChanged(T oldModel) {
		@SuppressWarnings("unchecked")
		ModelChangedListener<T>[] listeners = listenerList.getListeners(ModelChangedListener.class);
		for (ModelChangedListener<T> listener : listeners) {
			listener.modelChanged(oldModel, model, this);
		}
	}
}
