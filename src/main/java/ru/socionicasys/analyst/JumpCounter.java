package ru.socionicasys.analyst;

import ru.socionicasys.analyst.util.EqualsUtil;
import ru.socionicasys.analyst.util.HashUtil;

import java.util.HashMap;
import java.util.Map;

public class JumpCounter {
	private static final class JumpPair {
		private final String from;
		private final String to;

		private JumpPair(final String from, final String to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof JumpPair)) {
				return false;
			}

			JumpPair jumpPair = (JumpPair) obj;
			return EqualsUtil.areEqual(from, jumpPair.from) &&
				EqualsUtil.areEqual(to, jumpPair.to);
		}

		@Override
		public int hashCode() {
			HashUtil hashUtil = new HashUtil();
			hashUtil.hash(from);
			hashUtil.hash(to);
			return hashUtil.getComputedHash();
		}
	}

	private final Map<JumpPair, Integer> jumpTable;

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
