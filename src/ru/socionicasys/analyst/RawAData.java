package ru.socionicasys.analyst;

public class RawAData {
	private int handle = -1;
	private int beg = -1;
	private int end = -1;

	private String aData;
	private String comment;

	public RawAData(int handle) {
		this.handle = handle;
	}

	public void setID(int handle) {
		this.handle = handle;
	}

	public void setBegin(int beg) {
		this.beg = beg;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setAData(String aData) {
		this.aData = aData;
	}

	public void setComment(String com) {
		this.comment = com;
	}

	public int getID() {
		return handle;
	}

	public int getBegin() {
		return beg;
	}

	public int getEnd() {
		return end;
	}

	public String getAData() {
		return aData;
	}

	public String getComment() {
		return this.comment;
	}
}
