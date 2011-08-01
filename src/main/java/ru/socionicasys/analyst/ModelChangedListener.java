package ru.socionicasys.analyst;

import java.util.EventListener;

/**
 * Интерфейс, реализуемый классами, заинтересованными в событии смены модели в {@link ModelHolder}.
 * @param <T> тип модели, хранящейся в {@code ModelHolder}.
 */
public interface ModelChangedListener<T> extends EventListener {
	/**
	 * Метод вызывается, когда модель в {@link ModelHolder} меняется на новую.
	 * @param oldModel старая модель
	 * @param newModel новая модель
	 * @param modelHolder экземпляр {@code ModelHolder&lt;T&gt;}, в котором произошла замена модели
	 */
	void modelChanged(T oldModel, T newModel, ModelHolder<T> modelHolder);
}
