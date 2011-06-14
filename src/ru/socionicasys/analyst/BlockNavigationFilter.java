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
	private int backupDot;
	private boolean backupDotActive;
	private static final Logger logger = LoggerFactory.getLogger(BlockNavigationFilter.class);

	public BlockNavigationFilter(ADocument document) {
		this.document = document;
		backupDotActive = false;
	}

	@Override
	public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
		logger.debug("setDot: dot = {}", dot);
		ASection currentSection = document.getASection(dot);
		if (currentSection == null) {
			logger.debug("setDot: no section here, keep dot where it is");
			backupDotActive = false;
			fb.setDot(dot, bias);
			return;
		}
		logger.debug("setDot: moving dot to section borders: {}, {}",
			currentSection.getStartOffset(), currentSection.getEndOffset());
		backupDotActive = true;
		backupDot = dot;
		fb.setDot(currentSection.getEndOffset(), bias);
		fb.moveDot(currentSection.getStartOffset(), bias);
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
