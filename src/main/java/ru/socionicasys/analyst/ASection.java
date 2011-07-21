package ru.socionicasys.analyst;

import ru.socionicasys.analyst.util.HashUtil;

import javax.swing.text.AttributeSet;

public class ASection implements Comparable<ASection> {
	private final int startOffset;
	private final int endOffset;
	private final AttributeSet attributes;

	public ASection(int startOffset, int endOffset) {
		this(startOffset, endOffset, null);
	}

	public ASection(int startOffset, int endOffset, AttributeSet attributes) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.attributes = attributes;
	}

	public AttributeSet getAttributes() {
		return attributes;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public int getMiddleOffset() {
		return (endOffset + startOffset) / 2;
	}

	public boolean containsOffset(int offset) {
		return startOffset < endOffset && offset >= startOffset && offset < endOffset;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ASection)) {
			return false;
		}
		ASection otherSection = (ASection) obj;
		return startOffset == otherSection.startOffset &&
			endOffset == otherSection.endOffset;
	}

	@Override
	public int hashCode() {
		HashUtil hashUtil = new HashUtil();
		hashUtil.hash(startOffset);
		hashUtil.hash(endOffset);
		return hashUtil.getComputedHash();
	}

	@Override
	public int compareTo(ASection o) {
		return startOffset - o.startOffset;
	}
}
