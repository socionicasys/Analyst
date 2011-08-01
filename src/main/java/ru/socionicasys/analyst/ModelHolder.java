package ru.socionicasys.analyst;

/**
 * Интерфейс «носителя модели». Носитель позволяет динамически подменять одну модель другой
 * (например, создавать новый документ), не разрушая связей модели со слушателями событий.
 */
public interface ModelHolder<T> {
	/**
	 * Возвращает модель, хранящуюся в носителе.
	 * @return модель
	 */
	T getModel();

	/**
	 * Заменяет в носителе модель на новую
	 * @param model новая модель
	 */
	void setModel(T model);

	/**
	 * Добавляет объект-слушатель событий замены модели
	 * @param listener слушатель
	 */
	void addModelChangedListener(ModelChangedListener<T> listener);

	/**
	 * Удаляет ранее добавленный объект-слушатель
	 * @param listener слушатель
	 */
	void removeModelChangedListener(ModelChangedListener<T> listener);
}
