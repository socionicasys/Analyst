package analyst;

import java.io.Serializable;

public class DocSection implements Serializable {

	int start;
	int end;

	public DocSection(int start, int end) {
		if (start > end) {
			int temp = start;
			start = end;
			end = temp;
		}
		this.start = start;
		this.end = end;
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
