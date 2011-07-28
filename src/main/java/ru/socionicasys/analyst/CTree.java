package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.socionicasys.analyst.types.Sociotype;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

/**
 * Окно статистики совпадений и несовпадений с разными ТИМами.
 * @author Виктор
 */
public class CTree extends JTree implements ADocumentChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(CTree.class);
	private static final Color TEXT_COLOR = new Color(30, 120, 255);
	private static final int SCALE = 10;
	private static final float PERCENT = 100.0f;

	private final DefaultMutableTreeNode rootNode;
	private final DefaultTreeModel treeModel;

	public CTree(DocumentHolder documentHolder) {
		rootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);

		setCellRenderer(new HistogramCellRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setEditable(false);
		toggleClickCount = 1;
		putClientProperty("JTree.lineStyle", "None");

		updateTree(documentHolder.getModel().getMatchMissModel());
	}

	private void updateTree(MatchMissModel matchMissModel) {
		rootNode.removeAllChildren();

		StringBuilder barBuilder = new StringBuilder(SCALE + 1);
		for (int i = 0; i <= SCALE; i++) {
			barBuilder.append('█');
		}
		String bar = barBuilder.toString();

		// Заполнение гистограммы
		for (Sociotype sociotype : Sociotype.values()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode();
			rootNode.add(node);
			float coefficient = matchMissModel.get(sociotype).getMatchCoefficient();
			node.setUserObject(String.format("%1$s : %2$s %3$2.0f",
				sociotype.getAbbreviation(),
				bar.substring(0, (int) (SCALE * coefficient + 1)),
				PERCENT * coefficient
			));
		}

		treeModel.reload();
	}

	public JScrollPane getContainer() {
		JScrollPane sp = new JScrollPane(this);
		sp.setPreferredSize(new Dimension(200, 500));
		return sp;
	}

	@Override
	public void aDocumentChanged(ADocument document) {
		rootNode.setUserObject(document.getProperty(Document.TitleProperty));
		updateTree(document.getMatchMissModel());
	}

	private static class HistogramCellRenderer implements TreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			JLabel label = new JLabel();
			if (leaf) {
				label.setText(value.toString());
				label.setFont(new Font(Font.MONOSPACED, Font.BOLD, label.getFont().getSize()));
				label.setForeground(TEXT_COLOR);
			} else {
				label.setText(" ");
				label.setForeground(Color.BLACK);
			}
			return label;
		}
	}
}
