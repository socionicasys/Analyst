package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.socionicasys.analyst.types.Sociotype;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.*;

/**
 * @author Виктор
 */
public class BTree extends JTree implements ADocumentChangeListener {
	private static final int MAX_PRESENTATION_CHARS = 100;

	private static final Logger logger = LoggerFactory.getLogger(BTree.class);

	private final DefaultMutableTreeNode rootNode;
	private final DefaultTreeModel treeModel;

	private final Map<Sociotype, DefaultMutableTreeNode> matchNodes;
	private final Map<Sociotype, DefaultMutableTreeNode> missNodes;

	public BTree(ADocument document) {
		rootNode = new DefaultMutableTreeNode(document.getProperty(Document.TitleProperty));
		treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);

		matchNodes = initNodes("Соответствие");
		missNodes = initNodes("Несоответствие");

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setEditable(false);
		toggleClickCount = 1;

		updateTree(document);
	}

	private Map<Sociotype, DefaultMutableTreeNode> initNodes(String parentLabel) {
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode(parentLabel);
		rootNode.add(parent);
		Map<Sociotype, DefaultMutableTreeNode> nodesMap =
			new EnumMap<Sociotype, DefaultMutableTreeNode>(Sociotype.class);
		for (Sociotype sociotype : Sociotype.values()) {
			DefaultMutableTreeNode sociotypeNode = new EndTreeNode(sociotype);
			parent.add(sociotypeNode);
			nodesMap.put(sociotype, sociotypeNode);
		}
		return nodesMap;
	}


	private void updateTree(ADocument document) {
		if (document == null) {
			return;
		}

		logger.debug("Document has changed, updating tree");

		rootNode.setUserObject(document.getProperty(Document.TitleProperty));
		TreePath selectionPath = getSelectionPath();
		if (selectionPath != null) {
			logger.debug("Previous selection path {}", selectionPath);
		}

		//Analyze document structure and update tree nodes
		try {
			removeAllChildren();
			for (Entry<ASection, AData> dataEntry : document.getADataMap().entrySet()) {
				int sectionOffset = dataEntry.getKey().getStartOffset();
				int sectionLength = Math.abs(dataEntry.getKey().getEndOffset() - sectionOffset);
				int quoteLength = Math.min(sectionLength, MAX_PRESENTATION_CHARS);

				AData data = dataEntry.getValue();

				String aspect = data.getAspect();
				String secondAspect = data.getSecondAspect();
				String modifier = data.getModifier();
				String dimension = data.getDimension();
				String sign = data.getSign();
				String mv = data.getMV();
				String quote = document.getText(sectionOffset, quoteLength);

				if (aspect == null || aspect.equals(AData.DOUBT)) {
					continue;
				}
				if (modifier != null && modifier.equals(AData.JUMP)) {
					continue;
				}

				for (Sociotype sociotype : Sociotype.values()) {
					MutableTreeNode quoteNode = new DefaultMutableTreeNode(
						new EndNodeObject(sectionOffset, "..." + quote + "..."), false);
					if (SocionicsType.matches(sociotype, aspect, secondAspect, sign, dimension, mv)) {
						matchNodes.get(sociotype).add(quoteNode);
					} else {
						missNodes.get(sociotype).add(quoteNode);
					}
				}
			}
		} catch (BadLocationException e) {
			logger.error("Illegal document location in updateTree()", e);
		}

		treeModel.reload();

		if (selectionPath != null) {
			logger.debug("Setting back selection path to {} after model reload", selectionPath);
			setSelectionPath(selectionPath);
		}
	}

	public JScrollPane getContainer() {
		JScrollPane sp = new JScrollPane(this);
		sp.setPreferredSize(new Dimension(200, 500));
		return sp;
	}

	@Override
	public void aDocumentChanged(ADocument doc) {
		updateTree(doc);
	}

	private void removeAllChildren() {
		for (DefaultMutableTreeNode node : matchNodes.values()) {
			node.removeAllChildren();
		}
		for (DefaultMutableTreeNode node : missNodes.values()) {
			node.removeAllChildren();
		}
	}
}
