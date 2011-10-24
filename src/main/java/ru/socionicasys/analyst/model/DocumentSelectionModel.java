package ru.socionicasys.analyst.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Описывает выделение внутри документа, и возможные отметки в этом выделении.
 * Позволяет другим объектам отслеживать изменения в этом выделении.
 */
@SuppressWarnings("NestedAssignment")
public class DocumentSelectionModel {
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

	private static final Logger logger = LoggerFactory.getLogger(DocumentSelectionModel.class);

	/**
	 * Инициализирует пустую модель выделения.
	 */
	public DocumentSelectionModel() {
		logger.trace("DocumentSelectionModel(): entering");
		propertyChangeSupport = new PropertyChangeSupport(this);
		comment = "";
		logger.trace("DocumentSelectionModel(): leaving");
	}

	/**
	 * @return пусто ли выделение
	 */
	public boolean isEmpty() {
		return startOffset == endOffset;
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
		updateProperty("initialized", this.initialized, this.initialized = initialized);
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
		updateProperty("startOffset", this.startOffset, this.startOffset = startOffset);
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
		updateProperty("endOffset", this.endOffset, this.endOffset = endOffset);
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
		updateProperty("aspect", this.aspect, this.aspect = aspect);
		if (aspect == null) {
			setSecondAspect(null);
			setModifier(null);
			setSign(null);
			setMV(null);
			setDimension(null);
			setComment("");
		} else if (AData.DOUBT.equals(aspect)) {
			setSecondAspect(null);
			setModifier(null);
		}
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
		updateProperty("secondAspect", this.secondAspect, this.secondAspect = secondAspect);
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
		updateProperty("modifier", this.modifier, this.modifier = modifier);
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
		updateProperty("sign", this.sign, this.sign = sign);
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
		updateProperty("MV", this.mv, this.mv = mv);
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
		updateProperty("dimension", this.dimension, this.dimension = dimension);
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
		updateProperty("comment", this.comment, this.comment = comment);
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
		return new AData(aspect, secondAspect, sign, dimension, mv, modifier, comment);
	}

	/**
	 * Заполняет выделение блоком данных.
	 *
	 * @param markupData объект с данными для заполнения выделения. {@code null} очищает выделение.
	 */
	public void setMarkupData(AData markupData) {
		logger.trace("setMarkupData({}): entering", markupData);
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
		logger.trace("setMarkupData(): leaving");
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
	 * {@code updateProperty("myProperty", myProperty, myProperty = newPropertyValue)}</p>
	 * 
	 * @param propertyName имя свойства
	 * @param oldValue старое значение
	 * @param newValue новое значение
	 * @param <T> тип данных свойства
	 */
	private <T> void updateProperty(String propertyName, T oldValue, T newValue) {
		logger.trace("updateProperty({}, {}, {}): entering", new Object[]{propertyName, oldValue, newValue});
		if (oldValue != null || newValue != null) {
			propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
		}
		logger.trace("updateProperty({}, {}, {}): leaving", new Object[]{propertyName, oldValue, newValue});
	}
}
