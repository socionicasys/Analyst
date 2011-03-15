/**ypeypeype
 *
 */
package ru.socionicasys.analyst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.socionicasys.analyst.types.Sociotype;

import java.awt.Dimension;
import java.util.*;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * @author Виктор
 */
@SuppressWarnings("serial")

public class BTree extends JTree implements
	ADocumentChangeListener {

	ADocument aDoc;
	DefaultMutableTreeNode rootNode;
	DefaultTreeModel treeModel;
	TreePath path;

	private static final Logger logger = LoggerFactory.getLogger(BTree.class);

	private DefaultMutableTreeNode matchNode = new DefaultMutableTreeNode("Соответствие");
	private DefaultMutableTreeNode ileMatchNode = new EndTreeNode(Sociotype.ILE);
	private DefaultMutableTreeNode seiMatchNode = new EndTreeNode(Sociotype.SEI);
	private DefaultMutableTreeNode eseMatchNode = new EndTreeNode(Sociotype.ESE);
	private DefaultMutableTreeNode liiMatchNode = new EndTreeNode(Sociotype.LII);
	private DefaultMutableTreeNode eieMatchNode = new EndTreeNode(Sociotype.EIE);
	private DefaultMutableTreeNode lsiMatchNode = new EndTreeNode(Sociotype.LSI);
	private DefaultMutableTreeNode sleMatchNode = new EndTreeNode(Sociotype.SLE);
	private DefaultMutableTreeNode ieiMatchNode = new EndTreeNode(Sociotype.IEI);
	private DefaultMutableTreeNode seeMatchNode = new EndTreeNode(Sociotype.SEE);
	private DefaultMutableTreeNode iliMatchNode = new EndTreeNode(Sociotype.ILI);
	private DefaultMutableTreeNode lieMatchNode = new EndTreeNode(Sociotype.LIE);
	private DefaultMutableTreeNode esiMatchNode = new EndTreeNode(Sociotype.ESI);
	private DefaultMutableTreeNode lseMatchNode = new EndTreeNode(Sociotype.LSE);
	private DefaultMutableTreeNode eiiMatchNode = new EndTreeNode(Sociotype.EII);
	private DefaultMutableTreeNode ieeMatchNode = new EndTreeNode(Sociotype.IEE);
	private DefaultMutableTreeNode sliMatchNode = new EndTreeNode(Sociotype.SLI);

	private DefaultMutableTreeNode noMatchNode = new DefaultMutableTreeNode("Несоответствие");
	private DefaultMutableTreeNode ileNoMatchNode = new EndTreeNode(Sociotype.ILE);
	private DefaultMutableTreeNode seiNoMatchNode = new EndTreeNode(Sociotype.SEI);
	private DefaultMutableTreeNode eseNoMatchNode = new EndTreeNode(Sociotype.ESE);
	private DefaultMutableTreeNode liiNoMatchNode = new EndTreeNode(Sociotype.LII);
	private DefaultMutableTreeNode eieNoMatchNode = new EndTreeNode(Sociotype.EIE);
	private DefaultMutableTreeNode lsiNoMatchNode = new EndTreeNode(Sociotype.LSI);
	private DefaultMutableTreeNode sleNoMatchNode = new EndTreeNode(Sociotype.SLE);
	private DefaultMutableTreeNode ieiNoMatchNode = new EndTreeNode(Sociotype.IEI);
	private DefaultMutableTreeNode seeNoMatchNode = new EndTreeNode(Sociotype.SEE);
	private DefaultMutableTreeNode iliNoMatchNode = new EndTreeNode(Sociotype.ILI);
	private DefaultMutableTreeNode lieNoMatchNode = new EndTreeNode(Sociotype.LIE);
	private DefaultMutableTreeNode esiNoMatchNode = new EndTreeNode(Sociotype.ESI);
	private DefaultMutableTreeNode lseNoMatchNode = new EndTreeNode(Sociotype.LSE);
	private DefaultMutableTreeNode eiiNoMatchNode = new EndTreeNode(Sociotype.EII);
	private DefaultMutableTreeNode ieeNoMatchNode = new EndTreeNode(Sociotype.IEE);
	private DefaultMutableTreeNode sliNoMatchNode = new EndTreeNode(Sociotype.SLI);


	private class EndTreeNode extends DefaultMutableTreeNode {

		public EndTreeNode(Object o) {
			super(o);
		}

		@Override
		public String toString() {
			return "[" + getChildCount() + "] " + super.toString();
		}
	}

	private class Counter {

		private Vector<Sociotype> counter;

		public Counter() {
			counter = new Vector<Sociotype>();
			counter.add(Sociotype.ILE);
			counter.add(Sociotype.SEI);
			counter.add(Sociotype.ESE);
			counter.add(Sociotype.LII);
			counter.add(Sociotype.EIE);
			counter.add(Sociotype.LSI);
			counter.add(Sociotype.SLE);
			counter.add(Sociotype.IEI);
			counter.add(Sociotype.SEE);
			counter.add(Sociotype.ILI);
			counter.add(Sociotype.LIE);
			counter.add(Sociotype.ESI);
			counter.add(Sociotype.LSE);
			counter.add(Sociotype.EII);
			counter.add(Sociotype.IEE);
			counter.add(Sociotype.SLI);
		}

		public void exclude(Sociotype type) {
			counter.remove(type);
		}

		public Vector<Sociotype> getExcludedTypes() {
			Vector<Sociotype> vec = new Vector<Sociotype>();

			if (!counter.contains(Sociotype.ILE)) vec.add(Sociotype.ILE);
			if (!counter.contains(Sociotype.SEI)) vec.add(Sociotype.SEI);
			if (!counter.contains(Sociotype.ESE)) vec.add(Sociotype.ESE);
			if (!counter.contains(Sociotype.LII)) vec.add(Sociotype.LII);
			if (!counter.contains(Sociotype.EIE)) vec.add(Sociotype.EIE);
			if (!counter.contains(Sociotype.LSI)) vec.add(Sociotype.LSI);
			if (!counter.contains(Sociotype.SLE)) vec.add(Sociotype.SLE);
			if (!counter.contains(Sociotype.IEI)) vec.add(Sociotype.IEI);
			if (!counter.contains(Sociotype.SEE)) vec.add(Sociotype.SEE);
			if (!counter.contains(Sociotype.ILI)) vec.add(Sociotype.ILI);
			if (!counter.contains(Sociotype.LIE)) vec.add(Sociotype.LIE);
			if (!counter.contains(Sociotype.ESI)) vec.add(Sociotype.ESI);
			if (!counter.contains(Sociotype.LSE)) vec.add(Sociotype.LSE);
			if (!counter.contains(Sociotype.EII)) vec.add(Sociotype.EII);
			if (!counter.contains(Sociotype.IEE)) vec.add(Sociotype.IEE);
			if (!counter.contains(Sociotype.SLI)) vec.add(Sociotype.SLI);

			return vec;
		}


		public Vector<Sociotype> getMatchTypes() {

			return counter;
		}
	}//end class Counter


	public BTree(ADocument doc) {
		super();
		rootNode = new DefaultMutableTreeNode(doc.getProperty(Document.TitleProperty));
		treeModel = new DefaultTreeModel(rootNode);
		setModel(treeModel);
		this.aDoc = doc;
		doc.addADocumentChangeListener(this);
		init();
	}


	private void init() {
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setEditable(false);
		toggleClickCount = 1;
		makeTreeStructure();
		updateTree();
	}


	private void updateTree() {
//temporary return		
		//	if(true)return;

		if (aDoc == null) return;

		rootNode.setUserObject(aDoc.getProperty(ADocument.TitleProperty));
		TreePath newPath = getSelectionPath();
		if (newPath != null) path = newPath;

		//Analyze document structure and update tree nodes
		try {
			Map<ASection, AData> aDataMap = aDoc.getADataMap();

			removeAllChildren();

			Set<ASection> set = aDataMap.keySet();
			Iterator<ASection> it = set.iterator();
			ASection section = null;
			int sectionOffset = 0;
			int sectionLength = 0;
			int quoteLength = 0;
			AData data = null;
			String aspect, secondAspect;
			String modifier;
			String dimension;
			String sign;
			String comment;
			String mv;
			String quote;

			while (it.hasNext()) {
				section = it.next();

				sectionOffset = section.getStartOffset();
				sectionLength = Math.abs(section.getEndOffset() - sectionOffset);
				quoteLength = Math.min(sectionLength, ATree.MAX_PRESENTATION_CHARS);

				data = aDataMap.get(section);
				aspect = data.getAspect();
				secondAspect = data.getSecondAspect();
				modifier = data.getModifier();

				dimension = data.getDimension();
				sign = data.getSign();
				comment = data.getComment();
				mv = data.getMV();
				quote = aDoc.getText(sectionOffset, quoteLength);

				//Отладка
				//if (!aspect.equals(AData.L)) continue;
				if (aspect.equals(AData.DOUBT)) continue;
				if (modifier != null && modifier.equals(AData.JUMP)) continue;

				Counter counter = new Counter();

				if (aspect != null) {

					if (!SocionicsType.matches(Sociotype.ILE, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.ILE);
					if (!SocionicsType.matches(Sociotype.SEI, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.SEI);
					if (!SocionicsType.matches(Sociotype.ESE, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.ESE);
					if (!SocionicsType.matches(Sociotype.LII, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.LII);
					if (!SocionicsType.matches(Sociotype.EIE, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.EIE);
					if (!SocionicsType.matches(Sociotype.LSI, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.LSI);
					if (!SocionicsType.matches(Sociotype.SLE, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.SLE);
					if (!SocionicsType.matches(Sociotype.IEI, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.IEI);
					if (!SocionicsType.matches(Sociotype.SEE, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.SEE);
					if (!SocionicsType.matches(Sociotype.ILI, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.ILI);
					if (!SocionicsType.matches(Sociotype.LIE, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.LIE);
					if (!SocionicsType.matches(Sociotype.ESI, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.ESI);
					if (!SocionicsType.matches(Sociotype.LSE, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.LSE);
					if (!SocionicsType.matches(Sociotype.EII, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.EII);
					if (!SocionicsType.matches(Sociotype.IEE, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.IEE);
					if (!SocionicsType.matches(Sociotype.SLI, aspect, secondAspect, sign, dimension, mv))
						counter.exclude(Sociotype.SLI);
				}// if (aspect != null)

				Vector<Sociotype> c = counter.getMatchTypes();
				if (c.contains(Sociotype.ILE))
					ileMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					ileNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.SEI))
					seiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					seiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.ESE))
					eseMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					eseNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.LII))
					liiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					liiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.EIE))
					eieMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					eieNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.LSI))
					lsiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					lsiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.SLE))
					sleMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					sleNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.IEI))
					ieiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					ieiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.SEE))
					seeMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					seeNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.ILI))
					iliMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					iliNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.LIE))
					lieMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					lieNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.ESI))
					esiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					esiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.LSE))
					lseMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					lseNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.EII))
					eiiMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					eiiNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.IEE))
					ieeMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					ieeNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				if (c.contains(Sociotype.SLI))
					sliMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
				else
					sliNoMatchNode.add(new DefaultMutableTreeNode(new EndNodeObject(sectionOffset, "..." + quote + "..."), false));
			} //end While()
		} catch (BadLocationException e) {
			logger.error("Illegal document location in updateTree()", e);
		}

		treeModel.reload();

		if (path != null) {
			if (path.getLastPathComponent() instanceof DefaultMutableTreeNode
				&& ((DefaultMutableTreeNode) path.getLastPathComponent()).isLeaf()) expandPath(path.getParentPath());
			else expandPath(path);
		}
	}


	private void makeTreeStructure() {

		rootNode.add(matchNode);
		rootNode.add(noMatchNode);

		matchNode.add(ileMatchNode);
		matchNode.add(seiMatchNode);
		matchNode.add(eseMatchNode);
		matchNode.add(liiMatchNode);
		matchNode.add(eieMatchNode);
		matchNode.add(lsiMatchNode);
		matchNode.add(sleMatchNode);
		matchNode.add(ieiMatchNode);
		matchNode.add(seeMatchNode);
		matchNode.add(iliMatchNode);
		matchNode.add(lieMatchNode);
		matchNode.add(esiMatchNode);
		matchNode.add(lseMatchNode);
		matchNode.add(eiiMatchNode);
		matchNode.add(ieeMatchNode);
		matchNode.add(sliMatchNode);

		noMatchNode.add(ileNoMatchNode);
		noMatchNode.add(seiNoMatchNode);
		noMatchNode.add(eseNoMatchNode);
		noMatchNode.add(liiNoMatchNode);
		noMatchNode.add(eieNoMatchNode);
		noMatchNode.add(lsiNoMatchNode);
		noMatchNode.add(sleNoMatchNode);
		noMatchNode.add(ieiNoMatchNode);
		noMatchNode.add(seeNoMatchNode);
		noMatchNode.add(iliNoMatchNode);
		noMatchNode.add(lieNoMatchNode);
		noMatchNode.add(esiNoMatchNode);
		noMatchNode.add(lseNoMatchNode);
		noMatchNode.add(eiiNoMatchNode);
		noMatchNode.add(ieeNoMatchNode);
		noMatchNode.add(sliNoMatchNode);
	}

	public JScrollPane getContainer() {
		JScrollPane sp = new JScrollPane(this);
		sp.setPreferredSize(new Dimension(200, 500));
		return sp;
	}

	@Override
	public void aDocumentChanged(ADocument doc) {
		updateTree();
	}

	private void removeAllChildren() {

		ileMatchNode.removeAllChildren();
		seiMatchNode.removeAllChildren();
		eseMatchNode.removeAllChildren();
		liiMatchNode.removeAllChildren();
		eieMatchNode.removeAllChildren();
		lsiMatchNode.removeAllChildren();
		sleMatchNode.removeAllChildren();
		ieiMatchNode.removeAllChildren();
		seeMatchNode.removeAllChildren();
		iliMatchNode.removeAllChildren();
		lieMatchNode.removeAllChildren();
		esiMatchNode.removeAllChildren();
		lseMatchNode.removeAllChildren();
		eiiMatchNode.removeAllChildren();
		ieeMatchNode.removeAllChildren();
		sliMatchNode.removeAllChildren();

		ileNoMatchNode.removeAllChildren();
		seiNoMatchNode.removeAllChildren();
		eseNoMatchNode.removeAllChildren();
		liiNoMatchNode.removeAllChildren();
		eieNoMatchNode.removeAllChildren();
		lsiNoMatchNode.removeAllChildren();
		sleNoMatchNode.removeAllChildren();
		ieiNoMatchNode.removeAllChildren();
		seeNoMatchNode.removeAllChildren();
		iliNoMatchNode.removeAllChildren();
		lieNoMatchNode.removeAllChildren();
		esiNoMatchNode.removeAllChildren();
		lseNoMatchNode.removeAllChildren();
		eiiNoMatchNode.removeAllChildren();
		ieeNoMatchNode.removeAllChildren();
		sliNoMatchNode.removeAllChildren();
	}
}