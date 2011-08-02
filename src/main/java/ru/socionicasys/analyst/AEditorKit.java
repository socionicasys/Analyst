package ru.socionicasys.analyst;

import javax.swing.text.*;

/**
 * {@code EditorKit} для {@link TextPane}, задающий стандартным документом для него {@link ADocument}.
 */
@SuppressWarnings("serial")
public class AEditorKit extends StyledEditorKit {
	@Override
	public Document createDefaultDocument() {
		return new ADocument();
	}
}
