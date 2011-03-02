package ru.socionicasys.analyst;

import java.util.HashMap;

public class JumpCounter {
	private final HashMap<String, HashMap<String, Integer>> jumpTable;

	public JumpCounter() {
		jumpTable = new HashMap<String, HashMap<String, Integer>>();
	}

	public void addJump(String to, String from) {
		HashMap<String, Integer> t;
		if (!jumpTable.containsKey(to)) {
			t = new HashMap<String, Integer>();
		} else {
			t = jumpTable.get(to);
		}
		if (t.containsKey(from)) {
			t.put(from, t.remove(from) + 1);
		} else {
			t.put(from, 1);
		}
		jumpTable.put(to, t);
	}

	public int getJumpCount(String to, String from) {
		HashMap<String, Integer> t;
		if (jumpTable.containsKey(to)) {
			t = jumpTable.get(to);
		} else {
			return 0;
		}
		if (!t.containsKey(from)) {
			return 0;
		}
		return t.get(from);
	}

	public boolean isEmpty() {
		return jumpTable.isEmpty();
	}

	public void clear() {
		jumpTable.clear();
	}
}
