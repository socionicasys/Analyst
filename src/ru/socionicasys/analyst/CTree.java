/**
 *
 */
package ru.socionicasys.analyst;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * @author Виктор
 */
@SuppressWarnings("serial")

public class CTree extends JTree implements
	ADocumentChangeListener {

	ADocument aDoc;
	DefaultMutableTreeNode rootNode;
	DefaultTreeModel treeModel;
	TreePath path;

	private Color color = new Color(30, 120, 255);


	private DefaultMutableTreeNode ileNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode seiNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode eseNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode liiNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode eieNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode lsiNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode sleNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode ieiNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode seeNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode iliNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode lieNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode esiNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode lseNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode eiiNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode ieeNode = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode sliNode = new DefaultMutableTreeNode();


	private int ileMatchCount = 0;
	private int seiMatchCount = 0;
	private int eseMatchCount = 0;
	private int liiMatchCount = 0;
	private int eieMatchCount = 0;
	private int lsiMatchCount = 0;
	private int sleMatchCount = 0;
	private int ieiMatchCount = 0;
	private int seeMatchCount = 0;
	private int iliMatchCount = 0;
	private int lieMatchCount = 0;
	private int esiMatchCount = 0;
	private int lseMatchCount = 0;
	private int eiiMatchCount = 0;
	private int ieeMatchCount = 0;
	private int sliMatchCount = 0;


	private int ileNoMatchCount = 0;
	private int seiNoMatchCount = 0;
	private int eseNoMatchCount = 0;
	private int liiNoMatchCount = 0;
	private int eieNoMatchCount = 0;
	private int lsiNoMatchCount = 0;
	private int sleNoMatchCount = 0;
	private int ieiNoMatchCount = 0;
	private int seeNoMatchCount = 0;
	private int iliNoMatchCount = 0;
	private int lieNoMatchCount = 0;
	private int esiNoMatchCount = 0;
	private int lseNoMatchCount = 0;
	private int eiiNoMatchCount = 0;
	private int ieeNoMatchCount = 0;
	private int sliNoMatchCount = 0;

	private float ileMatch = 0;
	private float seiMatch = 0;
	private float eseMatch = 0;
	private float liiMatch = 0;
	private float eieMatch = 0;
	private float lsiMatch = 0;
	private float sleMatch = 0;
	private float ieiMatch = 0;
	private float seeMatch = 0;
	private float iliMatch = 0;
	private float lieMatch = 0;
	private float esiMatch = 0;
	private float lseMatch = 0;
	private float eiiMatch = 0;
	private float ieeMatch = 0;
	private float sliMatch = 0;

	private float scale = 10;

	private class EndTreeNode extends DefaultMutableTreeNode {

		public EndTreeNode(Object o) {
			super(o);
		}

		@Override
		public String toString() {
			return "[" + getChildCount() + "] " + super.toString();
		}
	}


	public CTree(ADocument doc) {
		super();
		rootNode = new DefaultMutableTreeNode(doc.getProperty(Document.TitleProperty));
		treeModel = new DefaultTreeModel(rootNode);

		setCellRenderer(new HystogramCellRenderer());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setEditable(false);
		toggleClickCount = 1;
		putClientProperty("JTree.lineStyle", "None");

		setModel(treeModel);
		this.aDoc = doc;
		doc.addADocumentChangeListener(this);
		init();
	}


	private void init() {

		makeTreeStructure();
		updateTree();
	}


	private void updateTree() {

		if (aDoc == null) return;

		TreePath newPath = getSelectionPath();
		if (newPath != null) path = newPath;

		//Analyze document structure and update tree nodes
		try {
			HashMap<ASection, AData> aDataMap = aDoc.getADataMap();

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

			ileMatchCount = 0;
			seiMatchCount = 0;
			eseMatchCount = 0;
			liiMatchCount = 0;
			eieMatchCount = 0;
			lsiMatchCount = 0;
			sleMatchCount = 0;
			ieiMatchCount = 0;
			seeMatchCount = 0;
			iliMatchCount = 0;
			lieMatchCount = 0;
			esiMatchCount = 0;
			lseMatchCount = 0;
			eiiMatchCount = 0;
			ieeMatchCount = 0;
			sliMatchCount = 0;

			ileNoMatchCount = 0;
			seiNoMatchCount = 0;
			eseNoMatchCount = 0;
			liiNoMatchCount = 0;
			eieNoMatchCount = 0;
			lsiNoMatchCount = 0;
			sleNoMatchCount = 0;
			ieiNoMatchCount = 0;
			seeNoMatchCount = 0;
			iliNoMatchCount = 0;
			lieNoMatchCount = 0;
			esiNoMatchCount = 0;
			lseNoMatchCount = 0;
			eiiNoMatchCount = 0;
			ieeNoMatchCount = 0;
			sliNoMatchCount = 0;

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

				if (aspect.equals(AData.DOUBT)) continue;
				if (modifier != null && modifier.equals(AData.JUMP)) continue;

				if (aspect != null) {

					if (SocionicsType.matches(SocionicsType.ILE, aspect, secondAspect, sign, dimension, mv))
						ileMatchCount++;
					else ileNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.SEI, aspect, secondAspect, sign, dimension, mv))
						seiMatchCount++;
					else seiNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.ESE, aspect, secondAspect, sign, dimension, mv))
						eseMatchCount++;
					else eseNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.LII, aspect, secondAspect, sign, dimension, mv))
						liiMatchCount++;
					else liiNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.EIE, aspect, secondAspect, sign, dimension, mv))
						eieMatchCount++;
					else eieNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.LSI, aspect, secondAspect, sign, dimension, mv))
						lsiMatchCount++;
					else lsiNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.SLE, aspect, secondAspect, sign, dimension, mv))
						sleMatchCount++;
					else sleNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.IEI, aspect, secondAspect, sign, dimension, mv))
						ieiMatchCount++;
					else ieiNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.SEE, aspect, secondAspect, sign, dimension, mv))
						seeMatchCount++;
					else seeNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.ILI, aspect, secondAspect, sign, dimension, mv))
						iliMatchCount++;
					else iliNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.LIE, aspect, secondAspect, sign, dimension, mv))
						lieMatchCount++;
					else lieNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.ESI, aspect, secondAspect, sign, dimension, mv))
						esiMatchCount++;
					else esiNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.LSE, aspect, secondAspect, sign, dimension, mv))
						lseMatchCount++;
					else lseNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.EII, aspect, secondAspect, sign, dimension, mv))
						eiiMatchCount++;
					else eiiNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.IEE, aspect, secondAspect, sign, dimension, mv))
						ieeMatchCount++;
					else ieeNoMatchCount++;
					if (SocionicsType.matches(SocionicsType.SLI, aspect, secondAspect, sign, dimension, mv))
						sliMatchCount++;
					else sliNoMatchCount++;
				}// if (aspect != null)
			} //end While()
		} catch (BadLocationException e) {
			System.out.println("Exception in BTree.updateTree() :");
			e.printStackTrace();
		}

		if (ileNoMatchCount != 0) ileMatch = ileMatchCount / ileNoMatchCount;
		else ileMatch = Float.MAX_VALUE;
		if (seiNoMatchCount != 0) seiMatch = seiMatchCount / seiNoMatchCount;
		else seiMatch = Float.MAX_VALUE;
		if (eseNoMatchCount != 0) eseMatch = eseMatchCount / eseNoMatchCount;
		else eseMatch = Float.MAX_VALUE;
		if (liiNoMatchCount != 0) liiMatch = liiMatchCount / liiNoMatchCount;
		else liiMatch = Float.MAX_VALUE;
		if (eieNoMatchCount != 0) eieMatch = eieMatchCount / eieNoMatchCount;
		else eieMatch = Float.MAX_VALUE;
		if (lsiNoMatchCount != 0) lsiMatch = lsiMatchCount / lsiNoMatchCount;
		else lsiMatch = Float.MAX_VALUE;
		if (sleNoMatchCount != 0) sleMatch = sleMatchCount / sleNoMatchCount;
		else sleMatch = Float.MAX_VALUE;
		if (ieiNoMatchCount != 0) ieiMatch = ieiMatchCount / ieiNoMatchCount;
		else ieiMatch = Float.MAX_VALUE;
		if (seeNoMatchCount != 0) seeMatch = seeMatchCount / seeNoMatchCount;
		else seeMatch = Float.MAX_VALUE;
		if (iliNoMatchCount != 0) iliMatch = iliMatchCount / iliNoMatchCount;
		else iliMatch = Float.MAX_VALUE;
		if (lieNoMatchCount != 0) lieMatch = lieMatchCount / lieNoMatchCount;
		else lieMatch = Float.MAX_VALUE;
		if (esiNoMatchCount != 0) esiMatch = esiMatchCount / esiNoMatchCount;
		else esiMatch = Float.MAX_VALUE;
		if (lseNoMatchCount != 0) lseMatch = lseMatchCount / lseNoMatchCount;
		else lseMatch = Float.MAX_VALUE;
		if (eiiNoMatchCount != 0) eiiMatch = eiiMatchCount / eiiNoMatchCount;
		else eiiMatch = Float.MAX_VALUE;
		if (ieeNoMatchCount != 0) ieeMatch = ieeMatchCount / ieeNoMatchCount;
		else ieeMatch = Float.MAX_VALUE;
		if (sliNoMatchCount != 0) sliMatch = sliMatchCount / sliNoMatchCount;
		else sliMatch = Float.MAX_VALUE;

		float maxValue = Math.max(ileMatch,
			Math.max(seiMatch,
				Math.max(eseMatch,
					Math.max(liiMatch,
						Math.max(eieMatch,
							Math.max(lsiMatch,
								Math.max(sleMatch,
									Math.max(ieiMatch,
										Math.max(seeMatch,
											Math.max(iliMatch,
												Math.max(lieMatch,
													Math.max(esiMatch,
														Math.max(lseMatch,
															Math.max(eiiMatch,
																Math.max(ieeMatch, sliMatch)))))))))))))));

		ileMatch = scale * (ileMatch / maxValue);
		seiMatch = scale * (seiMatch / maxValue);
		eseMatch = scale * (eseMatch / maxValue);
		liiMatch = scale * (liiMatch / maxValue);
		eieMatch = scale * (eieMatch / maxValue);
		lsiMatch = scale * (lsiMatch / maxValue);
		sleMatch = scale * (sleMatch / maxValue);
		ieiMatch = scale * (ieiMatch / maxValue);
		seeMatch = scale * (seeMatch / maxValue);
		iliMatch = scale * (iliMatch / maxValue);
		lieMatch = scale * (lieMatch / maxValue);
		esiMatch = scale * (esiMatch / maxValue);
		lseMatch = scale * (lseMatch / maxValue);
		eiiMatch = scale * (eiiMatch / maxValue);
		ieeMatch = scale * (ieeMatch / maxValue);
		sliMatch = scale * (sliMatch / maxValue);

		String s = "";
		for (int i = 0; i <= scale; i++) s += "█";

		ileNode.setUserObject("ИЛЭ : " + s.substring(0, (int) ileMatch + 1) + " " + String.format("%1$2.0f", ileMatch * 100 / scale));
		seiNode.setUserObject("СЭИ : " + s.substring(0, (int) seiMatch + 1) + " " + String.format("%1$2.0f", seiMatch * 100 / scale));
		eseNode.setUserObject("ЭСЭ : " + s.substring(0, (int) eseMatch + 1) + " " + String.format("%1$2.0f", eseMatch * 100 / scale));
		liiNode.setUserObject("ЛИИ : " + s.substring(0, (int) liiMatch + 1) + " " + String.format("%1$2.0f", liiMatch * 100 / scale));
		eieNode.setUserObject("ЭИЭ : " + s.substring(0, (int) eieMatch + 1) + " " + String.format("%1$2.0f", eieMatch * 100 / scale));
		lsiNode.setUserObject("ЛСИ : " + s.substring(0, (int) lsiMatch + 1) + " " + String.format("%1$2.0f", lsiMatch * 100 / scale));
		sleNode.setUserObject("СЛЭ : " + s.substring(0, (int) sleMatch + 1) + " " + String.format("%1$2.0f", sleMatch * 100 / scale));
		ieiNode.setUserObject("ИЭИ : " + s.substring(0, (int) ieiMatch + 1) + " " + String.format("%1$2.0f", ieiMatch * 100 / scale));
		seeNode.setUserObject("СЭЭ : " + s.substring(0, (int) seeMatch + 1) + " " + String.format("%1$2.0f", seeMatch * 100 / scale));
		iliNode.setUserObject("ИЛИ : " + s.substring(0, (int) iliMatch + 1) + " " + String.format("%1$2.0f", iliMatch * 100 / scale));
		lieNode.setUserObject("ЛИЭ : " + s.substring(0, (int) lieMatch + 1) + " " + String.format("%1$2.0f", lieMatch * 100 / scale));
		esiNode.setUserObject("ЭСИ : " + s.substring(0, (int) esiMatch + 1) + " " + String.format("%1$2.0f", esiMatch * 100 / scale));
		lseNode.setUserObject("ЛСЭ : " + s.substring(0, (int) lseMatch + 1) + " " + String.format("%1$2.0f", lseMatch * 100 / scale));
		eiiNode.setUserObject("ЭИИ : " + s.substring(0, (int) eiiMatch + 1) + " " + String.format("%1$2.0f", eiiMatch * 100 / scale));
		ieeNode.setUserObject("ИЭЭ : " + s.substring(0, (int) ieeMatch + 1) + " " + String.format("%1$2.0f", ieeMatch * 100 / scale));
		sliNode.setUserObject("СЛИ : " + s.substring(0, (int) sliMatch + 1) + " " + String.format("%1$2.0f", sliMatch * 100 / scale));

		treeModel.reload();
	}


	private void makeTreeStructure() {

		rootNode.add(ileNode);
		rootNode.add(seiNode);
		rootNode.add(eseNode);
		rootNode.add(liiNode);
		rootNode.add(eieNode);
		rootNode.add(lsiNode);
		rootNode.add(sleNode);
		rootNode.add(ieiNode);
		rootNode.add(seeNode);
		rootNode.add(iliNode);
		rootNode.add(lieNode);
		rootNode.add(esiNode);
		rootNode.add(lseNode);
		rootNode.add(eiiNode);
		rootNode.add(ieeNode);
		rootNode.add(sliNode);
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


	private class HystogramCellRenderer implements TreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree,
													  Object value,
													  boolean selected,
													  boolean expanded,
													  boolean leaf,
													  int row,
													  boolean hasFocus) {

			JLabel l = new JLabel();

			if (leaf) {
				l.setText(value.toString());
				l.setFont(new Font(Font.MONOSPACED, Font.BOLD, l.getFont().getSize()));
				l.setForeground(color);
			} else {
				l.setText(" ");
				l.setForeground(Color.BLACK);
			}
			return l;
		}
	}

	public String getReport() {
		String report = "";
		String s = "";
		int scaleMultiplier = 4;

		for (int i = 0; i <= scale * scaleMultiplier; i++) s += "#";

		if (!aDoc.getADataMap().isEmpty()) {
			report =
				"<br/>" +
					"<h2> Соответствие ТИМу </h2>" +
//			"<br/>" +   
					"Приведенная ниже таблица позволяет определить наиболее вероятный ТИМ типируемого.<br/>" +
					"Для получения данных таблицы использовался следующий алгоритм. Каждый из отмеченных экспертом фрагментов текста<br/> " +
					"типируемого проверяется на соответствие обработке информации каждым из 16 ТИМов. Если фрагмент соответствует модели <br/>" +
					"обработки информации для данного ТИМа, значение в столбце \"Соответствие\" для данного ТИМа будет увеличено на 1.<br/>" +
					"Если нет, соответственно, увеличивается значение  в столбце \"Несоответствие\" для данного ТИМа.<br/><br/>" +
					"В столбце \"Коэффициент соответствия\" приведен нормализованный расчетный коэффициент, который рассчитывается для каждого ТИМа " +
					"по формуле:<br/>   <code> К.С. = NORM<small style=\"vertical-align:sub;color:black\"> 100</small>( СООТВЕТСТВИЕ / НЕСООТВЕТСТВИЕ )</code><br/>" +
					"Этот коэффициент применяется для выделения наиболее вероятного ТИМа,<br/>" +
					"но не следует рассматривать его как математическую вероятность определения ТИМа. <br/><br/>" +
					"<table title=\"TIM analysis\" border=1 width=\"80%\">" +
					"<tr>" + "\n" +
					"	<th width=\"40%\"> ТИМ </th>" + "\n" +
					"	<th width=\"20%\"> Соответствие </th>" + "\n" +
					"	<th width=\"20%\"> Несоответствие </th>" + "\n" +
					"	<th width=\"20%\"> Коэффициент соответствия </th>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.ILE.toString() + "</td>" + "\n" +
					"		<td align=\"center\">" + ileMatchCount + " </td>" + "\n" +
					"		<td align=\"center\">" + ileNoMatchCount + " </td>" + "\n" +
					"		<td align=\"center\">" + " " + String.format("%1$2.0f", ileMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.SEI.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + seiMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + seiNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + seiMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", seiMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.ESE.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + eseMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + eseNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + eseMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", eseMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.LII.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + liiMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + liiNoMatchCount + " </td>" + "\n" +
// 				"		<td align=\"center\" >" + liiMatch+ " </td>"  + "\n" +		
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", liiMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.EIE.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + eieMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + eieNoMatchCount + " </td>" + "\n" +
// 				"		<td align=\"center\" >" + eieMatch+ " </td>"  + "\n" +		
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", eieMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.LSI.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + lsiMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + lsiNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + lsiMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", lsiMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.SLE.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + sleMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + sleNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + sleMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", sleMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.IEI.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + ieiMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + ieiNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\">" + ieiMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", ieiMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.SEE.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + seeMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + seeNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + seeMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", seeMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.ILI.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + iliMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + iliNoMatchCount + " </td>" + "\n" +
// 				"		<td align=\"center\" >" + iliMatch+ " </td>"  + "\n" +		
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", iliMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.LIE.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + lieMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + lieNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + lieMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", lieMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.ESI.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + esiMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + esiNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + esiMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", esiMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.LSE.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + lseMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + lseNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + lseMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", lseMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.EII.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + eiiMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + eiiNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + eiiMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", eiiMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.IEE.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + ieeMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + ieeNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + ieeMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", ieeMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"<tr>" + "\n" +
					"	<td style=\"font-weight:bold\">" + SocionicsType.SLI.toString() + "</td>" + "\n" +
					"		<td align=\"center\" >" + sliMatchCount + " </td>" + "\n" +
					"		<td align=\"center\" >" + sliNoMatchCount + " </td>" + "\n" +
					//				"		<td align=\"center\" >" + sliMatch+ " </td>"  + "\n" +
					"		<td align=\"center\" >" + " " + String.format("%1$2.0f", sliMatch * 100 / scale) + " </td>" + "\n" +
					"</tr>" + "\n" +
					"</table>";
		} else {
			report =
				"<br/>" +
					"<h2> Невозможно определить ТИМ </h2>" +
					"<br/>";
		}
		return report;
	}
}


