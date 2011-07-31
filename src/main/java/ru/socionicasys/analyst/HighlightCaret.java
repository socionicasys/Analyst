package ru.socionicasys.analyst;

import java.awt.event.FocusEvent;
import javax.swing.text.DefaultCaret;

/**
 * Курсор, не скрывающий выделение при потере фокуса.
 */
@SuppressWarnings("serial")
public class HighlightCaret extends DefaultCaret {
	@Override
	public void focusLost(FocusEvent e) {
	}
}
