package ru.socionicasys.analyst;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Наследник {@link JTextPane}, привязывающийся к контейнеру {@link DocumentHolder}
 * и меняющий свой документ при смене документа в контейнере.
 */
public class TextPane extends JTextPane implements ModelChangedListener<ADocument> {
	private final Map<String, Action> actionMap;

	/**
	 * Создает {@code TextPane}, привязанный к данному контейнеру {@link DocumentHolder}.
	 * @param documentHolder контейнер, к документам которого будет привязан экземпляр {@code TextPane}
	 */
	public TextPane(DocumentHolder documentHolder) {
		super(documentHolder.getModel());
		documentHolder.addModelChangedListener(this);

		// Replace the built-in  behavior when the caret highlight
		// becomes invisible when focus moves to another component
		setCaret(new HighlightCaret());

		setNavigationFilter(new BlockNavigationFilter(documentHolder));
		setCaretPosition(0);
		setMinimumSize(new Dimension(400, 100));

		actionMap = new HashMap<String, Action>();
		for (Action action : getActions()) {
			actionMap.put((String) action.getValue(Action.NAME), action);
		}

		setComponentPopupMenu(createPopupMenu());
	}

	private JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		Action cutAction = new AEditorKit.CutAction(this);
		cutAction.putValue(Action.NAME, "Вырезать");
		popupMenu.add(cutAction);

		Action copyAction = new AEditorKit.CopyAction(this);
		copyAction.putValue(Action.NAME, "Копировать");
		popupMenu.add(copyAction);

		Action pasteAction = new AEditorKit.PasteAction(this);
		pasteAction.putValue(Action.NAME, "Вставить");
		popupMenu.add(pasteAction);

		Action selectAllAction = getAction(DefaultEditorKit.selectAllAction);
		selectAllAction.putValue(Action.NAME, "Выделить всё");
		popupMenu.add(selectAllAction);

		popupMenu.addSeparator();

		popupMenu.add(new SearchAction(this));

		return popupMenu;
	}

	@Override
	public ADocument getDocument() {
		return (ADocument)super.getDocument();
	}

	@Override
	protected EditorKit createDefaultEditorKit() {
		return new AEditorKit();
	}

	@Override
	public void modelChanged(ADocument oldModel, ADocument newModel, ModelHolder<ADocument> modelHolder) {
		setDocument(newModel);
	}

	/**
	 * Возращает действие из списка внутренних действий {@code TextPane}, по его имени.
	 * @param name имя требуемого действия
	 * @return действие с заданным именем, или {@code null}, если компонент не содержит такого действия
	 */
	public Action getAction(String name) {
		return actionMap.get(name);
	}
}
