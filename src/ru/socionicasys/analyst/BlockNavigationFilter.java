package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.Caret;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;

/**
 * Реализует выделение блока с пометкой в документе при клике внутри этого блока.
 */
public class BlockNavigationFilter extends NavigationFilter {
	private final ADocument document;
	private static final Logger logger = LoggerFactory.getLogger(BlockNavigationFilter.class);

	public BlockNavigationFilter(ADocument document) {
		this.document = document;
	}

	@Override
	public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
		Caret caret = fb.getCaret();
		logger.debug("setDot: dot = {} (caret.dot = {}, caret.mark = {})",
			new Object[]{dot, caret.getDot(), caret.getMark()});
		ASection currentSection = document.getASection(dot);
		if (currentSection == null) {
			logger.debug("setDot: no section here, keep dot where it is");
			fb.setDot(dot, bias);
			return;
		}
		logger.debug("setDot: moving dot to section borders: {}, {}",
			currentSection.getStartOffset(), currentSection.getEndOffset());
		fb.setDot(currentSection.getEndOffset(), bias);
		fb.moveDot(currentSection.getStartOffset(), bias);
	}

	@Override
	public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
		logger.debug(String.format("moveDot: dot = %d", dot));
		super.moveDot(fb, dot, bias);
	}
}
