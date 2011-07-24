package ru.socionicasys.analyst;

/**
 * Интерфейс «носителя модели». Носитель позволяет динамически подменять одну модель другой
 * (например, создавать новый документ), не разрушая связей модели со слушателями событий.
 */
public interface ModelHolder<T> {
	T getModel();
	void setModel(T model);
}
