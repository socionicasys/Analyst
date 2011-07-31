package ru.socionicasys.analyst;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class EndTreeNode extends DefaultMutableTreeNode {
	public EndTreeNode(Object o) {
		super(o);
	}

	@Override
	public String toString() {
		return String.format("[%d] %s", getChildCount(), super.toString());
	}
}
