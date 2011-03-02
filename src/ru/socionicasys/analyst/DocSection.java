package ru.socionicasys.analyst;

import java.io.Serializable;

public class DocSection implements Serializable {
	private final int start;
	private final int end;

	public DocSection(int start, int end) {
		this.start = Math.min(start, end);
		this.end = Math.max(start, end);
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getLength() {
		return end - start;
	}
}
