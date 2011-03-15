package ru.socionicasys.analyst;

import javax.swing.tree.DefaultMutableTreeNode;

class EndTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -6457224131039102370L;

	public EndTreeNode(Object o) {
		super(o);
	}

	@Override
	public String toString() {
		return String.format("[%d] %s", getChildCount(), super.toString());
	}
}
