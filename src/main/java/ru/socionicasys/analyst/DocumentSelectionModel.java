package ru.socionicasys.analyst;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Описывает выделение внутри документа, и возможные отметки в этом выделении.
 * Позволяет другим объектам отслеживать изменения в этом выделении.
 */
public class DocumentSelectionModel {
	private boolean empty;
	private boolean initialized;
	private int startOffset;
	private int endOffset;
	private String aspect;
	private String secondAspect;
	private String modifier;
	private String sign;
	private String mv;
	private String dimension;
	private String comment;

	private final PropertyChangeSupport propertyChangeSupport;

	/**
	 * Инициализирует пустую модель выделения.
	 */
	public DocumentSelectionModel() {
		propertyChangeSupport = new PropertyChangeSupport(this);
		empty = true;
	}

	/**
	 * @return пусто ли выделение
	 */
	public boolean isEmpty() {
		return empty;
	}

	/**
	 * @param empty пусто ли выделение
	 */
	public void setEmpty(boolean empty) {
		this.empty = updateProperty("empty", this.empty, empty);
	}

	/**
	 * @return инициализирована ли в данный момент модель. {@code false} означает,
	 * что сейчас происходит заполнение свойств модели.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Устанавливает флаг инициализированности выделения.
	 * 
	 * @param initialized инициализировано ли выделение
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = updateProperty("initialized", this.initialized, initialized);
	}

	/**
	 * @return начальная позиция выделения в документе
	 */
	public int getStartOffset() {
		return startOffset;
	}

	/**
	 * @param startOffset новое значение начальной позиции выделения в документе
	 */
	public void setStartOffset(int startOffset) {
		this.startOffset = updateProperty("startOffset", this.startOffset, startOffset);
	}

	/**
	 * @return конечная позиция выделения в документе
	 */
	public int getEndOffset() {
		return endOffset;
	}

	/**
	 * @param endOffset новое значение конечной позиции выделения в документе
	 */
	public void setEndOffset(int endOffset) {
		this.endOffset = updateProperty("endOffset", this.endOffset, endOffset);
	}

	/**
	 * @return основной аспект выделения, {@code null} если для выделеного фрагмента
	 * нет пометок для анализа
	 */
	public String getAspect() {
		return aspect;
	}

	/**
	 * @param aspect новый основной аспект выделения
	 */
	public void setAspect(String aspect) {
		this.aspect = updateProperty("aspect", this.aspect, aspect);
	}

	/**
	 * @return второй аспект выделения, {@code null} если в текущем выделенном фрагменте не отмечены
	 * блок или перевод
	 */
	public String getSecondAspect() {
		return secondAspect;
	}

	/**
	 * @param secondAspect второй аспект выделения, если отмечены перевод или блок, иначе {@code null}
	 */
	public void setSecondAspect(String secondAspect) {
		this.secondAspect = updateProperty("secondAspect", this.secondAspect, secondAspect);
	}

	/**
	 * @return модификатор выделения — отмечен ли отдельный аспект, блок, или перевод
	 */
	public String getModifier() {
		return modifier;
	}

	/**
	 * @param modifier модификатор выделения — отмечен ли отдельный аспект, блок, или перевод
	 */
	public void setModifier(String modifier) {
		this.modifier = updateProperty("modifier", this.modifier, modifier);
	}

	/**
	 * @return знак основного аспекта в выделении
	 */
	public String getSign() {
		return sign;
	}

	/**
	 * @param sign знак основного аспекта в выделении
	 */
	public void setSign(String sign) {
		this.sign = updateProperty("sign", this.sign, sign);
	}

	/**
	 * @return индикатор ментала/витала
	 */
	public String getMV() {
		return mv;
	}

	/**
	 * @param mv индикатор ментала/витала
	 */
	public void setMV(String mv) {
		this.mv = updateProperty("MV", this.mv, mv);
	}

	/**
	 * @return индикатор размерности
	 */
	public String getDimension() {
		return dimension;
	}

	/**
	 * @param dimension индикатор размерности
	 */
	public void setDimension(String dimension) {
		this.dimension = updateProperty("dimension", this.dimension, dimension);
	}

	/**
	 * @return комментарий к выделению
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment комментарий к выделению
	 */
	public void setComment(String comment) {
		this.comment = updateProperty("comment", this.comment, comment);
	}

	/**
	 * @return является данный выделенный блок пустым ({@code true}), или с ним связаны какие-либо
	 * аналитические отметки ({@code false})
	 */
	public boolean isMarkupEmpty() {
		return aspect == null;
	}

	/**
	 * Собирает текущие данные из пометок в {@link AData} для хранения в документе.
	 *
	 * @return текущее состояние пометок
	 */
	public AData getMarkupData() {
		if (isMarkupEmpty()) {
			return null;
		}

		AData markupData = new AData(aspect, sign, dimension, mv, comment);
		markupData.setSecondAspect(secondAspect);
		markupData.setModifier(modifier);
		markupData.setComment(comment);
		return markupData;
	}

	/**
	 * Заполняет выделение блоком данных.
	 *
	 * @param markupData объект с данными для заполнения выделения. {@code null} очищает выделение.
	 */
	public void setMarkupData(AData markupData) {
		setInitialized(false);
		if (markupData == null) {
			setAspect(null);
		} else {
			setAspect(markupData.getAspect());
			setSecondAspect(markupData.getSecondAspect());
			setModifier(markupData.getModifier());
			setSign(markupData.getSign());
			setMV(markupData.getMV());
			setDimension(markupData.getDimension());
			setComment(markupData.getComment());
		}
		setInitialized(true);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * <p>Вспомогательный метод для изменения значений свойств объекта. Создает
	 * {@link java.beans.PropertyChangeEvent}, описывающий изменение, и возвращает новое значение свойства,
	 * которое можно присвоить полю.</p>
	 *
	 * <p>Использование:
	 * {@code myProperty = updateProperty("myProperty", myProperty, newPropertyValue)}</p>
	 * 
	 * @param propertyName имя свойства
	 * @param oldValue старое значение
	 * @param newValue новое значение
	 * @param <T> тип данных свойства
	 * @return новое значение свойства
	 */
	private <T> T updateProperty(String propertyName, T oldValue, T newValue) {
		if (oldValue != null || newValue != null) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
		}
		return newValue;
	}
}
