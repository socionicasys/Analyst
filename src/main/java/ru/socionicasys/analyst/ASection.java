package ru.socionicasys.analyst;

import ru.socionicasys.analyst.util.HashUtil;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

public class ASection implements Comparable<ASection> {
	private final Position start;
	private final Position end;
	private final AttributeSet attributes;

	public ASection(Document sourceDocument, int startOffset, int endOffset)
			throws BadLocationException {
		this(sourceDocument, startOffset, endOffset, null);
	}

	public ASection(Document sourceDocument, int startOffset, int endOffset, AttributeSet attributes)
			throws BadLocationException {
		start = sourceDocument.createPosition(startOffset);
		end = sourceDocument.createPosition(endOffset);
		this.attributes = attributes;
	}

	public AttributeSet getAttributes() {
		return attributes;
	}

	public int getStartOffset() {
		return start.getOffset();
	}

	public int getEndOffset() {
		return end.getOffset();
	}

	public int getMiddleOffset() {
		return (end.getOffset() + start.getOffset()) / 2;
	}

	public boolean containsOffset(int offset) {
		int startOffset = start.getOffset();
		int endOffset = end.getOffset();
		return startOffset < endOffset && offset >= startOffset && offset < endOffset;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ASection)) {
			return false;
		}
		ASection otherSection = (ASection) obj;
		return getStartOffset() == otherSection.getStartOffset()
				&& getEndOffset() == otherSection.getEndOffset();
	}

	@Override
	public int hashCode() {
		HashUtil hashUtil = new HashUtil();
		hashUtil.hash(start);
		hashUtil.hash(end);
		return hashUtil.getComputedHash();
	}

	@Override
	public int compareTo(ASection o) {
		return getStartOffset() - o.getStartOffset();
	}
}
