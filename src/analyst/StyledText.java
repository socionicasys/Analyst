package analyst;

import javax.swing.text.AttributeSet;

public final class StyledText {
	private final String text;
	private final AttributeSet style;

	StyledText(final String text, final AttributeSet style) {
		this.text = text;
		this.style = style.copyAttributes();
	}

	String getText() {
		return this.text;
	}

	AttributeSet getStyle() {
		return this.style;
	}
}
