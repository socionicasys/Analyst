package ru.socionicasys.analyst;

/**
 * @author Виктор
 */
public class EndNodeObject {
	private final String string;
	private final int offset;

	public EndNodeObject(int offset, String str) {
		this.string = str;
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public String getString() {
		return string;
	}

	@Override
	public String toString() {
		return string == null ? "" : string;
	}
}
