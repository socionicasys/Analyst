package ru.socionicasys.analyst;

import java.io.Serializable;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;

public class ASection implements Serializable, Comparable<ASection> {
	private Position start;
	private Position end;
	private AttributeSet attributes;

	public ASection(Position start, Position end) {
		this.start = start;
		this.end = end;
	}

	public AttributeSet getAttributes() {
		return attributes;
	}

	public void setAttributes(AttributeSet as) {
		this.attributes = as;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ASection)) {
			return false;
		}
		ASection otherSection = (ASection) o;
		return (start.getOffset() == otherSection.getStartOffset()) &&
			(end.getOffset() == otherSection.getEndOffset());
	}

	public int getStartOffset() {
		return start.getOffset();
	}

	public int getEndOffset() {
		return end.getOffset();
	}

	public int getMiddleOffset() {
		return ((end.getOffset() + start.getOffset()) / 2);
	}

	public boolean containsOffset(int offset) {
		int b = start.getOffset();
		int e = end.getOffset();
		return b < e && offset >= b && offset < e;
	}

	@Override
	public int compareTo(ASection o) {
		return getStartOffset() - o.getStartOffset();
	}
}
