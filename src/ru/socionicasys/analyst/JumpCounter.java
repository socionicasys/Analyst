package ru.socionicasys.analyst;

import java.util.HashMap;

public class JumpCounter {
	private static class JumpPair {
		private final String from;
		private final String to;

		public JumpPair(final String from, final String to) {
			this.from = from;
			this.to = to;
		}
	}

	private final HashMap<JumpPair, Integer> jumpTable;

	public JumpCounter() {
		jumpTable = new HashMap<JumpPair, Integer>();
	}

	public void addJump(String to, String from) {
		JumpPair p = new JumpPair(from, to);
		if (jumpTable.containsKey(p)) {
			jumpTable.put(p, jumpTable.remove(p) + 1);
		} else {
			jumpTable.put(p, 1);
		}
	}

	public int getJumpCount(String to, String from) {
		JumpPair p = new JumpPair(from, to);
		if (jumpTable.containsKey(p)) {
			return jumpTable.get(p);
		} else {
			return 0;
		}
	}

	public boolean isEmpty() {
		return jumpTable.isEmpty();
	}

	public void clear() {
		jumpTable.clear();
	}
}
