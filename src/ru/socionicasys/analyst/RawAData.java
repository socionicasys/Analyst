package ru.socionicasys.analyst;

public class RawAData {
	private int begin = -1;
	private int end = -1;

	private String aData;
	private String comment;

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setAData(String aData) {
		this.aData = aData;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	public String getAData() {
		return aData;
	}

	public String getComment() {
		return comment;
	}
}
