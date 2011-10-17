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
	/**
	 * Документ, к которому привязан фильтр
	 */
	private final DocumentHolder documentHolder;

	/**
	 * Позиция курсора до перемещения к границам блока
	 */
	private int backupDot;

	/**
	 * Произошло ли во время последнего вызова setDot перемещение курсора к границам блока
	 */
	private boolean backupDotActive;

	private static final Logger logger = LoggerFactory.getLogger(BlockNavigationFilter.class);

	public BlockNavigationFilter(DocumentHolder documentHolder) {
		this.documentHolder = documentHolder;
		backupDotActive = false;
	}

	@Override
	public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
		logger.debug("setDot: dot = {}", dot);
		backupDotActive = false;
		DocumentSection currentSection = documentHolder.getModel().getSection(dot);
		if (currentSection == null) {
			logger.debug("setDot: no section here, keep dot where it is");
			super.setDot(fb, dot, bias);
			return;
		}

		Caret caret = fb.getCaret();
		logger.debug("setDot: getCaret() = {}", caret);
		int startOffset = currentSection.getStartOffset();
		int endOffset = currentSection.getEndOffset();
		int caretDot = caret.getDot();
		int caretMark = caret.getMark();
		if (caretDot == endOffset && caretMark == startOffset
			|| caretDot == startOffset && caretMark == endOffset) {
			fb.setDot(dot, bias);
			return;
		}

		logger.debug("setDot: moving dot to section borders: {}, {}",
			startOffset, endOffset);
		backupDotActive = true;
		backupDot = dot;
		fb.setDot(endOffset, bias);
		fb.moveDot(startOffset, bias);
	}

	@Override
	public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
		logger.debug("moveDot: dot = {}", dot);
		if (backupDotActive) {
			backupDotActive = false;
			logger.debug("moveDot: restoring previous dot = {}", backupDot);
			fb.setDot(backupDot, bias);
		}
		super.moveDot(fb, dot, bias);
	}
}
